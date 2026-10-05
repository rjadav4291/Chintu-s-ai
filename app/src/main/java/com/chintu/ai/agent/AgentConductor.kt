package com.chintu.ai.agent

class AgentConductor(
    private val factory: AgentFactory = AgentFactory(),
    private val capabilityChecker: CapabilityChecker = CapabilityChecker(),
    private val humanHelpManager: HumanHelpManager = HumanHelpManager(),
    private val upgradePlanner: UpgradePlanner = UpgradePlanner()
) {

    fun start(request: String): AgentPlan {
        return factory.createPlan(request)
    }

    fun checkCapability(request: String): CapabilityGap? {
        return capabilityChecker.check(request)
    }

    fun askUser(
        taskId: String,
        reason: String,
        actionRequired: String
    ): HumanHelpRequest {
        return humanHelpManager.requestHelp(
            taskId = taskId,
            reason = reason,
            actionRequired = actionRequired
        )
    }

    fun resumeAfterUserAction() {
        humanHelpManager.completeHelp()
    }

    fun createUpgradePlan(gap: CapabilityGap): UpgradePlan {
        return upgradePlanner.createPlan(gap)
    }

    fun isWaitingForUser(): Boolean {
        return humanHelpManager.hasPendingRequest()
    }

    fun pendingUserRequest(): HumanHelpRequest? {
        return humanHelpManager.getPendingRequest()
    }
}
