package com.chintu.ai.skills

data class Skill(
    val id: String,
    val name: String,
    val description: String,
    val instructions: String,
    val requiredTools: List<String>,
    val enabled: Boolean = true,
    val version: Int = 1
)

class SkillStore(
    private val prefs:
        android.content.SharedPreferences
) {

    fun builtIns() =
        listOf(

            "Web Research",
            "Summarizer",
            "Translator",
            "Email Writer",
            "Resume Builder",
            "Study Assistant",
            "Travel Planner",
            "Shopping Research",
            "Job Search",
            "Document Analyzer",
            "YouTube Summarizer",
            "Product Comparison",
            "Finance Calculator",
            "Social Media Writer",
            "Image Prompt Generator",
            "Coding Assistant",
            "Meeting Assistant"
        )

    fun isEnabled(
        name: String
    ) =
        prefs.getBoolean(
            "skill_$name",
            true
        )

    fun setEnabled(
        name: String,
        enabled: Boolean
    ) =
        prefs.edit()
            .putBoolean(
                "skill_$name",
                enabled
            )
            .apply()

    fun create(
        skill: Skill
    ) {

        prefs.edit()
            .putString(

                "skill_${skill.id}",

                "${skill.name}|" +
                    "${skill.description}|" +
                    "${skill.instructions}|" +
                    skill.requiredTools
                        .joinToString(",")
            )
            .apply()
    }

    fun exists(
        id: String
    ) =
        prefs.contains(
            "skill_$id"
        )
}
