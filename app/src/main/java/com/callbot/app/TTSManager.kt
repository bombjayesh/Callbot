package com.callbot.app

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TTSManager(private val context: Context) {

    companion object {
        // Indian locale codes
        val INDIAN_LOCALES = arrayOf(
            Locale("hi", "IN"),   // Hindi
            Locale("en", "IN"),   // English (India)
            Locale("ta", "IN"),   // Tamil
            Locale("te", "IN"),   // Telugu
            Locale("bn", "IN"),   // Bengali
            Locale("mr", "IN"),   // Marathi
            Locale("gu", "IN"),   // Gujarati
            Locale("kn", "IN"),   // Kannada
        )
    }

    private var tts: TextToSpeech? = null
    private var isReady = false
    private val pendingQueue = mutableListOf<() -> Unit>()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isReady = true
                Log.d("TTSManager", "TTS initialized successfully")
                // Execute any pending speech
                pendingQueue.forEach { it.invoke() }
                pendingQueue.clear()
            } else {
                Log.e("TTSManager", "TTS initialization failed")
            }
        }
    }

    fun speak(
        text: String,
        languageIndex: Int = 1,
        speed: Float = 1.0f,
        pitch: Float = 1.0f,
        onComplete: (() -> Unit)? = null
    ) {
        val speakAction = {
            val locale = INDIAN_LOCALES.getOrElse(languageIndex) { Locale("en", "IN") }

            // Try to set the locale, fall back to en-IN
            val result = tts?.setLanguage(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("TTSManager", "Language not supported: $locale, falling back to en-IN")
                tts?.setLanguage(Locale("en", "IN"))
            }

            tts?.setSpeechRate(speed.coerceIn(0.5f, 2.0f))
            tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))

            if (onComplete != null) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == "callbot_speech") {
                            onComplete.invoke()
                        }
                    }
                    override fun onError(utteranceId: String?) {
                        Log.e("TTSManager", "TTS error for utterance: $utteranceId")
                        onComplete.invoke()
                    }
                })
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "callbot_speech")
            } else {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
            Log.d("TTSManager", "Speaking [$locale]: $text")
        }

        if (isReady) {
            speakAction()
        } else {
            pendingQueue.add(speakAction)
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
