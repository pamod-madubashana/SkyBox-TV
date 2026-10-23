# AGENTS.md — SkyBox TV

Guide for AI assistants working on **SkyBox TV**, a native Android TV / Google TV media-streaming application that lets users browse and play video files accessible through their Telegram account.

Start every session by reading this file.

## The goal (user's words)

"Create an app to watch movies on a TV." The user is **non-technical** — explain things in plain words, keep replies short and friendly, and always ask before anything that changes their TV or computer.

## Product direction

SkyBox TV turns a user's Telegram-hosted video library into a comfortable, remote-controlled living-room experience. It should feel like a modern streaming application: cinematic home screen, horizontal content rows, media details, Continue Watching, and a full-screen player.

SkyBox TV is a **media player for content the user can access through Telegram**. It is not a content service and does not automatically provide licensed movies, artwork, synopses, or episode metadata.

## Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose for TV
- **Playback**: AndroidX Media3 ExoPlayer
- **Telegram integration**: TDLib through its Android/Java interface
- **Local catalog**: Room
- **Settings**: DataStore
- **Image loading**: Coil
- **Application pattern**: MVVM plus repository interfaces
- **Dependency injection**: Constructor injection initially
- **Background work**: Coroutines; WorkManager only for work that must survive process death
- **Backend**: None required for initial release

## Architecture

The TV UI must not call TDLib directly. It talks to domain use cases and repositories. Playback must not depend on UI screens. The Telegram implementation must be replaceable behind an interface.

```text
Android TV / Google TV
        |
        v
Compose for TV screens
        |
        v
ViewModels -> Domain use cases
        |
        +-------------------------+
        |                         |
        v                         v
Catalog / Room              Playback coordinator
        |                         |
        v                         v
Telegram repository         Media3 ExoPlayer
        |                         |
        v                         v
TDLib adapter               TelegramMediaDataSource
        |                         |
        v                         v
Telegram MTProto            Bounded byte cache
```

## Key technical risk: Telegram streaming

This is the highest-risk part of the project. Prove it before building the complete home screen. A Telegram file is not automatically a regular HTTP URL. Media3 reads bytes through a `DataSource`; TDLib exposes file-management and download operations. The implementation must bridge these interfaces.

### Go/no-go gate

Do not proceed to full UI implementation until the streaming behavior is measured. If TDLib cannot provide reliable incremental/random-access reads, compare alternatives: TDLib-managed download to temp file, local-file bridge over growing download, or separate streaming service.

## Git commit and PR rules

- **Primary author**: All commits and PRs must be authored by the primary GitHub account `pamod-madubashana` (email: `contact@pamod.is-a.dev`).
- **Co-author**: Every commit and PR must include a co-author line for the secondary account:
  ```
  Co-authored-by: The-KLYRO <105803100+The-KLYRO@users.noreply.github.com>
  ```
- This applies to all commits, including fixups, squashes, and merge commits.
- Never commit or open a PR without the co-author line.

- Shell commands must go through **`rtk`** (`rtk run "..."` = cmd.exe on Windows; no `\"` inside args; `&` in args must be `^&`; avoid chained commands). File reads/edits use the dedicated tools, not the shell.
- Git: commit only on explicit request; imperative mood; one concern per commit; never commit secrets or unrelated files.
- Run lint/format/tests after edits.

## Android build toolchain on this machine

- JDK **17** Temurin: `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot` (JDK 25 hits a d8 dexer NPE — do not use).
- Android SDK: `E:\Softwares\AndroidSdk`.
- Platform: android-34, build-tools 34.0.0.
- Gradle 9.4.1 available via wrapper dists.

## Device/TV procedures

- TV reachable via `adb connect 192.168.1.12:5555` (DHCP — IP may change; connection drops when the TV sleeps; ask the user to keep the TV awake).
- Before `adb install -r`: `settings put global verifier_verify_adb_installs 0` → install → restore `1`.
- Restart an app with `am force-stop <pkg>` + `am start`.

## Security requirements

- Keep TDLib's database and encryption key in app-private storage.
- Never log phone verification codes, passwords, session secrets or encryption keys.
- Do not embed account credentials in source code or build configuration.
- Provide an explicit logout action and clear local account data according to the user's confirmation.
- Handle revoked sessions and authorization expiry with a clear UI state.

## Implementation roadmap

### Phase 0 — Requirements and environment
- Create a native Android project.
- Configure Kotlin, Compose for TV and a reproducible Gradle build.
- Add an Android TV launcher activity and manifest configuration.
- Run on a TV emulator.
- Verify that a standard D-pad remote can navigate the initial screen.

### Phase 1 — TDLib integration
- Validate TDLib Android/JNI availability and supported ABIs.
- Integrate the native library and required runtime dependencies.
- Implement authorization state handling.
- Browse chats and inspect messages containing media.
- Verify session persistence and logout.

### Phase 2 — Streaming proof of concept
- Create a minimal Media3 player screen.
- Implement a prototype Telegram-backed `DataSource` or a validated local-file bridge.
- Measure startup delay, buffering, memory and disk usage.
- Test seeking, cancellation, interrupted connectivity and recovery.

### Phase 3 — Catalog and local database
- Add Room entities, DAOs, migrations and repository implementations.
- Index selected chats incrementally.
- Store media metadata and stable IDs.
- Implement paging, local search and source filters.

### Phase 4 — Playback state and controls
- Implement player lifecycle management.
- Save and restore progress.
- Implement seeking and audio/subtitle track selection where supported.

### Phase 5 — Netflix-style UI
- Build Home, Library, Sources, Search, Details and Settings.
- Add poster cards, horizontal rows and focus animations.

### Phase 6 — Hardening and release
- Test on an emulator and at least one physical Android TV/Google TV device.
- Validate codec and subtitle behavior.
- Verify cache limits and storage cleanup.
- Validate launcher discovery, APK signing and release packaging.

## Definition of done for the first usable version

The first usable version is complete when a user can:
1. Install SkyBox TV on a supported Android TV or Google TV device.
2. Authenticate a Telegram account.
3. Select a chat containing videos.
4. Browse the available media in a TV-friendly library.
5. Open a media details screen and start playback.
6. Seek and control playback using only the remote.
7. Resume a video from saved progress.
8. Recover gracefully from common network and session failures.
9. Use bounded local caching without uncontrolled disk growth.
10. Log out and clear locally retained data.

## Recommended first engineering task

Create a minimal native Android TV project with Kotlin and Compose for TV, TDLib Android integration, a simple authorization-state screen, a screen that lists video messages from one selected chat, and Media3 playback using a deliberately small, testable streaming adapter. Do not build the full home screen first. The streaming proof of concept is the decision point.