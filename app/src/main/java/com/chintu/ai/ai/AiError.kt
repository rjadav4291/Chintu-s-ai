package com.chintu.ai.ai

enum class AiErrorType {
    NETWORK,
    TIMEOUT,
    AUTHENTICATION,
    FORBIDDEN,
    NOT_FOUND,
    INSUFFICIENT_CREDITS,
    RATE_LIMIT,
    INVALID_REQUEST,
    SERVER,
    INVALID_RESPONSE,
    CONFIGURATION,
    OFFLINE_UNAVAILABLE,
    UNKNOWN
}

data class AiError(
    val type: AiErrorType,
    val message: String,
    val httpStatus: Int? = null,
    val providerMessage: String? = null
) {
    fun userMessage(): String {
        return when (type) {
            AiErrorType.NETWORK ->
                "Internet connection problem છે. Network check કરો."

            AiErrorType.TIMEOUT ->
                "AI server તરફથી response આવવામાં સમય લાગી રહ્યો છે. ફરી પ્રયાસ કરો."

            AiErrorType.AUTHENTICATION ->
                "API key ખોટી છે અથવા valid નથી."

            AiErrorType.FORBIDDEN ->
                "આ API request માટે permission નથી."

            AiErrorType.NOT_FOUND ->
                "Selected AI model અથવા server endpoint મળ્યો નથી."

            AiErrorType.INSUFFICIENT_CREDITS ->
                "AI providerમાં પૂરતા credits નથી. બીજો model/provider પસંદ કરો."

            AiErrorType.RATE_LIMIT ->
                "AI providerની rate limit આવી ગઈ છે. થોડા સમય પછી ફરી પ્રયાસ કરો."

            AiErrorType.INVALID_REQUEST ->
                "AI requestમાં configuration problem છે."

            AiErrorType.SERVER ->
                "AI providerનો server error આવ્યો છે."

            AiErrorType.INVALID_RESPONSE ->
                "AI provider તરફથી યોગ્ય response મળ્યો નથી."

            AiErrorType.CONFIGURATION ->
                "AI provider configuration અધૂરું છે."

            AiErrorType.OFFLINE_UNAVAILABLE ->
                "Offline AI model ઉપલબ્ધ નથી."

            AiErrorType.UNKNOWN ->
                message
        }
    }
}
