# SkyBox TV

A native Android TV / Google TV media-streaming application that lets users browse and play video files accessible through their Telegram account.

## Project Description

SkyBox TV turns a user's Telegram-hosted video library into a comfortable, living-room streaming experience. It functions as a media player for content the user can access through Telegram, providing a cinematic home screen, horizontal content rows, media details, Continue Watching, and a full-screen player.

**This is not a content service.** SkyBox TV does not provide licensed movies, artwork, synopses, or episode metadata automatically. It browses and plays videos that the user already has access to through their Telegram account.

## Features

- **Telegram Integration:** Browse chats and messages containing media via TDLib
- **Cinematic Home Screen:** Compose for TV with horizontal content rows and poster cards
- **Media Library:** Index and search videos from selected Telegram chats
- **Continue Watching:** Save and restore playback progress
- **Full-Screen Player:** Media3 ExoPlayer-based video playback with seek, pause, and resume
- **Audio/Subtitle Track Selection:** Where supported by the media
- **Bounded Local Caching:** Room database with controlled disk growth
- **User Authentication:** Telegram account sign-in with session persistence
- **D-Pad Navigation:** Full remote control support for Android TV/D-pad

## Requirements

- **Device:** Android TV or Google TV device (emulator or physical)
- **JDK:** 17 (Temurin Adoptium)
- **Android SDK:** android-34, build-tools 34.0.0
- **Gradle:** 9.4.1 (wrapper available)
- **Telegram Account:** Active Telegram account for authentication
- **Remote:** D-pad remote for navigation

## Build Instructions

1. Ensure JDK 17 is installed and available in PATH
2. Install Android SDK at the default location or configure ` ANDROID_HOME`
3. Sync Gradle dependencies: `./gradlew sync`
4. Build the release APK: `./gradlew assembleRelease`
5. Install on TV: `adb connect 192.168.1.12:5555 && settings put global verifier_verify_adb_installs 0 && adb install -r app/build/outputs/apple/release/app-release.apk && settings put global verifier_verify_adb_installs 1`

## Installation

1. Connect your TV to the same network as this machine
2. `adb connect 192.168.1.12:5555` (IP may change; TV must stay awake)
3. Run the app: `am start -n com.skyboxtv/.MainActivity`
4. First run will prompt for Telegram account authorization
5. Select a chat containing videos to browse the library

## Known Limitations

- Streaming behavior depends on TDLib capabilities; incremental/random-access reads not guaranteed
- Large video files may require TDLib-managed download to local temp file before playback
- Codec support varies by TV hardware; some formats may not be playable
- Session persistence across app restarts requires valid TDLib encryption key storage
- Network-interrupted playback may not recover gracefully without re-buffering
- Artwork, synopses, and metadata not provided automatically

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `adb connection refused` | Ensure TV is awake on same network; IPs via DHCP may change |
| `verifier_verify_adb_installs` error | Run `settings put global verifier_verify_adb_installs 0` before install, restore to `1` after |
| App crashes on launch | Check TDLib database and encryption key are in app-private storage |
| No videos found | Ensure selected chat contains media messages; chats must be indexed first |
| Playback buffering | Try TDLib download to temp file first; verify network stability |
| Remote navigation stuck | Verify D-pad focus; some Compose elements require explicit focus requests |
| Session expired | Log out and re-authenticate via the authorization screen |

---

For more information, see the `docs/` directory for architecture, streaming feasibility, security, and testing documentation.