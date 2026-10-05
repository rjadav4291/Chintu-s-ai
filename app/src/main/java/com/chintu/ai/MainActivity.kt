package com.chintu.ai

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.chintu.ai.ai.*
import com.chintu.ai.agent.Orchestrator
import com.chintu.ai.memory.MemoryStore
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

    val cfg = remember {
        SecureConfig(activity)
    }

    val memory = remember {
        MemoryStore(activity)
    }

    val providers = remember {

        ProviderRegistry(
            listOf(

                OpenAiCompatibleProvider(
                    cfg,
                    "openai",
                    "OpenAI",
                    "https://api.openai.com/v1/chat/completions"
                ),

                OpenAiCompatibleProvider(
                    cfg,
                    "openrouter",
                    "OpenRouter",
                    "https://openrouter.ai/api/v1/chat/completions"
                ),

                OpenAiCompatibleProvider(
                    cfg,
                    "ollama",
                    "Ollama",
                    "http://127.0.0.1:11434/api/chat"
                ),

                AnthropicProvider(cfg),

                GeminiProvider(cfg)

            )
        )
    }

    val orchestrator =
        remember {
            Orchestrator(
                providers,
                memory
            )
        }

    val voice =
        remember {
            VoiceEngine(activity)
        }

    var mode by remember {
        mutableStateOf(AiMode.AUTO)
    }

    var model by remember {
        mutableStateOf(
            cfg.get("model")
                .ifBlank { "gpt-6-luna" }
        )
    }

    var input by remember {
        mutableStateOf("")
    }

    var status by remember {
        mutableStateOf("READY")
    }

    var settings by remember {
        mutableStateOf(false)
    }

    val messages =
        remember {
            mutableStateListOf<ChatMessage>()
        }

    val recordPermission =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {

        if (settings) {

            SettingsScreen(
                cfg = cfg,
                model = model,
                onModel = {
                    model = it
                    cfg.set("model", it)
                },
                mode = mode,
                onMode = {
                    mode = it
                },
                memory = memory,
                onBack = {
                    settings = false
                }
            )

        } else {

            Scaffold(

                topBar = {

                    TopAppBar(

                        title = {
                            Text("CHINTU")
                        },

                        navigationIcon = {

                            IconButton(
                                onClick = {
                                    settings = true
                                }
                            ) {

                                Icon(
                                    Icons.Default.Settings,
                                    "Settings"
                                )
                            }
                        },

                        actions = {

                            AssistChip(
                                onClick = {
                                    settings = true
                                },
                                label = {
                                    Text(mode.name)
                                }
                            )
                        }
                    )
                },

                bottomBar = {

                    Surface(
                        tonalElevation = 3.dp
                    ) {

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(10.dp),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            TextField(
                                value = input,
                                onValueChange = {
                                    input = it
                                },
                                modifier =
                                    Modifier.weight(1f),
                                placeholder = {
                                    Text("Ask CHINTU…")
                                },
                                singleLine = true
                            )

                            IconButton(
                                onClick = {

                                    recordPermission.launch(
                                        Manifest.permission.RECORD_AUDIO
                                    )

                                    voice.listen(
                                        {
                                            input = it
                                        },
                                        {
                                            status = it
                                        }
                                    )
                                }
                            ) {

                                Icon(
                                    Icons.Default.Mic,
                                    "Voice"
                                )
                            }

                            IconButton(

                                onClick = {

                                    val q =
                                        input.trim()

                                    if (q.isNotEmpty()) {

                                        messages.add(
                                            ChatMessage(
                                                "user",
                                                q
                                            )
                                        )

                                        input = ""

                                        status =
                                            "THINKING"

                                        activity.lifecycleScope.launch {

                                            val r =
                                                orchestrator.answer(
                                                    q,
                                                    mode,
                                                    model
                                                ) {
                                                    status = it
                                                }

                                            messages.add(
                                                ChatMessage(
                                                    "assistant",
                                                    r.text
                                                )
                                            )

                                            status =
                                                if (r.verified)
                                                    "✓ Completed"
                                                else
                                                    "✕ Failed"
                                        }
                                    }
                                }
                            ) {

                                Icon(
                                    Icons.Default.Send,
                                    "Send"
                                )
                            }
                        }
                    }
                }

            ) { pad ->

                Column(

                    Modifier
                        .fillMaxSize()
                        .padding(pad)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF08111F),
                                    Color(0xFF02050A)
                                )
                            )
                        )
                ) {

                    Orb(status)

                    Text(
                        status,
                        Modifier
                            .align(
                                Alignment.CenterHorizontally
                            )
                            .alpha(.75f)
                    )

                    LazyColumn(

                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(12.dp),

                        reverseLayout = true

                    ) {

                        items(messages.reversed()) {
                            MessageBubble(it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Orb(status: String) {

    val thinking =
        status.contains(
            "THINK",
            true
        ) ||
        status.contains(
            "Using",
            true
        )

    Box(
        Modifier
            .fillMaxWidth()
            .height(230.dp),

        contentAlignment =
            Alignment.Center
    ) {

        Surface(

            Modifier.size(
                if (thinking)
                    150.dp
                else
                    132.dp
            ),

            shape = CircleShape,

            tonalElevation = 12.dp,

            color =
                Color(0xFF102A43)

        ) {

            Box(
                contentAlignment =
                    Alignment.Center,

                modifier =
                    Modifier.fillMaxSize()
            ) {

                Text(
                    if (thinking)
                        "…"
                    else
                        "C",

                    style =
                        MaterialTheme
                            .typography
                            .displayLarge
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    m: ChatMessage
) {

    Row(

        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),

        horizontalArrangement =
            if (m.role == "user")
                Arrangement.End
            else
                Arrangement.Start
    ) {

        Surface(

            shape =
                RoundedCornerShape(18.dp),

            tonalElevation = 2.dp,

            modifier =
                Modifier.widthIn(
                    max = 330.dp
                )
        ) {

            Text(
                m.content,
                Modifier.padding(14.dp)
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    cfg: SecureConfig,
    model: String,
    onModel: (String) -> Unit,
    mode: AiMode,
    onMode: (AiMode) -> Unit,
    memory: MemoryStore,
    onBack: () -> Unit
) {

    var key by remember {
        mutableStateOf(
            cfg.get("openai_api_key")
        )
    }

    var mem by remember {
        mutableStateOf(true)
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {

                Icon(
                    Icons.Default.ArrowBack,
                    "Back"
                )
            }

            Text(
                "CHINTU Settings",
                style =
                    MaterialTheme
                        .typography
                        .headlineSmall
            )
        }

        Spacer(
            Modifier.height(10.dp)
        )

        Text("AI Mode")

        Row {

            AiMode.values().forEach {

                FilterChip(

                    selected =
                        mode == it,

                    onClick = {
                        onMode(it)
                    },

                    label = {
                        Text(it.name)
                    },

                    modifier =
                        Modifier.padding(
                            end = 4.dp
                        )
                )
            }
        }

        Spacer(
            Modifier.height(12.dp)
        )

        Text("Model")

        TextField(
            value = model,
            onValueChange = onModel,
            modifier =
                Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            Modifier.height(8.dp)
        )

        Text(
            "OpenAI-compatible API key (encrypted)"
        )

        TextField(

            value = key,

            onValueChange = {
                key = it
                cfg.set(
                    "openai_api_key",
                    it
                )
            },

            modifier =
                Modifier.fillMaxWidth(),

            visualTransformation =
                PasswordVisualTransformation(),

            singleLine = true
        )

        Spacer(
            Modifier.height(12.dp)
        )

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Switch(
                checked = mem,
                onCheckedChange = {
                    mem = it
                }
            )

            Text(" Memory enabled")
        }

        Spacer(
            Modifier.height(12.dp)
        )

        Text(
            "Memory entries: ${memory.list().size}"
        )

        Button(
            onClick = {
                memory.clear()
            }
        ) {

            Text(
                "Clear all memory"
            )
        }

        Spacer(
            Modifier.height(8.dp)
        )

        Text(
            "Privacy: OFFLINE and PRIVATE modes do not intentionally use online AI providers.",
            style =
                MaterialTheme.typography.bodySmall
        )
    }
}
