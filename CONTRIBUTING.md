# Contributing to YTune

Thanks for your interest! YTune is built with Kotlin and Jetpack Compose, and contributions of all sizes are welcome.

## Getting Started

1. **Fork and clone** the repo:
   ```bash
   git clone https://github.com/<your-username>/YTune.git
   cd YTune
   ```

2. **Open in Android Studio** (Hedgehog or newer recommended)

3. **Build and run**:
   ```bash
   ./gradlew assembleDebug
   ```
   Or just hit Run in Android Studio on an emulator or device.

## Project Structure

- `app/` — Main application module (UI, ViewModels, navigation)
- `innertube/` — YouTube Music InnerTube API client
- `ktor-client-brotli/` — Brotli compression support for Ktor
- `kugou/` — KuGou lyrics provider

## Code Style

- Kotlin with Jetpack Compose — follow the existing patterns
- Use Material 3 components and dynamic colors
- Keep composables focused and small
- Use ViewModels for state management

## Before Submitting a PR

- Test on a real device or emulator (API 24+)
- Make sure the app builds: `./gradlew assembleDebug`
- Keep changes focused — one feature or fix per PR
- Update the README if your change adds user-facing functionality

## Reporting Bugs

[Open an issue](../../issues/new/choose) with:
- Your device model and Android version
- YTune version (from app settings)
- Steps to reproduce
- Screenshots or screen recordings if applicable
