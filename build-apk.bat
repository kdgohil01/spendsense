@echo off
echo ========================================
echo SpendSense APK Builder
echo ========================================
echo.

REM Check if gradlew.bat exists
if not exist "gradlew.bat" (
    echo ERROR: gradlew.bat not found!
    echo.
    echo Please install Android Studio first.
    echo It will generate the gradlew.bat file when you open the project.
    echo.
    echo See BUILD_APK_GUIDE.md for detailed instructions.
    pause
    exit /b 1
)

REM Check if .env file exists
if not exist ".env" (
    echo WARNING: .env file not found!
    echo Creating .env file...
    echo GEMINI_API_KEY=YOUR_API_KEY_HERE > .env
    echo.
    echo Please edit .env file and add your Gemini API key.
    echo.
)

echo Building APK...
echo.

REM Clean previous build
call gradlew.bat clean

REM Build debug APK
call gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo Your APK is ready at:
    echo app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo You can now transfer this file to your phone and install it.
    echo.
    
    REM Try to open the folder
    if exist "app\build\outputs\apk\debug\" (
        echo Opening APK folder...
        start "" "app\build\outputs\apk\debug\"
    )
) else (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo.
    echo Please check the error messages above.
    echo See BUILD_APK_GUIDE.md for troubleshooting.
    echo.
)

pause
