package com.chintu.ai.agent

import com.chintu.ai.ai.AiMode
import com.chintu.ai.ai.AiResponse
import com.chintu.ai.ai.ChintuAiEngine
import com.chintu.ai.memory.MemoryStore

class Orchestrator(
    private val aiEngine: ChintuAiEngine,
    private val memory: MemoryStore
) {

    suspend fun answer(
        userText: String,
        mode: AiMode,
        model: String,
        onStatus: (String) -> Unit
    ): AiResponse {

        onStatus("Understanding…")

        val local =
            LocalCommandEngine(memory)
                .tryHandle(userText)

        if (local != null) {

            onStatus("✓ Completed")

            return AiResponse(
                text = local,
                provider = "Local command engine",
                model = "local",
                verified = true
            )
        }

        onStatus("Selecting AI…")

        onStatus("Thinking…")

        val result =
            aiEngine.ask(
                userText = userText,
                mode = mode
            )

        if (result.verified) {
            onStatus(
                "Using ${result.provider} • ${result.model}"
            )
        } else {
            onStatus("✕ AI request failed")
        }

        return result
    }
}

class LocalCommandEngine(
    private val memory: MemoryStore
) {

    fun tryHandle(
        input: String
    ): String? {

        val text =
            input.trim()

        val lower =
            text.lowercase()

        if (
            lower.startsWith("remember ") ||
            text.startsWith("યાદ રાખ")
        ) {

            val content =
                text.substringAfter(
                    " ",
                    ""
                ).trim()

            if (content.isNotBlank()) {

                memory.save(
                    category = "note",
                    content = content
                )

                return "✓ Memory saved."
            }
        }

        if (
            lower.contains(
                "what do you remember"
            ) ||
            text.contains("શું યાદ") ||
            lower.contains("kya yaad")
        ) {

            return memory
                .list()
                .joinToString("\n") {
                    "• ${it.content}"
                }
                .ifBlank {
                    "Memory empty."
                }
        }

        if (
            lower.contains(
                "clear all memory"
            ) ||
            text.contains("બધી મેમરી") ||
            lower.contains("memory clear")
        ) {

            memory.clear()

            return "✓ Memory cleared."
        }

        if (
            lower == "time" ||
            lower.contains("what time")
        ) {

            return java.text.SimpleDateFormat(
                "hh:mm a",
                java.util.Locale.getDefault()
            ).format(
                java.util.Date()
            )
        }

        if (
            lower == "date" ||
            lower.contains("today's date")
        ) {

            return java.text.SimpleDateFormat(
                "dd MMM yyyy",
                java.util.Locale.getDefault()
            ).format(
                java.util.Date()
            )
        }

        return null
    }
}
