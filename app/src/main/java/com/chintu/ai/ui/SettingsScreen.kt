package com.chintu.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chintu.ai.ai.AiMode
import com.chintu.ai.ai.SecureConfig
import com.chintu.ai.memory.MemoryStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    config: SecureConfig,
    model: String,
    mode: AiMode,
    onModelChange: (String) -> Unit,
    onModeChange: (AiMode) -> Unit,
    memory: MemoryStore,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChintuColors.Background)
    ) {

        SettingsHeader(
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(horizontal = 18.dp)
        ) {

            SectionTitle(
                text = "AI MODE"
            )

            ModeSelector(
                mode = mode,
                onClick = {}
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            SectionTitle(
                text = "MODEL"
            )

            OutlinedTextField(
                value = model,
                onValueChange = onModelChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("Model name")
                },
                placeholder = {
                    Text("gpt-6-luna")
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChintuColors.Primary,
                    unfocusedBorderColor = ChintuColors.Divider,
                    focusedTextColor = ChintuColors.TextPrimary,
                    unfocusedTextColor = ChintuColors.TextPrimary,
                    focusedLabelColor = ChintuColors.Primary,
                    unfocusedLabelColor = ChintuColors.TextSecondary,
                    cursorColor = ChintuColors.Accent
                )
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            SectionTitle(
                text = "SELECT MODE"
            )

            ModeButton(
                title = "AUTO",
                description = "CHINTU automatically chooses the best available route.",
                selected = mode == AiMode.AUTO
            ) {
                onModeChange(AiMode.AUTO)
            }

            ModeButton(
                title = "ONLINE",
                description = "Use configured online AI providers.",
                selected = mode == AiMode.ONLINE
            ) {
                onModeChange(AiMode.ONLINE)
            }

            ModeButton(
                title = "OFFLINE",
                description = "Use local/offline capabilities where available.",
                selected = mode == AiMode.OFFLINE
            ) {
                onModeChange(AiMode.OFFLINE)
            }

            ModeButton(
                title = "PRIVATE",
                description = "Prefer local/private processing.",
                selected = mode == AiMode.PRIVATE
            ) {
                onModeChange(AiMode.PRIVATE)
            }

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            SectionTitle(
                text = "PROVIDER SETTINGS"
            )

            ProviderField(
                label = "OpenAI API Key",
                value = config.get("openai_api_key"),
                onValueChange = {
                    config.set("openai_api_key", it)
                }
            )

            ProviderField(
                label = "OpenRouter API Key",
                value = config.get("openrouter_api_key"),
                onValueChange = {
                    config.set("openrouter_api_key", it)
                }
            )

            ProviderField(
                label = "Anthropic API Key",
                value = config.get("anthropic_api_key"),
                onValueChange = {
                    config.set("anthropic_api_key", it)
                }
            )

            ProviderField(
                label = "Gemini API Key",
                value = config.get("gemini_api_key"),
                onValueChange = {
                    config.set("gemini_api_key", it)
                }
            )

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            SectionTitle(
                text = "MEMORY"
            )

            Text(
                text = "CHINTU can store local memories to improve future conversations.",
                color = ChintuColors.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(
                    bottom = 10.dp
                )
            )

            Button(
                onClick = {
                    memory.clear()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear memory"
                )

                Text(
                    text = "  Clear Memory"
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "CHINTU",
                color = ChintuColors.TextPrimary,
                fontSize = 22.sp
            )

            Text(
                text = "Personal AI Assistant",
                color = ChintuColors.Accent,
                fontSize = 12.sp
            )

            Text(
                text = "Version 1.0.0",
                color = ChintuColors.TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(
                    top = 4.dp
                )
            )

            Spacer(
                modifier = Modifier.height(30.dp)
            )
        }
    }
}

@Composable
private fun SettingsHeader(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            onClick = onBack
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = ChintuColors.TextPrimary
            )
        }

        Column(
            modifier = Modifier.padding(
                start = 4.dp
            )
        ) {
            Text(
                text = "Settings",
                color = ChintuColors.TextPrimary,
                fontSize = 20.sp
            )

            Text(
                text = "Configure CHINTU",
                color = ChintuColors.TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ModeButton(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Button(
            onClick = onClick,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title
            )
        }

        Text(
            text = description,
            color = if (selected) {
                ChintuColors.TextPrimary
            } else {
                ChintuColors.TextMuted
            },
            fontSize = 11.sp,
            modifier = Modifier
                .weight(2f)
                .padding(start = 10.dp)
        )
    }
}

@Composable
private fun ProviderField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    varTextField(
        label = label,
        value = value,
        onValueChange = onValueChange
    )

    Spacer(
        modifier = Modifier.height(8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun varTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = {
            Text(label)
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ChintuColors.Primary,
            unfocusedBorderColor = ChintuColors.Divider,
            focusedTextColor = ChintuColors.TextPrimary,
            unfocusedTextColor = ChintuColors.TextPrimary,
            focusedLabelColor = ChintuColors.Primary,
            unfocusedLabelColor = ChintuColors.TextSecondary,
            cursorColor = ChintuColors.Accent
        )
    )
}
