# SkyBox TV Security

## Requirements

The following security requirements are mandatory and must be implemented and verified before any release:

### TDLib Key and Database Protection

- **Keep TDLib's database and encryption key in app-private storage only.**
  - Store the TDLib encryption key in the app's private `SharedPreferences` or `EncryptedSharedPreferences`
  - Never expose the key in build configuration, build logs, or source code
  - The TDLib database file (`tdlib-*) must reside in the app's private data directory (not external storage)
  - Set appropriate `MODE_PRIVATE` on any Java/Kotlin `SharedPreferences` used for key storage

- **Never log phone verification codes, passwords, session secrets, or encryption keys.**
  - Remove any `Log.d`, `Log.e`, or print statements that could emit sensitive data
  - Use `android.util.Log` with WARNING or ERROR level only for non-sensitive diagnostic info
  - Add `Log.isLoggable` checks before any debug logging

### Account Data Handling

- **Provide an explicit logout action** that clears local account data:
  - Clear TDLib session (reset authorization state)
  - Delete the TDLib database and encryption key from storage
  - Clear Room database of indexed media metadata (or mark as purged)
  - Clear any cached thumbnails or poster images from local storage

- **Handle revoked sessions and authorization expiry** with a clear UI state:
  - Detect when TDLib returns `error_code: 401` (Unauthorized) or similar expiry errors
  - Present re-authentication screen automatically
  - Do not crash or show raw error codes to the user

- **Do not embed account credentials in source code or build configuration.**
  - No `gradle.properties` with API keys, bot tokens, or phone numbers
  - Use Gradle variables or environment variables for CI builds if needed
  - Prompt user to enter phone number and verification code at runtime

### Data Privacy

- **Indexed media metadata is local only.** Room database lives on the device; nothing is uploaded to external services unless the user explicitly initiates it.
- **Telegram chat selection is user-driven.** The app does not automatically scan or join chats; the user selects which chats to index.
- **Thumbnail/poster images loaded via Coil** should have reasonable cache limits configured to prevent uncontrolled disk growth.

### Security Checklist for Release

- [ ] TDLib database path is internal/private (not on shared/external storage)
- [ ] Encryption key is cleared on logout
- [ ] No sensitive data in Logcat-outputted strings
- [ ] Login screen prompts for phone number at runtime (not hardcoded)
- [ ] Verification code is not logged or stored persistently
- [ ] `verifier_verify_adb_installs 0` is only set temporarily during install, restored immediately after
- [ ] Release APK does not contain debuggable flag (`android:debuggable="false"`)
- [ ] ProGuard/R8 rules do not keep sensitive strings obfuscated in a reversible way
- [ ] Explicit logout UI element present in Settings or profile screen

### Threat Model Summary

| Threat | Mitigation |
|--------|-----------|
| TDLib key exposure | Store in encrypted SharedPreferences; clear on logout |
| Malicious ADB install | Use `verifier_verify_adb_installs`; only temporary during install |
| Session expiry | Detect error 401; prompt re-auth; do not crash |
| Uncontrolled disk growth | Configure Room cache limits; Coil cache size cap |
| Data leakage via logs | Remove all sensitive Log calls; scrub before release |
| Credential embedding | Runtime-only input; no build-time secrets |