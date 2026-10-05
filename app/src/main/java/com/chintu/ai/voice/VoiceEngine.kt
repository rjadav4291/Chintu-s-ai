package com.chintu.ai.voice

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceEngine(
    private val context: Context
) {

    private var tts:
        TextToSpeech? = null

    fun speak(
        text: String,
        language: String = "en"
    ) {

        val loc =
            when (
                language.lowercase()
            ) {

                "gujarati",
                "gu" ->
                    Locale(
                        "gu",
                        "IN"
                    )

                "hindi",
                "hi" ->
                    Locale(
                        "hi",
                        "IN"
                    )

                else ->
                    Locale.ENGLISH
            }

        tts =
            tts
                ?: TextToSpeech(
                    context
                ) {

                    tts?.language = loc

                    tts?.speak(
                        text,
                        TextToSpeech
                            .QUEUE_FLUSH,
                        null,
                        "chintu"
                    )
                }

        tts?.language = loc

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "chintu"
        )
    }

    fun listen(
        onText: (String) -> Unit,
        onError: (String) -> Unit
    ) {

        if (
            !SpeechRecognizer
                .isRecognitionAvailable(
                    context
                )
        ) {

            onError(
                "Speech recognition unavailable"
            )

            return
        }

        val r =
            SpeechRecognizer
                .createSpeechRecognizer(
                    context
                )

        r.setRecognitionListener(
            object :
                android.speech.RecognitionListener {

                override fun
                    onReadyForSpeech(
                        p0: android.os.Bundle?
                    ) {
                }

                override fun
                    onBeginningOfSpeech() {
                }

                override fun
                    onRmsChanged(
                        p0: Float
                    ) {
                }

                override fun
                    onBufferReceived(
                        p0: ByteArray?
                    ) {
                }

                override fun
                    onEndOfSpeech() {
                }

                override fun
                    onError(
                        e: Int
                    ) {

                    onError(
                        "Speech error: $e"
                    )

                    r.destroy()
                }

                override fun
                    onResults(
                        b: android.os.Bundle
                    ) {

                    onText(
                        b.getStringArrayList(
                            SpeechRecognizer
                                .RESULTS_RECOGNITION
                        )
                            ?.firstOrNull()
                            .orEmpty()
                    )

                    r.destroy()
                }

                override fun
                    onPartialResults(
                        p0: android.os.Bundle?
                    ) {
                }

                override fun
                    onEvent(
                        p0: Int,
                        p1: android.os.Bundle?
                    ) {
                }
            }
        )

        r.startListening(

            Intent(
                RecognizerIntent
                    .ACTION_RECOGNIZE_SPEECH
            ).apply {

                putExtra(
                    RecognizerIntent
                        .EXTRA_LANGUAGE_MODEL,

                    RecognizerIntent
                        .LANGUAGE_MODEL_FREE_FORM
                )

                putExtra(
                    RecognizerIntent
                        .EXTRA_LANGUAGE,

                    "en-IN"
                )
            }
        )
    }
}
