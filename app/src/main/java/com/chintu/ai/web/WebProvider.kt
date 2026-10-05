package com.chintu.ai.web

import java.net.HttpURLConnection
import java.net.URL

data class WebSource(
    val title: String,
    val url: String,
    val snippet: String = ""
)

interface WebProvider {

    suspend fun search(
        query: String
    ): List<WebSource>

    suspend fun open(
        url: String
    ): String
}

class BasicWebProvider :
    WebProvider {

    override suspend fun search(
        query: String
    ): List<WebSource> {

        /*
         * Search requires a configured
         * search API.
         *
         * Never fake search results.
         */

        return emptyList()
    }

    override suspend fun open(
        url: String
    ): String {

        val c =
            URL(url)
                .openConnection()
                    as HttpURLConnection

        c.connectTimeout = 15000

        c.readTimeout = 30000

        return c.inputStream
            .bufferedReader()
            .readText()
            .take(200_000)
    }
    }
