package com.chintu.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chintu.ai.ai.AiMode
import com.chintu.ai.ai.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    mode: AiMode,
    model: String,
    input: String,
    status: String,
    messages: List<ChatMessage>,
    onSettings: () -> Unit,
    onModeClick: () -> Unit,
    onInputChange: (String) -> Unit,
    onVoice: () -> Unit,
    onSend: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ChintuColors.Background,

        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CHINTU",
                            color = ChintuColors.TextPrimary,
                            fontSize = 20.sp
                        )

                        Text(
                            text = "Personal AI Assistant",
                            color = ChintuColors.TextMuted,
                            fontSize = 10.sp
                        )
                    }
                },

                actions = {

                    StatusBadge(
                        status = status,
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    IconButton(
                        onClick = onSettings
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = ChintuColors.TextPrimary
                        )
                    }
                }
            )
        },

        bottomBar = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ChintuColors.Background)
            ) {

                PrivacyFooter(
                    mode = mode.name
                )

                InputBar(
                    value = input,
                    onValueChange = onInputChange,
                    onVoice = onVoice,
                    onSend = onSend
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            ModeSelector(
                mode = mode,
                onClick = onModeClick
            )

            if (messages.isEmpty()) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    item {

                        WelcomeContent(
                            model = model,
                            status = status,
                            modifier = Modifier.padding(
                                top = 8.dp
                            )
                        )
                    }

                    item {

                        QuickActions(
                            onAction = onInputChange,
                            modifier = Modifier.padding(
                                top = 8.dp
                            )
                        )
                    }
                }

            } else {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = 12.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {

                        items(
                            items = messages
                        ) { message ->

                            MessageBubble(
                                message = message
                            )
                        }
                    }
                }
            }
        }
    }
}
