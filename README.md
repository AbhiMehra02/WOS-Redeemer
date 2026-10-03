# WOS Gift Redeemer

**Public release:** v1 (version 1.0) — first stable release.

A small Android app for managing Whiteout Survival accounts, checking active gift codes, and redeeming codes for selected accounts.

**Current version:** `v1`  
**Package:** `com.wos.giftredeemer`  
**Minimum Android:** 6.0 / API 23  
**Target / compile SDK:** API 34  
**Language:** Java

## Features

- Check the current active gift-code list using a rendered WebView flow.
- Redeem the current fetched batch or previously saved codes.
- Multiple accounts with selection and aliases.
- Saved unique gift-code catalog.
- Run-based redemption history and technical details when needed.
- Network failures remain retryable rather than being permanently marked attempted.
- 30-day automatic cleanup for old catalog/history data.
- Two visual themes: **Classic Retro** and **Modern**.
- Appearance modes: **System**, **Light**, and **Dark**.

## Build on a laptop — Android Studio

1. Install a current Android Studio with JDK 17 support.
2. Clone/download this repository and open its root folder in Android Studio.
3. Allow Gradle sync to finish and install Android SDK 34 if Android Studio asks for it.
4. Select the `app` configuration.
5. Use **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

For command-line builds, use **JDK 17** and **Gradle 8.2.x**:

```bash
gradle clean assembleDebug
```

## Build on Android / mobile — Termux

Termux Android builds are more environment-sensitive than Android Studio. Install Termux from a maintained source, then install Java 17, Gradle, Android command-line/build tools, and a native `aapt2` package available for your Termux repository.

Once `java`, `gradle`, `adb`/Android tools and `aapt2` are available, run:

```bash
chmod +x build-termux.sh
./build-termux.sh
```

The script configures Termux's native `aapt2` override when it can find `aapt2`, then runs `assembleDebug`. See [`docs/BUILDING.md`](docs/BUILDING.md) for troubleshooting and requirements.

## Install the APK

On a laptop with ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or copy the APK to the Android device and open it with Android's package installer. Android may ask you to allow installation from that file manager/browser.

## GitHub Releases / APK

This repository includes a GitHub Actions workflow. Pushing a tag such as `v1` builds an installable debug-signed APK and attaches it automatically to the matching GitHub Release as:

```text
WOSGiftRedeemer-v1.apk
```

See [`docs/RELEASING.md`](docs/RELEASING.md).

> The automated APK is debug-signed for simple installation/testing. If you later distribute through an app store, use a private release signing key and never commit that key to Git.

## Project layout

```text
app/src/main/java/com/wos/giftredeemer/   Java application code
app/src/main/res/                         Android UI/resources
.github/workflows/                        CI and GitHub Release automation
docs/BUILDING.md                          Laptop/Termux build guide
docs/RELEASING.md                         Git + GitHub release instructions
CHANGELOG.md                              Version history
build-termux.sh                           Mobile build helper
```

## Privacy / credentials

Account information is stored locally by the application. Do not commit personal account data, signing keys, `local.properties`, or generated build directories.
