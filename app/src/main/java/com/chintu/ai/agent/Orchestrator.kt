package com.chintu.ai.agent

import com.chintu.ai.ai.*
import com.chintu.ai.memory.MemoryStore

class Orchestrator(
    private val registry: ProviderRegistry,
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
            LocalCommandEngine(
                memory
            ).tryHandle(userText)

        if (local != null) {

            onStatus("✓ Completed")

            return AiResponse(
                local,
                "Local command engine",
                "local"
            )
        }

        if (
            mode == AiMode.OFFLINE ||
            mode == AiMode.PRIVATE
        ) {

            val p =
                registry.select(mode)
                    ?: return AiResponse(
                        "Offline AI currently available nathi.",
                        "CHINTU",
                        "local",
                        true
                    )

            onStatus(
                "Using ${p.displayName}…"
            )

            return runCatching {

                p.generate(
                    AiRequest(
                        listOf(
                            ChatMessage(
                                "user",
                                userText
                            )
                        ),
                        model,
                        systemPrompt(),
                        mode
                    )
                )

            }.getOrElse {

                AiResponse(
                    "Feature configured/support nathi: " +
                        (
                            it.message
                                ?: "provider error"
                            ),
                    p.displayName,
                    model,
                    false
                )
            }
        }

        onStatus(
            "Selecting AI…"
        )

        val p =
            registry.select(mode)
                ?: return AiResponse(
                    "No AI provider is configured yet.",
                    "CHINTU",
                    model,
                    false
                )

        onStatus(
            "Using ${p.displayName}…"
        )

        return runCatching {

            p.generate(
                AiRequest(
                    listOf(
                        ChatMessage(
                            "user",
                            userText
                        )
                    ),
                    model,
                    systemPrompt(),
                    mode
                )
            )

        }.getOrElse {

            AiResponse(
                "AI request failed: " +
                    (
                        it.message
                            ?: "unknown error"
                        ),
                p.displayName,
                model,
                false
            )
        }
    }

    private fun systemPrompt() =
        """
        You are CHINTU, an original personal AI assistant.

        Be calm, friendly and concise by default.

        Support:
        Gujarati
        Hindi
        English
        Hinglish

        Never claim an action happened unless it was verified.

        Respect the selected AI mode and privacy boundaries.
        """.trimIndent()
}

class LocalCommandEngine(
    private val memory: MemoryStore
) {

    fun tryHandle(
        s: String
    ): String? {

        val t =
            s.trim()
                .lowercase()

        if (
            t.startsWith("remember ") ||
            t.startsWith("યાદ રાખ")
        ) {

            memory.save(
                "note",
                s.substringAfter(
                    ' '
                ).trim()
            )

            return "✓ Memory saved."
        }

        if (
            t.contains(
                "what do you remember"
            ) ||
            t.contains(
                "શું યાદ"
            ) ||
            t.contains(
                "kya yaad"
            )
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
            t.contains(
                "clear all memory"
            ) ||
            t.contains(
                "બધી મેમરી"
            ) ||
            t.contains(
                "memory clear"
            )
        ) {

            memory.clear()

            return "✓ Memory cleared."
        }

        if (
            t == "time" ||
            t.contains(
                "what time"
            )
        ) {

            return java.text.SimpleDateFormat(
                "hh:mm a",
                java.util.Locale.getDefault()
            ).format(
                java.util.Date()
            )
        }

        if (
            t == "date" ||
            t.contains(
                "today's date"
            )
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
