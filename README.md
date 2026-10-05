# CHINTU

CHINTU is an original modular Android personal AI assistant.

It supports the foundation for:

- Gujarati
- Hindi
- English
- Hinglish
- AI providers
- Memory
- Voice
- Android tools
- Skills
- Agents
- Automation
- Web
- Vision
- Files
- Privacy modes

## AI Modes

AUTO

ONLINE

OFFLINE

PRIVATE

## Architecture

UI
↓
Assistant Orchestrator
↓
Agent / Planner
↓
Tool Registry
↓
Providers
↓
Android / Web / AI / Memory

## Build

Requirements:

- Android SDK 35
- JDK 17
- Gradle 8.10.2

Run:

./gradlew test

Then:

./gradlew assembleDebug

APK:

app/build/outputs/apk/debug/app-debug.apk

## Truthfulness

CHINTU never intentionally fakes unavailable:

- AI
- Web
- Browser automation
- Memory
- Vision
- Android actions
- Automation
- File parsing

If a provider is not configured, CHINTU reports that honestly.
