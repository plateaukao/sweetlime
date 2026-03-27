# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Sweet LIME is a fork of LIME HD - a lightweight Android IME (Input Method Editor) for Traditional Chinese input. Changes from the original: removed ads, removed Google Drive/Dropbox integration, removed unused local libraries, improved UX. Published on F-Droid. Licensed under GPL v3.

Application ID: `info.plateaukao.sweetlime` (differs from Java package `net.toload.main.hd`).

## Build Commands

All commands run from `LimeStudio/`:

```bash
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build (with ProGuard minification)
./gradlew assembleReleaseDebuggable  # Release without minification, debuggable
./gradlew installDebug           # Build and install debug APK to connected device
./gradlew connectedAndroidTest   # Run instrumented tests (requires connected device)
```

No unit tests exist. The only test file (`ApplicationTest.java`) is an empty boilerplate instrumented test. All validation is done through manual testing on device.

## Architecture

Single-module Android app (`LimeStudio/app`). Source: `app/src/main/java/net/toload/main/hd/`. Almost entirely Java with one Kotlin file (`QueryDispatcher.kt`).

### Input Flow: Keypress → Candidate → Commit

```
LIMEKeyboardView (touch/key event)
  → LIMEService.onKey() → handleCharacter() [appends to mComposing]
    → updateCandidates() → QueryDispatcher.launchQuery() [coroutine on IO thread]
      → SearchServer.getMappingByCode()
        → cache hit? return cached results
        → cache miss? → LimeDB.getMappingByCode() [SQL: WHERE code LIKE 'x%']
      → CandidateView.setSuggestions(List<Mapping>)
        → user taps candidate → pickHighlightedCandidate()
          → LIMEService.commitText() → InputConnection.commitText()
```

### Core Classes

- **`LIMEService.java`** — Main `InputMethodService`. Very large (~42KB) monolithic class that wires keyboard input, candidate selection, and IME lifecycle together. Key fields: `SearchServer SearchSrv`, `QueryDispatcher queryDispatcher`, `StringBuilder mComposing`.
- **`SearchServer.java`** — Query and cache layer. Uses `ConcurrentHashMap` caches (`cache`, `engcache`, `emojicache`). Prefetches single-key results (a-z, 0-9) on IM switch. Has smart phrase-building via `makeRunTimeSuggestion()` that composes phrases from partial matches.
- **`LimeDB.java`** (in `limedb/`) — SQLite adapter. Static shared `db` instance across the app. Core query: `getMappingByCode(code, isCompact, getAllRecords)`. Each input method has its own table (e.g., `phonetic`, `cj`, `dayi`, `array`). Columns: `_id`, `code`, `word`, `score`, `basescore`, `code3r` (no-tone variant for phonetic).
- **`DBServer.java`** — Database manager for import/export/reset operations. Not used in the query path.
- **`QueryDispatcher.kt`** — Kotlin coroutine wrapper (`CoroutineScope(SupervisorJob())`) for async candidate queries, preventing UI thread blocking.
- **`LIMEKeyboardSwitcher.java`** — Manages switching between input methods.
- **`Lime.java`** (in `global/`) — Constants for all supported IMs and keyboard types.

### Sub-packages

- `keyboard/` — Custom keyboard view rendering, touch tracking (`PointerTracker`, `SwipeTracker`), key detection
- `candidate/` — Candidate bar UI: `CandidateView`, `CandidateExpandedView`, `CandidateInInputViewContainer`
- `limedb/` — SQLite helpers, Traditional→Simplified conversion (`LimeHanConverter`), emoji handling (`EmojiConverter`)
- `data/` — Model classes: `Mapping` (the primary candidate data object), `Im`, `Keyboard`, `Word`, `Related`, `ChineseSymbol`
- `ui/` — Dialogs and fragments for managing input methods, keyboard layouts, import/export
- `limesettings/` — Settings/preferences activity (`LIMEPreferenceHC`)
- `global/` — Constants, `LIMEPreferenceManager`, `LIMEUtilities`
- `tools/` — `FileUtil`, `Stemmer`

### Database Initialization

On first launch, `LimeSQLiteOpenHelper` copies the preloaded `lime.db` from `res/raw/lime` to the app's databases directory. The static `db` instance is shared across SearchServer and DBServer. Emoji and Han conversion databases are separate files (`emoji.db`, `hanconvertv2.db`).

### Supported Input Methods

Phonetic/Zhuyin (BPMF), Pinyin, Cangjie (CJ/CJ5/SCJ), Dayi, Array (10/30), Hsu, Wubi, EZ, Eten, and user-defined custom IMs. Each IM has a corresponding table in `lime.db` and keyboard layout XML in `res/xml/`.

### Keyboard Layouts

Defined as XML in `res/xml/` (e.g., `lime.xml`, `lime_phonetic.xml`, `lime_hsu.xml`). The `@xml/method` metadata in `AndroidManifest.xml` declares the IME service configuration.

## SDK & Build Config

- `compileSdk 34`, `targetSdk 33`, `minSdk 21`
- Java 8 source/target compatibility, Kotlin JVM target 1.8
- Kotlin 1.9.22, Android Gradle Plugin 8.1.4
- Kotlinx Coroutines 1.7.3
- AndroidX enabled
- ProGuard enabled for release builds (minimal rules: only preserves inner class attributes)
