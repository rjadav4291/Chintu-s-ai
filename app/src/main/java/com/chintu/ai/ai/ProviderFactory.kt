package com.chintu.ai.ai

import android.content.Context

class ProviderFactory(
    private val context: Context
) {

    fun create(): AiProvider? {

        val resolver =
            AiSettingsResolver(context)

        val settings =
            resolver.resolve()

        val secureConfig =
            SecureConfig(context)

        when (settings.provider) {

            ProviderManager.PROVIDER_OPENAI -> {

                secureConfig.set(
                    "openai_api_key",
                    settings.apiKey
                )

                secureConfig.set(
                    "openai_endpoint",
                    toChatEndpoint(settings.serverUrl)
                )

                return OpenAiCompatibleProvider(
                    cfg = secureConfig,
                    id = "openai",
                    displayName = "OpenAI",
                    defaultEndpoint =
                        "https://api.openai.com/v1/chat/completions"
                )
            }

            ProviderManager.PROVIDER_OPENROUTER -> {

                secureConfig.set(
                    "openrouter_api_key",
                    settings.apiKey
                )

                secureConfig.set(
                    "openrouter_endpoint",
                    toChatEndpoint(settings.serverUrl)
                )

                return OpenAiCompatibleProvider(
                    cfg = secureConfig,
                    id = "openrouter",
                    displayName = "OpenRouter",
                    defaultEndpoint =
                        "https://openrouter.ai/api/v1/chat/completions"
                )
            }

            ProviderManager.PROVIDER_OLLAMA -> {

                secureConfig.set(
                    "ollama_endpoint",
                    toOllamaEndpoint(settings.serverUrl)
                )

                return OpenAiCompatibleProvider(
                    cfg = secureConfig,
                    id = "ollama",
                    displayName = "Ollama",
                    defaultEndpoint =
                        "http://127.0.0.1:11434/api/chat"
                )
            }

            ProviderManager.PROVIDER_ANTHROPIC -> {

                secureConfig.set(
                    "anthropic_api_key",
                    settings.apiKey
                )

                secureConfig.set(
                    "anthropic_endpoint",
                    settings.serverUrl
                )

                return AnthropicProvider(
                    secureConfig
                )
            }

            ProviderManager.PROVIDER_GEMINI -> {

                secureConfig.set(
                    "gemini_api_key",
                    settings.apiKey
                )

                secureConfig.set(
                    "gemini_endpoint",
                    settings.serverUrl
                )

                return GeminiProvider(
                    secureConfig
                )
            }

            else -> {
                return null
            }
        }
    }

    private fun toChatEndpoint(
        serverUrl: String
    ): String {

        val clean =
            serverUrl
                .trim()
                .removeSuffix("/")

        if (clean.endsWith("/chat/completions")) {
            return clean
        }

        return "$clean/chat/completions"
    }

    private fun toOllamaEndpoint(
        serverUrl: String
    ): String {

        val clean =
            serverUrl
                .trim()
                .removeSuffix("/")

        if (clean.endsWith("/api/chat")) {
            return clean
        }

        if (clean.endsWith("/api")) {
            return "$clean/chat"
        }

        return "$clean/api/chat"
    }
}
