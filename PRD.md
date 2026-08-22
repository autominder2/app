AutoMinder
Product Requirements Document — v4.0
#1 Offline-First Car Maintenance App • Global Launch 2026
Package: com.autominder.app • Kotlin 2.1.21 • Compose M3 Expressive
17 Screens | 4 Algorithm Phases | 12 Build Phases | 30 Days to Ship

1. Product Identity & Strategic Positioning
1.1 App Identity
Field	Value
App Name	AutoMinder
Package ID	com.autominder.app  — FINAL. LOCKED FOREVER.
Version	1.0.0 (Play Store initial) → 1.1.0 (Fuel Intelligence)
Play Store Dev	TikiTaka3D
Category	Auto & Vehicles
Target Audience	All drivers globally — non-tech to pro
Business Model	Free + AdMob (non-intrusive) + One-Time Premium ‘AutoMinder Pro’
Premium Price	$4.99 one-time (never subscription)

1.2 Market Opportunity
The car maintenance app market has no dominant winner. 91.8% of drivers neglect maintenance, paying 167% more than expected annually ($7,303 actual vs $2,738 estimated). 69 million US breakdowns per year cost $44 billion. The best-rated app (CARFAX 4.72✳) is locked to North America. The opportunity is global and uncontested.

The 3 Problems No App Has Solved
- Accurate offline fuel prediction without OBD hardware or GPS
- Adaptive maintenance scheduling based on real driving conditions
- Daily engagement — no car app is used every day

1.3 Winning Formula: 3 Unbeatable Pillars
Pillar | Competitor Gap | AutoMinder Solution
---|---|---
Offline Fuel Intelligence | No app predicts fuel without OBD/GPS | 4-phase Bayesian+EMA algorithm. Day 1: ±30%. Fill-up #10: ±5%.
Daily Habit Loop | Car apps used monthly at best | 15-second pre-drive check + streak + eco-score = daily value
Local-First Privacy | aCar lost 4.5→ 1.6✳ after cloud sync destroyed data | Room DB is source of truth. Cloud sync is optional backup only.
Zero Subscriptions | Drivvo hiked 4x. Users call it exploitation. | $4.99 one-time. Reminders free forever.
2026 Premium UI	| aCar stuck in 2016 design. Simply Auto cluttered. | M3 Expressive. Spring physics. Exo2+NunitoSans. Dark-first.

2. Technical Foundation — Exact Versions (Never Deviate)
2.1 Build System (libs.versions.toml — Single Source of Truth)
Library | Version | Critical Note
---|---|---
Kotlin | 2.1.21 | KSP prefix MUST match this exactly
AGP | 8.9.1 | Must be 8.9+ for Kotlin 2.1.x
KSP | 2.1.21-2.0.1 | MUST mirror Kotlin version prefix — never change independently
Compose BOM | 2025.06.01 | M3 Expressive (May 2025). Never pin individual Compose libs.
Material3 Adaptive | 1.1.0 | NavigationSuiteScaffold — replaces deprecated WindowSizeClass
Hilt | 2.55 | KSP only — NEVER kapt. kapt is deprecated and broken with Room 2.7
Room | 2.7.1 | KSP only. exportSchema=true always. No fallbackToDestructiveMigration.
Navigation | 2.9.0 | Type-safe @Serializable routes ONLY — never raw strings
WorkManager | 2.10.0 | For ReminderCheckWorker + FuelCheckWorker
Coroutines | 1.9.0 | All async on viewModelScope or Dispatchers.IO
DataStore | 1.1.2 | Settings persistence (theme, units, onboarding flag)
Coil | 3.1.0 | Vehicle photos via AsyncImage
Kotlinx Serialization | 1.8.0 | Required for type-safe Navigation 2.9 routes
AdMob | 23.5.0 | IDs from local.properties only — never hardcoded
Billing | 7.1.1 | One-time purchase for AutoMinder Pro
Timber | 5.0.1 | Logging — debug builds only via DebugTree
Turbine | 1.2.0 | Flow testing in unit tests
minSdk | 26 (Android 8.0) | 97% of active devices covered
targetSdk | 36 (Android 16) | Required for 2026 Play Store compliance

2.2 Architecture Laws (Violation = Immediate Rollback)
Pattern: MVVM + Clean Architecture + Offline-First + Repository
Layer chain: UI → ViewModel → UseCase → Repository interface → RepositoryImpl → DAO → Room DB

Rule | Law | Penalty for Violation
---|---|---
ViewModel scope | NEVER imports DAO directly | Architecture collapse — untestable code
UI scope | NEVER imports Repository | Breaks Clean Architecture boundaries
DB reads | ALWAYS return Flow<T> (never suspend for reads) | UI won’t react to DB changes
DB writes | ALWAYS suspend fun (never Flow for writes) | Coroutine leaks
Error handling | ALL ops return sealed Result<T,AppError> | Raw exceptions crash ViewModel
Thread safety | ALL async on viewModelScope or Dispatchers.IO | ANR on main thread
Text | ALL strings in strings.xml — zero hardcoded English | i18n broken
Colors | ALL via MaterialTheme.colorScheme — zero Color(0xFF...) in UI | Dark mode breaks
Navigation | NavRoutes.kt sealed objects only — NEVER raw navigate() strings | Silent runtime crash
DI | KSP everywhere — NEVER kapt anywhere in project | Room 2.7 won’t compile
DB migrations | NEVER fallbackToDestructiveMigration() | User data silently wiped
PKs | ALL @PrimaryKey(autoGenerate=true) = Long (never Int) | Int overflow at 2.1B
Transactions | Multi-table writes MUST use @Transaction | Corrupted partial state
Money | Store as Int cents ONLY — NEVER Double or Float | Float rounding errors

2.3 Folder Structure
app/src/main/kotlin/com/autominder/app/
├── AutoMinderApp.kt              ← @HiltAndroidApp entry point
├── core/
│   └── di/                       ← DatabaseModule, NetworkModule, WorkerModule
├── data/
│   ├── local/
│   │   ├── dao/               ← VehicleDao, ReminderDao, FuelDao, ServiceDao, MileageDao
│   │   ├── entity/            ← Vehicle, Reminder, FuelEntry, Service, MileageLog
│   │   └── db/                ← AutoMinderDatabase (v2, exportSchema=true)
│   └── repository/           ← *RepositoryImpl (data sources)
├── domain/
│   ├── model/                ← Pure Kotlin models (no Android deps)
│   ├── repository/           ← IVehicleRepository, IReminderRepository, IFuelRepository
│   └── usecase/              ← StatusCalculator, FuelIntelligenceEngine, HealthScoreUseCase
├── ui/
│   ├── theme/                ← Color.kt, Type.kt, Shape.kt, AutoMinderTheme.kt
│   ├── navigation/           ← NavRoutes.kt, NavGraph.kt
│   ├── components/           ← StatusChip, VehicleCard, LoadingState, EmptyState
│   └── screens/
│       ├── dashboard/          ← DashboardScreen + DashboardViewModel
│       ├── vehicle/            ← VehicleDetail, AddEditVehicle, VehicleList
│       ├── reminder/           ← ReminderList, AddEditReminder
│       ├── fuel/               ← FuelScreen, FuelLogScreen, FuelHistoryScreen
│       ├── service/            ← ServiceHistory, AddEditService
│       ├── mileage/            ← MileageLogScreen
│       ├── onboarding/         ← OnboardingScreen
│       ├── analytics/          ← AnalyticsScreen (costs + trends)
│       ├── settings/           ← SettingsScreen, NotifPreferencesScreen
│       └── premium/            ← PremiumScreen (IAP upsell)
├── worker/
│   ├── ReminderCheckWorker.kt
│   ├── FuelReminderWorker.kt  ← NEW — Phase 4
│   ├── DailyCheckWorker.kt    ← NEW — morning habit loop
│   └── BootReceiver.kt

3. Database Design — Room Entities v2
3.1 Entity Specification
Entity | Table | Key Fields | Indices
---|---|---|---
Vehicle | vehicles | id: Long PK, make, model, year, currentOdometer: Int, tankCapacityLiters: Float, drivingStyle: DrivingStyle, fuelType: FuelType, photoUri: String?, isArchived: Boolean | None (primary table)
Reminder | reminders | id: Long PK, vehicleId: Long FK, serviceType: ServiceType, nextDueDate: Long?, nextDueOdometer: Int?, intervalDays: Int?, intervalKm: Int?, snoozeUntil: Long?, lastNotifiedAt: Long?, isEnabled: Boolean | vehicleId, vehicleId+serviceType
FuelEntry | fuel_entries | id: Long PK, vehicleId: Long FK, odometerKm: Int, litersAdded: Float, totalCostCents: Int, isFullFill: Boolean, fuelType: FuelType, garageName: String?, notes: String?, loggedAt: Long | vehicleId, vehicleId+loggedAt
Service | services | id: Long PK, vehicleId: Long FK, serviceType: ServiceType, serviceDate: Long, odometerKm: Int, costCents: Int, garageName: String?, notes: String?, receiptPhotoUri: String? | vehicleId, vehicleId+serviceDate
MileageLog | mileage_logs | id: Long PK, vehicleId: Long FK, odometer: Int, loggedAt: Long, note: String? | vehicleId

3.2 Enums (TypeConverter — stored as String name)
ServiceType: OIL_CHANGE, TIRE_ROTATION, AIR_FILTER, CABIN_FILTER, BRAKE_SERVICE, BRAKE_FLUID, SPARK_PLUGS, COOLANT, BATTERY, TIMING_BELT, TRANSMISSION, WIPER_BLADES, INSPECTION, REGISTRATION, INSURANCE, EMISSIONS_TEST, CUSTOM
FuelType: PETROL, DIESEL, CNG, LPG, ELECTRIC
DrivingStyle: CITY_SHORT_TRIPS, MIXED, HIGHWAY
ServiceStatus: OVERDUE (severity=0), DUE_SOON (severity=1), GOOD (severity=2), SNOOZED, DISABLED, NO_DATA
CRITICAL: Never rename existing enum entries post-launch. Stored as String via TypeConverter — renaming breaks all existing rows.

3.3 Database Version History
Version | Change | Migration Strategy
---|---|---
1 | Initial: vehicles, reminders, services, mileage_logs | N/A — fresh install
2 | Add fuel_entries table + @Index on reminders + lastNotifiedAt on reminders | AutoMigration + manual Migration(1,2) for index additions
3+ | Future changes | Every change requires Migration object. NEVER fallbackToDestructiveMigration()

4. Screen Specifications — All 17 Screens
4.1 Navigation Architecture
Bottom Navigation: 3 tabs ONLY — Dashboard | Vehicles | Settings. ServiceHistory lives inside VehicleDetail as a tab. FuelScreen lives inside VehicleDetail.
NavRoutes.kt uses @Serializable sealed objects. ZERO raw string navigate() calls anywhere in the codebase.
Route | Type | Bottom Tab | Parent Screen
---|---|---|---
Dashboard | @Serializable object | Tab 1 | —
VehicleList | @Serializable object | Tab 2 | —
Settings | @Serializable object | Tab 3 | —
Onboarding | @Serializable object | None (pre-auth flow) | —
VehicleDetail(vehicleId: Long) | @Serializable data class | None | VehicleList / Dashboard
AddEditVehicle(vehicleId: Long? = null) | @Serializable data class | None | VehicleList / Dashboard
ReminderList(vehicleId: Long) | @Serializable data class | None | VehicleDetail
AddEditReminder(vehicleId: Long, reminderId: Long? = null) | @Serializable data class | None | ReminderList
FuelScreen(vehicleId: Long) | @Serializable data class | None | VehicleDetail
FuelLogEntry(vehicleId: Long) | @Serializable data class | None | FuelScreen
ServiceHistory(vehicleId: Long) | @Serializable data class | None | VehicleDetail
AddEditService(vehicleId: Long, serviceId: Long? = null) | @Serializable data class | None | ServiceHistory
MileageLog(vehicleId: Long) | @Serializable data class | None | VehicleDetail
Analytics | @Serializable object | None | Dashboard (card tap)
NotifPreferences | @Serializable object | None | Settings
PremiumScreen | @Serializable object | None | Settings / contextual
DrivingStyleProfile(vehicleId: Long) | @Serializable data class | None | VehicleDetail / Settings

4.2 Screen Design Specifications
S01 — Dashboard (score 72/100 → target 95/100)
Element | Specification
---|---
TopAppBar | Remove "My Vehicles" title. Logo left + "Good morning, [name]" right. LargeTopAppBar + scrollBehavior.
UrgencyBanner | ElevatedCard. Red if any OVERDUE, amber if DUE_SOON only. "3 overdue · 1 due soon". animateIntAsState on counts.
Vehicle Cards | Sort: OVERDUE → DUE_SOON → GOOD. NEVER alphabetical. animateFloatAsState corner morphing.
SpeedDial FAB | Primary: Log Service (Build icon). Secondary: Add Vehicle (Add icon). BackHandler closes dial.
Pull-to-Refresh | PullToRefreshBox wrapper on LazyColumn.
Empty State | Car illustration + "Add your first vehicle" FilledButton. Never blank screen.
Ads | ZERO ads on Dashboard — it is the premium-feel home. Ads ONLY on ServiceHistory and VehicleList.
State Machine | Loading → Empty → Error+retry → Success. All 4 states required.

S02 — Vehicle Detail (score 90/100 → target 98/100)
Element | Specification
---|---
Header | LargeTopAppBar with Coil AsyncImage parallax. Car color matches vehicle photo.
Health Score Ring | Circular arc 0-100. Green >70, Amber 40-70, Red <40. animateFloatAsState on enter.
Stats Row | Odometer (JetBrains Mono font) | Last Service date | Next Service countdown.
Quick Actions | 4 icon buttons: Log Service | Add Reminder | Mileage | Share health card.
Tab Row | 4 tabs: Reminders | Fuel | Services | Mileage. HorizontalPager.
Fuel Tab | Tank visual (animated fill). Estimated remaining km with confidence range. Eco-score.
Reminders Tab | OVERDUE section → DUE_SOON section → GOOD section. Swipe left to snooze/disable.

S10 — Fuel Screen (NEW — Phase 4 core feature)
Element | Specification
---|---
Tank Visual | Animated semi-circular gauge. Color shifts green→amber→red as level drops. Estimated km shown large.
Confidence Range | "Between 45 and 72 km remaining" — always distance, never percentage alone.
Algorithm Phase Badge | "Learning (3/10 fill-ups)" or "Accurate (±7%)" badge. Builds user trust in predictions.
Quick Log Button | Primary CTA. Opens bottom sheet: Liters + Cost + Full tank toggle. 3 required fields.
History Chart | Bar chart: L/100km per fill-up. Trend line. 3-month view. Sparkline thumbnail on card.
Insight Card | "Your Suzuki Alto uses avg 6.8L/100km. City driving increases this by 18%." Contextual tips.
Fuel Type | Petrol/Diesel/CNG/LPG/Electric selector. CNG = pressure-based estimation note.

S07 — Onboarding (60-second target)
Page | Content | Duration Target
---|---|---
Page 1: Welcome | Hero illustration. 3 value bullets (not features). "Get Started" FilledButton. POST_NOTIFICATIONS permission request with clear value prop. | 10 seconds
Page 2: Add Vehicle | Year/Make/Model/Mileage fields. Tank size (auto-filled from DB). Fuel type. DrivingStyle chips. "Get Started" immediately creates vehicle. | 30 seconds
Page 3: First Reminder | Pre-populated Oil Change suggestion based on vehicle. User confirms interval or adjusts. Creates reminder. Confetti animation. | 20 seconds

5. Fuel Intelligence Algorithm — 4-Phase Offline System
5.1 Algorithm Overview
No existing app predicts fuel needs without OBD hardware or GPS. AutoMinder uses a Hybrid Bayesian + Exponential Moving Average (EMA) system that improves accuracy from ±30% on day one to ±5-7% by fill-up #10. This is the core technical differentiator.

Phase | Fill-ups | Accuracy | Method | Data Source
---|---|---|---|---
1: Cold Start | 0 | ±25-30% | Vehicle database seed rates + driving style multiplier + regional fuel quality factor | Vehicle make/model DB + user declared style
2: Learning | 1-4 | ±15-20% | Bayesian updating: posterior = (obs variance × prior + prior variance × obs) / (obs var + prior var) | Each full fill-up narrows the uncertainty envelope
3: Stable | 5-9 | ±8-12% | EMA: new = 0.25 × latest + 0.75 × previous. Running standard deviation tracked. | Historical fill-up record
4: Mature | 10+ | ±5-7% | Seasonal adjustment enabled. Adaptive α: increases to 0.4 when recent readings diverge >1.5σ | Multi-month history + pattern detection

5.2 Reminder Trigger Logic
Reminder fires on the PESSIMISTIC (lower) confidence bound, not the average. Always better to remind early than have the user run empty.
estimated_remaining_fuel = tank_capacity - (current_odo - last_fill_odo) * ema_rate
estimated_remaining_km = estimated_remaining_fuel / ema_rate
confidence_low_km = estimated_remaining_km - 2 * sigma_km
IF confidence_low_km < buffer_km (default 50) -> SEND REMINDER

5.3 Notification Zones
Zone | Estimated Tank % | Message Format | Channel Priority
---|---|---|---
Green | >25% | No notification | N/A
Yellow | 15-25% | "Consider refueling on your next trip" | DEFAULT
Orange | 10-15% | "~X km remaining. Refuel soon." (confidence range shown) | DEFAULT
Red | <10% | "Very low fuel. Refuel immediately. ~X km remaining." | HIGH

5.4 Edge Cases (all must be handled)
- Partial fill: isFullFill=false. Accumulate fuel+distance across partials until next full fill before calculating rate.
- Outlier rejection: if calculated rate exceeds 3σ from running mean, prompt user to confirm entry ("This seems unusually high — is this correct?")
- Stale data (>14 days no fill-up): widen confidence interval by 20% per week. After 30 days, fall back to time-frequency estimation.
- CNG/LPG fuel: different energy density. Show pressure-based estimation warning. Use separate rate history from petrol.
- First fill-up: if user hasn’t declared tank capacity, prompt after logging. Pre-fill from vehicle DB if make/model known.

5.5 Vehicle Seed Rate Database
Make/Model | City L/100km | Highway L/100km | Tank (L) | Market
---|---|---|---|---
Suzuki Alto (660cc) | 5.8 | 5.2 | 32 | South Asia
Suzuki WagonR | 7.0 | 6.2 | 35 | South Asia
Suzuki Swift (1.2L) | 7.5 | 6.0 | 37 | South Asia / Global
Toyota Corolla (1.8L) | 9.8 | 7.5 | 50 | Global
Toyota Yaris (1.3L) | 8.5 | 6.8 | 42 | Global
Honda City (1.5L) | 9.2 | 7.0 | 40 | South Asia / SE Asia
Honda Civic (1.5T) | 10.5 | 7.8 | 47 | Global
Hyundai Tucson (2.0L) | 12.0 | 9.0 | 62 | Global
Toyota Land Cruiser | 17.5 | 13.0 | 93 | Middle East
Generic Small (fallback) | 9.0 | 7.0 | 45 | All

6. Daily Engagement System — 15-Second Habit Loop
6.1 The Core Problem
Car maintenance apps are inherently periodic (oil changes every 3-6 months). No app achieves daily engagement because none offers daily value. AutoMinder solves this with 5 micro-interactions that take under 15 seconds each but provide real daily value.

6.2 Daily Micro-Interactions
Interaction | Trigger | Duration | Value Delivered | Engagement Mechanic
---|---|---|---|---
Morning Pre-Drive Check | Push notification at learned departure time -15min | 15 seconds | Peace of mind. Safety. | Streak counter +1. Daily tip.
Fill-Up Logger | Geo-trigger at known fuel stations OR manual tap | 20 seconds | Fuel cost awareness. Algorithm learns. | Accuracy badge improves.
Mileage Quick-Update | End-of-day prompt OR +50/+100/+500 chips | 5 seconds | Reminders stay accurate. | Health score updates.
Post-Trip Eco Score | After detected trip (via Bluetooth/sensor) | 3 seconds | Financial awareness. Saves money. | Weekly leaderboard.
Weekly Health Summary | Sunday 6pm notification | Passive | "Your car health: 84. Oil change in 18 days." | Streak protection alert if needed.

6.3 Pre-Drive Checklist — 6-Item Visual Check
Item | Weather Condition | Auto-Show | One-Tap Response
---|---|---|---
Tire pressure check | After temp drop >10°C overnight | Yes (seasonal) | OK / Flag
Fuel level sufficient | Always (if fuel estimate enabled) | Yes | OK / Go to fuel screen
Warning lights clear | Always | Yes | OK / Flag (opens tips)
Lights working | Low visibility forecast | Conditional | OK / Flag
Wipers OK | Rain forecast | Conditional | OK / Flag
Windows defrosted | Temperature <2°C | Conditional | OK / Flag

6.4 Gamification System
- Care Streak: consecutive days completing pre-drive check. Flame counter visible on dashboard. Green → Gold after 30 days.
- Streak Freeze: 1 per week (like Duolingo). Prevents churn after missed days.
- Car Health Score 0-100: updates daily. OVERDUE = -15pts each, DUE_SOON = -7pts, GOOD = full. Visible number users want to improve.
- Eco-Driving Score per trip: 0-100 based on inferred avg consumption vs vehicle baseline. Weekly average shown.
- Monthly Cost Report: "You spent PKR 12,400 on fuel and PKR 3,500 on service in April." Financial awareness drives engagement.
- Milestone badges: First reminder set, 30-day streak, 100th mileage log, “Fuel prophet” (10+ fill-ups logged).

6.5 Smart Notification Schedule
Notification Type | Max Frequency | Suppression Rule | Channel
---|---|---|---
Morning pre-drive check | 1 per day | Suppress if app opened in last 2 hours | Daily Habit (LOW)
Fuel reminder | On trigger only | Max 1 per 12 hours per vehicle | Fuel (HIGH if Red zone, DEFAULT otherwise)
Maintenance OVERDUE | 1 per 24 hours | lastNotifiedAt cooldown enforced | OVERDUE (HIGH)
Maintenance DUE_SOON | 1 per 3 days | lastNotifiedAt cooldown enforced | DUE_SOON (DEFAULT)
30-day planning alert (insurance/registration) | 1 per 30 days | Only in 21-30 day window before due | Planning (DEFAULT)
Weekly health summary | 1 per week | Sunday 6pm only | Summary (LOW)
Streak protection | Max 1 per day | Only if streak >7 days at risk | Engagement (DEFAULT)
Never send | Never | Marketing, upsells disguised as utility, duplicate content | —

7. 2026 Premium UI/UX System
7.1 Design Philosophy
"Faster than paper, smarter than memory." AutoMinder should feel like a premium tool, not a database form. Every interaction should be no more than 2 taps. Every screen should have one clear primary action.

7.2 Brand Design Tokens
Token | Value | Usage
---|---|---
Primary | #006B5F (Racing Teal 700) | Primary buttons, FABs, logo, active states
Font Display | Exo 2, weight 700-800 | Vehicle names, health score, large numbers, headlines
Font Body | Nunito Sans, weight 400-600 | All UI text, labels, descriptions, lists
Font Mono | JetBrains Mono, weight 500-600 | Odometer readings, fuel costs, km values, dates
OVERDUE | MaterialTheme.colorScheme.errorContainer + onErrorContainer | All overdue status indicators
DUE_SOON | MaterialTheme.colorScheme.tertiaryContainer + onTertiaryContainer | Upcoming due indicators
GOOD | MaterialTheme.colorScheme.secondaryContainer + onSecondaryContainer | Healthy status indicators
Card corners OVERDUE | 8dp — sharp = urgent | animateFloatAsState shape morphing
Card corners DUE_SOON | 16dp — moderate | animateFloatAsState shape morphing
Card corners GOOD | 28dp — rounded = calm | animateFloatAsState shape morphing
Dark mode base | #121212 (never pure black) | Surface 0
Dark mode card | #2A2A2A | Surface 1 (cards)
Dark mode elevated | #3A3A3A | Surface 2 (dialogs, bottom sheets)

7.3 M3 Expressive Components to Use (BOM 2025.06.01)
- NavigationSuiteScaffold — replaces deprecated calculateWindowSizeClass(). Auto-adapts phone/tablet/foldable. 3 bottom tabs.
- FloatingActionButton with SpeedDial pattern — Log Service (primary) + Add Vehicle (secondary).
- animateFloatAsState with spring(dampingRatio=DampingRatioMediumBouncy, stiffness=StiffnessMedium) on ALL status transitions.
- AnimatedContent for status badge transitions (OVERDUE → DUE_SOON → GOOD).
- PullToRefreshBox — on Dashboard and VehicleList.
- ModalBottomSheet — for Add Vehicle quick-flow and fuel log entry.
- LargeTopAppBar with scrollBehavior on VehicleDetail, Dashboard.
- SegmentedButton (3-way) for reminder trigger mode: Date | Km | Both.
- FlowRow with FilterChips for ServiceType selection.
- DatePickerDialog for all date selection.
- SwipeToReveal on reminder and service list items (snooze, delete, edit).
- LazyColumn with stickyHeader() on ServiceHistory timeline (month headers).

7.4 Screen-by-Screen UI Audit Scores
Screen | Current Score | Target | Top 3 Fixes
---|---|---|---
Dashboard | 72/100 | 95/100 | 1. UrgencyBanner. 2. SpeedDial FAB. 3. Sort by status not alpha.
Vehicle Detail | 90/100 | 98/100 | 1. Health score ring animation. 2. Fuel tab. 3. Quick actions row.
Onboarding | 70/100 | 95/100 | 1. 2 pages only. 2. Create vehicle on page 2. 3. POST_NOTIFICATIONS inline.
Service History | 45/100 | 90/100 | 1. Move from bottom tab to VehicleDetail tab. 2. Month headers. 3. Cost totals.
Mileage Log | 80/100 | 92/100 | 1. +50/+100/+500 chips. 2. Trip distance calculation. 3. Sparkline.
Settings | 78/100 | 90/100 | 1. Notify X days before slider. 2. Driving style profile. 3. Dark mode preview.
Fuel Screen | NEW | 95/100 | Full spec in Section 5. Tank visual + confidence range + quick log.
Analytics | NEW | 90/100 | Monthly cost bar chart. Cost per km trend. Comparison to vehicle baseline.

8. Maintenance Interval Algorithm
8.1 Dual-Trigger System (Both Must Fire)
Every maintenance item has a mileage trigger AND a time trigger. Whichever comes first activates the reminder. This is industry standard (Honda Maintenance Minder, Toyota service schedules). Single-trigger apps are inferior.
Service | Normal KM | Normal Days | Severe KM | Severe Days | Notes
---|---|---|---|---|---
Oil Change | 8,000 | 180 | 5,000 | 90 | 80% of drivers are "severe" without knowing it
Tire Rotation | 8,000 | 180 | 6,000 | 120 | Extend tire life 40-80%
Air Filter | 24,000 | 365 | 16,000 | 180 | Dusty environments (PK/ME): more frequent
Cabin Filter | 16,000 | 365 | 12,000 | 180 | City pollution warrants shorter intervals
Brake Fluid | 40,000 | 730 | 32,000 | 365 | Safety-critical. Never skip.
Spark Plugs (copper) | 30,000 | null | 20,000 | null | Iridium: 96,000 km
Coolant | 60,000 | 1825 | 40,000 | 1095 | Extreme heat markets: shorter
Battery Test | null | 365 | null | 180 | Extreme heat: more frequent (Middle East)
Tires (replacement) | 60,000 | 1825 | 50,000 | 1460 | Visual inspection monthly
Insurance | null | 365 | null | 365 | 30-day planning alert required
Registration | null | 365 | null | 365 | 30-day planning alert required
Inspection/MOT | null | 365 | null | 365 | Market-specific (EU mandatory, US varies)

8.2 Severe-Duty Auto-Detection
80% of drivers are under severe conditions but self-classify as "normal". AutoMinder auto-detects:
- Average trip length < 8km → SEVERE (city short-trip = engine never reaches optimal temp)
- User declares "City" driving style → SEVERE
- Region classified as dusty/sandy (South Asia, Middle East, North Africa) → SEVERE
- Vehicle age > 10 years → apply 10% earlier intervals
When SEVERE detected: reduce all intervals by 30%. Show user: "Based on your driving, we recommend oil changes every 5,000 km instead of 8,000 km."

(...)
(Sections 9 and 10 intentionally omitted from PRD as they exist natively in CLAUDE.md)

11. 30-Day Build Roadmap
Phase | Days | Milestone | Critical
---|---|---|---
P1: Foundation | 1-2 | libs.versions.toml correct. build.gradle.kts clean. ./gradlew compileDebugKotlin PASSES. AutoMinderApp @HiltAndroidApp. Theme system complete. | YES
P2: Data Layer | 3-4 | 5 entities. 5 DAOs. AppDatabase v2. Converters.kt. DatabaseModule. /schemas committed. Unit tests pass. | YES
P3: Domain Layer | 5-6 | Repository interfaces. RepositoryImpl with try/catch. StatusCalculator FIXED. FuelIntelligenceEngine. HealthScoreUseCase. Turbine tests. | YES
P4: Navigation | 7 | NavGraph.kt. All 17 routes registered. NavigationSuiteScaffold. Onboarding vs main conditional. Back stack correct. Zero raw strings. | YES
P5: Vehicle CRUD | 8-10 | VehicleList. AddEditVehicle. VehicleDetail with tabs. Photo picker. Data survives process kill. | NO
P6: Reminder Engine | 11-14 | ReminderList. AddEditReminder. ReminderCheckWorker. 4 notification channels. BootReceiver. Mark Done from notification. | YES
P7: Dashboard | 15-16 | Dashboard live data. UrgencyBanner. SpeedDial FAB. OVERDUE sorts first. Dark mode correct. Pull-to-refresh. | YES
P8: Service + Mileage | 17-19 | ServiceHistory timeline. AddEditService. Auto-reset @Transaction. MileageLog with quick chips. | YES
P9: Fuel Intelligence | 20-22 | FuelEntry entity. FuelIntelligenceEngine (4 phases). FuelReminderWorker. FuelScreen with tank visual. | YES
P10: Daily Engagement | 23-24 | DailyCheckWorker. Morning pre-drive check notification. Streak counter. Eco-score display. Analytics screen. | NO
P11: Monetization + Onboarding | 25-27 | Onboarding 3-page flow. AdMob (ServiceHistory + VehicleList only). Premium IAP. isPremium DataStore flag. | NO
P12: Production Launch | 28-30 | ProGuard rules. Baseline profile. Signed AAB. Play Store listing. Internal track. Performance audit. | YES

12. Production Readiness Checklist
12.1 Build System (Critical — all must pass)
- Package com.autominder.app in ALL .kt files (find+replace verify)
- libs.versions.toml: Kotlin 2.1.21, KSP 2.1.21-2.0.1, AGP 8.9.1
- Zero kapt() anywhere in project (grep confirms)
- ./gradlew compileDebugKotlin: PASS
- ./gradlew assembleRelease with minifyEnabled=true: PASS
- AdMob IDs read from local.properties with test ID fallback (never empty string)

12.2 Architecture (Critical)
- StatusCalculator: OVERDUE wins over SNOOZED (unit test confirms)
- Auto-reset wrapped in @Transaction (insertService + updateReminder atomic)
- lastNotifiedAt cooldown on all WorkManager notifications
- No fallbackToDestructiveMigration() (grep confirms)
- /schemas directory committed to git with version JSON files

12.3 Fuel Algorithm
- FuelEntry entity in AppDatabase v2 with proper Migration(1,2)
- Outlier rejection: entries >3σ from mean prompt user confirmation
- Partial fill accumulation working correctly
- Phase badge shown in UI (builds user trust)
- Confidence range displayed as km range (never percentage alone)

12.4 UI/UX
- Every screen: 4 states (Loading/Empty/Error/Success)
- Every LazyColumn: key{item.id}
- Every Icon/Image: contentDescription
- stateIn(WhileSubscribed(5000)) on all list ViewModels (no nav flash)
- Dashboard: OVERDUE sorts first, never alphabetical
- ServiceHistory: inside VehicleDetail tab, NOT a bottom tab
- All colors via MaterialTheme.colorScheme (zero hardcoded Color())
- Dark mode full pass: all screens tested
- Touch targets: minimum 48x48dp

12.5 Play Store Launch
- ProGuard rules: keep Room, Hilt, Compose, Coil, AdMob, Billing classes
- google-services.json in place (pre-flight requirement)
- Release signing config: reads from local.properties only
- versionCode incremented for every Play Store upload
- Store listing: icon, screenshots (phone + tablet), feature graphic, description
- Privacy policy URL (required for apps that handle personal data)

13. Monetization Strategy
AutoMinder uses a Free + One-Time Purchase model. Research confirms users viscerally reject subscriptions for utility apps. Drivvo lost user trust permanently by raising prices 4x.
13.1 Free Tier (forever free)
- Up to 2 vehicles
- All reminder types (date + mileage dual-trigger)
- Fuel tracking and intelligent reminders
- Pre-drive daily check
- Service history and mileage log
- AdMob banners (ServiceHistory + VehicleList only)
- AdMob interstitial (after AddService, every 3rd time)

13.2 AutoMinder Pro ($4.99 one-time — never subscription)
- Unlimited vehicles
- Remove all ads
- CSV + JSON data export
- Advanced analytics: cost per km, year-over-year comparison
- Priority customer support
- Future premium features (OBD integration, VIN scan)

13.3 Ad Placement Rules
- Dashboard: ZERO ads. This is the trust-building home screen.
- VehicleDetail: ZERO ads. Premium feel for core usage.
- ServiceHistory: BannerAd at bottom (below the fold).
- VehicleList: BannerAd at bottom (if user has >2 vehicles in free tier).
- After AddService: Interstitial (max every 3rd logged service).
- BuildConfig.ENABLE_ADS gate: false in debug. Ads never show in development.

14. Closing Statement
The car maintenance app market has 69 million annual breakdowns, 91.8% driver neglect, and no global winner. AutoMinder’s three pillars — offline fuel intelligence, daily habit loop, and local-first architecture — address the exact gaps the competition has failed to solve.

The technical foundation eliminates the agent context amnesia that caused days of debugging. Every session starts with the same locked package name, the same tech stack, the same architecture laws. Ships fast. Stays stable.

"Faster than paper. Smarter than memory. Two taps to log. Fifteen seconds to check. One number to know your car is healthy."

AutoMinder PRD v4.0 — Production Release 2026
Package: com.autominder.app • Kotlin 2.1.21 • M3 Expressive
