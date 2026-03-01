package com.callbot.app

import android.content.Context

data class BotResponse(
    val text: String,
    val shouldEndCall: Boolean = false,
    val savedMessage: String = ""
)

class ConversationEngine(private val context: Context) {

    private val prefs = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
    private var turnCount = 0
    private var capturedMessage = StringBuilder()
    private var callerName = ""

    fun generateResponse(input: String, callerNumber: String): BotResponse {
        turnCount++
        val lower = input.lowercase().trim()
        val ownerName = prefs.getString(MainActivity.KEY_OWNER_NAME, "them") ?: "them"
        val langIndex = prefs.getInt(MainActivity.KEY_LANGUAGE, 0)

        // Extract caller name if introduced
        if (lower.contains("i am") || lower.contains("this is") ||
            lower.contains("main") || lower.contains("mera naam") || lower.contains("meri")) {
            callerName = extractName(input)
        }

        // Capture message content
        if (turnCount > 1) {
            capturedMessage.appendLine(input)
        }

        // End call conditions
        if (isGoodbye(lower)) {
            val savedMsg = if (capturedMessage.isNotEmpty()) capturedMessage.toString() else ""
            return BotResponse(
                text = getFarewellMessage(langIndex, ownerName),
                shouldEndCall = true,
                savedMessage = savedMsg
            )
        }

        // Max turns reached
        if (turnCount >= 5) {
            val savedMsg = if (capturedMessage.isNotEmpty()) capturedMessage.toString() else ""
            return BotResponse(
                text = getMaxTurnsMessage(langIndex, ownerName),
                shouldEndCall = true,
                savedMessage = savedMsg
            )
        }

        // Handle specific intents
        return when {
            isUrgent(lower) -> handleUrgent(langIndex, ownerName)
            isLeaveMessage(lower) -> handleLeaveMessage(langIndex, ownerName)
            isCallBack(lower) -> handleCallBack(langIndex, ownerName, callerNumber)
            isWhenAvailable(lower) -> handleWhenAvailable(langIndex, ownerName)
            isAboutOwner(lower, ownerName) -> handleAboutOwner(langIndex, ownerName)
            isYes(lower) -> handleYes(langIndex, ownerName)
            isNo(lower) -> handleNo(langIndex, ownerName)
            else -> handleGeneral(langIndex, ownerName, input)
        }
    }

    private fun extractName(input: String): String {
        val patterns = listOf(
            "my name is (.+)",
            "this is (.+)",
            "i am (.+)",
            "main (.+) bol raha",
            "mera naam (.+) hai"
        )
        for (pattern in patterns) {
            val match = Regex(pattern, RegexOption.IGNORE_CASE).find(input)
            if (match != null) return match.groupValues[1].trim().split(" ").take(2).joinToString(" ")
        }
        return ""
    }

    private fun isGoodbye(text: String) =
        listOf("bye", "goodbye", "ok bye", "alvida", "dhanyawad", "shukriya", "thanks", "thank you",
            "theek hai", "accha", "nothing", "kuch nahi", "no thanks", "no need").any { text.contains(it) }

    private fun isUrgent(text: String) =
        listOf("urgent", "emergency", "important", "zaruri", "jaldi", "help", "asap").any { text.contains(it) }

    private fun isLeaveMessage(text: String) =
        listOf("message", "sandesh", "bata dena", "tell", "inform", "convey").any { text.contains(it) }

    private fun isCallBack(text: String) =
        listOf("call back", "callback", "call me", "wapas call", "phone karo", "return call").any { text.contains(it) }

    private fun isWhenAvailable(text: String) =
        listOf("when", "kab", "what time", "kitne baje", "available", "free", "milenge").any { text.contains(it) }

    private fun isAboutOwner(text: String, ownerName: String) =
        text.contains(ownerName.lowercase()) || listOf("where", "kahan", "why not", "kyun nahi").any { text.contains(it) }

    private fun isYes(text: String) =
        listOf("yes", "haan", "ha", "sure", "ok", "okay", "bilkul", "zarur").any { text.contains(it) }

    private fun isNo(text: String) =
        listOf("no", "nahi", "na", "nope", "not really", "mat").any { text.contains(it) }

    // Response generators per language
    private fun handleUrgent(lang: Int, owner: String): BotResponse {
        val text = when (lang) {
            0 -> "Mujhe samajh aaya ki yeh urgent hai. Main $owner ko turant message karunga. Kya aap apna naam aur message de sakte hain?"
            2 -> "Idhu urgent nu purinjukitte. $owner ku udanee message pannuven. Ungal peyar solluveengala?"
            else -> "I understand this is urgent. I'll immediately notify $owner. Could you please share your name and the message?"
        }
        return BotResponse(text = text, shouldEndCall = false)
    }

    private fun handleLeaveMessage(lang: Int, owner: String): BotResponse {
        val text = when (lang) {
            0 -> "Zaroor, aap apna message chod sakte hain. Boliye, main record kar lunga."
            else -> "Of course! Please go ahead and leave your message. I'll make sure $owner gets it."
        }
        return BotResponse(text = text)
    }

    private fun handleCallBack(lang: Int, owner: String, callerNumber: String): BotResponse {
        val text = when (lang) {
            0 -> "Bilkul! Main $owner ko aapka number $callerNumber de dunga aur unhe wapas call karne ko kahunga. Kuch aur?"
            else -> "Absolutely! I'll pass on your number $callerNumber to $owner and ask them to call you back. Anything else?"
        }
        return BotResponse(text = text, savedMessage = "Caller wants a callback at: $callerNumber")
    }

    private fun handleWhenAvailable(lang: Int, owner: String): BotResponse {
        val text = when (lang) {
            0 -> "Mujhe exact time pata nahi, lekin main unhe aaapka message zaroor dunga. Kya aap apna naam bata sakte hain?"
            else -> "I'm not sure of the exact time, but I'll make sure $owner gets your message. Could you share your name?"
        }
        return BotResponse(text = text)
    }

    private fun handleAboutOwner(lang: Int, owner: String): BotResponse {
        val text = when (lang) {
            0 -> "$owner abhi available nahi hain. Main unhe aapke baare mein bataunga. Aap koi message dena chahte hain?"
            else -> "$owner is currently not available. I can pass along a message. Would you like to leave one?"
        }
        return BotResponse(text = text)
    }

    private fun handleYes(lang: Int, owner: String): BotResponse {
        val text = when (lang) {
            0 -> "Theek hai! Aap apna message boliye."
            else -> "Great! Please go ahead with your message."
        }
        return BotResponse(text = text)
    }

    private fun handleNo(lang: Int, owner: String): BotResponse {
        val savedMsg = if (capturedMessage.isNotEmpty()) capturedMessage.toString() else ""
        val text = when (lang) {
            0 -> "Theek hai. Main $owner ko bataunga ki aapne call kiya. Dhanyawad! Goodbye."
            else -> "Alright. I'll let $owner know you called. Thank you! Goodbye."
        }
        return BotResponse(text = text, shouldEndCall = true, savedMessage = savedMsg)
    }

    private fun handleGeneral(lang: Int, owner: String, input: String): BotResponse {
        capturedMessage.appendLine(input)
        val callerRef = if (callerName.isNotEmpty()) callerName else "the caller"
        val text = when (lang) {
            0 -> "Samjha. Main yeh $owner ko bataunga. Kya koi aur baat hai?"
            else -> "Understood. I'll pass that along to $owner. Is there anything else you'd like to add?"
        }
        return BotResponse(text = text, savedMessage = "Message from $callerRef: $input")
    }

    private fun getFarewellMessage(lang: Int, owner: String): String = when (lang) {
        0 -> "Theek hai. Main $owner ko aapka sandesh deta hoon. Dhanyawad. Shubh din!"
        2 -> "Sari. $owner ku ungal message solluven. Nandri. Nalla naal!"
        else -> "Alright. I'll make sure $owner receives your message. Thank you for calling. Have a wonderful day!"
    }

    private fun getMaxTurnsMessage(lang: Int, owner: String): String = when (lang) {
        0 -> "Main aapke saath bahut baat kar chuka hoon. Aapka message $owner ko forward kiya jayega. Dhanyawad! Goodbye."
        else -> "I've captured your message and will forward it to $owner. Thank you for calling! Goodbye."
    }
}
