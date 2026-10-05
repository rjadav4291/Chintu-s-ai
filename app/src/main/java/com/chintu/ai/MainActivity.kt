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
     * CORE STORES
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
     * AI ENGINE + ORCHESTRATOR
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
     * UI STATE
     * ---------------------------------------------------------
     */

    var mode by remember {
        mutableStateOf(AiMode.AUTO)
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
     * CHAT MESSAGES
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
     * START VOICE
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


        /*
         * Add user message
         */

        messages.add(
            ChatMessage(
                role = "user",
                content = question
            )
        )


        /*
         * Clear input
         */

        input = ""


        /*
         * Show thinking status
         */

        status = "THINKING"


        /*
         * Run AI
         */

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


            /*
             * Add assistant response
             */

            messages.add(
                ChatMessage(
                    role = "assistant",
                    content = result.text
                )
            )


            /*
             * Final status
             */

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
     * CHINTU THEME
     * ---------------------------------------------------------
     */

    ChintuTheme {


        /*
         * =====================================================
         * SETTINGS SCREEN
         * =====================================================
         */

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

                    /*
                     * Reload selected model
                     */

                    model =
                        providerManager
                            .getModel()
                            .ifBlank {
                                modelManager.getSelectedModel()
                            }


                    /*
                     * Return to home
                     */

                    showSettings = false
                }
            )


        } else {


            /*
             * =================================================
             * HOME SCREEN
             * =================================================
             *
             * IMPORTANT:
             * This matches the actual HomeScreen.kt signature.
             *
             * NO model parameter.
             * NO onModeClick parameter.
             */

            HomeScreen(

                mode = mode,

                status = status,

                messages = messages,

                input = input,


                /*
                 * Input text changed
                 */

                onInputChange = { text ->

                    input = text
                },


                /*
                 * Send button
                 */

                onSend = {

                    sendMessage()
                },


                /*
                 * Voice button
                 */

                onVoice = {

                    startVoice()
                },


                /*
                 * Settings button
                 */

                onSettings = {

                    showSettings = true
                },


                /*
                 * Quick action
                 */

                onQuickAction = { suggestion ->

                    input = suggestion
                }
            )
        }
    }
}
