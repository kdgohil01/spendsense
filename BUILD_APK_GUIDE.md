# How to Build APK for SpendSense App

## Prerequisites

You need to install **Android Studio** or **Android Command Line Tools** to build the APK.

---

## Method 1: Using Android Studio (Easiest)

1. **Download and Install Android Studio**
   - Download from: https://developer.android.com/studio
   - Install with default settings

2. **Open the Project**
   - Launch Android Studio
   - Click "Open" and select: `K:\projects\SpendSense`
   - Wait for Gradle sync to complete (this may take a few minutes)

3. **Set up API Key** (if needed)
   - Open the `.env` file in the project root
   - Replace `YOUR_API_KEY_HERE` with your actual Gemini API key
   - Get API key from: https://ai.google.dev/

4. **Build APK**
   - Click **Build** menu → **Build Bundle(s) / APK(s)** → **Build APK(s)**
   - Wait for build to complete
   - Click "locate" in the notification to find your APK

5. **APK Location**
   - The APK will be at: `app\build\outputs\apk\debug\app-debug.apk`

---

## Method 2: Using Command Line (After Installing Android Studio)

Once Android Studio is installed, you can use the command line:

1. **Open Command Prompt or PowerShell**

2. **Navigate to project**
   ```cmd
   cd K:\projects\SpendSense
   ```

3. **Build Debug APK**
   ```cmd
   gradlew.bat assembleDebug
   ```

4. **Find your APK**
   - Location: `app\build\outputs\apk\debug\app-debug.apk`

---

## Method 3: Download Android Command Line Tools Only

If you don't want the full Android Studio:

1. **Download Command Line Tools**
   - Visit: https://developer.android.com/studio#command-line-tools-only
   - Download "Command line tools only" for Windows

2. **Extract and Setup**
   - Extract to: `C:\Android\cmdline-tools`
   - Create folder structure: `C:\Android\cmdline-tools\latest\`
   - Move contents into the `latest` folder

3. **Set Environment Variables**
   - Add to PATH: `C:\Android\cmdline-tools\latest\bin`
   - Set ANDROID_HOME: `C:\Android`

4. **Install SDK Components**
   ```cmd
   sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   ```

5. **Accept Licenses**
   ```cmd
   sdkmanager --licenses
   ```

6. **Build APK**
   ```cmd
   cd K:\projects\SpendSense
   gradlew.bat assembleDebug
   ```

---

## Installing APK on Your Phone

### Option A: USB Cable
1. Enable **Developer Options** on your phone:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
2. Enable **USB Debugging**:
   - Settings → Developer Options → USB Debugging
3. Connect phone to PC via USB
4. Copy `app-debug.apk` to your phone
5. Open the APK file on your phone to install

### Option B: Direct Transfer
1. Copy `app-debug.apk` to your phone (via USB, Bluetooth, or cloud)
2. Open the file on your phone
3. Allow "Install from Unknown Sources" if prompted
4. Install the app

---

## Troubleshooting

### "Gradle not found"
- Install Android Studio first, it includes Gradle

### "SDK not found"
- Set ANDROID_HOME environment variable
- Or let Android Studio install SDK automatically

### "Build failed"
- Check if `.env` file exists with API key
- Make sure you have internet connection (for downloading dependencies)
- Try: `gradlew.bat clean` then `gradlew.bat assembleDebug`

---

## Quick Summary

**Easiest way:** Install Android Studio → Open Project → Build → Build APK

**APK Location:** `app\build\outputs\apk\debug\app-debug.apk`
