package com.chintu.ai.agent

enum class UpgradeStatus {
    NOT_REQUIRED,
    REQUIRED,
    PLANNING,
    WAITING_FOR_USER,
    BUILDING,
    TESTING,
    READY_TO_INSTALL,
    FAILED
}

data class UpgradeState(
    val status: UpgradeStatus = UpgradeStatus.NOT_REQUIRED,
    val message: String = "",
    val version: String? = null
)
