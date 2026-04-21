# Changelog

All notable changes to YTune will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/), and this project adheres to [Semantic Versioning](https://semver.org/).

## [1.8.5] - 2026-04-21

### Fixed
- Android Auto discovery now accepts verified media browser clients instead of relying on a narrow hardcoded package list.
- Android Auto settings now treat the manifest default as enabled, so the app does not appear disabled unless the user explicitly turns it off.
- Search result pages now stop loading cleanly when a YouTube request fails instead of spinning forever.
- YouTube search and browse requests now include stored visitor data for more reliable responses.
- Search and YouTube Music playlist parsing now tolerate renderer placement changes in YouTube responses.

## [1.8.4] - 2026-04-21

### Fixed
- Enabled the Android Auto media browser service in the manifest.
- Android Auto browse requests now load asynchronously instead of blocking the media browser callback.
- Local playlist sync no longer performs network work inside a database transaction.

### Improved
- YouTube playlist screens now show the first page quickly and append continuation pages in the background.
- YouTube Music library playlist fetching now follows grid continuations and avoids duplicate playlist entries.

## [1.8.2] - 2026-03-23

### Added
- Google Sign-In with YouTube Music playlist sync
- Background playback and offline caching
- Synced lyrics with editing support
- Material You dynamic colors
- Android Auto support
- Sleep timer and persistent queue
- Skip silence and audio normalization
- Swipe gestures for queue management
- Deep linking for YouTube/YouTube Music URLs
- Multilingual support (English, Spanish, German, French, Italian)
- In-app update download and install
- Auto-versioning from git tags

[1.8.5]: https://github.com/Ublaze/YTune/releases/tag/v1.8.5
[1.8.4]: https://github.com/Ublaze/YTune/releases/tag/v1.8.4
[1.8.2]: https://github.com/Ublaze/YTune/releases/tag/v1.8.2
