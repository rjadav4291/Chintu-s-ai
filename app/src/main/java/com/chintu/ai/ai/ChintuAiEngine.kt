package com.chintu.ai.ai

import android.content.Context

class ChintuAiEngine(
    context: Context
) {
    private val appContext = context.applicationContext

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

        val settings = resolver.resolve()

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
                text = "AI model set nathi. Settings માં model select કરો.",
                provider = "CHINTU",
                model = "none",
                verified = false,
                error = AiError(
                    type = AiErrorType.CONFIGURATION,
                    message = "AI model is not configured."
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
                        type = AiErrorType.OFFLINE_UNAVAILABLE,
                        message = "Local Ollama provider is not configured."
                    )
                )
            }
        }

        val provider =
            factory.create()

        if (provider == null) {
            return AiResponse(
                text = "Selected AI provider supported નથી.",
                provider = "CHINTU",
                model = model,
                verified = false,
                error = AiError(
                    type = AiErrorType.CONFIGURATION,
                    message = "Provider could not be created."
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
                    type = AiErrorType.AUTHENTICATION,
                    message = "API key is missing."
                )
            )
        }

        val contextMessages =
            buildList {
                addAll(history.takeLast(12))

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

        } catch (error: AiProviderException) {

            AiResponse(
                text = error.aiError.userMessage(),
                provider = provider.displayName,
                model = model,
                verified = false,
                error = error.aiError
            )

        } catch (error: Exception) {

            val aiError =
                when {
                    error.message
                        ?.contains(
                            "timeout",
                            ignoreCase = true
                        ) == true ->
                        AiError(
                            AiErrorType.TIMEOUT,
                            error.message
                                ?: "Request timed out."
                        )

                    error.message
                        ?.contains(
                            "network",
                            ignoreCase = true
                        ) == true ->
                        AiError(
                            AiErrorType.NETWORK,
                            error.message
                                ?: "Network error."
                        )

                    else ->
                        AiError(
                            AiErrorType.UNKNOWN,
                            error.message
                                ?: "Unknown AI error."
                        )
                }

            AiResponse(
                text = aiError.userMessage(),
                provider = provider.displayName,
                model = model,
                verified = false,
                error = aiError
            )
        }
    }

    private fun systemPrompt(): String {
        return """
            You are CHINTU, an original personal AI assistant.

            Languages:
            Gujarati, Hindi, English and Hinglish.

            Reply naturally in the language used by the user.

            Rules:
            1. Be helpful and practical.
            2. Be concise unless detail is requested.
            3. Never claim an action was completed unless it was actually verified.
            4. Respect AUTO, ONLINE, OFFLINE and PRIVATE modes.
            5. Never invent unavailable tools or information.
            6. If something cannot be done, explain honestly.
        """.trimIndent()
    }
}
