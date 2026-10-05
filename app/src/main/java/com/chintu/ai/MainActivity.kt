package com.chintu.ai

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.chintu.ai.agent.Orchestrator
import com.chintu.ai.ai.AiMode
import com.chintu.ai.ai.AnthropicProvider
import com.chintu.ai.ai.ChatMessage
import com.chintu.ai.ai.GeminiProvider
import com.chintu.ai.ai.OpenAiCompatibleProvider
import com.chintu.ai.ai.ProviderRegistry
import com.chintu.ai.ai.SecureConfig
import com.chintu.ai.memory.MemoryStore
import com.chintu.ai.voice.VoiceEngine
import kotlinx.coroutines.launch

private val ChintuBlue = Color(0xFF4DA3FF)
private val ChintuCyan = Color(0xFF4DE8FF)
private val ChintuBackground = Color(0xFF02050A)
private val ChintuTop = Color(0xFF08182A)
private val ChintuCard = Color(0xFF0C1928)
private val ChintuCard2 = Color(0xFF122337)
private val ChintuText = Color(0xFFEAF4FF)
private val ChintuMuted = Color(0xFF8EA4BA)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChintuApp(this)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            cfg.get("model").ifBlank {
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
        ) { granted ->

            if (!granted) {
                status = "MIC PERMISSION NEEDED"
            }
        }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = ChintuBlue,
            secondary = ChintuCyan,
            background = ChintuBackground,
            surface = ChintuCard
        )
    ) {

        if (showSettings) {

            SettingsScreen(
                cfg = cfg,
                model = model,
                onModelChange = {
                    model = it
                    cfg.set("model", it)
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

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {

                    TopAppBar(
                        title = {
                            Column {

                                Text(
                                    text = "CHINTU",
                                    color = ChintuText,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Personal AI Assistant",
                                    color = ChintuMuted,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        },

                        navigationIcon = {

                            Box(
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .size(40.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                ChintuBlue,
                                                ChintuCyan
                                            )
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {

                                Text(
                                    text = "C",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },

                        actions = {

                            AssistChip(
                                onClick = {
                                    showSettings = true
                                },
                                label = {
                                    Text(mode.name)
                                }
                            )

                            IconButton(
                                onClick = {
                                    showSettings = true
                                }
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = ChintuText
                                )
                            }
                        }
                    )
                },

                bottomBar = {

                    InputBar(
                        input = input,
                        status = status,
                        onInputChange = {
                            input = it
                        },
                        onVoice = {

                            microphonePermission.launch(
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
                        },
                        onSend = {

                            val question = input.trim()

                            if (question.isNotEmpty()) {

                                messages.add(
                                    ChatMessage(
                                        "user",
                                        question
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
                                            "assistant",
                                            result.text
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
                        }
                    )
                }

            ) { padding ->

                HomeContent(
                    padding = padding,
                    status = status,
                    messages = messages,
                    onQuickAction = {
                        input = it
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    padding: PaddingValues,
    status: String,
    messages: List<ChatMessage>,
    onQuickAction: (String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                Brush.verticalGradient(
                    listOf(
                        ChintuTop,
                        ChintuBackground
                    )
                )
            )
    ) {

        if (messages.isEmpty()) {

            WelcomeContent(status)

        } else {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(
                    top = 12.dp,
                    bottom = 12.dp
                )
            ) {

                items(messages.reversed()) { message ->

                    MessageBubble(
                        message = message
                    )
                }
            }
        }

        QuickActions(
            onAction = onQuickAction
        )

        PrivacyFooter()
    }
}

@Composable
private fun WelcomeContent(
    status: String
) {

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        ChintuOrb(status)

        Spacer(
            Modifier.height(18.dp)
        )

        Text(
            text = "Hello, I'm CHINTU",
            color = ChintuText,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(
            Modifier.height(6.dp)
        )

        Text(
            text = "Your personal AI assistant",
            color = ChintuMuted,
            textAlign = TextAlign.Center
        )

        Spacer(
            Modifier.height(14.dp)
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = ChintuCard
        ) {

            Text(
                text = status,
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 8.dp
                ),
                color = ChintuCyan,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun ChintuOrb(
    status: String
) {

    val active =
        status.contains("THINK", true) ||
        status.contains("LISTEN", true)

    val scale =
        if (active) 1.06f else 1f

    Box(
        modifier = Modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(scale)
                .alpha(0.16f)
                .background(
                    Brush.radialGradient(
                        listOf(
                            ChintuCyan,
                            ChintuBlue,
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Surface(
            modifier = Modifier.size(135.dp),
            shape = CircleShape,
            color = Color(0xFF102A43),
            border = BorderStroke(
                1.dp,
                ChintuCyan.copy(alpha = 0.7f)
            )
        ) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = ChintuCyan,
                    modifier = Modifier.size(34.dp)
                )

                Spacer(
                    Modifier.height(5.dp)
                )

                Text(
                    text = "CHINTU",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuickActions(
    onAction: (String) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        QuickActionChip(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.AutoAwesome,
            text = "Ask AI",
            onClick = {
                onAction("")
            }
        )

        QuickActionChip(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Language,
            text = "Web",
            onClick = {
                onAction("Search the web for ")
            }
        )

        QuickActionChip(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Memory,
            text = "Memory",
            onClick = {
                onAction("Show my memory")
            }
        )
    }
}

@Composable
private fun QuickActionChip(
    modifier: Modifier,
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {

    Surface(
        modifier = modifier.clickable {
            onClick()
        },
        shape = RoundedCornerShape(14.dp),
        color = ChintuCard,
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.06f)
        )
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 9.dp
            ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ChintuCyan,
                modifier = Modifier.size(16.dp)
            )

            Spacer(
                Modifier.width(5.dp)
            )

            Text(
                text = text,
                color = ChintuText,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun InputBar(
    input: String,
    status: String,
    onInputChange: (String) -> Unit,
    onVoice: () -> Unit,
    onSend: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),
        shape = RoundedCornerShape(25.dp),
        color = ChintuCard2,
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.08f)
        )
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onVoice
            ) {

                Icon(
                    imageVector =
                        if (status.c
