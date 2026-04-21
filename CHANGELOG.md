# Changelog

All notable changes to YTune will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/), and this project adheres to [Semantic Versioning](https://semver.org/).

## [1.8.6] - 2026-04-21

### Added
- Sign-in prompt card in the Playlists tab when not logged in
- Loading skeleton (shimmer) in the Playlists tab while YTM playlists are fetching
- Pull-to-refresh in the Playlists tab to reload YTM playlists
- Sync badge overlay on playlist thumbnails that are linked to a YTM playlist
- Playlist author name and song count shown below the cover in the playlist detail screen
- Smart "Already in library" icon in PlaylistScreen — tapping opens the local playlist directly instead of showing the import dialog
- Sync spinner and success/failure Snackbar in LocalPlaylistScreen during sync
- "Sign in for personalized recommendations" card on the Home tab when not logged in
- Shimmer placeholder on the Home tab while personalized sections are loading
- Channel handle (@handle) display in Account Settings
- "Refresh account info" button in Account Settings to update name, photo, and handle without signing out
- `forceRefresh` support in personalized home loading so a new login always fetches fresh sections

### Fixed
- Re-login after a failed personalized home load now always retries the fetch

[1.8.6]: https://github.com/Ublaze/YTune/compare/v1.8.5...v1.8.6

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

[1.8.2]: https://github.com/Ublaze/YTune/releases/tag/v1.8.2
