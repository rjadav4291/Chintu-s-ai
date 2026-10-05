package com.chintu.ai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope

import com.chintu.ai.ai.AiMode
import com.chintu.ai.ai.ApiKeyStore
import com.chintu.ai.ai.ChatMessage
import com.chintu.ai.ai.ChintuAiEngine
import com.chintu.ai.ai.ModelManager
import com.chintu.ai.ai.ProviderManager
import com.chintu.ai.ai.ServerConfigStore

import com.chintu.ai.agent.Orchestrator
import com.chintu.ai.memory.MemoryStore

import com.chintu.ai.ui.ChintuTheme
import com.chintu.ai.ui.HomeScreen
import com.chintu.ai.ui.SettingsScreen

import com.chintu.ai.voice.VoiceEngine

import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChintuApp(this)
        }
    }
}


@Composable
fun ChintuApp(activity: MainActivity) {

    /*
     * ---------------------------------------------------------
     * STORES
     * ---------------------------------------------------------
     */

    val memory = remember {
        MemoryStore(activity)
    }

    val voice = remember {
        VoiceEngine(activity)
    }

    val providerManager = remember {
        ProviderManager(activity)
    }

    val apiKeyStore = remember {
        ApiKeyStore(activity)
    }

    val serverConfigStore = remember {
        ServerConfigStore(activity)
    }

    val modelManager = remember {
        ModelManager(activity)
    }


    /*
     * ---------------------------------------------------------
     * AI ENGINE
     * ---------------------------------------------------------
     */

    val aiEngine = remember {
        ChintuAiEngine(activity)
    }

    val orchestrator = remember {
        Orchestrator(
            aiEngine = aiEngine,
            memory = memory
        )
    }


    /*
     * ---------------------------------------------------------
     * STATE
     * ---------------------------------------------------------
     */

    var mode by remember {
        mutableStateOf(
            AiMode.AUTO
        )
    }

    var model by remember {
        mutableStateOf(
            providerManager
                .getModel()
                .ifBlank {
                    modelManager.getSelectedModel()
                }
        )
    }

    var input by remember {
        mutableStateOf("")
    }

    var status by remember {
        mutableStateOf("READY")
    }

    var showSettings by remember {
        mutableStateOf(false)
    }


    /*
     * ---------------------------------------------------------
     * CHAT
     * ---------------------------------------------------------
     */

    val messages = remember {
        mutableStateListOf<ChatMessage>()
    }


    /*
     * ---------------------------------------------------------
     * MICROPHONE PERMISSION
     * ---------------------------------------------------------
     */

    val micPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                status = "LISTENING"

                voice.listen(
                    onText = { text ->
                        input = text
                        status = "READY"
                    },

                    onError = { error ->
                        status = error
                    }
                )

            } else {

                status = "MIC PERMISSION NEEDED"
            }
        }


    /*
     * ---------------------------------------------------------
     * VOICE
     * ---------------------------------------------------------
     */

    fun startVoice() {

        val granted =
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

        if (granted) {

            status = "LISTENING"

            voice.listen(
                onText = { text ->
                    input = text
                    status = "READY"
                },

                onError = { error ->
                    status = error
                }
            )

        } else {

            micPermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }


    /*
     * ---------------------------------------------------------
     * SEND MESSAGE
     * ---------------------------------------------------------
     */

    fun sendMessage() {

        val question = input.trim()

        if (question.isEmpty()) {
            return
        }

        if (status == "THINKING") {
            return
        }


        messages.add(
            ChatMessage(
                role = "user",
                content = question
            )
        )

        input = ""

        status = "THINKING"


        activity.lifecycleScope.launch {

            val result =
                orchestrator.answer(
                    userText = question,
                    mode = mode,
                    model = model,
                    onStatus = { newStatus ->
                        status = newStatus
                    }
                )


            messages.add(
                ChatMessage(
                    role = "assistant",
                    content = result.text
                )
            )


            status =
                if (result.verified) {
                    "✓ COMPLETED"
                } else {
                    "✕ FAILED"
                }
        }
    }


    /*
     * ---------------------------------------------------------
     * UI
     * ---------------------------------------------------------
     */

    ChintuTheme {

        if (showSettings) {

            SettingsScreen(

                model = model,

                onModelChange = { selectedModel ->
                    model = selectedModel
                },

                mode = mode,

                onModeChange = { selectedMode ->
                    mode = selectedMode
                },

                memory = memory,

                onBack = {

                    model =
                        providerManager
                            .getModel()
                            .ifBlank {
                                modelManager.getSelectedModel()
                            }

                    showSettings = false
                }
            )

        } else {

            HomeScreen(

                model = model,

                mode = mode,

                status = status,

                messages = messages,

                input = input,

                onInputChange = { text ->
                    input = text
                },

                onSend = {
                    sendMessage()
                },

                onVoice = {
                    startVoice()
                },

                onSettings = {
                    showSettings = true
                },

                onModeClick = { selectedMode ->
                    mode = selectedMode
                },

                onQuickAction = { suggestion ->
                    input = suggestion
                }
            )
        }
    }
}
