package com.callbot.app

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "CallBotPrefs"
        const val KEY_BOT_ENABLED = "bot_enabled"
        const val KEY_BOT_NAME = "bot_name"
        const val KEY_OWNER_NAME = "owner_name"
        const val KEY_GREETING = "greeting"
        const val KEY_AWAY_MSG = "away_message"
        const val KEY_VOICE_SPEED = "voice_speed"
        const val KEY_VOICE_PITCH = "voice_pitch"
        const val KEY_LANGUAGE = "language"
        const val PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var prefs: SharedPreferences
    private lateinit var switchBotEnabled: Switch
    private lateinit var etBotName: EditText
    private lateinit var etOwnerName: EditText
    private lateinit var etGreeting: EditText
    private lateinit var etAwayMsg: EditText
    private lateinit var seekBarSpeed: SeekBar
    private lateinit var seekBarPitch: SeekBar
    private lateinit var spinnerLanguage: Spinner
    private lateinit var tvStatus: TextView
    private lateinit var btnSave: Button
    private lateinit var btnTestVoice: Button
    private lateinit var btnSetDefault: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        initViews()
        loadSettings()
        requestPermissions()
        checkDefaultDialer()
    }

    private fun initViews() {
        switchBotEnabled = findViewById(R.id.switch_bot_enabled)
        etBotName = findViewById(R.id.et_bot_name)
        etOwnerName = findViewById(R.id.et_owner_name)
        etGreeting = findViewById(R.id.et_greeting)
        etAwayMsg = findViewById(R.id.et_away_msg)
        seekBarSpeed = findViewById(R.id.seekbar_speed)
        seekBarPitch = findViewById(R.id.seekbar_pitch)
        spinnerLanguage = findViewById(R.id.spinner_language)
        tvStatus = findViewById(R.id.tv_status)
        btnSave = findViewById(R.id.btn_save)
        btnTestVoice = findViewById(R.id.btn_test_voice)
        btnSetDefault = findViewById(R.id.btn_set_default)

        // Setup language spinner with Indian languages
        val languages = arrayOf(
            "Hindi (हिन्दी)",
            "English (Indian)",
            "Tamil (தமிழ்)",
            "Telugu (తెలుగు)",
            "Bengali (বাংলা)",
            "Marathi (मराठी)",
            "Gujarati (ગુજરાતી)",
            "Kannada (ಕನ್ನಡ)"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        switchBotEnabled.setOnCheckedChangeListener { _, isChecked ->
            updateStatus(isChecked)
            prefs.edit().putBoolean(KEY_BOT_ENABLED, isChecked).apply()
        }

        btnSave.setOnClickListener { saveSettings() }

        btnTestVoice.setOnClickListener {
            val ttsManager = TTSManager(this)
            val lang = spinnerLanguage.selectedItemPosition
            val greeting = etGreeting.text.toString().ifEmpty { getDefaultGreeting() }
            ttsManager.speak(
                text = greeting,
                languageIndex = lang,
                speed = seekBarSpeed.progress / 50f + 0.5f,
                pitch = seekBarPitch.progress / 50f + 0.5f
            )
            Toast.makeText(this, "Testing voice...", Toast.LENGTH_SHORT).show()
        }

        btnSetDefault.setOnClickListener {
            val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
            if (telecomManager.defaultDialerPackage != packageName) {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Already set as default!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSettings() {
        switchBotEnabled.isChecked = prefs.getBoolean(KEY_BOT_ENABLED, false)
        etBotName.setText(prefs.getString(KEY_BOT_NAME, "Priya"))
        etOwnerName.setText(prefs.getString(KEY_OWNER_NAME, ""))
        etGreeting.setText(prefs.getString(KEY_GREETING, ""))
        etAwayMsg.setText(prefs.getString(KEY_AWAY_MSG, ""))
        seekBarSpeed.progress = prefs.getInt(KEY_VOICE_SPEED, 50)
        seekBarPitch.progress = prefs.getInt(KEY_VOICE_PITCH, 50)
        spinnerLanguage.setSelection(prefs.getInt(KEY_LANGUAGE, 0))
        updateStatus(switchBotEnabled.isChecked)
    }

    private fun saveSettings() {
        val ownerName = etOwnerName.text.toString().trim()
        if (ownerName.isEmpty()) {
            Toast.makeText(this, "Please enter your name!", Toast.LENGTH_SHORT).show()
            return
        }

        prefs.edit().apply {
            putString(KEY_BOT_NAME, etBotName.text.toString().ifEmpty { "Priya" })
            putString(KEY_OWNER_NAME, ownerName)
            putString(KEY_GREETING, etGreeting.text.toString())
            putString(KEY_AWAY_MSG, etAwayMsg.text.toString())
            putInt(KEY_VOICE_SPEED, seekBarSpeed.progress)
            putInt(KEY_VOICE_PITCH, seekBarPitch.progress)
            putInt(KEY_LANGUAGE, spinnerLanguage.selectedItemPosition)
            apply()
        }
        Toast.makeText(this, "✓ Settings saved successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun updateStatus(enabled: Boolean) {
        tvStatus.text = if (enabled) "🟢 Bot is ACTIVE — calls will be answered automatically"
        else "🔴 Bot is INACTIVE — calls ring normally"
        tvStatus.setBackgroundColor(
            if (enabled) 0xFF1B5E20.toInt() else 0xFF7F0000.toInt()
        )
    }

    private fun getDefaultGreeting(): String {
        val ownerName = etOwnerName.text.toString().ifEmpty { "the owner" }
        return "Namaste! I am Priya, the virtual assistant of $ownerName. They are unable to take your call right now. How can I help you?"
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.ANSWER_PHONE_CALLS
        )
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun checkDefaultDialer() {
        val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
        if (telecomManager.defaultDialerPackage != packageName) {
            tvStatus.text = "⚠️ Please set CallBot as Default Dialer for full call control"
        }
    }
}
