# SkyBox TV Testing

## Test Matrix

| Test Category | Scope | Priority |
|--------------|-------|----------|
| **Unit Tests** | ViewModel use cases, repository implementations, Room DAOs/migrations | P0 — run on every build |
| **Integration Tests** | TDLib initialization, authorization flow, chat browsing, message indexing | P0 — requires TDLib native library |
| **UI Tests** | Compose navigation, D-pad focus, player controls (play/pause/seek), Continue Watching | P1 — requires TV emulator or physical device |
| **Streaming Tests** | Playback start/buffer/seek, track selection, recovery from network interruption | P1 — measures startup delay, buffering, disk usage |
| **Security Tests** | Logout clears all data, encryption key removal, no sensitive logs, ADB verifier restore | P0 — manual verification |
| **Build Tests** | Gradle assembleRelease, APK signing, install via adb | P0 — CI pipeline |

## Unit Testing

- **ViewModel tests:** Use `view-ktx` `ViewModelRule` or `InstantTaskExecutorRule`; mock repositories
- **Repository tests:** Use `room-test` library; provide in-memory SQLite database
- **DAOs and migrations:** Verify schema auto-validation; test migration paths via `RoomDatabaseTestRule`
- **Mock TDLib:** Where TDLib dependencies exist, use stub implementations that return predefined responses

## Integration Testing

### TDLib Initialization

1. Load native TDLib library; verify ABIs (armeabi-v7a, arm64-v8a, x86, x86_64)
2. Initialize TDLib with empty string session (no phone number yet)
3. Verify session persistence across app restarts when encryption key is stored
4. Test logout resets session and clears database

### Authorization Flow

1. User enters phone number → TDLib `setAuthenticationPhoneNumber`
2. User enters verification code → TDLib `setAuthenticationCode`
3. Completion of authorization → TDLib `setAuthorizationState` with `authorized`
4. Verify session can be saved and restored on next launch

### Chat and Media Browsing

1. Select chat → TDLib `getChatHistory` with proper `limit`
2. Messages containing `photo`, `document`, or `video` are surfaced
3. Indexing into Room database; verify stable IDs and metadata
4. Incremental indexing — re-running does not duplicate entries

## UI Testing

### Navigation

- [ ] D-pad Up/Down/Left/Right moves focus between Compose TV elements
- [ ] Center/Select button activates focused element
- [ ] Back button navigates up the hierarchy; exits player properly (pause vs. exit)
- [ ] Focus order is logical and follows TV-friendly patterns

### Player Screen

- [ ] Video opens and starts playback within reasonable startup delay
- [ ] Pause/resume works via remote control
- [ ] Seek bar is draggable to arbitrary positions (if seeking supported)
- [ ] Audio track switching (if multiple tracks available)
- [ ] Subtitle track selection (if subtitles embedded or SRT)
- [ ] Progress is saved when user exits; resume from same position on re-entry

### Home and Library

- [ ] Home screen shows horizontal rows of poster cards
- [ ] Library lists videos from selected chat with title/thumbnail
- [ ] Search filter narrows results by matching metadata
- [ ] Continue Watching row shows partially watched videos with resume button

## Streaming Tests

### Playback Measurement

| Metric | Method | Acceptance Criteria |
|--------|--------|---------------------|
| **Startup delay** | Time from `onPlayerOpened` to first video frame | < 8 seconds on physical TV, < 15 seconds on emulator |
| **Buffering events** | Count of `onBuffering` callbacks during 2-minute playback | ≤ 3 events |
| **Total buffer time** | Sum of buffer durations | < 5 seconds total |
| **Seek accuracy** | Seek to 30% position; verify position within ± 5 seconds | ± 5s accuracy |
| **Seek speed** | Time from seek command to new position | < 3 seconds |
| **Network interruption** | Stop network mid-playback; resume after 10s | Recovery within 5s or graceful stop |

### Disk and Memory Monitoring

- **Temp file size** after 5-minute playback should not exceed reasonable limit (e.g., 200MB for tested files)
- **Room cache size** should not grow unboundedly; configure `fallbackSupportLimit` or similar
- **ExoPlayer memory** should stay within expected bounds per Android TV guidelines

## Manual Testing Procedures

### On Physical Android TV/Google TV Device

1. `adb connect 192.168.1.12:5555`
2. `settings put global verifier_verify_adb_installs 0`
3. `adb install -r app/build/outputs/apple/release/app-release.apk`
4. `settings put global verifier_verify_adb_installs 1`
5. `am start -n com.skyboxtv/.MainActivity`
6. Follow on-screen prompts: authorize Telegram, select chat, browse videos, play, seek, resume

### On Android TV Emulator

1. Create AVD with `android-34` platform, TV hardware profile
2. `adb install -r app/build/outputs/apple/release/app-release.apk`
3. Launch app; use emulator's control panel for remote input (Keyboard arrows = D-pad)
4. Run through same user flows as physical device

### Troubleshooting Test Failures

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| `No native libraries found` | TDLib ABI not included in build | Add all required ABIs to `android { ndk { abiFilters } }` |
| Playback stalls immediately | DataSource cannot read file | Verify TDLib download path; test with small test file first |
| `Session expired` error | TDLib key lost after restart | Persist encryption key in `EncryptedSharedPreferences`; restore on launch |
| No videos found | Chat not indexed | Verify TDLib `getChatHistory` returns messages; check Room DAO inserts |
| Remote navigation lag | Focus traversal not optimized | Use `focus { direction }` modifiers; test with `FocusNode` |
| APK install rejected | Debuggable flag in release build | Ensure `android:debuggable="false"`; clean rebuild |

## CI / CD Testing

- **GitHub Actions** (or equivalent): Run unit tests and lint on every PR
- **Disable device-requiring tests** in CI (UI, streaming, security) — mark as `requires: [device]`
- **Code coverage minimum:** 80% for unit tests (viewModel, repository, DAO levels)
- **Build APK** as a separate step; upload as artifact for manual testing on device