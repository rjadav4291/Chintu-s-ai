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
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.chintu.ai.ai.ApiKeyStore
import com.chintu.ai.ai.ConnectionTester
import com.chintu.ai.ai.ModelCatalog
import com.chintu.ai.ai.ModelManager
import com.chintu.ai.ai.ProviderCatalog
import com.chintu.ai.ai.ProviderManager
import com.chintu.ai.ai.ServerConfigStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSettingsScreen(
    providerManager: ProviderManager,
    modelManager: ModelManager,
    serverConfigStore: ServerConfigStore,
    apiKeyStore: ApiKeyStore,
    onBack: () -> Unit
) {

    var provider by remember {
        mutableStateOf(
            providerManager.getProvider()
        )
    }

    var apiKey by remember {
        mutableStateOf(
            apiKeyStore.getApiKey(provider)
        )
    }

    var serverUrl by remember {
        mutableStateOf(
            serverConfigStore.getServerUrl(provider)
        )
    }

    var model by remember {
        mutableStateOf(
            providerManager.getModel()
        )
    }

    var status by remember {
        mutableStateOf("")
    }

    var testing by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(provider) {

        apiKey =
            apiKeyStore.getApiKey(provider)

        serverUrl =
            serverConfigStore.getServerUrl(provider)

        val savedModel =
            providerManager.getModel()

        model =
            if (savedModel.isNotBlank()) {
                savedModel
            } else {
                ModelCatalog
                    .defaultModels(provider)
                    .firstOrNull()
                    ?: ""
            }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(16.dp)
    ) {

        TopAppBar(

            title = {
                Text(
                    "CHINTU AI Settings"
                )
            },

            navigationIcon = {

                IconButton(
                    onClick = onBack
                ) {

                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        ProviderSelector(
            selectedProvider = provider,

            onProviderSelected = {
                provider = it
            }
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        val providerInfo =
            ProviderCatalog.find(provider)

        if (providerInfo?.requiresApiKey == true) {

            OutlinedTextField(

                value = apiKey,

                onValueChange = {
                    apiKey = it
                },

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("API Key")
                },

                leadingIcon = {

                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null
                    )
                },

                visualTransformation =
                    PasswordVisualTransformation(),

                singleLine = true
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }

        OutlinedTextField(

            value = serverUrl,

            onValueChange = {
                serverUrl = it
            },

            modifier = Modifier.fillMaxWidth(),

            label = {
                Text("Server URL")
            },

            leadingIcon = {

                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null
                )
            },

            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        ModelSelector(

            provider = provider,

            selectedModel = model,

            modelManager = modelManager,

            onModelSelected = {
                model = it
            }
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(

            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(10.dp)

        ) {

            Button(

                onClick = {

                    testing = true

                    status =
                        "Testing connection..."

                    scope.launch {

                        val result =
                            ConnectionTester().test(
                                serverUrl = serverUrl,
                                apiKey = apiKey
                            )

                        testing = false

                        status =
                            result.message
                    }
                },

                enabled = !testing,

                modifier =
                    Modifier.weight(1f)

            ) {

                Text(
                    if (testing) {
                        "Testing..."
                    } else {
                        "Test Connection"
                    }
                }
            }

            Button(

                onClick = {

                    providerManager.saveSettings(
                        provider,
                        apiKey,
                        serverUrl,
                        model
                    )

                    if (apiKey.isNotBlank()) {

                        apiKeyStore.saveApiKey(
                            provider,
                            apiKey
                        )
                    }

                    serverConfigStore.saveServerUrl(
                        provider,
                        serverUrl
                    )

                    if (model.isNotBlank()) {

                        modelManager.addModel(
                            model,
                            provider
                        )

                        modelManager.selectModel(
                            model
                        )
                    }

                    status =
                        "Settings saved successfully."
                },

                modifier =
                    Modifier.weight(1f)

            ) {

                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )

                Text(" Save")
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (status.isNotBlank()) {

            ConnectionStatusCard(
                message = status
            )
        }
    }
                    }
