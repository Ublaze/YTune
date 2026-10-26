# YTune — Project Handover

> Prepared 2026-06-10 for picking the project back up / handing to another agent (Fable) for
> review and future improvements. This documents **what the project is, what has been built,
> how the YTune-specific pieces work, the current state, and the known open items / risks.**

---

## 1. What this project is

**YTune** is a free, open-source Android app that streams music from **YouTube Music**, with
background playback, offline caching, synced lyrics, Material You theming, and Android Auto
support.

It is a **fork of "Music You"** (itself a Material You–styled descendant of the ViMusic/InnerTune
lineage). The original package name `com.github.musicyou` and the helper Gradle modules
(`innertube`, `kugou`, `ktor-client-brotli`) are inherited from that base — they were **not**
renamed during the rebrand, so internally the app is still `com.github.musicyou` even though it
ships as "YTune."

- **Repo:** https://github.com/Ublaze/YTune
- **Local path:** `D:\Projects\YTune`
- **License:** GPL-3.0
- **Working version:** `2.3.0` (versionName/versionCode auto-derived from git tags, with the `appVersion` override — see §6)
- **Default branch:** `main`

### Tech stack
| Layer | Tech |
|-------|------|
| Language | Kotlin 2.3 (context parameters enabled via `-Xcontext-parameters`) |
| UI | Jetpack Compose + Material 3 (Material You dynamic color) |
| Playback | Media3 / ExoPlayer |
| Local DB | Room (schemas in `app/schemas`) |
| Networking | Ktor (OkHttp engine) + Brotli content-encoding |
| Persisted prefs | SharedPreferences (`utils/Preferences.kt`) + DataStore in places |
| Min / Target / Compile SDK | minSdk 23, compileSdk 36 |
| JDK | 17 |

### Gradle modules
- `:app` — the Android app (package `com.github.musicyou`)
- `:innertube` — YouTube Music private-API client (the most heavily modified module for YTune)
- `:kugou` — KuGou lyrics provider
- `:ktor-client-brotli` — Brotli decompression plugin for Ktor
- `:github` — **YTune-added** tiny module that hits the GitHub Releases API for the in-app updater

---

## 2. Fork history — where YTune diverges

The base "Music You" history runs up to commit **`1b420a2 Adding a search shortcut`**. Everything
YTune-specific begins at:

```
993ba3d  Rebrand to YTune + add Google Sign-In & YouTube Music playlist sync   (2026-03-22)
```

The full YTune-specific commit set (oldest → newest):

| Date | Commit | What it did |
|------|--------|-------------|
| 2026-03-22 | `993ba3d` | Rebrand to YTune; first cut of Google Sign-In + YTM playlist sync |
| 2026-03-22 | `cc7886d` | Add GitHub Actions build/release workflow |
| 2026-03-22 | `311d791` | Fix release workflow permissions |
| 2026-03-22 | `de4bb8b` | Fix app name, login flow, cookie capture |
| 2026-03-22 | `d976dc6` | Switch to signed release APK (reduce Play Protect warnings) |
| 2026-03-22 | `6dbd346` | Add **SAPISIDHASH** auth header + improve cookie capture |
| 2026-03-22 | `e29a49a` | Rewrite login flow to match InnerTune's proven approach |
| 2026-03-22 | `4c2d2be` | UI polish: Poppins font + blurred album-art player background |
| 2026-03-22 | `eb4ac54` | Cookie capture across **all** YouTube domains on every page load |
| 2026-03-23 | `a758945` | Profile photo, rich account UI, personalized home screen |
| 2026-03-23 | `4194381`,`8fb6b40` | Fix HomePage.kt thumbnail type mismatches |
| 2026-03-23 | `15f3b39` | Fix player 400 when authenticated + redesign home |
| 2026-03-23 | `9d591f0` | **Dedicated anonymous client for player requests** (key fix — see §3.2) |
| 2026-03-23 | `f834157` | New branding: bold "Y" icon on red→purple gradient |
| 2026-03-23 | `31abb2b` | **Auto-version from git tags** + in-app update download & install |
| 2026-03-23 | `f4ca06c` | Use `providers.exec` for config-cache compatibility |
| 2026-03-23 | `043bf2e` | Soften Android Auto setup message |
| 2026-03-23 | `0ed3acc`,`fafda53` | README revamp + repo grooming (badges, CONTRIBUTING, CHANGELOG) |
| 2026-03-23 | `116c167` | **Persistent signing keystore** so APK updates install over each other |
| 2026-03-24 | `55ada24`,`29f7584` | README download button + fixed-filename one-click APK download |
| 2026-04-21 | `63074db` | Improve playlist sync + Android Auto browsing |
| 2026-04-21 | `020d9b9` | Fix Android Auto discovery + search loading |
| 2026-04-22 | `8d4aeb0` | Complete `PlayerMediaBrowserService.SessionCallback` for vehicle controls |
| 2026-04-22 | `565d516` | Bump version to 2.1.0 |
| 2026-04-22 | `3fc24f9` | Merge PR #2 (skip/pause controls) |

Open remote branches not yet merged:
- `origin/copilot/review-app-good-to-have-enhancements` — un-merged enhancement spike (worth reviewing)
- `origin/copilot/add-skip-pause-controls` — merged via PR #2
- `origin/groomer/improvements-2026-03-23` — merged via PR #1

---

## 3. How the YTune-specific features work

### 3.1 Google Sign-In / YouTube Music authentication
This is the headline feature and the most fragile part. Flow:

1. **`ui/screens/login/LoginScreen.kt`** — a Compose `WebView` loads a Google `ServiceLogin`
   URL (`GOOGLE_LOGIN_URL`) configured to redirect to `music.youtube.com` after login.
   - On every page event (`onPageFinished` + `doUpdateVisitedHistory`) it calls
     `tryCaptureCookies()`, which scans **multiple domains** (`music.youtube.com`,
     `www.youtube.com`, `youtube.com`, `accounts.google.com`) for auth cookies. It treats login
     as successful once it sees `SAPISID=` or `__Secure-3PAPISID=`, preferring the
     `music.youtube.com` cookie string for the API.
   - It also injects a JS bridge (`Android.onRetrieveVisitorData`) to pull
     `window.yt.config_.VISITOR_DATA` off the YTM page and store it in `Innertube.visitorData`.
2. **`ui/navigation/Navigation.kt`** (~line 287) — the `Login` route's `onLoginSuccess`:
   - Sets `Innertube.cookie = cookies`, persists it to prefs (`ytmCookieKey`) plus `visitorData`.
   - Fires `Innertube.accountInfo()` and stores name / email / photo URL into prefs.
3. **`MainApplication.onCreate()`** — on every cold start, restores `Innertube.cookie` and
   `Innertube.visitorData` from prefs, and lazily fetches fresh visitorData if missing.
4. **`ui/screens/settings/AccountSettings.kt`** — shows profile photo / name / email / "Connected"
   card when logged in; Sign-out clears prefs, `Innertube.cookie`, and `CookieManager` cookies.

**Auth header generation — `innertube/.../Innertube.kt`:**
- `generateSapisidHash()` builds the `SAPISIDHASH <timestamp>_<sha1(timestamp + " " + SAPISID + " " + ORIGIN)>`
  header that YouTube's private API requires for cookie auth. `ORIGIN = https://music.youtube.com`.
- The **authenticated** `client` sends `Cookie`, `Authorization: SAPISIDHASH…`, `X-Origin`,
  `Origin` when a cookie is present; otherwise falls back to an `X-Goog-Api-Key`.

### 3.2 Two HTTP clients (important architectural decision)
`Innertube.kt` deliberately keeps **two** Ktor clients:
- `client` — authenticated; used for browse/search/account/library.
- `playerClient` — **never** sends auth headers; always uses the anonymous `X-Goog-Api-Key`.

Reason (commit `9d591f0`): the `ANDROID_VR` client returns direct streaming URLs but **rejects
cookie auth** — sending cookies caused HTTP 400 on player requests once the user logged in. Using
a dedicated anonymous client for `/player` also avoids leaking YouTube cookies to external APIs.
**Keep this separation** — collapsing the two clients will reintroduce the 400 bug.

### 3.3 YouTube Music library / playlist sync
- `innertube/.../requests/LibraryPage.kt` → `Innertube.libraryPlaylists()` browses
  `FEmusic_liked_playlists`, follows **grid continuations** to page through all playlists, and
  de-dupes by key (commit `63074db`/`020d9b9` hardened the continuation loop against infinite
  loops and duplicates).
- `ui/screens/home/HomePlaylists.kt` calls it inside a `LaunchedEffect(Innertube.isLoggedIn)` and
  merges the YTM playlists into the home view.
- `AccountPage.kt` → `Innertube.accountInfo()` parses the `account_menu` popup renderer for
  name/email/handle/photo.

### 3.4 In-app updater
- `:github` module — `GitHub.getLastestRelease()` calls the GitHub Releases API, filters out
  drafts/prereleases, returns the newest. `Release.apkDownloadUrl` points at the release asset.
- `ui/screens/settings/About.kt` — "Check for Updates" button compares the GitHub release version
  to the installed `versionName` (`compareVersions`), and if newer, downloads the APK to `cacheDir`
  with a progress bar and launches the system installer via a `FileProvider` + `ACTION_VIEW`
  package-archive intent.
- Requires `REQUEST_INSTALL_PACKAGES` permission (declared in manifest) and the `fileprovider`
  (`@xml/file_paths`).

### 3.5 Android Auto
- `service/PlayerMediaBrowserService.kt` — a `MediaBrowserService` exposing **Songs / Playlists /
  Albums** browse trees plus Shuffle / Favorites / Offline quick actions, all sourced from the Room
  DB. `SessionCallback` wires vehicle transport controls (play/pause/skip/seek) and
  `onPlayFromMediaId` to the running `PlayerService`.
- Client gating: `isAllowedClient()` accepts the app's own UID, `SYSTEM_UID`, or any package that
  genuinely owns the calling UID (commit `020d9b9` replaced a narrow hardcoded package allowlist
  with this verified-caller check, fixing AA discovery).
- Manifest declares the service `exported="true"` with the `MediaBrowserService` intent filter,
  plus `automotive_app_desc`, the car notification small-icon, and a leanback banner.

### 3.6 Branding / UI polish
- New launcher/app icon: bold "Y" on a red→purple gradient (`f834157`).
- Poppins font in `res/font`, blurred album-art player background (`4c2d2be`).
- Personalized home screen with account avatar (`a758945`, `15f3b39`).
- Deep links: manifest handles `music.youtube.com` / `youtube.com` / `youtu.be` `/playlist`,
  `/watch`, `/channel` URLs and `SEND` text intents.

---

## 4. Build, signing & release

### Local build
```bash
./gradlew assembleDebug      # APK → app/build/outputs/apk/debug/
./gradlew assembleRelease    # signed if KEYSTORE_PATH env is set, else debug-signed
```
Debug builds get `applicationIdSuffix = ".debug"` so they install alongside release.

### Versioning (auto, from git tags) — `app/build.gradle.kts`
- `gitVersion` runs `git describe --tags --abbrev=0`, strips a leading `v`.
- A `gradle.properties` override `appVersion=2.1.0` takes precedence (`appVersionOverride`).
- `versionCode` is computed `major*10000 + minor*100 + patch`.
- **To cut a release:** bump `appVersion` in `gradle.properties` (and/or push a `vX.Y.Z` tag),
  then push the tag — CI builds and publishes.

### Signing
- Release signing reads `KEYSTORE_PATH / KEYSTORE_PASSWORD / KEY_ALIAS / KEY_PASSWORD` from env.
  If `KEYSTORE_PATH` is unset, release falls back to the **debug** signing config.
- A **persistent keystore** is required so users can update over a prior install (commit `116c167`)
  — a fresh keystore each build would break in-app updates with a signature-mismatch.

### CI — `.github/workflows/build-release.yml`
- Triggers on `v*` tag push or manual dispatch.
- Restores the keystore from the `KEYSTORE_BASE64` repo secret, builds `assembleRelease`, renames
  the universal APK to **`YTune.apk`**, uploads it as an artifact, and on a tag push creates a
  GitHub Release with auto-generated notes + the APK attached.
- ⚠️ **Security note:** the keystore passwords are **hardcoded in plaintext** in the workflow
  (`KEYSTORE_PASSWORD: ytune2026`, `KEY_PASSWORD: ytune2026`, alias `ytune`). The keystore content
  itself is a secret, but the passwords are not. See §7.

---

## 5. Key files map (for the next person)

```
app/src/main/kotlin/com/github/musicyou/
├── MainApplication.kt              # cookie/visitorData restore on startup
├── ui/navigation/Navigation.kt     # Login route → persists cookie + accountInfo
├── ui/screens/login/LoginScreen.kt # WebView Google login + cookie capture
├── ui/screens/settings/
│   ├── AccountSettings.kt          # logged-in profile UI + sign-out
│   └── About.kt                    # in-app updater (check / download / install)
├── service/
│   ├── PlayerService.kt            # core playback (mostly inherited from Music You)
│   └── PlayerMediaBrowserService.kt# Android Auto browse tree + transport controls
└── utils/Preferences.kt            # ytmCookieKey, ytmAccountNameKey/Email/Photo, ytmVisitorDataKey

innertube/src/main/kotlin/com/github/innertube/
├── Innertube.kt                    # two HTTP clients + SAPISIDHASH auth
├── requests/AccountPage.kt         # accountInfo() — name/email/photo
├── requests/LibraryPage.kt         # libraryPlaylists() — paged playlist sync
└── requests/VisitorData.kt         # visitorData() fetch

github/src/main/kotlin/com/github/api/
├── GitHub.kt                       # getLastestRelease()
├── Release.kt                      # release model + apkDownloadUrl
└── ReleaseService.kt               # Releases API call
```

---

## 6. Current state

- The released app is **v2.2.1**. The working tree contains the **2.3.0 reliability update**;
  it is not a published release until committed, tagged, and built by CI.
- 2.3.0 moves stream recovery below ExoPlayer into `RecoveringDataSource`. URL resolution and
  bounded fresh-URL retries happen upstream of `CacheDataSource`, reopening at the exact unread
  byte and preserving remaining range length. Cached playback does not contact the network.
- Playback no longer auto-skips tracks after errors. The sleep timer stops the service without
  killing the process; service coroutines and audio device callbacks are cleaned up.
- Google Sign-In, playlist sync, in-app updater, Android Auto, and deep links remain implemented.
  The updater now rejects HTTP-error, empty, and incomplete downloads.
- Signed releases use CI with a persistent keystore; in-app self-update works against GitHub
  Releases. Signing credentials now come from repository secrets.
- `origin/copilot/review-app-good-to-have-enhancements` is an old spike based on pre-2.2 code;
  do not merge it wholesale. Its playback changes would undo the newer recovery work.

---

## 7. Known issues, risks & suggested next steps

**Correctness / maintenance**
1. **Package name still `com.github.musicyou`** — the rebrand was cosmetic. If a clean YTune
   identity is wanted (and to avoid collisions/confusion), a package rename is a sizeable but
   worthwhile refactor. Note `applicationId` is also `com.github.musicyou` — changing it is a
   breaking change for existing installs (would require a migration / fresh install).
2. There is no automated test suite or attached-device playback soak test. After installing 2.3.0,
   verify several long tracks, seeking, network toggling, cached offline playback, and Android Auto
   on real hardware before publishing.

**Auth fragility (highest-risk area)**
3. The login + cookie-capture flow depends on **undocumented YouTube/Google redirect behavior and
   page internals** (`window.yt.config_.VISITOR_DATA`, SAPISIDHASH, multi-domain cookie scan).
   Google changes these periodically; expect to re-test sign-in after any YTM web change. The
   `innertube` request parsers already have defensive null-handling (commits note "tolerate
   renderer placement changes") — keep that pattern.
4. Cookies are stored in **plain SharedPreferences**. Consider `EncryptedSharedPreferences` /
   Keystore-backed storage for the auth cookie.

**Security / release hygiene**
5. The current workflow reads signing credentials from secrets, but older public revisions exposed
   the keystore passwords. Do not rotate the key lightly — a new key breaks self-update for existing
   users; plan a migration if rotation is needed.
6. The updater checks transport, status, and completeness; Android still enforces APK signatures.
   A published SHA-256 asset and in-app verification would add protection against a bad release asset.

**Legal**
7. This streams from YouTube Music without API authorization and uses Google account cookies —
   it violates YouTube/Google ToS (the README disclaimer acknowledges no affiliation). Keep this in
   mind for any distribution decisions; it cannot go on Google Play.

**Good first improvements (low-risk)**
- Surface update-available state proactively (badge on Settings) instead of only on manual check.
- Wire `generate_release_notes` to the curated CHANGELOG.
- Triage / merge or close the stale `copilot/*` enhancement branch.

---

## 8. Quick orientation for review

If you (Fable) are reviewing for improvements, the **three areas that carry the most YTune-specific
risk and reward** are, in order:

1. **`innertube/Innertube.kt` + `LoginScreen.kt` + `Navigation.kt` login wiring** — the auth path.
   Most likely to break from upstream YouTube changes; also where security hardening matters most.
2. **`About.kt` updater + the CI workflow + signing** — release/update integrity and the hardcoded
   passwords.
3. **`PlayerMediaBrowserService.kt`** — Android Auto correctness (recently completed in PR #2;
   verify transport controls and the `isAllowedClient` gating on real head units).

Everything else (`PlayerService`, the Compose screens, Room DB, kugou lyrics) is largely inherited
from Music You and is comparatively stable.
