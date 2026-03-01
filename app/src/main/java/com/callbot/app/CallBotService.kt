package com.callbot.app

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CallBotService : Service() {

    companion object {
        const val CHANNEL_ID = "CallBotChannel"
        const val NOTIF_ID = 1001
        const val TAG = "CallBotService"

        // Conversation states
        const val STATE_GREETING = 0
        const val STATE_LISTENING = 1
        const val STATE_PROCESSING = 2
        const val STATE_RESPONDING = 3
        const val STATE_FAREWELL = 4
    }

    private lateinit var ttsManager: TTSManager
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var conversationEngine: ConversationEngine
    private lateinit var prefs: android.content.SharedPreferences

    private var callerNumber = "Unknown"
    private var conversationState = STATE_GREETING
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val callLog = StringBuilder()

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        createNotificationChannel()
        ttsManager = TTSManager(this)
        conversationEngine = ConversationEngine(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        callerNumber = intent?.getStringExtra("caller_number") ?: "Unknown"

        startForeground(NOTIF_ID, buildNotification("Answering call from $callerNumber"))
        Log.d(TAG, "Bot handling call from $callerNumber")

        // Wait for call to connect, then start greeting
        executor.schedule({
            answerCall()
            Handler(Looper.getMainLooper()).postDelayed({
                startConversation()
            }, 1500)
        }, 1, TimeUnit.SECONDS)

        return START_NOT_STICKY
    }

    private fun answerCall() {
        try {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            telecomManager.acceptRingingCall()
            // Route audio to speaker for processing
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_IN_CALL
            audioManager.isSpeakerphoneOn = true
        } catch (e: Exception) {
            Log.e(TAG, "Error answering call: ${e.message}")
        }
    }

    private fun startConversation() {
        conversationState = STATE_GREETING
        val greeting = buildGreeting()
        callLog.appendLine("BOT: $greeting")

        ttsManager.speak(
            text = greeting,
            languageIndex = prefs.getInt(MainActivity.KEY_LANGUAGE, 0),
            speed = prefs.getInt(MainActivity.KEY_VOICE_SPEED, 50) / 50f + 0.5f,
            pitch = prefs.getInt(MainActivity.KEY_VOICE_PITCH, 50) / 50f + 0.5f,
            onComplete = {
                conversationState = STATE_LISTENING
                startListening()
            }
        )
    }

    private fun buildGreeting(): String {
        val ownerName = prefs.getString(MainActivity.KEY_OWNER_NAME, "the owner") ?: "the owner"
        val botName = prefs.getString(MainActivity.KEY_BOT_NAME, "Priya") ?: "Priya"
        val customGreeting = prefs.getString(MainActivity.KEY_GREETING, "") ?: ""

        return if (customGreeting.isNotEmpty()) {
            customGreeting.replace("{name}", ownerName).replace("{bot}", botName)
        } else {
            val langIndex = prefs.getInt(MainActivity.KEY_LANGUAGE, 0)
            when (langIndex) {
                0 -> "Namaste! Main $botName hoon, $ownerName ki virtual assistant. Abhi woh available nahi hain. Aap kaise help kar sakti hoon aapki?"
                1 -> "Hello! I'm $botName, $ownerName's virtual assistant. They're unavailable right now. How may I help you?"
                2 -> "Vanakkam! Naan $botName, $ownerName oda virtual assistant. Ippo avanga kedaikala. Ungalukku epdi udavi seiyalam?"
                3 -> "Namaskaram! Nenu $botName, $ownerName gari virtual assistant. Ipudu vaaru andubatu leeru. Meeru ela sahayapadali?"
                4 -> "Namaskar! Ami $botName, $ownerName-er virtual assistant. Ekhon tara pawa jacche na. Apnake kemon sahajya korte pari?"
                5 -> "Namaskar! Mi $botName ahe, $ownerName chi virtual assistant. Te ata upalabdha nahi. Tumhala kashi madad karu?"
                6 -> "Kem Cho! Hu $botName chhu, $ownerName ni virtual assistant. Te haju uplabdh nathi. Hu tamari kevi rite madad kari shakun?"
                7 -> "Namaskara! Naanu $botName, $ownerName avara virtual assistant. Avaru eeaga sigalla. Neevu hege sahaya maadabahudhu?"
                else -> "Hello! I'm $botName, $ownerName's assistant. How can I help you?"
            }
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.w(TAG, "Speech recognition not available")
            endCallWithMessage()
            return
        }

        Handler(Looper.getMainLooper()).post {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val userSpeech = matches?.firstOrNull() ?: ""
                    Log.d(TAG, "Caller said: $userSpeech")
                    callLog.appendLine("CALLER: $userSpeech")

                    if (userSpeech.isNotEmpty()) {
                        processCallerInput(userSpeech)
                    } else {
                        // Ask again if nothing heard
                        speakAndListen("Kya aapne kuch kaha? Kripya dobara boliye. \nDid you say something? Please speak again.")
                    }
                }

                override fun onError(error: Int) {
                    Log.w(TAG, "STT error: $error")
                    when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> speakAndListen(
                            "Mujhe aapki awaaz nahi sunai di. Kripya dobara boliye."
                        )
                        else -> endCallWithMessage()
                    }
                }

                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, getRecognitionLanguage())
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, getRecognitionLanguage())
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
            }
            speechRecognizer.startListening(intent)
        }
    }

    private fun getRecognitionLanguage(): String {
        return when (prefs.getInt(MainActivity.KEY_LANGUAGE, 0)) {
            0 -> "hi-IN"
            1 -> "en-IN"
            2 -> "ta-IN"
            3 -> "te-IN"
            4 -> "bn-IN"
            5 -> "mr-IN"
            6 -> "gu-IN"
            7 -> "kn-IN"
            else -> "hi-IN"
        }
    }

    private fun processCallerInput(input: String) {
        conversationState = STATE_PROCESSING
        val response = conversationEngine.generateResponse(input, callerNumber)
        callLog.appendLine("BOT: $response.text")

        if (response.shouldEndCall) {
            ttsManager.speak(
                text = response.text,
                languageIndex = prefs.getInt(MainActivity.KEY_LANGUAGE, 0),
                speed = prefs.getInt(MainActivity.KEY_VOICE_SPEED, 50) / 50f + 0.5f,
                pitch = prefs.getInt(MainActivity.KEY_VOICE_PITCH, 50) / 50f + 0.5f,
                onComplete = { endCall() }
            )
        } else {
            speakAndListen(response.text)
        }

        // Save message if requested
        if (response.savedMessage.isNotEmpty()) {
            saveMessage(callerNumber, response.savedMessage)
        }
    }

    private fun speakAndListen(text: String) {
        ttsManager.speak(
            text = text,
            languageIndex = prefs.getInt(MainActivity.KEY_LANGUAGE, 0),
            speed = prefs.getInt(MainActivity.KEY_VOICE_SPEED, 50) / 50f + 0.5f,
            pitch = prefs.getInt(MainActivity.KEY_VOICE_PITCH, 50) / 50f + 0.5f,
            onComplete = {
                conversationState = STATE_LISTENING
                startListening()
            }
        )
    }

    private fun endCallWithMessage() {
        val farewell = prefs.getString(MainActivity.KEY_AWAY_MSG, "") ?: ""
        val finalMsg = farewell.ifEmpty {
            "Theek hai. Main ${ prefs.getString(MainActivity.KEY_OWNER_NAME, "them") } ko bataunga ki aapne call kiya. Dhanyawad. Goodbye!"
        }
        ttsManager.speak(
            text = finalMsg,
            languageIndex = prefs.getInt(MainActivity.KEY_LANGUAGE, 0),
            speed = prefs.getInt(MainActivity.KEY_VOICE_SPEED, 50) / 50f + 0.5f,
            pitch = prefs.getInt(MainActivity.KEY_VOICE_PITCH, 50) / 50f + 0.5f,
            onComplete = { endCall() }
        )
    }

    private fun endCall() {
        try {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            telecomManager.endCall()
        } catch (e: Exception) {
            Log.e(TAG, "Error ending call: ${e.message}")
        }
        saveCallLog()
        stopSelf()
    }

    private fun saveMessage(caller: String, message: String) {
        val msgs = prefs.getStringSet("saved_messages", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val timestamp = System.currentTimeMillis()
        msgs.add("$timestamp|$caller|$message")
        prefs.edit().putStringSet("saved_messages", msgs).apply()
        Log.d(TAG, "Saved message from $caller: $message")
    }

    private fun saveCallLog() {
        val logs = prefs.getStringSet("call_logs", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        logs.add("${System.currentTimeMillis()}|$callerNumber|${callLog}")
        prefs.edit().putStringSet("call_logs", logs).apply()
    }

    private fun buildNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🤖 CallBot Active")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.sym_call_incoming)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "CallBot Service",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Call Bot active notification"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        super.onDestroy()
        if (::speechRecognizer.isInitialized) speechRecognizer.destroy()
        ttsManager.shutdown()
        executor.shutdown()
    }
}
