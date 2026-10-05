# Build and Verification

Install:

- Android SDK 35
- JDK 17

Run:

./gradlew test

Then:

./gradlew assembleDebug

APK:

app/build/outputs/apk/debug/app-debug.apk

Install the APK on an Android 8.0+ device.

Test:

- App launch
- Chat
- AI mode
- Memory
- Settings
- Voice
- Permissions
- Android tools
- Error handling

GitHub Actions is configured to build the debug APK automatically.
