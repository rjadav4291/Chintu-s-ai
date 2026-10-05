package com.chintu.ai.ai

import android.content.Context
import org.json.JSONObject

/**
 * CHINTU Provider Manager
 *
 * Stores:
 * - Selected provider
 * - API key
 * - Server URL
 * - Model name
 *
 * Data is stored locally on the device.
 */
class ProviderManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "chintu_provider_settings",
        Context.MODE_PRIVATE
    )

    companion object {

        const val PROVIDER_OPENAI = "openai"
        const val PROVIDER_OPENROUTER = "openrouter"
        const val PROVIDER_GEMINI = "gemini"
        const val PROVIDER_ANTHROPIC = "anthropic"
        const val PROVIDER_OLLAMA = "ollama"
        const val PROVIDER_CUSTOM = "custom"

        private const val KEY_PROVIDER = "provider"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_MODEL = "model"
    }

    fun saveSettings(
        provider: String,
        apiKey: String,
        serverUrl: String,
        model: String
    ) {
        prefs.edit()
            .putString(KEY_PROVIDER, provider)
            .putString(KEY_API_KEY, apiKey)
            .putString(KEY_SERVER_URL, serverUrl)
            .putString(KEY_MODEL, model)
            .apply()
    }

    fun getProvider(): String {
        return prefs.getString(
            KEY_PROVIDER,
            PROVIDER_OPENAI
        ) ?: PROVIDER_OPENAI
    }

    fun getApiKey(): String {
        return prefs.getString(
            KEY_API_KEY,
            ""
        ) ?: ""
    }

    fun getServerUrl(): String {
        return prefs.getString(
            KEY_SERVER_URL,
            ""
        ) ?: ""
    }

    fun getModel(): String {
        return prefs.getString(
            KEY_MODEL,
            ""
        ) ?: ""
    }

    fun clearSettings() {
        prefs.edit().clear().apply()
    }

    fun hasApiKey(): Boolean {
        return getApiKey().isNotBlank()
    }

    fun hasServerUrl(): Boolean {
        return getServerUrl().isNotBlank()
    }

    fun hasModel(): Boolean {
        return getModel().isNotBlank()
    }

    fun isConfigured(): Boolean {
        val provider = getProvider()

        return when (provider) {
            PROVIDER_OLLAMA -> {
                hasServerUrl() && hasModel()
            }

            PROVIDER_CUSTOM -> {
                hasServerUrl() && hasModel()
            }

            else -> {
                hasApiKey() && hasModel()
            }
        }
    }

    fun getSettings(): ProviderSettings {
        return ProviderSettings(
            provider = getProvider(),
            apiKey = getApiKey(),
            serverUrl = getServerUrl(),
            model = getModel()
        )
    }

    fun exportSettings(): String {

        val settings = getSettings()

        return JSONObject().apply {
            put("provider", settings.provider)
            put("apiKey", settings.apiKey)
            put("serverUrl", settings.serverUrl)
            put("model", settings.model)
        }.toString()
    }

    fun importSettings(json: String): Boolean {

        return try {

            val objectJson = JSONObject(json)

            val provider = objectJson.optString(
                "provider",
                PROVIDER_OPENAI
            )

            val apiKey = objectJson.optString(
                "apiKey",
                ""
            )

            val serverUrl = objectJson.optString(
                "serverUrl",
                ""
            )

            val model = objectJson.optString(
                "model",
                ""
            )

            saveSettings(
                provider = provider,
                apiKey = apiKey,
                serverUrl = serverUrl,
                model = model
            )

            true

        } catch (_: Exception) {
            false
        }
    }
}

data class ProviderSettings(
    val provider: String,
    val apiKey: String,
    val serverUrl: String,
    val model: String
)
