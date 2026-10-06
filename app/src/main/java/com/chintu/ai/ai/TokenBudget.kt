package com.chintu.ai.ai

object TokenBudget {

    const val DEFAULT_MAX_OUTPUT = 2048
    const val SAFE_MAX_OUTPUT = 4096
    const val MIN_OUTPUT = 256

    fun forModel(model: String): Int {
        val name = model.lowercase()

        return when {
            name.contains("free") -> 1024
            name.contains("mini") -> 2048
            name.contains("haiku") -> 2048
            name.contains("flash") -> 2048
            name.contains("ollama") -> 2048
            else -> DEFAULT_MAX_OUTPUT
        }
    }

    fun clamp(value: Int): Int {
        return value.coerceIn(MIN_OUTPUT, SAFE_MAX_OUTPUT)
    }

    fun safeForModel(model: String): Int {
        return clamp(forModel(model))
    }
}
