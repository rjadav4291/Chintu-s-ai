package com.chintu.ai.ai

class AiProviderException(
    val aiError: AiError
) : Exception(aiError.message)
