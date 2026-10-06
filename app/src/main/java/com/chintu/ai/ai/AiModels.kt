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
    val timestamp: Long = System.currentTimeMillis()
)

data class AiRequest(
    val messages: List<ChatMessage>,
    val model: String,
    val systemPrompt: String,
    val mode: AiMode,
    val maxOutputTokens: Int = 2048,
    val temperature: Double = 0.7
)

data class AiResponse(
    val text: String,
    val provider: String,
    val model: String,
    val verified: Boolean = true,
    val error: AiError? = null
)
