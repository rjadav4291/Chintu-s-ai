package com.chintu.ai.ai

import android.content.Context

data class ResolvedAiSettings(
    val provider: String,
    val apiKey: String,
    val serverUrl: String,
    val model: String
)

class AiSettingsResolver(context: Context) {

    private val providerManager =
        ProviderManager(context)

    private val apiKeyStore =
        ApiKeyStore(context)

    private val serverConfigStore =
        ServerConfigStore(context)

    private val modelManager =
        ModelManager(context)

    fun resolve(): ResolvedAiSettings {

        val provider =
            providerManager.getProvider()

        val apiKey =
            apiKeyStore.getApiKey(provider)
                .ifBlank {
                    providerManager.getApiKey()
                }

        val serverUrl =
            serverConfigStore.getServerUrl(provider)
                .ifBlank {
                    providerManager.getServerUrl()
                }

        val savedModel =
            providerManager.getModel()

        val selectedModel =
            modelManager.getSelectedModel()

        val model =
            savedModel
                .ifBlank { selectedModel }

        return ResolvedAiSettings(
            provider = provider,
            apiKey = apiKey,
            serverUrl = serverUrl,
            model = model
        )
    }

    fun isReady(): Boolean {

        val settings = resolve()

        if (settings.model.isBlank()) {
            return false
        }

        return when (settings.provider) {

            ProviderManager.PROVIDER_OLLAMA -> {
                settings.serverUrl.isNotBlank()
            }

            else -> {
                settings.apiKey.isNotBlank() &&
                    settings.serverUrl.isNotBlank()
            }
        }
    }
}
