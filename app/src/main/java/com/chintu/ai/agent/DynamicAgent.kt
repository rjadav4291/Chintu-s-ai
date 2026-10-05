package com.chintu.ai.agent

import java.util.UUID

enum class AgentStatus {
    CREATED,
    WAITING_FOR_USER,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED
}

data class AgentTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val status: AgentStatus = AgentStatus.CREATED,
    val requiresUser: Boolean = false,
    val userRequest: String? = null,
    val result: String? = null,
    val error: String? = null
)

data class AgentPlan(
    val taskId: String = UUID.randomUUID().toString(),
    val originalRequest: String,
    val agents: List<AgentTask>,
    val createdAt: Long = System.currentTimeMillis()
)
