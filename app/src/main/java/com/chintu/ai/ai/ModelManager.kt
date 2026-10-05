package com.chintu.ai.ai

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * CHINTU Model Manager
 *
 * Handles:
 * - Model list
 * - Selected/default model
 * - Add/remove models
 * - Provider-wise models
 * - Import/export model settings
 */
class ModelManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "chintu_model_settings",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_MODELS = "models"
        private const val KEY_SELECTED_MODEL = "selected_model"
    }

    fun addModel(
        name: String,
        provider: String
    ): Boolean {

        val cleanName = name.trim()

        if (cleanName.isEmpty()) {
            return false
        }

        val models = getModels().toMutableList()

        val alreadyExists = models.any {
            it.name.equals(cleanName, ignoreCase = true) &&
            it.provider == provider
        }

        if (alreadyExists) {
            return false
        }

        models.add(
            ChintuModel(
                name = cleanName,
                provider = provider
            )
        )

        saveModels(models)

        if (getSelectedModel().isEmpty()) {
            selectModel(cleanName)
        }

        return true
    }

    fun removeModel(
        name: String,
        provider: String? = null
    ) {

        val models = getModels()
            .filterNot {
                it.name.equals(name, ignoreCase = true) &&
                (provider == null || it.provider == provider)
            }

        saveModels(models)

        if (getSelectedModel().equals(name, ignoreCase = true)) {

            val nextModel = models.firstOrNull()

            selectModel(
                nextModel?.name ?: ""
            )
        }
    }

    fun getModels(): List<ChintuModel> {

        val json = prefs.getString(
            KEY_MODELS,
            "[]"
        ) ?: "[]"

        return try {

            val array = JSONArray(json)
            val result = mutableListOf<ChintuModel>()

            for (index in 0 until array.length()) {

                val item = array.optJSONObject(index)
                    ?: continue

                val name = item.optString(
                    "name",
                    ""
                ).trim()

                val provider = item.optString(
                    "provider",
                    ProviderManager.PROVIDER_CUSTOM
                ).trim()

                if (name.isNotEmpty()) {
                    result.add(
                        ChintuModel(
                            name = name,
                            provider = provider
                        )
                    )
                }
            }

            result

        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getModelsForProvider(
        provider: String
    ): List<ChintuModel> {

        return getModels().filter {
            it.provider == provider
        }
    }

    fun selectModel(
        modelName: String
    ) {

        prefs.edit()
            .putString(
                KEY_SELECTED_MODEL,
                modelName.trim()
            )
            .apply()
    }

    fun getSelectedModel(): String {

        return prefs.getString(
            KEY_SELECTED_MODEL,
            ""
        ) ?: ""
    }

    fun getSelectedModelInfo(): ChintuModel? {

        val selected = getSelectedModel()

        if (selected.isBlank()) {
            return null
        }

        return getModels().firstOrNull {
            it.name.equals(
                selected,
                ignoreCase = true
            )
        }
    }

    fun setModels(
        models: List<ChintuModel>
    ) {

        saveModels(models)

        val selected = getSelectedModel()

        if (
            selected.isBlank() &&
            models.isNotEmpty()
        ) {
            selectModel(models.first().name)
        }
    }

    fun clearModels() {

        prefs.edit()
            .remove(KEY_MODELS)
            .remove(KEY_SELECTED_MODEL)
            .apply()
    }

    fun exportModels(): String {

        val array = JSONArray()

        getModels().forEach { model ->

            array.put(
                JSONObject().apply {
                    put("name", model.name)
                    put("provider", model.provider)
                }
            )
        }

        return JSONObject().apply {
            put(
                "selectedModel",
                getSelectedModel()
            )
            put(
                "models",
                array
            )
        }.toString()
    }

    fun importModels(
        json: String
    ): Boolean {

        return try {

            val root = JSONObject(json)

            val array = root.optJSONArray(
                "models"
            ) ?: JSONArray()

            val models = mutableListOf<ChintuModel>()

            for (index in 0 until array.length()) {

                val item = array.optJSONObject(index)
                    ?: continue

                val name = item.optString(
                    "name",
                    ""
                ).trim()

                val provider = item.optString(
                    "provider",
                    ProviderManager.PROVIDER_CUSTOM
                ).trim()

                if (name.isNotEmpty()) {

                    models.add(
                        ChintuModel(
                            name = name,
                            provider = provider
                        )
                    )
                }
            }

            saveModels(models)

            val selected = root.optString(
                "selectedModel",
                ""
            ).trim()

            if (selected.isNotEmpty()) {
                selectModel(selected)
            } else if (models.isNotEmpty()) {
                selectModel(models.first().name)
            }

            true

        } catch (_: Exception) {
            false
        }
    }

    private fun saveModels(
        models: List<ChintuModel>
    ) {

        val array = JSONArray()

        models.forEach { model ->

            array.put(
                JSONObject().apply {
                    put("name", model.name)
                    put("provider", model.provider)
                }
            )
        }

        prefs.edit()
            .putString(
                KEY_MODELS,
                array.toString()
            )
            .apply()
    }
}

data class ChintuModel(
    val name: String,
    val provider: String
)
