package com.chintu.ai.tools

enum class ToolPermission {
    NONE,
    USER_CONFIRMATION,
    ANDROID_RUNTIME_PERMISSION
}

data class ToolResult(
    val tool: String,
    val success: Boolean,
    val verified: Boolean,
    val message: String,
    val data: Map<String, String> =
        emptyMap()
)

interface ChintuTool {

    val id: String

    val description: String

    val permission: ToolPermission

    suspend fun execute(
        input: Map<String, String>
    ): ToolResult
}

class ToolRegistry(
    private val tools: List<ChintuTool>
) {

    fun find(
        id: String
    ) =
        tools.firstOrNull {
            it.id == id
        }

    fun ids() =
        tools.map {
            it.id
        }
}
