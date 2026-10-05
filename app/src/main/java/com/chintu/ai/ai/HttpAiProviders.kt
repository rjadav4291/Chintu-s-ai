package com.chintu.ai.ai

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class SecureConfig(
    context: Context
) {

    private val prefs =
        EncryptedSharedPreferences.create(
            "chintu_secrets",
            MasterKeys.getOrCreate(
                MasterKeys.AES256_GCM_SPEC
            ),
            context,
            EncryptedSharedPreferences
                .PrefKeyEncryptionScheme
                .AES256_SIV,
            EncryptedSharedPreferences
                .PrefValueEncryptionScheme
                .AES256_GCM
        )

    fun get(
        key: String
    ): String =
        prefs.getString(
            key,
            ""
        ) ?: ""

    fun set(
        key: String,
        value: String
    ) {

        prefs.edit()
            .putString(
                key,
                value
            )
            .apply()
    }

    fun clear(
        key: String
    ) {

        prefs.edit()
            .remove(key)
            .apply()
    }
}

private fun postJson(
    endpoint: String,
    headers: Map<String, String>,
    body: JSONObject
): JSONObject {

    val c =
        URL(endpoint)
            .openConnection() as HttpURLConnection

    c.requestMethod = "POST"

    c.connectTimeout =
        TimeUnit.SECONDS
            .toMillis(15)
            .toInt()

    c.readTimeout =
        TimeUnit.SECONDS
            .toMillis(90)
            .toInt()

    c.doOutput = true

    c.setRequestProperty(
        "Content-Type",
        "application/json"
    )

    headers.forEach {
        (k, v) ->
        c.setRequestProperty(
            k,
            v
        )
    }

    c.outputStream.use {
        it.write(
            body.toString()
                .toByteArray()
        )
    }

    val stream =
        if (c.responseCode in 200..299)
            c.inputStream
        else
            c.errorStream

    val text =
        stream
            ?.bufferedReader()
            ?.readText()
            ?: ""

    if (c.responseCode !in 200..299) {
        error(
            "HTTP ${c.responseCode}: $text"
        )
    }

    return JSONObject(text)
}

class OpenAiCompatibleProvider(
    private val cfg: SecureConfig,
    override val id: String,
    override val displayName: String,
    private val defaultEndpoint: String
) : AiProvider {

    override suspend fun isAvailable() =
        cfg.get("${id}_api_key").isNotBlank()
            || id == "ollama"

    override suspend fun generate(
        request: AiRequest
    ): AiResponse {

        val endpoint =
            cfg.get("${id}_endpoint")
                .ifBlank {
                    defaultEndpoint
                }

        val key =
            cfg.get(
                "${id}_api_key"
            )

        val msgs =
            JSONArray().apply {

                put(
                    JSONObject()
                        .put(
                            "role",
                            "system"
                        )
                        .put(
                            "content",
                            request.systemPrompt
                        )
                )

                request.messages.forEach {

                    put(
                        JSONObject()
                            .put(
                                "role",
                                it.role
                            )
                            .put(
                                "content",
                                it.content
                            )
                    )
                }
            }

        val body =
            JSONObject()
                .put(
                    "model",
                    request.model
                )
                .put(
                    "messages",
                    msgs
                )

        val headers =
            if (key.isBlank())
                emptyMap()
            else
                mapOf(
                    "Authorization" to
                        "Bearer $key"
                )

        val json =
            postJson(
                endpoint,
                headers,
                body
            )

        val text =
            json.optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")

                ?: json
                    .optJSONObject("message")
                    ?.optString("content")
                    ?.takeIf {
                        it.isNotBlank()
                    }

                ?: json
                    .optString("response")
                    .takeIf {
                        it.isNotBlank()
                    }

                ?: error(
                    "Provider returned no assistant text"
                )

        return AiResponse(
            text,
            displayName,
            request.model
        )
    }
}

class AnthropicProvider(
    private val cfg: SecureConfig
) : AiProvider {

    override val id =
        "anthropic"

    override val displayName =
        "Anthropic"

    override suspend fun isAvailable() =
        cfg.get(
            "anthropic_api_key"
        ).isNotBlank()

    override suspend fun generate(
        request: AiRequest
    ): AiResponse {

        val msgs =
            JSONArray()

        request.messages
            .filter {
                it.role != "system"
            }
            .forEach {

                msgs.put(
                    JSONObject()
                        .put(
                            "role",
                            it.role
                        )
                        .put(
                            "content",
                            it.content
                        )
                )
            }

        val body =
            JSONObject()
                .put(
                    "model",
                    request.model
                )
                .put(
                    "max_tokens",
                    2048
                )
                .put(
                    "system",
                    request.systemPrompt
                )
                .put(
                    "messages",
                    msgs
                )

        val json =
            postJson(
                cfg.get(
                    "anthropic_endpoint"
                ).ifBlank {
                    "https://api.anthropic.com/v1/messages"
                },
                mapOf(
                    "x-api-key" to
                        cfg.get(
                            "anthropic_api_key"
                        ),

                    "anthropic-version" to
                        "2023-06-01"
                ),
                body
            )

        val text =
            json
                .optJSONArray("content")
                ?.optJSONObject(0)
                ?.optString("text")
                ?: error(
                    "Provider returned no text"
                )

        return AiResponse(
            text,
            displayName,
            request.model
        )
    }
}

class GeminiProvider(
    private val cfg: SecureConfig
) : AiProvider {

    override val id =
        "gemini"

    override val displayName =
        "Gemini"

    override suspend fun isAvailable() =
        cfg.get(
            "gemini_api_key"
        ).isNotBlank()

    override suspend fun generate(
        request: AiRequest
    ): AiResponse {

        val contents =
            JSONArray()

        request.messages.forEach {

            contents.put(

                JSONObject()
                    .put(
                        "role",
                        if (
                            it.role ==
                            "assistant"
                        )
                            "model"
                        else
                            "user"
                    )
                    .put(
                        "parts",
                        JSONArray()
                            .put(
                                JSONObject()
                                    .put(
                                        "text",
                                        it.content
                                    )
                            )
                    )
            )
        }

        val body =
            JSONObject()
                .put(
                    "systemInstruction",
                    JSONObject()
                        .put(
                            "parts",
                            JSONArray()
                                .put(
                                    JSONObject()
                                        .put(
                                            "text",
                                            request.systemPrompt
                                        )
                                )
                        )
                )
                .put(
                    "contents",
                    contents
                )

        val endpoint =
            cfg.get(
                "gemini_endpoint"
            ).ifBlank {

                "https://generativelanguage.googleapis.com/" +
                    "v1beta/models/${request.model}:generateContent"
            }

        val url =
            "$endpoint?key=" +
                java.net.URLEncoder.encode(
                    cfg.get(
                        "gemini_api_key"
                    ),
                    "UTF-8"
                )

        val json =
            postJson(
                url,
                emptyMap(),
                body
            )

        val text =
            json
                .optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")
                ?: error(
                    "Provider returned no text"
                )

        return AiResponse(
            text,
            displayName,
            request.model
        )
    }
}
