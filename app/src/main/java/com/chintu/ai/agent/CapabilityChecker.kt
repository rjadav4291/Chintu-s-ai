package com.chintu.ai.agent

enum class Capability {
    WEB_SEARCH,
    WEB_BROWSING,
    FILE_ACCESS,
    CAMERA,
    MICROPHONE,
    CONTACTS,
    CALENDAR,
    DEVICE_ACTION,
    APP_LAUNCH,
    APK_BUILD,
    CODE_EDIT,
    SELF_REPAIR,
    SELF_UPGRADE
}

data class CapabilityGap(
    val capability: Capability,
    val reason: String,
    val suggestedUpgrade: String
)

class CapabilityChecker {

    fun check(request: String): CapabilityGap? {
        val text = request.lowercase()

        return when {
            text.contains("build apk") || text.contains("make apk") -> {
                CapabilityGap(
                    capability = Capability.APK_BUILD,
                    reason = "APK building requires a configured build environment.",
                    suggestedUpgrade = "Connect CHINTU to a trusted build pipeline such as GitHub Actions."
                )
            }

            text.contains("edit code") || text.contains("change code") -> {
                CapabilityGap(
                    capability = Capability.CODE_EDIT,
                    reason = "Code editing requires access to the project source.",
                    suggestedUpgrade = "Provide a connected project repository and an approved code-edit workflow."
                )
            }

            text.contains("search web") || text.contains("internet") -> {
                CapabilityGap(
                    capability = Capability.WEB_SEARCH,
                    reason = "Web search requires an available web-search provider.",
                    suggestedUpgrade = "Configure a supported web provider."
                )
            }

            else -> null
        }
    }
}
