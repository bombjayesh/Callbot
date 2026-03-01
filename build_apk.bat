@echo off
REM ============================================================
REM  CallBot India — Build APK on Windows (batch script)
REM  Prerequisites: Android Studio OR Android SDK installed
REM ============================================================

echo.
echo  ======================================
echo   CallBot India — APK Builder
echo  ======================================
echo.

REM Check Java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java not found! Install JDK 17 from https://adoptium.net
    pause & exit /b 1
)

REM Check Android SDK
if "%ANDROID_HOME%"=="" (
    echo [WARN] ANDROID_HOME not set. Checking common locations...
    if exist "%LOCALAPPDATA%\Android\Sdk" (
        set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
    ) else if exist "%USERPROFILE%\AppData\Local\Android\Sdk" (
        set ANDROID_HOME=%USERPROFILE%\AppData\Local\Android\Sdk
    ) else (
        echo [ERROR] Android SDK not found!
        echo Please install Android Studio from https://developer.android.com/studio
        pause & exit /b 1
    )
)

echo [OK] Java found
echo [OK] Android SDK: %ANDROID_HOME%
echo.
echo [BUILD] Running Gradle build...
call gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo  ================================================
    echo   SUCCESS! APK built at:
    echo   app\build\outputs\apk\debug\app-debug.apk
    echo  ================================================
    echo.
    echo  Transfer this APK to your Android phone and install it!
    explorer app\build\outputs\apk\debug\
) else (
    echo.
    echo [ERROR] Build failed. See error above.
)

pause
