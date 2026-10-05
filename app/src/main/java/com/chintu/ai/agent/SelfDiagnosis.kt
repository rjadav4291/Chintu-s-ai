package com.chintu.ai.agent

data class Diagnosis(
    val problem: String,
    val probableCause: String,
    val recommendedAction: String
)

class SelfDiagnosis {

    fun diagnose(error: Throwable): Diagnosis {
        val message = error.message ?: "Unknown error"

        return Diagnosis(
            problem = message,
            probableCause = classify(message),
            recommendedAction = "Review the failed operation, apply a safe repair if available, then run verification tests."
        )
    }

    private fun classify(message: String): String {
        val text = message.lowercase()

        return when {
            "permission" in text ->
                "Required Android permission is missing."

            "network" in text || "timeout" in text ->
                "Network connection or remote service may be unavailable."

            "authentication" in text || "unauthorized" in text ->
                "Authentication or authorization may be missing."

            "not found" in text ->
                "Required resource or capability could not be found."

            else ->
                "The exact cause requires additional diagnostics."
        }
    }
}
