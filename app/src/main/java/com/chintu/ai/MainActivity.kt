package com.chintu.ai

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.chintu.ai.agent.Orchestrator
import com.chintu.ai.ai.*
import com.chintu.ai.memory.MemoryStore
import com.chintu.ai.voice.VoiceEngine
import kotlinx.coroutines.launch

private val ChintuBackgroundTop = Color(0xFF07111F)
private val ChintuBackgroundBottom = Color(0xFF02050A)
private val ChintuBlue = Color(0xFF4DA3FF)
private val ChintuCyan = Color(0xFF4DE8FF)
private val ChintuCard = Color(0xFF0B1726)
private val ChintuCard2 = Color(0xFF101F32)
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
            cfg.get("model")
                .ifBlank {
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

    var settings by remember {
        mutableStateOf(false)
    }

    val messages = remember {
        mutableStateListOf<ChatMessage>()
    }

    val recordPermission =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (!granted) {
                status = "Microphone permission required"
            }
        }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = ChintuBackgroundBottom,
            surface = ChintuCard,
            primary = ChintuBlue,
            secondary = ChintuCyan,
            onBackground = ChintuText,
            onSurface = ChintuText
        )
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

            ChintuHomeScreen(
                activity = activity,
                mode = mode,
                status = status,
                input = input,
                messages = messages,

                onSettings = {
                    settings = true
                },

                onMode = {
                    settings = true
                },

                onInputChange = {
                    input = it
                },

                onVoice = {

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
                                    "✓ Completed"
                                } else {
                                    "✕ Failed"
                                }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun ChintuHomeScreen(
    activity: MainActivity,
    mode: AiMode,
    status: String,
    input: String,
    messages: List<ChatMessage>,
    onSettings: () -> Unit,
    onMode: () -> Unit,
    onInputChange: (String) -> Unit,
    onVoice: () -> Unit,
    onSend: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        ChintuBackgroundTop,
                        ChintuBackgroundBottom
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            ChintuTopBar(
                mode = mode,
                onSettings = onSettings,
                onMode = onMode
            )

            if (messages.isEmpty()) {

                WelcomeArea(
                    status = status
                )

            } else {

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(
                            horizontal = 14.dp
                        ),
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(
                        top = 12.dp,
                        bottom = 12.dp
                    )
                ) {

                    items(
                        messages.reversed()
                    ) { message ->

                        MessageBubble(
                            message = message
                        )
                    }
                }
            }

            QuickActions(
                onAction = { action ->

                    onInputChange(action)
                }
            )

            ChintuInputBar(
                input = input,
                status = status,
                onInputChange = onInputChange,
                onVoice = onVoice,
                onSend = onSend
            )

            Spacer(
                Modifier.height(8.dp)
            )

            PrivacyFooter()

            Spacer(
                Modifier.height(10.dp)
            )
        }
    }
}

@Composable
private fun ChintuTopBar(
    mode: AiMode,
    onSettings: () -> Unit,
    onMode: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 18.dp,
                end = 12.dp,
                top = 14.dp,
                bottom = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(42.dp)
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
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(
            Modifier.width(10.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = "CHINTU",
                color = ChintuText,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Personal AI Assistant",
                color = ChintuMuted,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Surface(
            modifier = Modifier
                .clickable {
                    onMode()
                },
            shape = RoundedCornerShape(20.dp),
            color = ChintuCard2
        ) {

            Row(
                modifier = Modifier.padding(
                    horizontal = 11.dp,
                    vertical = 7.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = ChintuCyan,
                    modifier = Modifier.size(15.dp)
                )

                Spacer(
                    Modifier.width(5.dp)
                )

                Text(
                    text = mode.name,
                    color = ChintuText,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        IconButton(
            onClick = onSettings
        ) {

            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = ChintuText
            )
        }
    }
}

@Composable
private fun WelcomeArea(
    status: String
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        ChintuOrb(
            status = status
        )

        Spacer(
            Modifier.height(20.dp)
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
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(
            Modifier.height(18.dp)
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ChintuCard,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color.White.copy(alpha = 0.06f)
            )
        ) {

            Row(
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 9.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            Color(0xFF45E39B),
                            CircleShape
                        )
                )

                Spacer(
                    Modifier.width(8.dp)
                )

                Text(
                    text = status,
                    color = ChintuMuted,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun ChintuOrb(
    status: String
) {

    val thinking =
        status.contains(
            "THINK",
            ignoreCase = true
        ) ||
        status.contains(
            "Using",
            ignoreCase = true
        ) ||
        status.contains(
            "LISTEN",
            ignoreCase = true
        )

    val transition =
        rememberInfiniteTransition(
            label = "chintu_orb"
        )

    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(
                    durationMillis = 1400,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
        label = "orb_pulse"
    )

    Box(
        modifier = Modifier.size(210.dp),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(190.dp)
                .scale(
                    if (thinking) pulse
                    else 1f
                )
                .alpha(
                    if (thinking) 0.20f
                    else 0.12f
                )
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

        Box(
            modifier = Modifier
                .size(145.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF214D78),
                            Color(0xFF0D2036),
                            Color(0xFF07111F)
                        )
                    ),
                    CircleShape
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            ChintuCyan,
                            ChintuBlue
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    imageVector =
                        if (thinking) {
                            Icons.Default.GraphicEq
                        } else {
                            Icons.Default.AutoAwesome
                        },
                    contentDescription = null,
                    tint = ChintuCyan,
                    modifier = Modifier.size(35.dp)
                )

                Spacer(
                    Modifier.height(5.dp)
                )

                Text(
                    text = "CHINTU",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
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
                vertical = 5.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {

    Surface(
        modifier = modifier.clickable {
            onClick()
        },
        shape = RoundedCornerShape(14.dp),
        color = ChintuCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
        
