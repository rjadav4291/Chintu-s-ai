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
        mode: AiMode
    ): AiResponse {

        val settings =
            resolver.resolve()

        if (userText.isBlank()) {
            return AiResponse(
                text = "Please enter a message.",
                provider = "CHINTU",
                model = settings.model,
                verified = false
            )
        }

        if (settings.model.isBlank()) {

            return AiResponse(
                text =
                    "AI model set nathi. Settings ma model select karo.",
                provider = "CHINTU",
                model = "none",
                verified = false
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
                    model = settings.model,
                    verified = false
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
                model = settings.model,
                verified = false
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
                model = settings.model,
                verified = false
            )
        }

        val request =
            AiRequest(
                messages = listOf(
                    ChatMessage(
                        role = "user",
                        content = userText
                    )
                ),
                model = settings.model,
                systemPrompt =
                    systemPrompt(),
                mode = mode
            )

        return try {

            provider.generate(
                request
            )

        } catch (error: Exception) {

            AiResponse(
                text =
                    "AI connection failed: ${
                        error.message
                            ?: "Unknown error"
                    }",
                provider =
                    provider.displayName,
                model =
                    settings.model,
                verified = false
            )
        }
    }

    private fun systemPrompt(): String {

        return """
            You are CHINTU, an original personal AI assistant.

            Support:
            Gujarati
            Hindi
            English
            Hinglish

            Be helpful, clear and concise.

            If the user writes Gujarati,
            reply naturally in Gujarati.

            Never claim that an action was completed
            unless the action was actually verified.

            Respect OFFLINE and PRIVATE modes.
        """.trimIndent()
    }
}
