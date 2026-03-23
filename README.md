<p align="center">
  <img src="screenshots/repository_preview.jpg" alt="YTune Banner" width="600">
</p>

<h1 align="center">YTune</h1>

<p align="center">
  <strong>YouTube Music, your way. Free, open-source, and gorgeous.</strong>
</p>

<p align="center">
  <a href="https://github.com/Ublaze/YTune/releases/latest"><img src="https://img.shields.io/github/v/release/Ublaze/YTune?style=flat-square&color=blue" alt="Latest Release"></a>
  <img src="https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <a href="LICENSE"><img src="https://img.shields.io/github/license/Ublaze/YTune?style=flat-square" alt="License"></a>
</p>

---

Stream your favorite music straight from YouTube Music -- with background playback, offline caching, lyrics, and a beautiful Material You interface that adapts to your wallpaper. No ads, no nonsense. Just music.

<details>
<summary><strong>Table of Contents</strong></summary>

- [Screenshots](#screenshots)
- [Why YTune?](#why-ytune)
- [Features](#features)
- [Installation](#installation)
- [Built With](#built-with)
- [Available Languages](#available-languages)
- [Contributing](#contributing)
- [Disclaimer](#disclaimer)
- [License](#license)

</details>

## Screenshots

<p align="center">
  <img src="screenshots/screenshot_home.png" width="200">
  <img src="screenshots/screenshot_player.png" width="200">
  <img src="screenshots/screenshot_songs.png" width="200">
  <img src="screenshots/screenshot_albums.png" width="200">
</p>

<p align="center">
  <img src="screenshots/screenshot_artist.png" width="200">
  <img src="screenshots/screenshot_artists.png" width="200">
</p>

## Why YTune?

| Feature | YTune | ViMusic | InnerTune | Musify |
|---------|:---:|:---:|:---:|:---:|
| Google Sign-In | Yes | - | - | - |
| Playlist sync | Yes | - | Partial | - |
| Background playback | Yes | Yes | Yes | Yes |
| Offline caching | Yes | Yes | Yes | Yes |
| Synced lyrics | Yes | - | Yes | - |
| Material You | Yes | Yes | Yes | - |
| Android Auto | Yes | - | - | - |
| Sleep timer | Yes | - | Yes | - |
| Audio normalization | Yes | - | Yes | - |
| Skip silence | Yes | - | Yes | - |
| Multilingual | 5 langs | Yes | Yes | Yes |
| Open source | GPL-3.0 | GPL-3.0 | GPL-3.0 | GPL-3.0 |

## Features

### Playback
- **Background playback** -- keep listening while you browse
- **Offline caching** -- save songs and play them without a connection
- **Skip silence & audio normalization** -- smooth, consistent listening
- **Sleep timer** -- fall asleep to your favorite tracks
- **Persistent queue** -- pick up right where you left off

### Library
- **Google Sign-In** -- access your YouTube Music playlists and library
- **Search everything** -- songs, albums, artists, videos, and playlists
- **Bookmark** artists and albums for quick access
- **Import playlists** from YouTube Music or manage your own locally
- **Lyrics** -- fetch, display, and edit synced or plain lyrics

### Customization
- **Material You** -- dynamic colors that match your wallpaper
- **Multilingual** -- English, Spanish, German, French, and Italian
- **Android Auto** -- control your music from the car

### Quality of Life
- **Swipe gestures** -- enqueue or remove songs with a flick
- **Deep linking** -- opens YouTube and YouTube Music links automatically

## Installation

### Download the APK

Grab the latest release and sideload it:

1. Head to the [Releases](../../releases/latest) page
2. Download the `.apk` file
3. Open it on your Android device and install (you may need to allow installs from unknown sources)

### Build from source

```bash
git clone https://github.com/Ublaze/YTune.git
cd YTune
./gradlew assembleDebug
```

The APK will be in `app/build/outputs/apk/debug/`.

## Built With

| Component | Technology |
|-----------|-----------|
| Language | [Kotlin](https://kotlinlang.org/) |
| UI Framework | [Jetpack Compose](https://developer.android.com/jetpack/compose) |
| Design System | [Material Design 3](https://m3.material.io/) |
| Media Playback | [ExoPlayer (Media3)](https://developer.android.com/media/media3) |
| Local Storage | [Room Database](https://developer.android.com/training/data-storage/room) |
| Networking | [Ktor HTTP Client](https://ktor.io/) |

## Available Languages

- English
- Spanish
- German
- French
- Italian

Want to help translate? Contributions are always welcome.

## Contributing

Check out [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines, or just [open an issue](../../issues/new/choose).


## Disclaimer

This project and its contents are not affiliated with, funded, authorized, endorsed by, or in any way associated with YouTube, Google LLC or any of its affiliates and subsidiaries.

Any trademark, service mark, trade name, or other intellectual property rights used in this project are owned by the respective owners.

## License

YTune is licensed under the [GNU General Public License v3.0](LICENSE).

---

<div align="center">

If you enjoy YTune, consider giving it a :star: — it helps others discover it!

</div>
