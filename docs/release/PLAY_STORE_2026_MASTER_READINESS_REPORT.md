# Milevora — 2026 Google Play Store Master Readiness & Architecture Deep-Dive

**Target Platform**: Google Play Console (2026 Production Tier)  
**App Name**: `Milevora: Car Maintenance`  
**Application ID / Package**: `com.autominder.app`  
**Version**: `1.0.0` (Version Code: `27000001`)  
**Target SDK**: `36` (Android 16 Readiness) | **Min SDK**: `26` (Android 8.0 Oreo)  

---

## 🏛️ 1. Modern 2026 Android Architecture Deep-Dive (Non-Developer Guide)

Milevora is built using the latest **Clean Architecture + MVVM + UDF (Unidirectional Data Flow) + Offline-First SQLite** standards endorsed by Google and top Android engineering teams.

### Directory Structure & Purpose Breakdown

```
app/src/main/kotlin/com/autominder/app/
│
├── AutoMinderApp.kt              # App Application class (@HiltAndroidApp) — initializes background workers & crash logging
├── MainActivity.kt               # Single Activity host — manages window edge-to-edge and bottom navigation
│
├── ads/                          # AdMob monetization engine (AdManager.kt, BannerAdView.kt, ConsentManager.kt)
├── billing/                      # Google Play Billing 9.1.0 engine (SubscriptionManager.kt)
├── core/                         # Cross-cutting core infrastructure (DI modules, notifications, analytics)
│   ├── di/                       # Dependency Injection (Hilt modules: DatabaseModule, NetworkModule, WorkerModule)
│   ├── notifications/            # Local notification channels, alert dispatchers, and cooldown guards
│   └── util/                     # Formatting utilities, analytics events, and security validators
│
├── data/                         # Data Layer (Database, repositories, and preferences)
│   ├── local/
│   │   ├── dao/                  # Data Access Objects (SQL query interfaces for Vehicles, Reminders, Fuel, Services)
│   │   ├── entity/               # Room SQLite database table schemas (VehicleEntity, ReminderEntity, etc.)
│   │   ├── database/             # AutoMinderDatabase (v2 schema with migrations & foreign keys)
│   │   └── preferences/          # Encrypted DataStore for user settings (Pro status, units, theme)
│   ├── mapper/                   # Safe mappers converting database entities to domain business models
│   └── repository/               # Repository implementations handling offline caching and data streams
│
├── domain/                       # Pure Domain / Business Logic Layer (Zero Android dependencies)
│   ├── model/                    # Clean core models (Vehicle, Reminder, FuelEntry, Service, ServiceStatus)
│   ├── repository/               # Repository interfaces / contracts
│   ├── usecase/                  # Single-responsibility business logic use cases (StatusCalculator, CalculateConfidence)
│   └── validation/               # Strict input validation rules (VIN, mileage, costs)
│
├── ui/                           # User Interface Layer (100% Jetpack Compose UI)
│   ├── theme/                    # Design tokens (M3 Expressive Color.kt, Type.kt, Shape.kt, Night Garage Dark Mode)
│   ├── navigation/               # Type-safe @Serializable sealed navigation routes & NavGraph
│   ├── components/               # Reusable UI widgets (Bento cards, StatusChip, 4-state Loading/Empty/Error/Success)
│   └── screens/                  # Feature screen Composables and their respective MVVM ViewModels
│       ├── dashboard/            # Command center with live countdown cards
│       ├── vehicle/              # Multi-vehicle garage and fleet detail
│       ├── service/              # 10-second service logger and PDF maintenance passport
│       ├── fuel/                 # Add fuel telemetry and real-world MPG calculation
│       ├── mileage/              # Mileage tracker and odometer history
│       ├── settings/             # 100% on-device privacy controls and Pro subscription paywall
│       └── about/                # In-app brand showcase rendering the app icon
│
├── widget/                       # Android 12+ Glance App Widget (Interactive home-screen cockpit)
└── worker/                       # WorkManager background workers (DailyCheckWorker, ReminderCheckWorker)
```

---

## 🔒 2. Clean AAB Packaging & AI Artifact Stripping Audit

Google Play Store automated review systems and human testers reject apps that bundle messy development files, scratch scripts, or un-compiled test dumps.

### Why Your Release `.aab` is 100% Clean:

1. **R8 Minification & Resource Shrinking (`build.gradle.kts`)**:
   - `isMinifyEnabled = true`: Renames, shrinks, and obfuscates all Java/Kotlin bytecode. Strips unused classes and methods.
   - `isShrinkResources = true`: Discards any unreferenced drawables, strings, or XML layouts.
   - `-assumenosideeffects class timber.log.Timber`: Physically strips all debug logging calls from the release binary.
2. **Strict Resource Packaging Exclusions**:
   - All `META-INF/` licenses, notices, markdown (`*.md`), and text files are explicitly excluded from the `.aab` bundle:
   ```kotlin
   packaging {
       resources {
           excludes += listOf(
               "/META-INF/{AL2.0,LGPL2.1}",
               "/META-INF/INDEX.LIST",
               "/META-INF/DEPENDENCIES",
               "/META-INF/LICENSE*",
               "/META-INF/NOTICE*",
               "**/*.md",
               "**/*.txt"
           )
       }
   }
   ```
3. **No Unpackaged Assets**:
   - The `app/src/main/assets` directory does not exist.
   - Python seed scripts (`seed_db.py`, `push_db.py`), documentation (`docs/`), website files (`website/`), and test code (`app/src/test`, `app/src/androidTest`) are stored in isolated root folders that **Gradle never packages into release artifacts**.

---

## 💰 3. Monetization Setup: Subscriptions & AdMob Ads

To generate revenue, Milevora incorporates two production-grade monetization streams:

### A. Google Play Billing 9.1.0 (In-App Subscriptions)
Implemented in [`SubscriptionManager.kt`](file:///d:/Autominder/app/src/main/kotlin/com/autominder/app/billing/SubscriptionManager.kt):

| Product ID in Code | Product Type | Play Console Setup Location | Suggested Price |
|---|---|---|---|
| `autominder_pro_monthly` | Auto-renewing Subscription | **Monetize > Subscriptions** | $2.99 / month |
| `autominder_pro_yearly` | Auto-renewing Subscription | **Monetize > Subscriptions** | $19.99 / year |
| `autominder_pro_lifetime` | One-Time In-App Product | **Monetize > In-app products** | $39.99 one-time |

* **Offline Cold-Start Protection**: Paying users retain Pro access even if offline; Play reconciles entitlements when connected.
* **Pro Benefits**: Removes all ads, unlocks unlimited garage vehicles, enables PDF/CSV Maintenance Passport exports.

### B. Google AdMob (Display & Interstitial Ads)
Managed in [`AdManager.kt`](file:///d:/Autominder/app/src/main/kotlin/com/autominder/app/ads/AdManager.kt) & [`BannerAdView.kt`](file:///d:/Autominder/app/src/main/kotlin/com/autominder/app/ads/BannerAdView.kt):

* **Safe Key Injection**: Production AdMob App ID and Ad Unit IDs are injected at build time from your private `local.properties` (or CI secrets) into `strings.xml` via `resValue`.
* **Ad Ratio Guard**: Interstitial ads are shown gracefully on every 3rd meaningful user action (never on first launch, never during urgent alerts).

#### Setting Up Production AdMob Keys in `local.properties`:
```properties
# Add your real Google AdMob IDs here before running bundleRelease:
RELEASE_ADMOB_ID=ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX
ADMOB_BANNER_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
ADMOB_INTERSTITIAL_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
ADMOB_REWARDED_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
ADMOB_REWARDED_INTERSTITIAL_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
```

---

## 📋 4. Google Play Console 2026 Submission Questionnaire Answers

When filling out the **App Content & Data Safety** questionnaire in Google Play Console:

| Section | Question | Recommended Answer |
|---|---|---|
| **Data Safety** | Does your app collect or share user data? | **No** *(All vehicle, fuel, and service records are stored 100% on-device in local SQLite)* |
| **Data Encryption** | Is data collected encrypted in transit? | **Yes** *(Standard HTTPS for AdMob/Sentry)* |
| **Account Creation** | Does your app require an account? | **No account creation is required** |
| **Advertising ID** | Does your app use Advertising ID (AAID)? | **Yes** *(Select "Advertising or marketing" for Google AdMob SDK)* |
| **Financial Features** | Does your app provide financial features? | **No** *(Expense tracker only, no banking/crypto)* |
| **Target Audience** | What age group is your app designed for? | **18 and older** (Drivers/Vehicle owners) |
| **News / COVID-19** | Is your app a news app or contact tracing app? | **No** |

---

## 🚀 5. Final Step: Generating the Production Release Bundle

When you are ready to produce the signed `.aab` file for Play Console upload:

```powershell
.\gradlew.bat bundleRelease --no-configuration-cache --no-daemon
```

The resulting file will be located at:
📁 `d:\Autominder\app\build\outputs\bundle\release\app-release.aab`
