# Changelog

## 2.0.0 — 2026-10-26

Modernization of the original Full Sail Async Timer (v1.0). The app does
the same job — count down from a user-entered minutes/seconds value —
with none of the original's lifecycle bugs.

This public repository is a squashed export of that modernization: the
code and tests below are the v2.0.0 tree, in a single commit, without
the earlier private history.

### Changed

- Rewrote in Kotlin with Jetpack Compose and Material 3; the XML layout,
  AppCompat Activity, and ConstraintLayout are gone.
- Replaced the deprecated `AsyncTask` with a coroutine countdown owned by a
  `ViewModel` and exposed as a `StateFlow`. Rotating the phone no longer
  leaks the old Activity or loses the running timer, and a timer that is
  still running when the process is recreated resumes with its remaining time.
- Replaced shouting Toast messages with inline validation errors and a
  status line. Empty, zero, negative, and non-numeric input are all rejected
  with one clear message each (the original could show two Toasts at once).
- Renamed the application ID and package from `com.example.lyonsjustin_ce06`
  to `com.enonymous1.asynctimer`, and the app name to Async Timer.
- Toolchain: AGP 8.13.0, Gradle 8.14.3, Kotlin 2.2.0, Java 17,
  compile/target SDK 36, minSdk 24, Kotlin DSL build scripts, and a version
  catalog (`gradle/libs.versions.toml`).
- Release builds now use R8 shrinking (`minifyEnabled`, `shrinkResources`).
- Removed the assignment TODO comments, the `.idea` directory (now ignored),
  and the stale "GitHub Preparation" instructions from the README.

### Added

- Real tests: duration parsing/formatting, the countdown engine driven by
  virtual time (countdown, cancel-keeps-remaining, restart), ViewModel
  validation and input filtering, a package-name instrumentation test, and
  deterministic Compose UI tests for the initial state and empty-input error.
- GitHub Actions CI: unit tests plus a debug build on every push and pull
  request to `main`.
- MIT LICENSE, this changelog, and a `.gitattributes` file normalizing
  line endings to LF.

### Notes and non-goals

- Seconds above 59 are still accepted and normalized, as in v1.0: `1:90`
  is a 150-second timer, displayed as `02:30`.
- After a timer finishes, the entered values are kept so the same timer can
  be started again; v1.0 reset the fields to `00:00`.
- The timer does not finish while the app is killed, and it does not post a
  notification. That would need a foreground service or alarm, which is a
  feature, not part of this modernization.

## 1.0 — 2026-09-07

Original release: Java 8, AGP 8.0.1, SDK 33, Views + `AsyncTask` countdown.
