package com.chintu.ai.privacy

data class PrivacyState(
    val mode: String,
    val memoryEnabled: Boolean,
    val networkAllowed: Boolean,
    val notificationEnabled: Boolean
)
