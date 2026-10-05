package com.chintu.ai.ai

data class ProviderInfo(
    val id: String,
    val name: String,
    val requiresApiKey: Boolean,
    val supportsCustomServer: Boolean
)

object ProviderCatalog {

    val all: List<ProviderInfo> = listOf(

        ProviderInfo(
            id = ProviderManager.PROVIDER_OPENAI,
            name = "OpenAI",
            requiresApiKey = true,
            supportsCustomServer = true
        ),

        ProviderInfo(
            id = ProviderManager.PROVIDER_OPENROUTER,
            name = "OpenRouter",
            requiresApiKey = true,
            supportsCustomServer = true
        ),

        ProviderInfo(
            id = ProviderManager.PROVIDER_GEMINI,
            name = "Google Gemini",
            requiresApiKey = true,
            supportsCustomServer = true
        ),

        ProviderInfo(
            id = ProviderManager.PROVIDER_ANTHROPIC,
            name = "Anthropic",
            requiresApiKey = true,
            supportsCustomServer = true
        ),

        ProviderInfo(
            id = ProviderManager.PROVIDER_OLLAMA,
            name = "Ollama",
            requiresApiKey = false,
            supportsCustomServer = true
        ),

        ProviderInfo(
            id = ProviderManager.PROVIDER_CUSTOM,
            name = "Custom / OpenAI Compatible",
            requiresApiKey = true,
            supportsCustomServer = true
        )
    )

    fun find(
        id: String
    ): ProviderInfo? {
        return all.firstOrNull {
            it.id.equals(
                id,
                ignoreCase = true
            )
        }
    }

    fun name(
        id: String
    ): String {
        return find(id)?.name ?: id
    }
}
