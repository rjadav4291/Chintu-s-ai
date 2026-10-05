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
        serverUrl: String
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

        try {

            val url = URL(cleanUrl)

            val connection =
                url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = 7000
            connection.readTimeout = 7000
            connection.instanceFollowRedirects = true

            val status = connection.responseCode

            connection.disconnect()

            if (status in 200..399) {

                ConnectionResult(
                    success = true,
                    message = "Server is reachable.",
                    statusCode = status
                )

            } else {

                ConnectionResult(
                    success = false,
                    message = "Server returned HTTP $status.",
                    statusCode = status
                )
            }

        } catch (exception: Exception) {

            ConnectionResult(
                success = false,
                message = exception.message
                    ?: "Unable to connect to server."
            )
        }
    }
}
