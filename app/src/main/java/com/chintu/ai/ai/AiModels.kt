package com.chintu.ai.ai

enum class AiMode {
    AUTO,
    ONLINE,
    OFFLINE,
    PRIVATE
}

data class ChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long =
        System.currentTimeMillis()
)

data class AiRequest(
    val messages: List<ChatMessage>,
    val model: String,
    val systemPrompt: String,
    val mode: AiMode
)

data class AiResponse(
    val text: String,
    val provider: String,
    val model: String,
    val verified: Boolean = true
)

interface AiProvider {

    val id: String

    val displayName: String

    suspend fun isAvailable(): Boolean

    suspend fun generate(
        request: AiRequest
    ): AiResponse
}

class ProviderRegistry(
    private val providers: List<AiProvider>
) {

    suspend fun available():
            List<AiProvider> =
        providers.filter {
            it.isAvailable()
        }

    suspend fun select(
        mode: AiMode
    ): AiProvider? = when (mode) {

        AiMode.OFFLINE ->
            providers.firstOrNull {
                it.id == "ollama" &&
                    it.isAvailable()
            }

        AiMode.ONLINE,
        AiMode.AUTO,
        AiMode.PRIVATE ->

            providers.firstOrNull {
                it.isAvailable() &&
                    (
                        mode != AiMode.PRIVATE ||
                            it.id == "ollama"
                        )
            }
    }
}
