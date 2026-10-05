package com.chintu.ai.agent

class AgentFactory {

    fun createPlan(request: String): AgentPlan {
        val complex = isComplex(request)

        if (!complex) {
            return AgentPlan(
                originalRequest = request,
                agents = listOf(
                    AgentTask(
                        title = "Direct Executor",
                        description = request
                    )
                )
            )
        }

        val agents = mutableListOf<AgentTask>()

        agents += AgentTask(
            title = "Planner Agent",
            description = "Break the user's request into safe executable steps."
        )

        agents += AgentTask(
            title = "Research Agent",
            description = "Collect required information using available tools."
        )

        agents += AgentTask(
            title = "Execution Agent",
            description = "Execute the available actions and tools."
        )

        agents += AgentTask(
            title = "Verification Agent",
            description = "Verify whether the requested work actually succeeded."
        )

        agents += AgentTask(
            title = "Result Agent",
            description = "Combine verified results into the final response."
        )

        return AgentPlan(
            originalRequest = request,
            agents = agents
        )
    }

    private fun isComplex(request: String): Boolean {
        val text = request.lowercase()

        val indicators = listOf(
            "research",
            "report",
            "find",
            "compare",
            "search",
            "multiple",
            "analyze",
            "analysis",
            "website",
            "file",
            "build",
            "upgrade",
            "fix",
            "repair",
            "create app",
            "apk"
        )

        return text.length > 120 ||
                indicators.count { text.contains(it) } >= 2
    }
}
