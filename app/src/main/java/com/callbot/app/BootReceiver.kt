package com.callbot.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("CallBot", "Device booted - CallBot is ready")
            // Bot auto-activates on boot if it was enabled before
            val prefs = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            val wasEnabled = prefs.getBoolean(MainActivity.KEY_BOT_ENABLED, false)
            if (wasEnabled) {
                Log.d("CallBot", "Bot was active - resuming bot mode")
            }
        }
    }
}
