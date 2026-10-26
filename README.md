# Async Timer

A small Android countdown timer. Enter minutes and seconds, tap Start, and
watch it count down — stop it at any point to see the time that was left.

Originally a Full Sail assignment built on Java and `AsyncTask`; v2.0 rewrote
it in Kotlin with Compose and a ViewModel-owned coroutine timer, so rotation
no longer loses or leaks a running countdown. See [CHANGELOG.md](CHANGELOG.md)
for what changed and what was deliberately left out.

This public repository is a squashed export of that v2.0.0 modernization:
a single commit, with the earlier private history intentionally not included.

## Tech stack

- Kotlin 2.2.0, Jetpack Compose, Material 3
- ViewModel + StateFlow, Kotlin coroutines
- Android Gradle Plugin 8.13.0, Gradle 8.14.3 (via the wrapper), JDK 17
- compile/target SDK 36, minSdk 24
- JUnit 4, kotlinx-coroutines-test, Compose UI tests

The application ID is `com.enonymous1.asynctimer`.

## Features

- Enter a two-digit minute value, a two-digit second value, or both.
- Start a countdown and watch the remaining time in `MM:SS` format.
- Stop an active countdown and see the remaining time in a dialog.
- Clear, single inline errors for empty, zero, negative, or non-numeric input.
- Survives rotation; a still-running timer resumes after process recreation
  with its remaining time (see the non-goals note below).

## Folder structure

```text
async-timer/
├── .github/workflows/android.yml     # CI: unit tests + debug build
├── app/
│   ├── build.gradle.kts             # App module configuration
│   ├── proguard-rules.pro           # Release shrinker rules
│   └── src/
│       ├── androidTest/             # Instrumentation + Compose UI tests
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/enonymous1/asynctimer/
│       │   │   ├── MainActivity.kt    # Compose host, edge-to-edge
│       │   │   ├── TimerScreen.kt     # Material 3 UI
│       │   │   ├── TimerViewModel.kt  # State, validation, saved end time
│       │   │   ├── TimerEngine.kt     # Coroutine countdown, StateFlow
│       │   │   └── TimerLogic.kt      # Pure parse/format functions
│       │   └── res/                 # Strings, themes, icons, backup rules
│       └── test/                    # Local JVM unit tests
├── gradle/
│   ├── libs.versions.toml           # Version catalog
│   └── wrapper/                     # Gradle Wrapper configuration
├── build.gradle.kts                 # Root plugin configuration
├── gradle.properties                # AndroidX and Gradle settings
├── gradlew / gradlew.bat            # Gradle Wrapper scripts
└── settings.gradle.kts              # Project and module registration
```

## Requirements

- Android Studio with Android SDK 36 installed
- JDK 17 (Android Studio's bundled JDK works)
- An Android emulator or physical device running API 24 or newer

## Setup and run

1. Clone the repository and open the project directory in Android Studio.
2. Let Android Studio sync the Gradle project and install any requested
   SDK components.
3. Select an emulator or connected Android device.
4. Run the `app` configuration from Android Studio.

From a terminal at the project root, the debug build can also be created with:

```powershell
.\gradlew.bat assembleDebug
```

The generated APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
For a release build, use `assembleRelease` (R8 shrinking is enabled) or
`bundleRelease` for a Play Store bundle.

## Testing

Run local unit tests with:

```powershell
.\gradlew.bat testDebugUnitTest
```

Run connected-device instrumentation tests with:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Unit tests cover duration parsing/formatting, the countdown engine under
virtual time (countdown, cancel, restart), and ViewModel validation and
input filtering. The UI tests are deliberately deterministic — initial
state and empty-input error — and do not wait for real seconds to pass.

## Non-goals

The timer does not finish while the app is killed, and it does not post a
notification when it expires. That needs a foreground service or an alarm
plus notification permission, which is a feature on top of this app, not
part of the modernization. It also collects nothing, uses no network, and
requests no permissions.

## License

MIT — see [LICENSE](LICENSE).
