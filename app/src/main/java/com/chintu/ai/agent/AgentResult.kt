package com.chintu.ai.agent

sealed class AgentResult {

    data class Success(
        val message: String
    ) : AgentResult()

    data class WaitingForUser(
        val request: HumanHelpRequest
    ) : AgentResult()

    data class CapabilityMissing(
        val gap: CapabilityGap,
        val upgradePlan: UpgradePlan
    ) : AgentResult()

    data class Failure(
        val message: String,
        val diagnosis: Diagnosis? = null
    ) : AgentResult()
}
