package com.chintu.ai.files

import android.net.Uri

data class FileAnswer(
    val text: String,
    val verified: Boolean
)

interface FileIntelligence {

    suspend fun summarize(
        uri: Uri
    ): FileAnswer

    suspend fun search(
        uri: Uri,
        query: String
    ): FileAnswer
}

class SafeFileIntelligence :
    FileIntelligence {

    override suspend fun summarize(
        uri: Uri
    ) =
        FileAnswer(
            "This file type is not configured for parsing yet. Please connect a supported document parser/provider.",
            false
        )

    override suspend fun search(
        uri: Uri,
        query: String
    ) =
        FileAnswer(
            "File search is not configured for this file/provider yet.",
            false
        )
    }
