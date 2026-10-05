package com.chintu.ai.ai

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * CHINTU Secure API Key Store
 *
 * API keys are stored using Android encrypted storage.
 */
class ApiKeyStore(context: Context) {

    companion object {
        private const val FILE_NAME = "chintu_secure_keys"

        private const val KEY_OPENAI = "openai"
        private const val KEY_OPENROUTER = "openrouter"
        private const val KEY_GEMINI = "gemini"
        private const val KEY_ANTHROPIC = "anthropic"
        private const val KEY_OLLAMA = "ollama"
        private const val KEY_CUSTOM = "custom"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(
            MasterKey.KeyScheme.AES256_GCM
        )
        .build()

    private val preferences =
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    fun saveApiKey(
        provider: String,
        apiKey: String
    ) {
        preferences.edit()
            .putString(
                keyForProvider(provider),
                apiKey.trim()
            )
            .apply()
    }

    fun getApiKey(
        provider: String
    ): String {

        return preferences.getString(
            keyForProvider(provider),
            ""
        ) ?: ""
    }

    fun hasApiKey(
        provider: String
    ): Boolean {
        return getApiKey(provider).isNotBlank()
    }

    fun deleteApiKey(
        provider: String
    ) {
        preferences.edit()
            .remove(
                keyForProvider(provider)
            )
            .apply()
    }

    fun clearAll() {
        preferences.edit().clear().apply()
    }

    private fun keyForProvider(
        provider: String
    ): String {

        return when (
            provider.lowercase()
        ) {

            ProviderManager.PROVIDER_OPENAI ->
                KEY_OPENAI

            ProviderManager.PROVIDER_OPENROUTER ->
                KEY_OPENROUTER

            ProviderManager.PROVIDER_GEMINI ->
                KEY_GEMINI

            ProviderManager.PROVIDER_ANTHROPIC ->
                KEY_ANTHROPIC

            ProviderManager.PROVIDER_OLLAMA ->
                KEY_OLLAMA

            ProviderManager.PROVIDER_CUSTOM ->
                KEY_CUSTOM

            else ->
                "provider_${provider.lowercase()}"
        }
    }
}
