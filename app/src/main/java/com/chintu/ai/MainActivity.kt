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
import com.chintu.ai.ai.ModelManager
import com.chintu.ai.ai.ProviderManager
import com.chintu.ai.ai.ServerConfigStore
import com.chintu.ai.ai.ChintuAiEngine
import com.chintu.ai.agent.Orchestrator
import com.chintu.ai.memory.MemoryStore
import com.chintu.ai.ui.ChintuTheme
import com.chintu.ai.ui.HomeScreen
import com.chintu.ai.ui.SettingsScreen
import com.chintu.ai.voice.VoiceEngine
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {
            ChintuApp(this)
        }
    }
}

@Composable
fun ChintuApp(
    activity: MainActivity
) {

    val memory =
        remember {
            MemoryStore(activity)
        }

    val voice =
        remember {
            VoiceEngine(activity)
        }

    val providerManager =
        remember {
            ProviderManager(activity)
        }

    val apiKeyStore =
        remember {
            ApiKeyStore(activity)
        }

    val serverConfigStore =
        remember {
            ServerConfigStore(activity)
        }

    val modelManager =
        remember {
            ModelManager(activity)
        }

    val aiEngine =
        remember {
            ChintuAiEngine(activity)
        }

    val orchestrator =
        remember {
            Orchestrator(
                aiEngine = aiEngine,
                memory = memory
            )
        }

    var mode by remember {
        mutableStateOf(
            AiMode.AUTO
        )
    }

    var model by remember {
        mutableStateOf(
            providerManager.getModel()
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

    val messages =
        remember {
            mutableStateListOf<ChatMessage>()
        }

    val micPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                status = "LISTENING"

                voice.listen(
                    onText = {
                        input = it
                        status = "READY"
                    },
                    onError = {
                        status = it
                    }
                )

            } else {

                status =
                    "MIC PERMISSION NEEDED"
            }
        }

    fun startVoice() {

        val granted =
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

        if (granted) {

            status = "LISTENING"

            voice.listen(
                onText = {
                    input = it
                    status = "READY"
                },
                onError = {
                    status = it
                }
            )

        } else {

            micPermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    fun sendMessage() {

        val question =
            input.trim()

        if (
            question.isEmpty() ||
            status == "THINKING"
        ) {
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
                    onStatus = {
                        status = it
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

    ChintuTheme {

        if (showSettings) {

            SettingsScreen(
                cfg = null,
                model = model,
                onModelChange = {
                    model = it

                    providerManager.saveSettings(
                        provider =
                            providerManager.getProvider(),
                        apiKey =
                            apiKeyStore.getApiKey(
                                providerManager.getProvider()
                            ),
                        serverUrl =
                            serverConfigStore.getServerUrl(
                                providerManager.getProvider()
                            ),
                        model = it
                    )

                    modelManager.selectModel(it)
                },
                mode = mode,
                onModeChange = {
                    mode = it
                },
                memory = memory,
                onBack = {
                    showSettings = false
                }
            )

        } else {

            HomeScreen(
                mode = mode,
                status = status,
                messages = messages,
                input = input,
                onInputChange = {
                    input = it
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
                onQuickAction = {
                    input = it
                }
            )
        }
    }
}
