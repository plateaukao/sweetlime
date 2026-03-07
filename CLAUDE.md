# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Sweet LIME is a fork of LIME HD - a lightweight Android IME (Input Method Editor) for Traditional Chinese input. Changes from the original: removed ads, removed Google Drive/Dropbox integration, removed unused local libraries, improved UX. Published on F-Droid.

## Build Commands

All commands run from `LimeStudio/`:

```bash
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build (with ProGuard minification)
./gradlew installDebug           # Build and install debug APK to connected device
./gradlew connectedAndroidTest   # Run instrumented tests (requires connected device)
```

## Architecture

The project is a single-module Android app (`LimeStudio/app`). Source code is in `app/src/main/java/net/toload/main/hd/`.

**Core service:**
- `LIMEService.java` — Main `InputMethodService` entry point. Handles all keyboard input, candidate selection, and IME lifecycle. This is the central class that wires everything together.
- `LIMEKeyboardSwitcher.java` — Manages switching between input methods (Zhuyin, Cangjie, Dayi, ABC, etc.)
- `SearchServer.java` — Word/candidate searching and ranking logic
- `DBServer.java` — Database access layer

**Sub-packages:**
- `keyboard/` — Custom keyboard view rendering, touch tracking (`PointerTracker`, `SwipeTracker`), key detection
- `candidate/` — Candidate bar UI: `CandidateView`, `CandidateViewContainer`, `CandidateExpandedView`, `CandidateInInputViewContainer`
- `limedb/` — SQLite helpers, Traditional→Simplified conversion (`LimeHanConverter`), emoji handling (`EmojiConverter`)
- `data/` — Model classes: `Im`, `Keyboard`, `Mapping`, `Word`, `Related`, `ChineseSymbol`
- `ui/` — Dialogs and fragments for managing input methods, keyboard layouts, import/export
- `limesettings/` — Settings/preferences activity
- `global/` — Constants (`LIME.java`), `LIMEPreferenceManager`, `LIMEUtilities`
- `tools/` — `FileUtil`, `Stemmer`

**Databases** (shipped as raw resources in `res/raw/`):
- `lime.db` — Main dictionary and IM definitions
- `emoji.db` — Emoji characters
- `hanconvertv2.db` — Traditional→Simplified conversion tables
- `blank.db`, `blankrelated.db` — Templates for user-created custom IMs

**Keyboard layouts** are defined as XML in `res/xml/` (e.g., `lime.xml`, `lime_phonetic.xml`, `lime_hsu.xml`, `lime_cj_number.xml`).

## SDK & Build Config

- `compileSdk 34`, `targetSdk 33`, `minSdk 21`
- Java 8 source/target compatibility
- AndroidX enabled
- ProGuard enabled for release builds
