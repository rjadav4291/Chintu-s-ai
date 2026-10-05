package com.chintu.ai.vision

import android.net.Uri

data class VisionResult(
    val text: String,
    val verified: Boolean
)

interface VisionProvider {

    suspend fun analyze(
        uri: Uri,
        prompt: String
    ): VisionResult
}

class UnconfiguredVisionProvider :
    VisionProvider {

    override suspend fun analyze(
        uri: Uri,
        prompt: String
    ) =
        VisionResult(
            "Vision AI is not configured yet.",
            false
        )
    }
