# 🤖 CallBot India — AI Call Answering Bot
### Auto-answers your calls with Indian voices (Hindi, Tamil, Telugu & more)

---

## 📱 What This App Does

CallBot India automatically answers your incoming calls when you're busy, using an AI bot that:

- **Speaks in your chosen Indian language** — Hindi, English (Indian), Tamil, Telugu, Bengali, Marathi, Gujarati, Kannada
- **Understands the caller** — uses Speech Recognition to listen to the caller
- **Holds a smart conversation** — asks for name, takes messages, handles urgent calls
- **Saves all messages** — logs every call and message for you to review
- **Custom greeting** — personalize what the bot says when it picks up
- **Custom bot name** — name your bot Priya, Arjun, Kavita, etc.

---

## 🚀 How to Build & Install

### Requirements
- Android Studio (latest, download from developer.android.com/studio)
- Android phone with Android 10+ (API 29+)
- USB cable

### Step-by-Step

1. **Clone/Download this project**
   ```
   Open Android Studio → File → Open → Select the CallBot folder
   ```

2. **Sync Gradle**
   - Android Studio will auto-prompt. Click "Sync Now"
   - Wait for download to complete (~2-3 minutes)

3. **Connect your phone**
   - Enable Developer Options: Settings → About Phone → tap Build Number 7 times
   - Enable USB Debugging: Settings → Developer Options → USB Debugging ON
   - Connect phone via USB → Trust the computer

4. **Build & Install**
   ```
   Click the green ▶ Run button in Android Studio
   ```
   OR build an APK:
   ```
   Build → Build Bundle(s)/APK(s) → Build APK(s)
   ```
   APK location: `app/build/outputs/apk/debug/app-debug.apk`

5. **Install APK on phone** (if built separately)
   - Transfer APK to phone
   - Settings → Install Unknown Apps → Allow
   - Tap the APK to install

---

## ⚙️ App Setup After Install

### One-time Setup (IMPORTANT!)

1. **Open CallBot** on your phone

2. **Set as Default Dialer**
   - Tap "📞 Set as Default Dialer"
   - Confirm the dialog
   - *This gives CallBot full call control*

3. **Grant All Permissions**
   - Phone (READ_PHONE_STATE, ANSWER_PHONE_CALLS)
   - Microphone (RECORD_AUDIO)
   - *These are required for the bot to work*

4. **Configure Your Bot**
   - Enter **Your Name** (required)
   - Enter **Bot Name** (e.g., Priya)
   - Select **Language** (Hindi, Tamil, etc.)
   - Adjust **Speed** and **Pitch** sliders
   - Optionally write a custom greeting

5. **Tap "✓ SAVE SETTINGS"**

6. **Toggle "Enable Call Bot"** to ON

7. **Test it** — ask a friend to call you!

---

## 🗣️ Supported Indian Languages

| Language | Code | TTS | STT |
|----------|------|-----|-----|
| Hindi | hi-IN | ✅ | ✅ |
| English (Indian) | en-IN | ✅ | ✅ |
| Tamil | ta-IN | ✅ | ✅ |
| Telugu | te-IN | ✅ | ✅ |
| Bengali | bn-IN | ✅ | ✅ |
| Marathi | mr-IN | ✅ | ✅ |
| Gujarati | gu-IN | ✅ | ✅ |
| Kannada | kn-IN | ✅ | ✅ |

> **Note:** For best results, ensure your phone has the Google TTS engine installed with Indian language packs.  
> Settings → Accessibility → Text-to-Speech → Google TTS → Install language data

---

## 📞 How Bot Handles Calls

```
Incoming Call
     ↓
CallBot answers automatically
     ↓
Bot greets in chosen language:
"Namaste! Main Priya hoon, [Your Name] ki virtual assistant..."
     ↓
Bot listens to caller (Speech-to-Text)
     ↓
     ├─ Urgent? → Notifies with priority message
     ├─ Leave message? → Records caller's message  
     ├─ Call back? → Saves callback request with number
     ├─ When available? → Responds appropriately
     └─ General? → Continues conversation, captures info
     ↓
Bot says farewell & ends call
     ↓
Message saved to app for you to review
```

---

## 🔔 Message Review

All captured messages are saved in SharedPreferences.  
Future version will add a Messages tab to view all saved messages.

---

## 🛠️ Customization

### Custom Greeting Template
Use these placeholders in your custom greeting:
- `{name}` → replaced with your name
- `{bot}` → replaced with bot's name

**Example:**
```
Hello! I'm {bot}, personal assistant of {name}. They're in a meeting right now. How may I assist you?
```

---

## ⚠️ Troubleshooting

| Problem | Solution |
|---------|----------|
| Bot doesn't answer calls | Make sure CallBot is set as Default Dialer |
| Voice not in Hindi/Tamil | Install Google TTS → download language pack |
| Bot can't understand caller | Check Microphone permission is granted |
| App crashes on Android 14+ | Grant "Phone Calls" permission in app info |

---

## 📋 Permissions Explained

| Permission | Why Needed |
|-----------|------------|
| READ_PHONE_STATE | Detect incoming calls |
| ANSWER_PHONE_CALLS | Auto-answer the call |
| RECORD_AUDIO | Hear and understand the caller |
| MODIFY_AUDIO_SETTINGS | Route audio correctly |
| FOREGROUND_SERVICE | Keep bot running during calls |
| READ_CALL_LOG | Log call history |

---

## 🏗️ Project Structure

```
CallBot/
├── app/src/main/
│   ├── AndroidManifest.xml          # Permissions & components
│   ├── java/com/callbot/app/
│   │   ├── MainActivity.kt          # App UI & settings
│   │   ├── CallScreeningService.kt  # Intercepts incoming calls  
│   │   ├── CallBotService.kt        # Core bot logic
│   │   ├── TTSManager.kt            # Indian voice TTS
│   │   ├── ConversationEngine.kt    # NLU & response generation
│   │   └── BootReceiver.kt          # Auto-start on reboot
│   └── res/
│       ├── layout/activity_main.xml # App UI layout
│       └── values/                  # Strings, themes
├── build.gradle
└── README.md
```

---

## 🔮 Future Enhancements

- [ ] Messages inbox UI to review saved messages
- [ ] SMS notification when bot takes a message
- [ ] Call scheduling (bot only active during certain hours)
- [ ] VIP list (certain numbers always ring through)
- [ ] WhatsApp/SMS auto-reply integration
- [ ] Cloud backup of messages
- [ ] Multiple language per call (bilingual mode)

---

*Built with ❤️ for India — supports all major Indian languages*
