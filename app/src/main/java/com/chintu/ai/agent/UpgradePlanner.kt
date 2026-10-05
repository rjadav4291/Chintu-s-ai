package com.chintu.ai.agent

data class UpgradePlan(
    val title: String,
    val reason: String,
    val requiredCapability: Capability,
    val steps: List<String>
)

class UpgradePlanner {

    fun createPlan(gap: CapabilityGap): UpgradePlan {
        return UpgradePlan(
            title = "CHINTU Capability Upgrade",
            reason = gap.reason,
            requiredCapability = gap.capability,
            steps = listOf(
                "Identify the missing capability.",
                "Create a safe implementation plan.",
                "Update the required project files.",
                "Run unit tests.",
                "Build the application.",
                "Verify the generated APK.",
                "Ask the user to install the verified APK."
            )
        )
    }
}
