#!/bin/bash
# ============================================================
#  CallBot India — Build APK on Mac/Linux
#  Prerequisites: Android Studio OR Android SDK installed
# ============================================================

set -e

echo ""
echo " ======================================"
echo "  CallBot India — APK Builder"
echo " ======================================"
echo ""

# Check Java
if ! command -v java &> /dev/null; then
    echo "[ERROR] Java not found!"
    echo "Install JDK 17: https://adoptium.net"
    exit 1
fi

# Find Android SDK
if [ -z "$ANDROID_HOME" ]; then
    if [ -d "$HOME/Library/Android/sdk" ]; then
        export ANDROID_HOME="$HOME/Library/Android/sdk"
    elif [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
    else
        echo "[ERROR] Android SDK not found! Set ANDROID_HOME or install Android Studio."
        exit 1
    fi
fi

echo "[OK] Java: $(java -version 2>&1 | head -1)"
echo "[OK] Android SDK: $ANDROID_HOME"
echo ""
echo "[BUILD] Running Gradle..."

chmod +x gradlew
./gradlew assembleDebug

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

if [ -f "$APK_PATH" ]; then
    echo ""
    echo " ================================================"
    echo "  SUCCESS! APK built:"
    echo "  $APK_PATH"
    echo " ================================================"
    echo ""
    echo " To install on your phone:"
    echo "  adb install $APK_PATH"
    echo ""
    
    # Try to open finder/file manager
    if command -v open &> /dev/null; then
        open "app/build/outputs/apk/debug/"
    fi
else
    echo "[ERROR] Build failed — APK not found"
    exit 1
fi
