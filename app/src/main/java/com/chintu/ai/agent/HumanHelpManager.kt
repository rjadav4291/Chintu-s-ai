package com.chintu.ai.agent

data class HumanHelpRequest(
    val taskId: String,
    val reason: String,
    val actionRequired: String,
    val createdAt: Long = System.currentTimeMillis()
)

class HumanHelpManager {

    private var pendingRequest: HumanHelpRequest? = null

    fun requestHelp(
        taskId: String,
        reason: String,
        actionRequired: String
    ): HumanHelpRequest {
        val request = HumanHelpRequest(
            taskId = taskId,
            reason = reason,
            actionRequired = actionRequired
        )

        pendingRequest = request
        return request
    }

    fun getPendingRequest(): HumanHelpRequest? {
        return pendingRequest
    }

    fun completeHelp() {
        pendingRequest = null
    }

    fun hasPendingRequest(): Boolean {
        return pendingRequest != null
    }
}
