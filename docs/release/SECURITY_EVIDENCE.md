# 🔒 Security & Privacy Evidence Dossier
**Application:** AutoMinder (`com.autominder.app`)  
**Release Version:** `1.0.0` (Build `27000001`)  
**Security Standard:** Zero-PII Leakage · Bytecode Log Scrubbing · Offline Data Sovereignty  
**Date:** August 2026

---

## 1. Secret Scanning & Credential Isolation Evidence

* **Automated Scan Execution:** Recursively checked `app/src/main/` for `AIza`, `API_KEY`, `secret`, `token`, `password`, `Bearer`, `sk-`.
* **Result:** **0 hardcoded credentials found.**
* **Git Defense:**
  ```gitignore
  # Secrets / Credentials
  local.properties
  *.keystore
  *.jks
  .env
  google-services.json
  sentry.properties
  ```

---

## 2. Bytecode Log Stripping & R8 Optimization Proof

To guarantee that no debug logs survive in the production APK/AAB, ProGuard physically purges all `Timber.d`, `Timber.v`, and `Timber.i` calls during R8 compilation:

```proguard
# proguard-rules.pro — Rule #6: Timber Bytecode Elimination
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
```

### Automated CI Regression Guard:
* [`NoPiiInReleaseLogsTest.kt`](file:///d:/Autominder/app/src/test/kotlin/com/autominder/app/core/util/NoPiiInReleaseLogsTest.kt) validates that no release-level logging statement interpolates sensitive identifiers (`vin`, `plate`, `license`, `email`, `phone`, `token`, `address`).
* [`SensitiveDataLeakTest.kt`](file:///d:/Autominder/app/src/test/kotlin/com/autominder/app/core/util/SensitiveDataLeakTest.kt) confirms zero raw `println`, `System.out`, or unrouted `android.util.Log` calls exist in production Kotlin files.

---

## 3. Manifest Component Export & Permission Minimization

| Component | Exported | Gating / Protection | Intent-Filter / Action |
|---|---|---|---|
| `MainActivity` | `true` | System Launcher | `android.intent.action.MAIN` / `LAUNCHER` |
| `SystemEventReceiver` | `true` | `permission="android.permission.RECEIVE_BOOT_COMPLETED"` | `BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED`, `MY_PACKAGE_REPLACED` |
| `AutoMinderWidgetReceiver` | `true` | System AppWidget Manager | `android.appwidget.action.APPWIDGET_UPDATE` |
| `NotificationActionReceiver` | `false` | Internal App Process Only | None |
| `ReminderAlarmReceiver` | `false` | Explicit Component AlarmManager Only | None |
| `FileProvider` | `false` | `grantUriPermissions="true"` | File Provider Paths (`file_paths.xml`) |

---

## 4. Telemetry Privacy & Sentry Sanitization

To ensure 100% compliance with Google Play's User Data & Privacy policies:
1. **Screen Captures Disabled:** `io.sentry.attach-screenshot = false`
2. **View Tree Serialization Disabled:** `io.sentry.attach-view-hierarchy = false`
3. **Tracing Sample Rate:** `io.sentry.traces.sample-rate = 0.2` (Controlled telemetry footprint).
4. **Crashlytics Breadcrumbs Filter:** `CrashlyticsTree` returns immediately for `priority <= Log.DEBUG`, uploading only sanitised non-PII diagnostic summaries on crash.
