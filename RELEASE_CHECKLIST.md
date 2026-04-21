# YTune Release Checklist

Use this checklist before tagging a new release. Complete every item in order.

---

## 1. Pre-release preparation

- [ ] All target issues and PRs for this milestone are merged
- [ ] `CHANGELOG.md` is updated with a summary of changes under the new version heading
- [ ] Version number follows semantic versioning (`MAJOR.MINOR.PATCH`)
- [ ] The app builds cleanly with no errors: `./gradlew assembleRelease`
- [ ] All CI checks on `main` are green

## 2. Smoke testing (real device or emulator)

- [ ] App launches without crashing on a clean install
- [ ] App upgrades cleanly from the previous release (no data loss)
- [ ] Sign-in / sign-out flow works correctly
- [ ] Home screen loads and Quick Picks populate
- [ ] Search returns results for songs, albums, artists, and playlists
- [ ] A song plays end-to-end (streaming + notification controls)
- [ ] Background playback continues after leaving the app
- [ ] Offline caching stores a song and plays it without network
- [ ] Synced lyrics display and scroll correctly
- [ ] Sleep timer starts, counts down, and stops playback
- [ ] Deep links (YouTube / YouTube Music URLs) open the correct screen
- [ ] Android Auto launches without errors (if a compatible head unit or emulator is available)
- [ ] Settings → About → Check for Updates finds the new release after tagging

## 3. Tagging the release

- [ ] Create the git tag from `main`: `git tag v<VERSION> && git push origin v<VERSION>`
- [ ] The `Build & Release APK` GitHub Actions workflow completes successfully
- [ ] The APK signature verification step in the workflow passes
- [ ] The release APK is attached to the GitHub Release page
- [ ] Release notes are accurate and free of typos

## 4. Post-release monitoring (first 48 hours)

- [ ] Monitor open issues for crash reports or regressions
- [ ] Verify the in-app update check points to the new release
- [ ] If a critical bug is found, open a hotfix branch from the tag and follow the same checklist

## 5. Rollback plan

If a critical issue is found after release:

1. Identify the last known-good tag
2. Create a hotfix branch: `git checkout -b hotfix/v<X.Y.Z> v<PREVIOUS_TAG>`
3. Apply the minimal fix, update `CHANGELOG.md`, and push
4. Tag the hotfix release and let CI publish it
5. Update any pinned references or documentation
