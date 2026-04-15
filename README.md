# AutoMinder — Car Maintenance Reminder

[![Build](https://github.com/abdulghni490/Car-maintainence-reminder/actions/workflows/ci.yml/badge.svg?branch=latest-version-2)](https://github.com/abdulghni490/Car-maintainence-reminder/actions/workflows/ci.yml)

> Your personal car health companion. Track services, fuel, mileage, and get proactive reminders — fully offline-first.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.1.21 |
| UI | Jetpack Compose + Material 3 Expressive (BOM 2025.06.01) |
| Architecture | Clean Architecture + MVVM + Offline-First |
| DI | Hilt 2.55 (KSP only) |
| Database | Room 2.7.1 (KSP only, no destructive migrations) |
| Navigation | Navigation Compose 2.9.0 (type-safe sealed routes) |
| Background | WorkManager 2.10.0 |
| Storage | DataStore Preferences 1.1.2 |
| Image Loading | Coil 3.1.0 |
| Ads | AdMob 23.5.0 |
| Billing | Google Play Billing 7.1.1 |

- **minSdk**: 26 (Android 8.0)
- **targetSdk**: 36
- **Package**: `com.autominder.app`

## Prerequisites

- **Android Studio**: Meerkat or newer
- **JDK**: 17
- **Gradle**: managed via wrapper (`./gradlew`)
- **Google Services**: `app/google-services.json` (not committed — obtain from Firebase Console)
- **Local secrets**: `local.properties` (not committed — see below)

## Local Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/abdulghni490/Car-maintainence-reminder.git
   git checkout latest-version-2
   ```

2. **Create `local.properties`** in the project root (never commit this file):
   ```properties
   sdk.dir=C\:\\Users\\<YourUser>\\AppData\\Local\\Android\\Sdk
   ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX
   DEBUG_ADMOB_ID=ca-app-pub-3940256099942544~3347511713
   RELEASE_ADMOB_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
   KEYSTORE_PATH=path/to/release.keystore
   ```

3. **Place `google-services.json`** in `app/` (from Firebase Console).

4. **Open in Android Studio** → sync Gradle → run on device or emulator.

## Building

```bash
# Debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Compile check only (fast)
./gradlew compileDebugKotlin
```

## Project Structure

```
app/src/main/kotlin/com/autominder/app/
├── AutoMinderApp.kt          # @HiltAndroidApp entry point
├── MainActivity.kt           # Single-Activity host
├── ads/                      # AdMob integration
├── core/di/                  # Hilt modules (Database, Repository, Worker)
├── data/                     # Local DB (Room), DataStore, Repositories
├── domain/                   # Models, Use Cases, Repository interfaces
├── ui/                       # Compose screens, navigation, theme, components
├── widget/                   # Glance home-screen widget
└── worker/                   # WorkManager workers + BootReceiver
```

## Development Phases

| Phase | Status | Description |
|---|---|---|
| P1 — Foundation | ✅ Done | Room DB, Hilt, Navigation, Theme |
| P2 — Vehicle CRUD | ✅ Done | Add/Edit/Delete vehicles |
| P3 — Service Tracking | ✅ Done | Service history, status engine |
| P4 — Fuel Tracking | ✅ Done | Fuel entries, efficiency calc |
| P5 — Mileage Logging | ✅ Done | Daily mileage logs |
| P6 — Reminder Engine | 🔄 Active | WorkManager reminders, notifications |
| P7 — Ads + Billing | 🔜 Planned | AdMob banner + one-time purchase |

## Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Stable, always buildable |
| `latest-version-2` | Current active development |
| `agent/phase-*` | Agent-scoped feature branches |
| `fix/*` | Quick bug fixes |

## Secrets Policy

The following files are **never committed**:
- `local.properties` — SDK path and AdMob IDs
- `app/google-services.json` — Firebase config
- `*.keystore` / `*.jks` — signing keys
- `.env` files

## License

This project is proprietary. All rights reserved.
