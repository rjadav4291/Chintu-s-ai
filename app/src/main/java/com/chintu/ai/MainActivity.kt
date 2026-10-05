package com.chintu.ai

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.chintu.ai.ai.*
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
            ChintuRoot(this)
        }
    }
}

@Composable
private fun ChintuRoot(
    activity: MainActivity
) {

    val config = remember {
        SecureConfig(activity)
    }

    val memory = remember {
        MemoryStore(activity)
    }

    val providers = remember {
        ProviderRegistry(
            listOf(
                OpenAiCompatibleProvider(
                    config,
                    "openai",
                    "OpenAI",
                    "https://api.openai.com/v1/chat/completions"
                ),

                OpenAiCompatibleProvider(
                    config,
                    "openrouter",
                    "OpenRouter",
                    "https://openrouter.ai/api/v1/chat/completions"
                ),

                OpenAiCompatibleProvider(
                    config,
                    "ollama",
                    "Ollama",
                    "http://127.0.0.1:11434/api/chat"
                ),

                AnthropicProvider(config),

                GeminiProvider(config)
            )
        )
    }

    val orchestrator = remember {
        Orchestrator(
            providers,
            memory
        )
    }

    val voice = remember {
        VoiceEngine(activity)
    }

    var mode by remember {
        mutableStateOf(AiMode.AUTO)
    }

    var model by remember {
        mutableStateOf(
            config.get("model").ifBlank {
                "gpt-6-luna"
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

    val messages = remember {
        mutableStateListOf<ChatMessage>()
    }

    val microphonePermission =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (!it) {
                status = "MIC PERMISSION NEEDED"
            }
        }

    ChintuTheme {

        if (showSettings) {

            SettingsScreen(
                config = config,
                model = model,
                mode = mode,

                onModelChange = {
                    model = it
                    config.set("model", it)
                },

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

                model = model,

                input = input,

                status = status,

                messages = messages,

                onSettings = {
                    showSettings = true
                },

                onModeClick = {
                    showSettings = true
                },

                onInputChange = {
                    input = it
                },

                onVoice = {

                    microphonePermission.launch(
                        Manifest.permission.RECORD_AUDIO
                    )

                    voice.listen(

                        onText = {
                            input = it
                            status = "READY"
                        },

                        onError = {
                            status = it
                        }
                    )
                },

                onSend = {

                    val question = input.trim()

                    if (question.isEmpty()) {
                        return@HomeScreen
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
                                question,
                                mode,
                                model
                            ) {
                                status = it
                            }

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
            )
        }
    }
}
