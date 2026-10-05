package com.chintu.ai.ai

object ModelCatalog {

    fun defaultModels(
        provider: String
    ): List<String> {

        return when (provider.lowercase()) {

            ProviderManager.PROVIDER_OPENAI -> {
                listOf(
                    "gpt-5",
                    "gpt-5-mini"
                )
            }

            ProviderManager.PROVIDER_OPENROUTER -> {
                listOf(
                    "openai/gpt-5",
                    "openai/gpt-5-mini",
                    "anthropic/claude"
                )
            }

            ProviderManager.PROVIDER_GEMINI -> {
                listOf(
                    "gemini-2.5-pro",
                    "gemini-2.5-flash"
                )
            }

            ProviderManager.PROVIDER_ANTHROPIC -> {
                listOf(
                    "claude-sonnet",
                    "claude-haiku"
                )
            }

            ProviderManager.PROVIDER_OLLAMA -> {
                listOf(
                    "qwen",
                    "llama3"
                )
            }

            else -> {
                emptyList()
            }
        }
    }
}
