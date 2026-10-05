package com.chintu.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chintu.ai.ai.AiMode
import com.chintu.ai.ai.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    model: String,
    mode: AiMode,
    status: String,
    messages: List<ChatMessage>,
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoice: () -> Unit,
    onSettings: () -> Unit,
    onModeClick: (AiMode) -> Unit,
    onQuickAction: (String) -> Unit
) {
    Scaffold(
        containerColor = ChintuBackground,

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
                            text = if (model.isBlank()) {
                                "Personal AI Assistant"
                            } else {
                                model
                            },
                            color = ChintuMuted,
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                        )
                    }
                },

                navigationIcon = {
                    OrbLogo()
                },

                actions = {
                    AssistChip(
                        onClick = {
                            onSettings()
                        },
                        label = {
                            Text(mode.name)
                        }
                    )

                    IconButton(
                        onClick = {
                            onSettings()
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
                onInputChange = onInputChange,
                onVoice = onVoice,
                onSend = onSend
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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

                WelcomeContent(
                    model = model,
                    status = status
                )

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

                        MessageBubble(message)
                    }
                }
            }

            QuickActions(
                onAction = onQuickAction
            )

            PrivacyFooter(
                mode = mode
            )
        }
    }
}
