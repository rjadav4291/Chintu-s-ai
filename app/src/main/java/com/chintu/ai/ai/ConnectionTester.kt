package com.chintu.ai.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class ConnectionResult(
    val success: Boolean,
    val message: String,
    val statusCode: Int? = null
)

class ConnectionTester {

    suspend fun test(
        serverUrl: String,
        apiKey: String = ""
    ): ConnectionResult = withContext(Dispatchers.IO) {

        val cleanUrl = serverUrl
            .trim()
            .removeSuffix("/")

        if (cleanUrl.isBlank()) {
            return@withContext ConnectionResult(
                success = false,
                message = "Server URL is empty."
            )
        }

        val testUrl = when {
            cleanUrl.contains("openrouter.ai") ->
                "$cleanUrl/models"

            cleanUrl.contains("127.0.0.1:11434") ->
                "$cleanUrl/api/tags"

            cleanUrl.contains("localhost:11434") ->
                "$cleanUrl/api/tags"

            cleanUrl.endsWith("/models") ->
                cleanUrl

            cleanUrl.endsWith("/api/tags") ->
                cleanUrl

            else ->
                cleanUrl
        }

        try {
            val connection =
                URL(testUrl).openConnection() as HttpURLConnection

            connection.requestMethod = "GET"

            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            connection.instanceFollowRedirects = true

            if (
