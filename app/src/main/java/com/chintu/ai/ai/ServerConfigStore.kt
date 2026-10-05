package com.chintu.ai.ai

import android.content.Context

class ServerConfigStore(context: Context) {

    private val prefs = context.getSharedPreferences(
        "chintu_server_config",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val DEFAULT_OPENAI =
            "https://api.openai.com/v1"

        private const val DEFAULT_OPENROUTER =
            "https://openrouter.ai/api/v1"

        private const val DEFAULT_GEMINI =
            "https://generativelanguage.googleapis.com"

        private const val DEFAULT_ANTHROPIC =
            "https://api.anthropic.com"

        private const val DEFAULT_OLLAMA =
            "http://127.0.0.1:11434"

        private const val KEY_PREFIX = "server_"
    }

    fun saveServerUrl(
        provider: String,
        url: String
    ) {
        prefs.edit()
            .putString(
                KEY_PREFIX + provider.lowercase(),
                url.trim().removeSuffix("/")
            )
            .apply()
    }

    fun getServerUrl(
        provider: String
    ): String {

        val saved = prefs.getString(
            KEY_PREFIX + provider.lowercase(),
            null
        )

        return saved ?: getDefaultServerUrl(provider)
    }

    fun hasCustomServer(
        provider: String
    ): Boolean {
        return prefs.contains(
            KEY_PREFIX + provider.lowercase()
        )
    }

    fun resetServer(
        provider: String
    ) {
        prefs.edit()
            .remove(
                KEY_PREFIX + provider.lowercase()
            )
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private fun getDefaultServerUrl(
        provider: String
    ): String {

        return when (provider.lowercase()) {

            ProviderManager.PROVIDER_OPENAI ->
                DEFAULT_OPENAI

            ProviderManager.PROVIDER_OPENROUTER ->
                DEFAULT_OPENROUTER

            ProviderManager.PROVIDER_GEMINI ->
                DEFAULT_GEMINI

            ProviderManager.PROVIDER_ANTHROPIC ->
                DEFAULT_ANTHROPIC

            ProviderManager.PROVIDER_OLLAMA ->
                DEFAULT_OLLAMA

            else ->
                ""
        }
    }
}
