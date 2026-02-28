# Car-maintainence-reminder

Car Maintenance Reminder
Product Requirements Document & Technical Roadmap
Version 1.0 | Android Native (Kotlin + Jetpack Compose)
Production-Ready Implementation Guide
Document Type
PRD + Technical Architecture + Build Plan
Target Platform
Android 8.0+ (API 26+)
Tech Stack
Kotlin, Jetpack Compose, Room, Hilt, WorkManager
Primary Objective
100% offline-first, bulletproof reminders, instant load


Table of Contents



1. Executive Summary
This PRD defines a production-ready Car Maintenance Reminder Android application designed to solve real problems faced by car owners: missed oil changes, forgotten insurance renewals, ignored filter/brake/tire services, and lack of clear visibility into upcoming maintenance.
1.1 Product Vision
A fast, reliable, offline-first maintenance tracker that answers "What do I need to do next?" within 3 seconds of app launch. The app targets non-technical car owners who need simple, dependable reminders without complexity.
1.2 Success Metrics
Cold start time < 1.5 seconds on mid-range devices
Zero missed reminders (bulletproof notification system)
100% offline functionality for core features
< 3 taps to add a new service reminder
> 4.5 star rating on Google Play Store

2. Real User Problems Identified
Based on comprehensive market research analyzing CARFAX, Drivvo, Simply Auto, Fuelly, Openbay, and user feedback from Reddit/forums, the following critical problems have been validated:
2.1 What Car Owners Most Commonly Forget
Maintenance Item
Typical Consequence
Oil Changes
Engine damage, voided warranty
Insurance/Registration Renewal
Legal issues, fines, lapsed coverage
Air Filter Replacement
Reduced fuel efficiency, poor performance
Brake Pad Inspection
Safety hazard, rotor damage ($$$)
Tire Rotation
Uneven wear, premature replacement
Battery Check (3+ years)
Unexpected breakdown
Timing Belt (60-100k mi)
Catastrophic engine failure
Coolant Flush
Overheating, head gasket damage

2.2 Why Existing Apps Fail (Competitive Analysis)
Data Sync & Reliability Issues
Simply Auto: Users report data loss when switching phones, sync conflicts between family members, photos not syncing
Drivvo: Backup/restore is "finicky", requires careful manual export; cloud sync behind paywall
aCar: Acquired by Fuelly then stagnated, no longer works on newer Android devices (32-bit)
Poor User Experience
Drivvo: "Hard to navigate" despite powerful features - too many options, overwhelming for casual users
Simply Auto: "Bad UI", website barely works, dated/cluttered design
Most apps: Too much manual input required for every entry (5+ fields for a fuel log)
App Abandonment & Support
Simply Auto: "They never respond", "no support at all - stay away!" in reviews
Users fear investing time logging data in apps that might be abandoned
Key insight: "I don't want to start over again" - users burned by past app shutdowns
Regional Limitations
CARFAX: North America only - unusable in Europe, Asia, etc.
Openbay: Service marketplace only useful in major US cities
No global app properly handles units (km vs mi), currencies, or region-specific inspections
2.3 User Expectations (First 3 Seconds)
When opening the app, users expect to immediately see:
What maintenance is OVERDUE (red, urgent)
What's coming up SOON (yellow/orange, warning)
Quick action to mark something complete or snooze
Clear visual hierarchy without clutter
2.4 Edge Cases to Handle
Multiple vehicles: Families with 2-4 cars, small business fleets
Irregular driving: Some users drive 3k miles/year, others 30k miles/year
Selling a car: Clean export of service history, ability to archive/delete vehicle
Shared vehicles: Multiple family members tracking same car
New vs old cars: Different maintenance needs (warranty tracking vs timing belts)

3. Feature Requirements
3.1 Critical Features (Must Have - MVP)
Feature
Details
Dashboard Home
Shows overdue (red), upcoming (yellow), healthy (green) items. Answers "what's due" in <3 sec
Vehicle Management
Add/edit/delete multiple vehicles. Store: make, model, year, VIN, current mileage, photo
Service Reminders
Time-based AND mileage-based intervals. Support both simultaneously per service
Core Service Items
Oil change, filters (air/cabin/fuel), brakes, tires, battery, fluids, belts
Document Reminders
Insurance renewal, registration, inspection/MOT, driver's license
Service History
Log completed services with date, mileage, cost, notes, optional photo
Notifications
Push notifications at configurable intervals (30/14/7/3/1 day before)
Offline-First
100% functionality without internet. Local Room database as source of truth
Quick Actions
Mark service complete, snooze reminder, update mileage - all <3 taps
Data Export
Export service history as PDF/CSV for resale or insurance claims

3.2 Nice-to-Have Features (V2)
Feature
Rationale
Cloud Backup/Sync
Users want this but MVP must work offline-first. Add in V2 with conflict resolution
Receipt Photo OCR
Auto-extract date/cost from receipt photos. Reduces manual input
Fuel Tracking
Track MPG and fuel costs. Popular but not core to maintenance reminders
Cost Analytics
Charts showing maintenance spend over time, per category
Family Sharing
Multiple users tracking shared vehicles. Complex sync logic required
OBD-II Integration
Auto-read mileage and diagnostics. Hardware dependency, niche audience
Widgets
Home screen widget showing next due item. Android-specific UX enhancement

3.3 Features to Avoid/Delay
Feature
Why Avoid
Service Marketplace
Openbay model - regional, requires partnerships, distracts from core value
Social/Community
Car forums exist. Community features add complexity without solving core problems
AI Predictions
"Your battery might fail soon" - requires data science, accuracy concerns
Trip Logging
Mileage tracking for taxes - different use case, separate app territory
Subscription Model
Users hate subscriptions for simple utilities. One-time purchase or freemium
Ads in Free Version
Frustrates users. Better: free with limits, premium unlocks more vehicles


4. UI/UX Design Guidelines
4.1 Design Principles
Clarity Over Features: Every screen answers ONE question. Home = "What's due?"
3-Second Rule: Primary information visible immediately, no scrolling required
Minimal Input: Smart defaults, auto-fill where possible, optional fields clearly marked
Accessible: 48dp+ tap targets, WCAG AA contrast ratios, Material 3 guidelines
Predictable: Consistent navigation, familiar patterns, no surprises
4.2 Color System
Status
Color
Usage
Overdue
#E53935 (Red)
Past due items, urgent alerts, error states
Warning
#FF9800 (Orange)
Due within 7 days or 500 miles
Upcoming
#FFC107 (Amber)
Due within 30 days, needs attention soon
Healthy
#4CAF50 (Green)
Recently completed, good status
Primary
#1F4E79 (Blue)
CTAs, navigation, brand elements
Surface
#FFFFFF/#121212
Card backgrounds (light/dark mode)

4.3 Screen-by-Screen Structure
Home/Dashboard
Purpose: Answer "What do I need to do next?" immediately
Vehicle selector at top (if multiple cars)
Prominent "Overdue" section with red badge count
"Coming Soon" section with yellow/orange items
Quick-action FAB: "+ Add Service" or "Update Mileage"
Each item shows: Service name, due date/mileage, status color, tap to expand
Add/Edit Service Screen
Purpose: Quick entry with smart defaults
Service type picker (common items as quick-select chips)
Auto-suggest intervals based on service type (e.g., oil = 5000 mi / 6 mo)
Toggle: Time-based / Mileage-based / Both
Date picker, mileage input with validation
Optional: cost, notes, receipt photo
Service History
Purpose: Timeline of all completed services
Chronological list with filters (by service type, date range)
Each entry: Service name, date, mileage, cost (if entered)
Expand for notes, photos, edit/delete options
Export button (PDF/CSV)
Vehicle Management
Purpose: Manage all vehicles in one place
Card-based list of all vehicles with photo thumbnail
Vehicle details: Make, Model, Year, VIN, Color, License Plate
Current mileage with "Update" quick action
Archive vehicle (for sold cars, keeps history)
4.4 Navigation Structure
Bottom Navigation Bar (4 items maximum per Material guidelines):
Icon
Label
Destination
🏠
Home
Dashboard with overdue/upcoming items
📋
History
Service history timeline
🚗
Vehicles
Vehicle management list
⚙️
Settings
Preferences, notifications, export, about


5. Architecture & Technical Design
5.1 Architecture Overview
Clean MVVM Architecture with Single Source of Truth (Room Database)
Layer Structure:
UI Layer: Jetpack Compose screens + ViewModels (StateFlow for UI state)
Domain Layer: Use cases encapsulating business logic
Data Layer: Repository pattern → Room DAOs → SQLite
DI Layer: Hilt modules for dependency injection
5.2 Module Structure
:app - Main application module:core:database - Room entities, DAOs, migrations:core:common - Shared utilities, extensions:feature:dashboard - Home screen feature:feature:service - Add/edit service feature:feature:history - Service history feature:feature:vehicle - Vehicle management feature:feature:settings - Settings feature
5.3 Key Design Decisions
Decision
Rationale
Room as Source of Truth
Offline-first requirement. Network sync (V2) writes to Room, UI reads from Room only
StateFlow over LiveData
Better null safety, Flow operators, Compose integration, lifecycle-aware collection
Hilt over Koin
Compile-time verification, better Android integration, Google-recommended
WorkManager for Reminders
Survives app kill, handles Doze, battery optimization, guaranteed execution
Single-Activity + Compose
Modern architecture, navigation-compose, better back stack handling
No business logic in Composables
Composables are pure UI. All logic in ViewModels/UseCases for testability

5.4 Anti-Patterns to Avoid
NO direct database calls from ViewModels - always go through Repository
NO Android framework dependencies in domain layer
NO mutable state exposed from ViewModels - expose StateFlow with private MutableStateFlow
NO blocking calls on Main thread - use viewModelScope + Dispatchers.IO
NO hardcoded strings - use string resources for i18n

6. Data Model Design
6.1 Entity Definitions
Vehicle Entity
@Entity(tableName = "vehicles")data class VehicleEntity(    @PrimaryKey val id: String = UUID.randomUUID().toString(),    val make: String,    val model: String,    val year: Int,    val vin: String? = null,    val licensePlate: String? = null,    val color: String? = null,    val photoUri: String? = null,    val currentMileage: Int,    val mileageUnit: MileageUnit = MileageUnit.MILES,    val isArchived: Boolean = false,    val createdAt: Long = System.currentTimeMillis(),    val updatedAt: Long = System.currentTimeMillis())
ServiceItem Entity (Template)
@Entity(tableName = "service_items")data class ServiceItemEntity(    @PrimaryKey val id: String = UUID.randomUUID().toString(),    val vehicleId: String,    val serviceType: ServiceType,    val customName: String? = null,    val intervalMonths: Int? = null,  // Time-based    val intervalMiles: Int? = null,   // Mileage-based    val lastServiceDate: Long? = null,    val lastServiceMileage: Int? = null,    val nextDueDate: Long? = null,    val nextDueMileage: Int? = null,    val isEnabled: Boolean = true,    val notes: String? = null,    val createdAt: Long = System.currentTimeMillis())
ServiceRecord Entity (History)
@Entity(tableName = "service_records")data class ServiceRecordEntity(    @PrimaryKey val id: String = UUID.randomUUID().toString(),    val vehicleId: String,    val serviceItemId: String,    val serviceType: ServiceType,    val serviceDate: Long,    val mileageAtService: Int,    val cost: Double? = null,    val currency: String = "USD",    val notes: String? = null,    val receiptPhotoUri: String? = null,    val shopName: String? = null,    val createdAt: Long = System.currentTimeMillis())
ReminderSchedule Entity
@Entity(tableName = "reminder_schedules")data class ReminderScheduleEntity(    @PrimaryKey val id: String = UUID.randomUUID().toString(),    val serviceItemId: String,    val vehicleId: String,    val reminderDaysBefore: List<Int> = listOf(30, 14, 7, 3, 1),    val isEnabled: Boolean = true,    val lastNotifiedAt: Long? = null,    val snoozedUntil: Long? = null,    val workRequestId: String? = null)
6.2 ServiceType Enum
enum class ServiceType {    // Engine & Fluids    OIL_CHANGE, OIL_FILTER, TRANSMISSION_FLUID, COOLANT_FLUSH,    BRAKE_FLUID, POWER_STEERING_FLUID,        // Filters    AIR_FILTER, CABIN_FILTER, FUEL_FILTER,        // Brakes & Tires    BRAKE_PADS, BRAKE_ROTORS, TIRE_ROTATION, TIRE_REPLACEMENT,    WHEEL_ALIGNMENT,        // Battery & Electrical    BATTERY, SPARK_PLUGS,        // Belts & Hoses    TIMING_BELT, SERPENTINE_BELT,        // Documents    INSURANCE, REGISTRATION, INSPECTION, EMISSIONS_TEST,        // Other    CUSTOM}
6.3 Default Intervals by Service Type
Service
Time
Mileage
Oil Change (Conventional)
3 months
3,000-5,000 mi
Oil Change (Synthetic)
6 months
7,500-10,000 mi
Air Filter
12 months
15,000-30,000 mi
Cabin Filter
12 months
15,000-25,000 mi
Tire Rotation
6 months
5,000-7,500 mi
Brake Inspection
12 months
12,000-15,000 mi
Battery Check
36 months
N/A
Transmission Fluid
24-36 months
30,000-60,000 mi
Coolant Flush
24-60 months
30,000-100,000 mi
Timing Belt
60-84 months
60,000-100,000 mi


7. Reminder System Design (Critical)
7.1 Requirements
NEVER miss a reminder - bulletproof reliability
Survive device reboot, app kill, Doze mode, battery optimization
Multiple notification intervals: 30, 14, 7, 3, 1 day before due date
Recovery mechanism for missed notifications
Respect user's notification preferences (quiet hours, channels)
7.2 Technology Choice: WorkManager
Why WorkManager over AlarmManager:
Factor
WorkManager Advantage
Doze Mode
Handles automatically - AlarmManager requires setExactAndAllowWhileIdle()
Battery Optimization
Battery-friendly by default, respects system constraints
Device Reboot
Built-in support with PeriodicWorkRequest
API Compatibility
Abstracts differences across API levels (JobScheduler vs AlarmManager)
Guaranteed Execution
Survives app death, process kill, system restarts
Chaining/Constraints
Can chain work, add network/charging constraints

7.3 Implementation Flow
Step 1: Schedule Daily Check Worker
// Run daily at 8 AM to check all upcoming remindersval dailyCheckRequest = PeriodicWorkRequestBuilder<ReminderCheckWorker>(    1, TimeUnit.DAYS).setInitialDelay(    calculateDelayUntil8AM(), TimeUnit.MILLISECONDS).addTag("daily_reminder_check").build()WorkManager.getInstance(context).enqueueUniquePeriodicWork(    "daily_reminder_check",    ExistingPeriodicWorkPolicy.KEEP,    dailyCheckRequest)
Step 2: ReminderCheckWorker Logic
class ReminderCheckWorker : CoroutineWorker {    override suspend fun doWork(): Result {        // 1. Get all enabled service items        // 2. For each item, check if due date matches reminder intervals        // 3. Check if already notified for this interval        // 4. If not notified, show notification and mark as notified        // 5. Handle overdue items specially (daily reminder)        return Result.success()    }}
Step 3: Notification Decision Logic
fun shouldNotify(item: ServiceItem, reminderDays: List<Int>): Boolean {    val daysUntilDue = calculateDaysUntilDue(item)        // Check time-based due date    if (item.nextDueDate != null) {        if (daysUntilDue in reminderDays && !alreadyNotified(item, daysUntilDue)) {            return true        }        if (daysUntilDue < 0) { // Overdue            return !notifiedTodayForOverdue(item)        }    }        // Check mileage-based (requires recent mileage update)    if (item.nextDueMileage != null) {        val milesRemaining = item.nextDueMileage - currentMileage        if (milesRemaining <= 500) { // Warn at 500 miles            return !alreadyNotifiedForMileage(item)        }    }    return false}
7.4 Failure Recovery
Boot Receiver: Re-schedule WorkManager tasks on device reboot
App Start Check: Verify WorkManager is scheduled, re-schedule if not
Catch-Up Logic: On app open, check for any missed reminders and show them
Overdue Escalation: Overdue items get daily notifications until addressed
7.5 Notification Channels
// Create channels in Application.onCreate()val urgentChannel = NotificationChannel(    "maintenance_urgent",    "Urgent Reminders",    NotificationManager.IMPORTANCE_HIGH).apply { description = "Overdue and imminent maintenance" }val normalChannel = NotificationChannel(    "maintenance_normal",    "Maintenance Reminders",    NotificationManager.IMPORTANCE_DEFAULT).apply { description = "Upcoming maintenance reminders" }

8. Performance & Stability
8.1 Performance Targets
Metric
Target
Cold Start Time
< 1.5 seconds on Snapdragon 6-series
Warm Start Time
< 500ms
Frame Rate
60fps during scrolling, no jank
Memory Usage
< 100MB heap in typical usage
APK Size
< 15MB (minified, without proguard)
Database Query
< 50ms for dashboard load
ANR Rate
< 0.1%
Crash Rate
< 0.5%

8.2 Optimization Strategies
Startup Optimization
App Startup Library: Initialize Hilt, Room in background before first frame
Lazy Initialization: Only load active vehicle's data on startup
Baseline Profiles: Pre-compile hot paths using Macrobenchmark
Compose Optimization
Stable Classes: Use @Immutable/@Stable annotations on data classes
Key Parameters: Always use key() in LazyColumn for stable recomposition
derivedStateOf: Use for computed values to avoid unnecessary recomposition
remember: Cache expensive computations and lambdas
Database Optimization
Indices: Add indices on vehicleId, serviceType, nextDueDate for fast queries
Projections: Select only needed columns, not entire entities
Batch Operations: Use @Transaction for related inserts/updates
8.3 Risks to Monitor
Image Loading: Vehicle photos could cause OOM - use Coil with memory cache limits
History List: Users with years of records - implement pagination
Background Work: WorkManager can be delayed by OEMs - test on Samsung, Xiaomi, Huawei

9. Seven-Day Build Plan
This plan delivers a functional MVP in 7 days. Each day has clear deliverables and acceptance criteria.
Day 1: Project Setup & Data Layer
Tasks:
Create project with Compose, Hilt, Room, Navigation dependencies
Define all Room entities: Vehicle, ServiceItem, ServiceRecord, ReminderSchedule
Create DAOs with queries for dashboard (overdue, upcoming items)
Implement Repository layer with offline-first logic
Write unit tests for critical queries
Deliverable: App compiles, Room tests pass, data layer complete
Day 2: Vehicle Management Feature
Tasks:
Create VehicleListScreen with card-based UI
Create AddEditVehicleScreen with form validation
Implement VehicleViewModel with StateFlow
Add vehicle photo picker (camera + gallery)
Implement mileage update quick action
Deliverable: Can add/edit/delete vehicles with photos, update mileage
Day 3: Service Items & Dashboard
Tasks:
Create DashboardScreen with overdue/upcoming sections
Implement color-coded status indicators (red/orange/yellow/green)
Create AddServiceScreen with service type picker
Auto-populate default intervals by service type
Support both time-based and mileage-based intervals
Deliverable: Dashboard shows upcoming/overdue items, can add new service reminders
Day 4: Service History & Completion Flow
Tasks:
Create ServiceHistoryScreen with timeline view
Implement "Mark Complete" flow from dashboard
Auto-calculate next due date based on completion
Add cost/notes/photo fields to completion flow
Implement history filtering by service type and date
Deliverable: Can mark services complete, view history with filters
Day 5: Reminder System
Tasks:
Create notification channels (urgent, normal)
Implement ReminderCheckWorker with WorkManager
Schedule daily check at 8 AM
Implement notification display with deep links
Add boot receiver for WorkManager re-scheduling
Test on device with Doze mode simulation
Deliverable: Notifications work reliably, survive reboot
Day 6: Settings & Export
Tasks:
Create SettingsScreen with notification preferences
Implement unit selection (miles/km, currency)
Create PDF export for service history
Create CSV export for data portability
Add theme toggle (light/dark/system)
Implement snooze functionality for reminders
Deliverable: Settings functional, can export data
Day 7: Polish & Testing
Tasks:
Complete bottom navigation setup
Add empty states for all screens
Add loading states and error handling
Run full UI test suite
Performance profiling with Android Studio
Build signed APK/AAB for release
Deliverable: Production-ready MVP build

10. Final Engineering Verdict
10.1 Competitive Advantages
100% Offline-First: Unlike competitors relying on cloud sync that fails
Bulletproof Reminders: WorkManager + recovery logic beats competitors' unreliable notifications
Simplicity: Focused feature set avoids "feature bloat" of Drivvo/Simply Auto
Modern Stack: Kotlin + Compose vs aging Java/XML competitors
Data Portability: Export prevents user lock-in anxiety
10.2 Risk Assessment
Risk
Likelihood
Mitigation
OEM Battery Restrictions
High
Test on Samsung/Xiaomi, guide users to whitelist app
User Doesn't Update Mileage
High
Prompt on app open if stale, mileage-based reminders secondary
Competition Copies Features
Medium
First-mover advantage, rapid iteration, user trust
Play Store Rejection
Low
Follow all guidelines, no sensitive permissions

10.3 Go/No-Go Decision
RECOMMENDATION: GO
The market analysis reveals significant opportunity: competitors have reliability issues (Simply Auto data loss), support problems (abandoned apps), and UX complexity (Drivvo). A focused, offline-first, bulletproof-reminder app addresses the core user need without the baggage. The 7-day build plan is achievable with modern Android tooling. The feature set is minimal but complete. Success criteria are measurable. Ship it.
— End of Document —
