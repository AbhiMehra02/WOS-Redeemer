# Building WOS Gift Redeemer

## Windows

Install Android Studio and its Android SDK, then clone/open the project.

```powershell
git clone https://github.com/YOUR_USERNAME/WOS-Gift-Redeemer.git
cd WOS-Gift-Redeemer
.\gradlew.bat assembleDebug
```

You can alternatively use Android Studio: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

APK output:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Copy it to the v1 filename:

```powershell
Copy-Item app\build\outputs\apk\debug\app-debug.apk WOSGiftRedeemer-v1.apk
```

The Gradle Wrapper is included, so a separate Gradle installation is not required.

## macOS / Linux

Install Android Studio and its Android SDK. Using Android Studio's bundled JDK is the easiest setup.

```bash
git clone https://github.com/YOUR_USERNAME/WOS-Gift-Redeemer.git
cd WOS-Gift-Redeemer
chmod +x gradlew
./gradlew assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Copy it:

```bash
cp app/build/outputs/apk/debug/app-debug.apk WOSGiftRedeemer-v1.apk
```

You may also open the project in Android Studio and use **Build APK(s)**.

## Android / Termux

This is the Termux workflow used during development.

### First-time setup

```bash
cd ~/termux-android-studio
sed -i 's/openjdk-17/openjdk-21/g' install.sh
bash install.sh
```

This installation step is only required once.

### Build

```bash
cd /storage/emulated/0/Projects
unzip /storage/emulated/0/Download/WOSGiftRedeemer-v1.zip
cd WOSGiftRedeemer
studio build .
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Copy it to Downloads:

```bash
cp app/build/outputs/apk/debug/app-debug.apk /storage/emulated/0/Download/WOSGiftRedeemer-v1.apk
```

## Signing

These commands normally produce a debug-signed APK. Configure a private release signing key for production/store distribution. Never commit keystores or signing passwords.
