package com.chintu.ai.automation

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class AutomationStatus {
    ACTIVE,
    PAUSED,
    FAILED,
    COMPLETED
}

data class Automation(
    val id: String,
    val title: String,
    val prompt: String,
    val schedule: String,
    val status: AutomationStatus
)

class AutomationStore(
    context: Context
) {

    private val p =
        context.getSharedPreferences(
            "automations",
            Context.MODE_PRIVATE
        )

    private val key =
        "items"

    fun list(): List<Automation> {

        val a =
            JSONArray(
                p.getString(
                    key,
                    "[]"
                )
            )

        return (
            0 until a.length()
        ).map {

            val o =
                a.getJSONObject(it)

            Automation(
                o.getString("id"),
                o.getString("title"),
                o.getString("prompt"),
                o.getString("schedule"),
                AutomationStatus.valueOf(
                    o.getString("status")
                )
            )
        }
    }

    fun save(
        a: Automation
    ) {

        val arr =
            JSONArray(
                list()
                    .map {

                        JSONObject()
                            .put(
                                "id",
                                it.id
                            )
                            .put(
                                "title",
                                it.title
                            )
                            .put(
                                "prompt",
                                it.prompt
                            )
                            .put(
                                "schedule",
                                it.schedule
                            )
                            .put(
                                "status",
                                it.status.name
                            )
                    }
                    .toString()
            )

        arr.put(

            JSONObject()
                .put(
                    "id",
                    a.id
                )
                .put(
                    "title",
                    a.title
                )
                .put(
                    "prompt",
                    a.prompt
                )
                .put(
                    "schedule",
                    a.schedule
                )
                .put(
                    "status",
                    a.status.name
                )
        )

        p.edit()
            .putString(
                key,
                arr.toString()
            )
            .apply()
    }
}
