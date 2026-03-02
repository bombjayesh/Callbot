package com.callbot.app

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TTSManager(private val context: Context) {

    companion object {
        val INDIAN_LOCALES = arrayOf(
            Locale("hi", "IN"),
            Locale("en", "IN"),
            Locale("ta", "IN"),
            Locale("te", "IN"),
            Locale("bn", "IN"),
            Locale("mr", "IN"),
            Locale("gu", "IN"),
            Locale("kn", "IN"),
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
                pendingQueue.forEach { action: () -> Unit -> action() }
                pendingQueue.clear()
            } else {
                Log.e("TTSManager", "TTS initialization failed")
            }
        }
    }

    fun speak(
        text: String,
        languageIndex: Int = 0,
        speed: Float = 1.0f,
        pitch: Float = 1.0f,
        onComplete: (() -> Unit)? = null
    ) {
        val speakAction: () -> Unit = {
            val locale = INDIAN_LOCALES.getOrElse(languageIndex) { Locale("en", "IN") }

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
