# AutoMinder — Play Store Launch Plan
**Target:** v1.0.0 production on Google Play, **3–4 weeks** from today (2026-05-17 → ~2026-06-14)
**Mode:** Plan-only. No code changes until each task is explicitly approved.
**Owner:** TikiTaka3D (penupemamo83@gmail.com)
**Source of truth:** This file + `CLAUDE.md` (tech stack law) + the audit dated 2026-05-17.

---

## 0. Audit Reconciliation — what's actually true today

Before planning, re-verified the audit against the live repo. Findings adjusted:

| Audit # | Severity | Verified status | Notes |
|---|---|---|---|
| 1 — Missing `@Transaction` on multi-table writes | 🔴 HIGH | **CONFIRMED** | `FuelRepositoryImpl.insertFuelEntry` does 2 DAO calls back-to-back without transaction. |
| 2 — Missing `FuelEntryMapper` file | 🔴 HIGH | **CONFIRMED** | Mappers inlined in repo impl, breaks pattern. |
| 3 — `MIGRATION_1_2` completeness | 🔴 HIGH | **NEEDS VERIFY** | Both `app/schemas/1.json` and `2.json` exist — diff them in Task 1.4 before writing any migration code. |
| 4 — Missing `proguard-rules.pro` | 🔴 HIGH | **FALSE — already exists** (70 lines, Room+Hilt+Serialization rules present). Audit was wrong. Will still review for AdMob/Glance/Coil completeness. |
| 5 — `DISABLED` status not implemented | 🟡 MED | CONFIRMED — defer to v1.1 (not blocking launch). |
| 6 — `isPremium` not in DataStore | 🟡 MED | CONFIRMED — defer to v1.1 Premium phase. |
| 7 — Worker returns `Result.retry()` on exception | 🟡 MED | **CONFIRMED** (line 117). Easy fix. |
| 8 — Unconditional odometer update on fuel insert | 🟡 MED | **CONFIRMED** (line 29). Comment says "if higher" but code doesn't enforce. |
| 9 — `ExportServiceHistoryUseCase` has Android deps | 🟡 MED | CONFIRMED — architectural smell, non-blocking. |
| 10 — Notification ID `Long.toInt()` collision risk | 🟢 LOW | CONFIRMED — accept for v1.0 (PKs won't exceed Int.MAX_VALUE in user lifetime). |
| 11 — Fixed banner vs adaptive | 🟢 LOW | CONFIRMED — fix in UI modernization phase. |
| 12 — Missing `values-night/themes.xml` | 🟢 LOW | CONFIRMED — fix in UI phase. |
| 13 — Bottom nav shown on all screens | 🟢 LOW | CONFIRMED — UX bug, fix in UI phase. |
| 14 — Unused `coil-network-okhttp` declared | 🟢 LOW | CONFIRMED — cleanup task. |

**Net real work from audit:** 3 HIGH + 1 schema verify + 5 MEDIUM + 4 LOW = **13 fix tasks** (≈ 6 dev hours).

---

## 1. Competitive positioning — why we win

Top Play Store competitors (Drivvo, Fuelio, Car Maintenance Reminder, AUTOsist) are all stuck on:
- Material 2 visuals (rounded blue cards, FAB-centric, generic icon set)
- Dense list-first dashboards with no health narrative
- Manual everything — no smart fuel prediction, no proactive nudges
- Ad-heavy free tier with disruptive interstitials between every action

**AutoMinder's differentiation (already in code / planned):**
1. **M3 Expressive + bold typography** (Exo 2 display, JetBrains Mono for numbers) — feels 2026, not 2018
2. **Status-driven UI** — card corner radius morphs with health state (OVERDUE 8dp → GOOD 28dp), color from `errorContainer`/`tertiaryContainer`/`secondaryContainer`
3. **Offline-first** — works in the parking garage, syncs nothing, owns user data
4. **Fuel Intelligence (v1.1)** — Bayesian → EMA → seasonal-adjusted prediction with confidence intervals. Nobody in this category has this.
5. **Glance widget** — Pixel/OEM home-screen first-class citizen
6. **Single banner only in v1.0**, no interstitial spam (interstitial loaded but used sparingly)

---

## 2. Phased plan — 3 sprints over 4 calendar weeks

### Sprint 1 — Stability & Architecture (Week 1: May 17 → May 24)
**Goal:** Green build, audit findings closed, release variant proven to assemble.
**Exit gate:** `./gradlew assembleRelease` succeeds with a signed APK + AAB.

### Sprint 2 — UI/UX Modernization & Brand (Week 2: May 24 → May 31)
**Goal:** Every screen feels 2026-premium. Brand assets locked. Screenshots ready.
**Exit gate:** Designer-quality screenshots for 5 device sizes. Bottom-nav hide-on-detail working. Adaptive banners. Dark-mode verified.

### Sprint 3 — Store Compliance & Soft Launch (Week 3: May 31 → Jun 7)
**Goal:** Privacy policy live, Data Safety form, all store metadata, internal + closed testing tracks open.
**Exit gate:** Closed testing track receives APK; 12+ external testers added; crash-free rate baseline established.

### Sprint 4 — Production Promotion (Week 4: Jun 7 → Jun 14)
**Goal:** Address closed-test feedback, fix Sev-1/Sev-2 only, promote to production with staged rollout.
**Exit gate:** Production rollout at 20% → 50% → 100% over 5 days.

---

## 3. Task backlog — atomic, each ≤ 1 day

> Format: `[ID] Title — owner-layer — effort — depends-on`. Status updated via TodoWrite at execution time.

### Sprint 1 — Stability

| ID | Task | Layer | Effort | Depends |
|---|---|---|---|---|
| 1.1 | Change `Result.retry()` → `Result.success()` in `ReminderCheckWorker:117`. Add Timber error log only. | Worker | 15m | — |
| 1.2 | Add odometer guard in `FuelRepositoryImpl.insertFuelEntry` — only call `updateOdometer` if `fuelEntry.odometer > currentOdometer`. Reuse existing DAO `getCurrentOdometer(vehicleId)` or add it. | Data | 30m | — |
| 1.3 | Wrap `insertFuelEntry` in `@Transaction`. Create new `FuelDao.insertFuelEntryWithOdometerSync(entry, vehicleId, newOdo)` with `@Transaction` annotation. Update repo to call it. | Data | 45m | 1.2 |
| 1.4 | Diff `app/schemas/1.json` vs `2.json` — verify `MIGRATION_1_2` is complete. Write test `MigrationTest` using `MigrationTestHelper` to assert v1→v2 succeeds with sample data. | Data | 2h | — |
| 1.5 | Extract `FuelEntryMapper.kt` in `data/mapper/` matching the pattern of `VehicleMapper`. Remove inlined `toDomain/toEntity` from `FuelRepositoryImpl`. | Data | 30m | — |
| 1.6 | Audit `proguard-rules.pro` — add missing rules for: AdMob (`-keep public class com.google.android.gms.ads.**`), Coil 3, Glance widget receivers, kotlinx-datetime if used. Verify against `app/build/outputs/mapping/release/usage.txt` after first R8 run. | Build | 1h | 1.7 |
| 1.7 | Generate upload keystore: `keytool -genkey -v -keystore autominder-upload.jks -alias upload -keyalg RSA -keysize 4096 -validity 25000`. Store password in 1Password/Bitwarden. Add `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`, `KEY_ALIAS` to `local.properties`. Verify wired to `signingConfigs.release` block. | Build/Security | 1h | — |
| 1.8 | Run `./gradlew assembleRelease bundleRelease` end-to-end. Fix R8 errors iteratively. Confirm `.aab` produced at `app/build/outputs/bundle/release/app-release.aab`. | Build | 2h | 1.6, 1.7 |
| 1.9 | Install release APK on physical device (`adb install -r app-release.apk`). Smoke test: add vehicle, add reminder, trigger notification via WorkManager `runOnce`, add fuel, restart device, confirm reminders re-scheduled. | QA | 2h | 1.8 |
| 1.10 | Move `ExportServiceHistoryUseCase` Android-dependent file generation to `data/export/CsvExporter.kt`. Use case calls a domain interface. | Domain/Data | 1.5h | — |
| 1.11 | Remove unused `coil-network-okhttp` from `libs.versions.toml`. | Build | 5m | — |
| 1.12 | Verify all 4 screen states (`Loading`/`Empty`/`Error`/`Success`) on each of 16 screens. Add missing ones. Likely culprits: AddFuel, AddService, ServiceDetail. | UI | 3h | — |
| 1.13 | Verify every `LazyColumn` has `key = { it.id }`. Grep + manual review. | UI | 1h | — |

**Sprint 1 effort: ~16 hours / 2 dev days.**

### Sprint 2 — UI/UX & Brand

| ID | Task | Layer | Effort | Depends |
|---|---|---|---|---|
| 2.1 | **Hide bottom nav on non-root screens** in `MainActivity`. Compute from `currentBackStackEntry` destination — show only on Dashboard/VehicleList/Settings. | UI/Nav | 1h | — |
| 2.2 | Convert `BannerAdView` to **adaptive banner** (`AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidthDp)`). Update height calc. | Ads | 1h | — |
| 2.3 | Add `res/values-night/themes.xml` mirroring values/themes.xml with dark splash bg. Verify splash respects system theme. | Theme | 30m | — |
| 2.4 | Dashboard hero redesign: large `Exo 2` vehicle name, `JetBrains Mono` odometer at 32sp, status-colored chip row, single primary CTA. Reference: `ui-ux-pro-max` skill, style = "bento grid + dark mode optional". | UI | 4h | — |
| 2.5 | Vehicle card morphing corners: `animateFloatAsState` between 8/16/28dp by status. Already in CLAUDE.md spec — implement in `VehicleListScreen`. | UI | 2h | — |
| 2.6 | Add subtle haptic feedback on primary actions (add vehicle, complete reminder, log fuel) via `LocalHapticFeedback`. | UI | 1h | — |
| 2.7 | Empty states with personality — illustrated SVGs for: no vehicles, no reminders, no fuel entries, no service history. Source from open-source or generate via Whimsy Injector pattern. | UI/Assets | 3h | — |
| 2.8 | **App icon final** — adaptive icon at 5 densities + monochrome (Android 13+ themed icons) + Play Store 512×512. Design: stylized car silhouette + clock arc in Racing Teal #006B5F. Deliver as `mipmap-anydpi-v26` set. | Brand/Assets | 4h | — |
| 2.9 | **Feature graphic** 1024×500 for Play Store. Hero: phone mockup of Dashboard + tagline "Your car. Cared for." | Brand/Assets | 2h | 2.4 |
| 2.10 | **Screenshots** — 8 per device size: Dashboard, Vehicle Detail, Add Reminder, Fuel History, Mileage Log, Service History, Onboarding hero, Widget. Sizes: phone (1080×1920), 7" tablet (1200×1920), 10" tablet (1600×2560). Use Android Studio screenshot tool + Figma frames with captions. | Brand/Assets | 6h | 2.4, 2.5 |
| 2.11 | Short promo video 30s (optional but boosts conversion 25%). Screen recording via `adb shell screenrecord` + descript/CapCut. | Brand | 4h | 2.10 |
| 2.12 | Accessibility pass: `contentDescription` on every Image/IconButton, min 48dp touch targets, TalkBack walk-through, contrast ratio ≥ 4.5:1 verified. | UI/A11y | 3h | — |

**Sprint 2 effort: ~32 hours / 4 dev days.**

### Sprint 3 — Store compliance & soft launch

| ID | Task | Layer | Effort | Depends |
|---|---|---|---|---|
| 3.1 | **Privacy policy** — write & host on GitHub Pages or autominder.app domain. Must disclose: AdMob (Google), offline-only data, no analytics in v1.0, contact email. Use generator (PrivacyPolicies.com) as starting template, customize. | Legal | 2h | — |
| 3.2 | **Data Safety form** in Play Console: declare advertising_id (AdMob), no other data collected. Mark "Data not collected" for personal/financial/photos/location categories. | Compliance | 1h | 3.1 |
| 3.3 | **Content rating** questionnaire (IARC). Expected: Everyone. | Compliance | 30m | — |
| 3.4 | **Store listing copy** — app name, short description (80 chars), long description (4000 chars) with keywords: "car maintenance, oil change reminder, fuel log, mileage tracker, service history". ASO keyword research first. | Marketing | 3h | — |
| 3.5 | **Localization v1** — English only at launch. Mark structure ready for es/pt/hi/id in v1.1 (highest-volume Android markets). | I18n | 30m | — |
| 3.6 | Add Firebase Crashlytics OR enable Play Console's vitals-only crash reporting. Recommend Crashlytics for symbol resolution. Add `google-services` plugin (already in `google-services.json`). | Infra | 2h | — |
| 3.7 | Create **Play Console app** under TikiTaka3D account. Upload first AAB to **Internal testing** track. Add yourself + 2 trusted testers via email. | Release | 1h | 1.8 |
| 3.8 | After 24h on internal track without crashes → create **Closed testing** track with 12–20 testers (Reddit r/androidapps, family, Discord beta groups). Form: testing opt-in URL + invite emails. | Release | 2h | 3.7 |
| 3.9 | Feedback intake form (Google Form or Tally.so) linked from About screen "Send Feedback". | Support | 1h | — |
| 3.10 | Wire **pre-launch report** in Play Console — automatic UI crawler on real devices. Review and fix any P0 issues found. | QA | 2h | 3.7 |
| 3.11 | Set up `support@autominder.app` (or use personal email) — required by Play Console. | Ops | 30m | — |

**Sprint 3 effort: ~16 hours / 2 dev days.**

### Sprint 4 — Production rollout

| ID | Task | Layer | Effort | Depends |
|---|---|---|---|---|
| 4.1 | Triage closed-test feedback. Fix only Sev-1 (crashes, data loss) and Sev-2 (broken core flow). Defer everything else to v1.0.1. | Triage | ~ | 3.8 |
| 4.2 | Bump versionCode/versionName, regenerate AAB, upload to **Production** track. | Release | 30m | 4.1 |
| 4.3 | Staged rollout: 20% (day 1) → 50% (day 3) → 100% (day 5). Monitor crash-free rate; halt if < 99.0%. | Release | 1h/day | 4.2 |
| 4.4 | ASO monitoring — track keyword rankings via AppFollow free tier or manual. Iterate on title/short description in week 2 if needed. | Marketing | 2h | 4.2 |
| 4.5 | Post-launch: collect Play Console reviews, respond to every 1-star within 24h. | Support | ongoing | 4.2 |

---

## 4. Play Store launch checklist (printable)

```
PRE-UPLOAD
[ ] Upload keystore generated, password vaulted, backed up to 2 locations
[ ] signingConfig.release wired in build.gradle.kts
[ ] versionCode = 1, versionName = "1.0.0"
[ ] applicationId = com.autominder.app (LOCKED)
[ ] minSdk 26, targetSdk 36
[ ] Real AdMob IDs in release variant (NOT test IDs)
[ ] ENABLE_ADS = true in release
[ ] proguard-rules.pro covers Room, Hilt, Serialization, AdMob, Coil, Glance
[ ] assembleRelease + bundleRelease both succeed locally
[ ] R8 mapping file backed up (for crash deobfuscation)
[ ] Smoke test on physical device passes
[ ] Pre-launch report has no P0 issues

PLAY CONSOLE SETUP
[ ] App created under TikiTaka3D developer account ($25 paid)
[ ] Default language: English (US)
[ ] App name: AutoMinder
[ ] Short description (80 chars) drafted
[ ] Full description (4000 chars) drafted
[ ] App category: Auto & Vehicles
[ ] Tags: 5 selected
[ ] Contact email: support@autominder.app verified

STORE ASSETS
[ ] App icon 512×512 PNG, alpha allowed
[ ] Feature graphic 1024×500 PNG/JPG (no alpha)
[ ] Phone screenshots ×8 (min 2)
[ ] 7" tablet screenshots ×8 (optional but boosts tablet placement)
[ ] 10" tablet screenshots ×8 (optional)
[ ] Promo video YouTube URL (optional)

COMPLIANCE
[ ] Privacy policy URL live and reachable
[ ] Data Safety form completed
[ ] Content rating questionnaire submitted → Everyone
[ ] Ads declaration: "Yes, contains ads"
[ ] News app: No
[ ] Target audience: 18+ (driving age)
[ ] Government app: No
[ ] Financial features: No
[ ] Health features: No

TESTING TRACKS
[ ] Internal testing track active, 0 crashes in 24h
[ ] Closed testing track active, ≥12 testers, ≥7 days runtime
[ ] Crash-free rate ≥ 99.0%
[ ] ANR rate ≤ 0.47% (Play's bad-behavior threshold)
[ ] No policy violations flagged

PRODUCTION
[ ] AAB uploaded to Production track
[ ] Release notes written (under 500 chars)
[ ] Countries: select 20+ initial (skip EU until GDPR-Plus form done if needed)
[ ] Staged rollout 20% → review vitals daily
```

---

## 5. Risk register

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| R8/Proguard strips a serialization class → release crash | M | H | Task 1.8 + smoke test on real device. Keep `mapping.txt` for symbolication. |
| AdMob rejects app for "broken experience" (e.g. ad shown over content) | L | H | Task 2.2 adaptive banner + bottom-bar placement only. No interstitials in v1.0 hot paths. |
| Play Console rejects for missing privacy policy / data safety | M | H | Sprint 3 prioritized before any upload attempt. |
| Closed testers report fuel calculation bug from Phase 4 | M | M | Task 1.4 migration test + dedicated QA pass in Sprint 3. |
| Keystore lost → cannot ever update app on this listing | L | CRITICAL | Backup to 2 offline locations (USB + cloud vault). Document in 1Password. **DO THIS DAY 1**. |
| Notification permission denied on Android 13+ → reminders silently broken | H | H | Already handled in `PermissionUtils` — verify UX nudge on first reminder add in Sprint 2. |
| OEM battery optimizer kills WorkManager on Xiaomi/Vivo/Oppo | H | M | Add Settings entry: "Allow background activity" with deep-link to per-OEM battery settings. Optional Sprint 2 task. |
| Compose BOM 2025.06.01 has unknown regression in M3 Expressive | L | M | Stick to BOM, no individual version overrides. |

---

## 6. Definition of Done — per sprint

**Sprint 1 DoD:**
- All 13 fix tasks merged on `latest-version-2` or `main`
- `./gradlew assembleRelease bundleRelease` green
- Signed AAB exists in `app/build/outputs/bundle/release/`
- Smoke test recording saved
- All audit HIGH findings closed

**Sprint 2 DoD:**
- 8 phone screenshots in `/marketing/screenshots/phone/`
- App icon set in `app/src/main/res/mipmap-*` complete (incl. monochrome)
- Bottom nav hidden on detail screens (verified by hand)
- Dark mode verified on real device
- A11y scanner (Android Accessibility Scanner app) shows 0 critical issues

**Sprint 3 DoD:**
- Privacy policy URL live
- Internal track has 0-crash 48h streak
- Closed track invites sent
- All Play Console fields green

**Sprint 4 DoD:**
- 100% production rollout
- 99%+ crash-free
- ≥ 4.0 average rating in first 50 reviews
- v1.0.1 backlog created in GitHub Issues

---

## 7. Out of scope for v1.0 (parked for v1.1)

- Fuel Intelligence algorithm phases 2–4 (Bayesian, EMA, seasonal)
- Premium/Billing flow + `isPremium` DataStore key
- Per-reminder `DISABLED` status + UI toggle
- Localization beyond English
- Cloud backup / sync (currently 100% offline by design)
- Vehicle photo cropping/filters
- Service-provider integration (oil change shop bookings)
- Driving style telemetry (accelerometer, GPS)

---

## 8. Next action (your call)

Approve Sprint 1 task list to begin execution. I'll work top-down through `1.1 → 1.13`, committing after each task with `./gradlew compileDebugKotlin` verification per CLAUDE.md gate, and report at every Sprint boundary for review.

Or — if you'd like deeper specialist input first — I can dispatch:
- **UI Designer agent** → produce Figma-ready mockups for Sprint 2 (Dashboard hero + Vehicle card morph)
- **Legal Compliance Checker** → draft the privacy policy + Data Safety answers in Sprint 3
- **App Store Optimizer** → ASO keyword research + store listing copy in Sprint 3

Tell me which to spawn (or "start Sprint 1") and I'll proceed.
