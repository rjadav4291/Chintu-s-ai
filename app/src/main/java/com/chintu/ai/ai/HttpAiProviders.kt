package com.chintu.ai.ai

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class SecureConfig(context: Context) {

    private val masterKey =
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    private val prefs =
        EncryptedSharedPreferences.create(
            context,
            "chintu_secure_config",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    fun get(key: String): String {
        return prefs.getString(key, "") ?: ""
    }

    fun set(key: String, value: String) {
        prefs.edit()
            .putString(key, value)
            .apply()
    }

    fun clear(key: String) {
        prefs.edit()
            .remove(key)
            .apply()
    }
}

private suspend fun postJson(
    endpoint: String,
    headers: Map<String, String>,
    body: JSONObject
): JSONObject = withContext(Dispatchers.IO) {

    val connection =
        URL(endpoint).openConnection() as HttpURLConnection

    try {

        connection.requestMethod = "POST"
        connection.connectTimeout =
            TimeUnit.SECONDS.toMillis(15).toInt()

        connection.readTimeout =
            TimeUnit.SECONDS.toMillis(90).toInt()

        connection.doOutput = true
        connection.useCaches = false

        connection.setRequestProperty(
            "Content-Type",
            "application/json"
        )

        connection.setRequestProperty(
            "Accept",
            "application/json"
        )

        headers.forEach { (key, value) ->
            connection.setRequestProperty(key, value)
        }

        val requestBytes =
            body.toString().toByteArray(Charsets.UTF_8)

        connection.outputStream.use { output ->
            output.write(requestBytes)
            output.flush()
        }

        val status =
            try {
                connection.responseCode
            } catch (error: SocketTimeoutException) {
                throw AiNetworkException(
                    AiError(
                        AiErrorType.TIMEOUT,
                        "Provider request timed out."
                    )
                )
            }

        val stream =
            if (status in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

        val responseText =
            stream?.bufferedReader()?.use {
                it.readText()
            } ?: ""

        if (status !in 200..299) {

            val providerMessage =
                try {
                    val json = JSONObject(responseText)

                    json.optJSONObject("error")
                        ?.optString("message")
                        ?.takeIf { it.isNotBlank() }
                        ?: json.optString("message")
                            .takeIf { it.isNotBlank() }
                } catch (_: Exception) {
                    null
                }

            val errorType =
                when (status) {
                    400 -> AiErrorType.INVALID_REQUEST
                    401 -> AiErrorType.AUTHENTICATION
                    403 -> AiErrorType.FORBIDDEN
                    404 -> AiErrorType.NOT_FOUND
                    402 -> AiErrorType.INSUFFICIENT_CREDITS
                    429 -> AiErrorType.RATE_LIMIT
                    in 500..599 -> AiErrorType.SERVER
                    else -> AiErrorType.UNKNOWN
                }

            throw AiNetworkException(
                AiError(
                    type = errorType,
                    message =
                        providerMessage
                            ?: "HTTP $status returned by provider.",
                    httpStatus = status,
                    providerMessage = providerMessage
                )
            )
        }

        if (responseText.isBlank()) {
            throw AiNetworkException(
                AiError(
                    AiErrorType.INVALID_RESPONSE,
                    "Provider returned an empty response."
                )
            )
        }

        try {
            JSONObject(responseText)
        } catch (_: Exception) {
            throw AiNetworkException(
                AiError(
                    AiErrorType.INVALID_RESPONSE,
                    "Provider returned invalid JSON."
                )
            )
        }

    } finally {
        connection.disconnect()
    }
}

private class AiNetworkException(
    val aiError: AiError
) : Exception(aiError.message)

class OpenAiCompatibleProvider(
    private val cfg: SecureConfig,
    override val id: String,
    override val displayName: String,
    private val defaultEndpoint: String
) : AiProvider {

    override suspend fun isAvailable(): Boolean {
        return cfg.get("${id}_api_key").isNotBlank() ||
                id == ProviderManager.PROVIDER_OLLAMA
    }

    override suspend fun generate(
        request: AiRequest
    ): AiResponse {

        val endpoint =
            cfg.get("${id}_endpoint")
                .ifBlank { defaultEndpoint }

        val apiKey =
            cfg.get("${id}_api_key")

        val messages =
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

                request.messages.forEach { message ->

                    put(
                        JSONObject()
                            .put(
                                "role",
                                message.role
                            )
                            .put(
                                "content",
                                message.content
                            )
                    )
                }
            }

        val maxTokens =
            TokenBudget.clamp(
                request.maxOutputTokens
            )

        val body =
            JSONObject()
                .put(
                    "model",
                    request.model
                )
                .put(
                    "messages",
                    messages
                )
                .put(
                    "max_tokens",
                    maxTokens
                )
                .put(
                    "temperature",
                    request.temperature
                )

        val headers =
            if (apiKey.isBlank()) {
                emptyMap()
            } else {
                mapOf(
                    "Authorization" to
                            "Bearer $apiKey"
                )
            }

        return try {

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
                    ?.takeIf { it.isNotBlank() }

                    ?: json.optJSONObject("message")
                        ?.optString("content")
                        ?.takeIf { it.isNotBlank() }

                    ?: json.optString("response")
                        .takeIf { it.isNotBlank() }

                    ?: throw AiNetworkException(
                        AiError(
                            AiErrorType.INVALID_RESPONSE,
                            "Provider returned no assistant text."
                        )
                    )

            AiResponse(
                text = text,
                provider = displayName,
                model = request.model,
                verified = true
            )

        } catch (error: AiNetworkException) {

            throw error

        } catch (error: SocketTimeoutException) {

            throw AiNetworkException(
                AiError(
                    AiErrorType.TIMEOUT,
                    "Provider request timed out."
                )
            )

        } catch (error: Exception) {

            throw AiNetworkException(
                AiError(
                    AiErrorType.NETWORK,
                    error.message
                        ?: "Unable to connect to provider."
                )
            )
        }
    }
}

class AnthropicProvider(
    private val cfg: SecureConfig
) : AiProvider {

    override val id = "anthropic"

    override val displayName = "Anthropic"

    override suspend fun isAvailable(): Boolean {
        return cfg.get("anthropic_api_key")
            .isNotBlank()
    }

    override suspend fun generate(
        request: AiRequest
    ): AiResponse {

        val messages =
            JSONArray().apply {

                request.messages
                    .filter {
                        it.role != "system"
                    }
                    .forEach { message ->

                        put(
                            JSONObject()
                                .put(
                                    "role",
                                    message.role
                                )
                                .put(
                                    "content",
                                    message.content
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
                    "max_tokens",
                    TokenBudget.clamp(
                        request.maxOutputTokens
                    )
                )
                .put(
                    "system",
                    request.systemPrompt
                )
                .put(
                    "messages",
                    messages
                )

        val endpoint =
            cfg.get("anthropic_endpoint")
                .ifBlank {
                    "https://api.anthropic.com/v1/messages"
                }

        val apiKey =
            cfg.get("anthropic_api_key")

        val headers =
            mapOf(
                "x-api-key" to apiKey,
                "anthropic-version" to
                        "2023-06-01"
            )

        return try {

            val json =
                postJson(
                    endpoint,
                    headers,
                    body
                )

            val text =
                json.optJSONArray("content")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    ?.takeIf { it.isNotBlank() }
                    ?: throw AiNetworkException(
                        AiError(
                            AiErrorType.INVALID_RESPONSE,
                            "Anthropic returned no text."
                        )
                    )

            AiResponse(
                text = text,
                provider = displayName,
                model = request.model,
                verified = true
            )

        } catch (error: AiNetworkException) {
            throw error
        } catch (error: Exception) {
            throw AiNetworkException(
                AiError(
                    AiErrorType.NETWORK,
                    error.message
                        ?: "Anthropic connection failed."
                )
            )
        }
    }
}

class GeminiProvider(
    private val cfg: SecureConfig
) : AiProvider {

    override val id = "gemini"

    override val displayName = "Gemini"

    override suspend fun isAvailable(): Boolean {
        return cfg.get("gemini_api_key")
            .isNotBlank()
    }

    override suspend fun generate(
        request: AiRequest
    ): AiResponse {

        val contents =
            JSONArray().apply {

                request.messages.forEach { message ->

                    put(
                        JSONObject()
                            .put(
                                "role",
                                if (
                                    message.role ==
                                    "assistant"
                                ) {
                                    "model"
                                } else {
                                    "user"
                                }
                            )
                            .put(
                                "parts",
                                JSONArray().put(
                                    JSONObject()
                                        .put(
                                            "text",
                                            message.content
                                        )
                                )
                            )
                    )
                }
            }

        val body =
            JSONObject()
                .put(
                    "systemInstruction",
                    JSONObject()
                        .put(
                            "parts",
                            JSONArray().put(
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
                .put(
                    "generationConfig",
                    JSONObject()
                        .put(
                            "maxOutputTokens",
                            TokenBudget.clamp(
                                request.maxOutputTokens
                            )
                        )
                        .put(
                            "temperature",
                            request.temperature
                        )
                )

        val baseEndpoint =
            cfg.get("gemini_endpoint")
                .ifBlank {
                    "https://generativelanguage.googleapis.com"
                }

        val cleanEndpoint =
            baseEndpoint.removeSuffix("/")

        val endpoint =
            if (
                cleanEndpoint.contains(
                    "/generateContent"
                )
            ) {
                cleanEndpoint
            } else {
                "$cleanEndpoint/v1beta/models/${request.model}:generateContent"
            }

        val apiKey =
            cfg.get("gemini_api_key")

        val url =
            "$endpoint?key=${
                URLEncoder.encode(
                    apiKey,
                    "UTF-8"
                )
            }"

        return try {

            val json =
                postJson(
                    url,
                    emptyMap(),
                    body
                )

            val text =
                json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    ?.takeIf { it.isNotBlank() }
                    ?: throw AiNetworkException(
                        AiError(
                            AiErrorType.INVALID_RESPONSE,
                            "Gemini returned no text."
                        )
                    )

            AiResponse(
                text = text,
                provider = displayName,
                model = request.model,
                verified = true
            )

        } catch (error: AiNetworkException) {
            throw error
        } catch (error: Exception) {
            throw AiNetworkException(
                AiError(
                    AiErrorType.NETWORK,
                    error.message
                        ?: "Gemini connection failed."
                )
            )
        }
    }
}
