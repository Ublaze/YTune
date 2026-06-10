# Changelog

All notable changes to YTune will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/), and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Fixed
- Stale stream URLs are no longer replayed from the cache after a playback error, so Retry actually retries (previously a failed song could stay broken until two other songs were played).
- Resolved stream URLs now expire with the URL's own `expire` parameter instead of living forever.
- Player requests now fall back across multiple YouTube clients (ANDROID_VR → IOS → TV embedded) instead of giving up after one, and the Piped fallback matches streams by itag (exact-bitrate matching rarely succeeded) across multiple instances.
- Fixed a potential infinite loop in playlist sync and album loading when YouTube echoes the same continuation token.
- Local playlist sync button and album screens benefit from the continuation guard above.
- Update check now reads the release tag instead of the release title, so renamed releases can't break version comparison.

### Added
- A track that repeatedly fails now auto-skips to the next song (capped, and never on network loss) instead of halting the queue.
- Playing a large YouTube playlist before it has fully loaded now appends the remaining songs to the queue as they arrive.
- Playlist pages show the loaded song count and a "loading more" indicator.
- YouTube Music playlists are cached locally and appear instantly (and offline) in the Playlists tab.
- Liked Songs entry in the YouTube Music section of the Playlists tab.
- Session-expiry detection with a re-login prompt when YouTube Music auth stops working.
- Pull-to-refresh on the Playlists tab.
- Sign-out now asks for confirmation.
- Importing an already-imported YouTube playlist refreshes it instead of creating a duplicate.

### Changed
- Release workflow now reads signing credentials from repository secrets instead of hardcoding them.

## [2.1.0] - 2026-04-22

### Fixed
- Completed `PlayerMediaBrowserService.SessionCallback` so vehicle media controls (play/pause/skip/seek) work reliably on Android Auto head units.

## [2.0.0] - 2026-04-22

### Added
- Skip/pause controls improvements for Android Auto (PR #2).

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

[2.1.0]: https://github.com/Ublaze/YTune/releases/tag/v2.1.0
[2.0.0]: https://github.com/Ublaze/YTune/releases/tag/v2.0.0
[1.8.5]: https://github.com/Ublaze/YTune/releases/tag/v1.8.5
[1.8.4]: https://github.com/Ublaze/YTune/releases/tag/v1.8.4
[1.8.2]: https://github.com/Ublaze/YTune/releases/tag/v1.8.2
