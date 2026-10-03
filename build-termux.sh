#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
cd "$(dirname "$0")"

echo "== Java =="
java -version
echo "== Gradle =="
gradle --version | head -20

if command -v aapt2 >/dev/null 2>&1; then
  mkdir -p "$HOME/.gradle"
  AAPT2="$(command -v aapt2)"
  if ! grep -q '^android.aapt2FromMavenOverride=' "$HOME/.gradle/gradle.properties" 2>/dev/null; then
    echo "android.aapt2FromMavenOverride=$AAPT2" >> "$HOME/.gradle/gradle.properties"
    echo "Configured native aapt2: $AAPT2"
  fi
else
  echo "WARNING: native aapt2 is not in PATH. AGP's Maven aapt2 may not run on ARM64 Termux."
fi

gradle clean assembleDebug --no-daemon
APK="app/build/outputs/apk/debug/app-debug.apk"
[ -f "$APK" ] || { echo "APK not found after build"; exit 1; }
echo "Built: $APK"
