package com.chintu.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.chintu.ai.ai.AiMode
import com.chintu.ai.ai.ApiKeyStore
import com.chintu.ai.ai.ModelManager
import com.chintu.ai.ai.ProviderManager
import com.chintu.ai.ai.ServerConfigStore
import com.chintu.ai.memory.MemoryStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    model: String,
    onModelChange: (String) -> Unit,
    mode: AiMode,
    onModeChange: (AiMode) -> Unit,
    memory: MemoryStore,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val providerManager = remember { ProviderManager(context) }
    val modelManager = remember { ModelManager(context) }
    val serverConfigStore = remember { ServerConfigStore(context) }
    val apiKeyStore = remember { ApiKeyStore(context) }

    var showProviderSettings by remember { mutableStateOf(false) }

    if (showProviderSettings) {
        ProviderSettingsScreen(
            providerManager = providerManager,
            modelManager = modelManager,
            serverConfigStore = serverConfigStore,
            apiKeyStore = apiKeyStore,
            onBack = {
                val selected = modelManager.getSelectedModel()
                if (selected.isNotBlank()) {
                    onModelChange(selected)
                }
                showProviderSettings = false
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        TopAppBar(
            title = {
                Text("CHINTU Settings")
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("AI Provider & API")

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Provider: ${
                        providerManager.getProvider()
                    }"
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Model: ${
                        if (model.isBlank()) "Not selected" else model
                    }"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        showProviderSettings = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null
                    )

                    Text("  AI Provider / API Settings")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("CHINTU Mode")

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = mode == AiMode.AUTO,
                        onClick = {
                            onModeChange(AiMode.AUTO)
                        },
                        label = {
                            Text("AUTO")
                        }
                    )

                    FilterChip(
                        selected = mode == AiMode.ONLINE,
                        onClick = {
                            onModeChange(AiMode.ONLINE)
                        },
                        label = {
                            Text("ONLINE")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = mode == AiMode.OFFLINE,
                        onClick = {
                            onModeChange(AiMode.OFFLINE)
                        },
                        label = {
                            Text("OFFLINE")
                        }
                    )

                    FilterChip(
                        selected = mode == AiMode.PRIVATE,
                        onClick = {
                            onModeChange(AiMode.PRIVATE)
                        },
                        label = {
                            Text("PRIVATE")
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Memory")

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null
                    )

                    Text("CHINTU local memory")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        memory.clear()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Memory")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Security")

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "API keys are stored using encrypted Android storage."
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "CHINTU will not claim an action is completed unless it is actually verified."
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
