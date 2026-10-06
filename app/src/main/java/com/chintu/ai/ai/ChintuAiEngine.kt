package com.chintu.ai.ai

import android.content.Context

class ChintuAiEngine(
    context: Context
) {

    private val appContext =
        context.applicationContext

    private val resolver =
        AiSettingsResolver(appContext)

    private val factory =
        ProviderFactory(appContext)

    suspend fun ask(
        userText: String,
        mode: AiMode,
        modelOverride: String? = null,
        history: List<ChatMessage> = emptyList()
    ): AiResponse {

        val settings =
            resolver.resolve()

        if (userText.isBlank()) {

            return AiResponse(
                text = "Please enter a message.",
                provider = "CHINTU",
                model = "none",
                verified = false
            )
        }

        val model =
            modelOverride
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: settings.model

        if (model.isBlank()) {

            return AiResponse(
                text =
                    "AI model set nathi. Settings માં model select કરો.",
                provider = "CHINTU",
                model = "none",
                verified = false,
                error = AiError(
                    AiErrorType.CONFIGURATION,
                    "AI model is not configured."
                )
            )
        }

        if (
            mode == AiMode.OFFLINE ||
            mode == AiMode.PRIVATE
        ) {

            if (
                settings.provider !=
                ProviderManager.PROVIDER_OLLAMA
            ) {

                return AiResponse(
                    text =
                        "OFFLINE/PRIVATE mode માટે local Ollama model configure કરો.",
                    provider = "CHINTU",
                    model = model,
                    verified = false,
                    error = AiError(
                        AiErrorType.OFFLINE_UNAVAILABLE,
                        "Local Ollama provider is not configured."
                    )
                )
            }
        }

        val provider =
            factory.create()

        if (provider == null) {

            return AiResponse(
                text =
                    "Selected AI provider supported નથી.",
                provider = "CHINTU",
                model = model,
                verified = false,
                error = AiError(
                    AiErrorType.CONFIGURATION,
                    "Provider could not be created."
                )
            )
        }

        if (
            settings.provider !=
            ProviderManager.PROVIDER_OLLAMA &&
            settings.apiKey.isBlank()
        ) {

            return AiResponse(
                text =
                    "API key missing છે. Settings માં API key add કરો.",
                provider = provider.displayName,
                model = model,
                verified = false,
                error = AiError(
                    AiErrorType.AUTHENTICATION,
                    "API key is missing."
                )
            )
        }

        val contextMessages =
            buildList {

                addAll(
                    history.takeLast(12)
                )

                add(
                    ChatMessage(
                        role = "user",
                        content = userText
                    )
                )
            }

        val request =
            AiRequest(
                messages = contextMessages,
                model = model,
                systemPrompt = systemPrompt(),
                mode = mode,
                maxOutputTokens =
                    TokenBudget.safeForModel(model),
                temperature = 0.7
            )

        return try {

            provider.generate(request)

        } catch (error: Exception) {

            val aiError =
                extractAiError(error)

            AiResponse(
                text =
                    aiError.userMessage(),
                provider =
                    provider.displayName,
                model =
                    model,
                verified = false,
                error =
                    aiError
            )
        }
    }

    private fun extractAiError(
        error: Exception
    ): AiError {

        if (
            error is AiNetworkExceptionAccessor
        ) {
            return error.aiError
        }

        val cause =
            error.cause

        if (
            cause is AiNetworkExceptionAccessor
        ) {
            return cause.aiError
        }

        val message =
            error.message
                ?: "Unknown AI error."

        val lower =
            message.lowercase()

        return when {

            lower.contains("timeout") ->
                AiError(
                    AiErrorType.TIMEOUT,
                    message
                )

            lower.contains("unable to resolve") ||
                    lower.contains("network") ||
                    lower.contains("connection") ->
                AiError(
                    AiErrorType.NETWORK,
                    message
                )

            else ->
                AiError(
                    AiErrorType.UNKNOWN,
                    message
                )
        }
    }

    private fun systemPrompt(): String {

        return """
            You are CHINTU, an original personal AI assistant.

            Languages:
            - Gujarati
            - Hindi
            - English
            - Hinglish

            Reply naturally in the language used by the user.

            Rules:
            1. Be helpful and concise.
            2. Never claim an action was completed unless it was actually verified.
            3. Respect AUTO, ONLINE, OFFLINE and PRIVATE modes.
            4. Do not invent unavailable tools, data or actions.
            5. If something cannot be done, explain honestly.
            6. Prefer practical answers.
        """.trimIndent()
    }
}

/*
 * Small internal interface used so ChintuAiEngine
 * can read provider errors without exposing the
 * private network exception implementation.
 */
private interface AiNetworkExceptionAccessor {
    val aiError: AiError
}
