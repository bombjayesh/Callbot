package com.callbot.app

import android.content.Intent
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

class CallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val prefs = applicationContext.getSharedPreferences(
            MainActivity.PREFS_NAME, MODE_PRIVATE
        )
        val botEnabled = prefs.getBoolean(MainActivity.KEY_BOT_ENABLED, false)
        val callerNumber = callDetails.handle?.schemeSpecificPart ?: "Unknown"

        Log.d("CallBot", "Incoming call from: $callerNumber | Bot enabled: $botEnabled")

        if (botEnabled) {
            // Allow the call to go through (we'll handle it in CallBotService)
            val response = CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSilenceCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
            respondToCall(callDetails, response)

            // Launch bot service to handle the call
            val serviceIntent = Intent(applicationContext, CallBotService::class.java).apply {
                putExtra("caller_number", callerNumber)
                putExtra("call_direction", callDetails.callDirection)
            }
            applicationContext.startForegroundService(serviceIntent)

        } else {
            // Bot disabled, let call ring normally
            val response = CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSilenceCall(false)
                .build()
            respondToCall(callDetails, response)
        }
    }
}
