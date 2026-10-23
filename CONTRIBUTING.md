# Contributing to SkyBox TV

Thank you for considering contributing! Please read these guidelines to help us maintain the project.

## Development Setup

1. **Fork the repository** and clone your fork locally
2. **Install JDK 17** (Temurin Adoptium) and **Android SDK** (android-34, build-tools 34.0.0)
3. **Install Android Studio** or use command-line `gradlew` for builds
4. **Set up TV device** — physical Android TV/Google TV or emulator with `android-34` profile
5. **Run `./gradlew sync`** to fetch dependencies

## Contribution Workflow

All commits and PRs must follow these rules:

### Commit Format

- **Primary author:** `pamod-madubashana` (contact@pamod.is-a.dev)
- **Co-author line required:** `Co-authored-by: The-KLYRO <105803100+The-KLYRO@users.noreply.github.com>`
- Use **imperative mood** in commit messages (e.g., "Add Telegram auth screen", not "Added Telegram auth screen")
- **One concern per commit** — don't bundle unrelated changes
- Inspect `git status` and `git diff` before committing

### Pull Request Process

1. Create a feature branch from `main`: `git checkout -b feature/amazing-feature`
2. Make changes and commit with proper attribution
3. Push to both GitHub and GitLab remotes
4. Open GitHub PR first (base = `main`)
5. Create identical GitLab MR with the same title and description
6. Both platforms: squash merge on merge, keeping `(#N)` suffix
7. After merge, sync local: `git checkout main && git pull origin main && git branch -d <feature-branch>`

### Code Style

- **Kotlin:** Follow project conventions; run `./gradlew ktlintCheck` / `./gradlew formatCheck`
- **Compose:** Use TV-friendly focus traversal; D-pad navigation patterns
- **Imports:** Use `androidx.*` versions matching `build-tools 34.0.0`
- **No hardcoded secrets:** No API keys, phone numbers, or passwords in source or build config

### Review Process

- PRs require at least one maintainer review before merge
- Ensure all checks pass (lint, format, unit tests)
- Address any requested changes promptly
- Squash merge format must match the project standard

### Branching Conventions

- **`main`** — production-ready state; merge via squash
- **`feature/*`** — new features; branch from `main`
- **`bugfix/*`** — bug repairs; branch from `main`
- **`hotfix/*`** — critical patches; branch from latest release tag

### Reporting Issues

- Search existing issues before filing new ones
- Use clear title and steps to reproduce
- Include TV model, Android version, and TDLib version if related to playback
- Tag with `question`, `bug`, or `enhancement` as appropriate

### Ask Before Expanding Scope

- If your change expands beyond the current roadmap phase, discuss with maintainers first
- New major features may require re-evaluation of the go/no-go gates (especially streaming)
- Keep changes focused; avoid bundling unrelated fixes

## Thank You

Your contribution helps make SkyBox TV better for everyone. By following these guidelines you help keep the codebase consistent, secure, and maintainable.

**Primary author:** pamod-madubashana <br>
**Co-author:** The-KLYRO