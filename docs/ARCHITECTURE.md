# SkyBox TV Architecture

## Overview

SkyBox TV is structured as a layered application following the **MVVM (Model-View-ViewModel)** pattern with repository interfaces, designed specifically for Android TV / Google TV with Compose UI.

```
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

## Layer Description

### UI Layer (Compose for TV)

- **Screen composables:** Home, Library, Sources, Search, Details, Player
- **Navigation:** D-pad focus handling, directional focus system
- **UI state:** ViewModel-sourced state, triggered via UI events
- **Theme:** Light/dark mode, focus animations, poster card visuals

### ViewModel Layer

- **Event-driven:** UI emits events, ViewModel processes and updates state
- **Survival:** ViewModels survive configuration changes via `ViewModelStore`
- **Dependency injection:** Constructor-injected repositories and use cases

### Domain Layer (Use Cases)

- **Single responsibility:** Each use case encapsulates a specific business operation
- **Pure Kotlin:** No Android dependencies; testable in isolation
- **Coordinates:** Repositories, data sources, and playback coordination

### Repository Layer

- **Abstraction:** Interfaces defining data operations (catalog, playback, Telegram)
- **Implementations:** Room for local catalog, TDLib adapter for Telegram, Media3 for playback
- **Paging integration:** Indexed media listings with local search and source filters

### Data Layer

- **Room database:** Entities, DAOs, and migrations for media metadata and stable IDs
- **TDLib adapter:** Wraps native Telegram library; handles authorization, chat browsing, message inspection
- **Media3 ExoPlayer:** Player lifecycle management, seeking, track selection
- **TelegramMediaDataSource:** Byte-stream bridge between ExoPlayer and Telegram file IDs

## Key Design Decisions

- **TV UI must not call TDLib directly.** UI talks to domain use cases and repositories.
- **Playback must not depend on UI screens.** Media coordinator is separate from screen logic.
- **Telegram implementation must be replaceable behind an interface.** Enables testing and future replacement.
- **Bounded local caching.** Room with configured cache limits prevents uncontrolled disk growth.
- **Coroutines for background work.** `WorkManager` only for work that must survive process death.

## Navigation

- **Home** -> **Library** -> **Chat selection** -> **Media list** -> **Details** -> **Player**
- **Back** key properly handles player lifecycle (pause vs. exit)
- **Focus:** Directional focus network between Compose TV elements