# Graph Report - Autominder  (2026-08-21)

## Corpus Check
- 259 files · ~158,248 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1918 nodes · 4131 edges · 132 communities (107 shown, 25 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 76 edges (avg confidence: 0.85)
- Token cost: 584,660 input · 0 output

## Community Hubs (Navigation)
- Fuel History & Efficiency
- Onboarding Flow
- Billing & Settings
- Quote Auditor
- Add Service Flow
- Service Type Model
- Ad Manager
- Vehicle Repository
- Edit Vehicle & Validation
- Design Blueprint Docs
- Add Fuel Flow
- Vehicle Detail Screen
- Service History ViewModel
- Reminder Repository
- Reminder DAO & Mapper
- Service Repository
- Vehicle DAO & Entity
- Vehicle Detail Charts
- Mileage Log Repository
- Dashboard Data Use Case
- Service Status Model
- Reminder Priority Engine
- Add/Edit Save Flow
- Service DAO & Mapper
- Reminder Priority Tests
- Predict Due Use Case
- Premium Paywall
- Nav Routes
- Mileage Log DAO
- Distance & Date Utilities
- Service Repository Tests
- Database Module & Migration
- Fuel DAO & Mapper
- Premium Status Style
- Reminder Check Worker Tests
- PRD Overview
- Backup Manager
- Dashboard Screen
- Validators Tests
- Counter Mode Flow Docs
- CLAUDE.md Governance
- Motion & Animation Theme
- Vehicle Hero Card
- Dashboard ViewModel
- Home Screen Widget
- Vehicle List Tests
- Main Activity & Theme
- Dashboard Recent Activity
- Nav Graph & Dialogs
- Mileage Log ViewModel
- Edit Reminder ViewModel
- Play Store Checklist
- Design System Docs
- README & Gemini Docs
- Backup Coordinator
- Add Reminder ViewModel
- Service Completion Model
- AutoMinder App Init
- Analytics Helper
- Backup Agent
- Export Service History
- Reminder Check & Status Calc
- Service Detail Screen
- Dashboard Vehicle Status Card
- Add Reminder ViewModel (dup cluster)
- Edit Reminder ViewModel (dup cluster)
- Product Strategy Doc
- Premium Components Skill
- Service Completion Transaction Test
- Vehicle List Screen
- Reminder Detail Sheet
- Reminder Staleness Test
- Service Detail ViewModel
- Vehicle List ViewModel
- Status Calculator Tests
- Mileage Log ViewModel Tests
- PLAN.md Roadmap
- UI Slice Skill
- Mileage Log ViewModel (variant)
- Backup Coordinator Tests
- UI Audit 2026 Doc
- UI Rules Governance
- Agents & Governance Docs
- Skeleton Loading Component
- Mileage Log Screen
- Fuel History Screen
- Service History Screen
- Add Vehicle ViewModel
- Service History ViewModel Tests
- Reminder Alarm Scheduler Tests
- Notification Action Receiver
- About Screen
- Add Vehicle ViewModel (variant)
- Reminder Alarm Scheduler
- Overdue Copy Tests
- Handoff Doc
- Test Runner
- Power Settings
- Dashboard What's Next Section
- Dashboard Quick Log Section
- Edit Vehicle Screen
- Play Launch Scorecard
- Release Checklist v1.1
- Monetization Rules
- Notification Helper
- App Lifecycle Observer
- Reminder Alarm Receiver
- System Event Receiver
- Stitch Proxy Script
- Consent Manager
- Crashlytics & App Info
- Insight Metric Card
- Premium Action Grid
- Work Scheduler
- Gradle Wrapper
- Update Helper
- Bottom Nav Bar
- Banner Ad View
- Permission Utils
- Pro Feature Gate
- Baseline Profile Generator
- MCP Config
- Play Listing Short Desc
- Play Listing Title
- Figma MCP Lessons Doc
- Favicon Icon

## God Nodes (most connected - your core abstractions)
1. `ServiceType` - 85 edges
2. `Vehicle` - 77 edges
3. `Reminder` - 61 edges
4. `Service` - 51 edges
5. `IVehicleRepository` - 47 edges
6. `ServiceStatus` - 45 edges
7. `UserPreferences` - 44 edges
8. `FuelEntry` - 44 edges
9. `ReminderDao` - 34 edges
10. `DistanceUtil` - 34 edges

## Surprising Connections (you probably didn't know these)
- `Quote Auditor v0 (no OCR, no photo)` --semantically_similar_to--> `Counter Mode (Quote Auditor UI)`  [INFERRED] [semantically similar]
  PRODUCT_STRATEGY_AUTOMINDER.md → COUNTER_MODE_FLOW.md
- `"Fleet Health Score" marketing claim` --semantically_similar_to--> `Contradictions, resolved (table)`  [INFERRED] [semantically similar]
  app/src/main/play/listings/en-US/full-description.txt → PLAN.md
- `Product quality gate (§1)` --semantically_similar_to--> `Billing 9.1.0 migration (migration/play-billing-9)`  [INFERRED] [semantically similar]
  PLAY_LAUNCH_SCORECARD.md → PLAN.md
- `Brutally honest risks (10 items)` --semantically_similar_to--> `Billing 9.1.0 migration (migration/play-billing-9)`  [INFERRED] [semantically similar]
  PRODUCT_STRATEGY_AUTOMINDER.md → PLAN.md
- `Numeric typography drift (JetBrains Mono not applied everywhere)` --semantically_similar_to--> `Data display conventions (money cents / distance km)`  [INFERRED] [semantically similar]
  UI_AUDIT_2026.md → DESIGN_SYSTEM.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **CLAUDE.md Read-Only Reviewer Agents** — _claude_agents_accessibility_reviewer_accessibility_reviewer, _claude_agents_android_ui_architect_android_ui_architect, _claude_agents_compose_performance_reviewer_compose_performance_reviewer, _claude_agents_visual_qa_agent_visual_qa_agent [EXTRACTED 1.00]
- **CLAUDE.md Skill Procedures Group** — _claude_skills_autominder_ui_slice_skill_autominder_ui_slice, _claude_skills_autominder_visual_qa_skill_autominder_visual_qa, _claude_skills_accessibility_qa_skill_accessibility_qa, _claude_skills_compose_performance_guardian_skill_compose_performance_guardian, _claude_skills_ui_diff_review_skill_ui_diff_review, _claude_skills_android_release_gate_skill_android_release_gate, _claude_skills_play_store_readiness_skill_play_store_readiness [EXTRACTED 1.00]
- **StatusCalculator Status-Model Definitions (GOOD/DISABLED contradiction across docs)** — _claude_rules_ui_service_status, _claude_rules_data_status_calculator, agents_status_calculator [INFERRED 0.75]
- **PLAN.md supersedes PRD.md, DESIGN_SYSTEM.md and HANDOFF.md where they conflict** — plan_doc, prd_doc, design_system_doc, handoff_doc [EXTRACTED 1.00]
- **Baseline Profile plugin applied but never wired — recurring cross-document finding** — plan_performancegates, play_store_checklist_stabilityperformance, product_strategy_autominder_architecture [INFERRED 0.85]
- **Three moments of vulnerability: Quote Auditor, Mechanic Prep, Service Passport** — product_strategy_autominder_quoteauditor, product_strategy_autominder_mechanicprep, product_strategy_autominder_servicepassport [EXTRACTED 1.00]
- **Blocking Release Gates (mvp-plan.html)** — docs_exec_plans_mvp_plan, concept_billing_migration_pbl9, concept_reminder_engine_reliability_risk, concept_locale_claims_mismatch, concept_demo_data_credibility, concept_data_safety_privacy_drift [INFERRED 0.85]
- **Health Score Fabrication Evidence Trail** — docs_exec_plans_mobbin_design_blueprint_2026, concept_health_score_fabrication, app_src_main_kotlin_com_autominder_app_ui_components_premium_healthcockpitcard_healthcockpitcard, app_src_main_kotlin_com_autominder_app_ui_components_fleethealthscore_fleethealthscore [INFERRED 0.85]
- **AutoMinder Governance Document Hierarchy** — docs_codex_handoff, docs_governance_reference, docs_exec_plans_mvp_plan, docs_exec_plans_mobbin_design_blueprint_2026 [INFERRED 0.85]

## Communities (132 total, 25 thin omitted)

### Community 0 - "Fuel History & Efficiency"
Cohesion: 0.05
Nodes (29): FuelRepositoryImpl, Flow, FuelEntry, IFuelRepository, Flow, CalculateEfficiencyUseCase, EfficiencyUnit, KM_L (+21 more)

### Community 1 - "Onboarding Flow"
Cohesion: 0.05
Nodes (26): DrivingAmount, HIGH, LOW, TYPICAL, CreateDefaultRemindersUseCase, PlannedReminder, ReminderTemplate, AddCarStep() (+18 more)

### Community 2 - "Billing & Settings"
Cohesion: 0.05
Nodes (36): Cancelled, Error, Idle, InProgress, Activity, ProductDetails, StateFlow, NotFound (+28 more)

### Community 3 - "Quote Auditor"
Cohesion: 0.08
Nodes (36): QuoteAuditResult, QuoteItem, QuoteLineVerdict, QuoteVerdictStatus, CAN_WAIT, LEGITIMATE_DUE, LIKELY_UPSELL, VERIFY_FIRST (+28 more)

### Community 4 - "Add Service Flow"
Cohesion: 0.06
Nodes (25): Flow, UserPreferences, SuggestedInterval, AddServiceUiEvent, AddServiceUiState, AddServiceViewModel, CostChanged, CustomLabelChanged (+17 more)

### Community 5 - "Service Type Model"
Cohesion: 0.06
Nodes (39): Context, labelRes(), localizedLabel(), Converters, ServiceType, AIR_FILTER, BATTERY, BRAKE_SERVICE (+31 more)

### Community 6 - "Ad Manager"
Cohesion: 0.11
Nodes (14): AdError, AdManager, InterstitialAdLoadCallback, RewardedAdLoadCallback, RewardedInterstitialAdLoadCallback, FullScreenContentCallback, FullScreenContentCallback, FullScreenContentCallback (+6 more)

### Community 7 - "Vehicle Repository"
Cohesion: 0.11
Nodes (8): RepositoryModule, toDomain(), toEntity(), Flow, VehicleRepositoryImpl, Vehicle, IVehicleRepository, Flow

### Community 8 - "Edit Vehicle & Validation"
Cohesion: 0.08
Nodes (24): ValidationError, ValidationErrorCode, COST_NEGATIVE, FIELD_REQUIRED, ODOMETER_NEGATIVE, VIN_INVALID_FORMAT, YEAR_TOO_EARLY, YEAR_TOO_LATE (+16 more)

### Community 9 - "Design Blueprint Docs"
Cohesion: 0.10
Nodes (33): FleetHealthScore, HealthCockpitCard, ImageVector, Modifier, RecordsTimelineCard(), Skeleton.kt (shimmer), AdMob ID Production Safety Gate Pattern, Play Billing 7.1.1 to 9.1.0 Migration (+25 more)

### Community 10 - "Add Fuel Flow"
Cohesion: 0.11
Nodes (18): AddFuelUiEvent, AddFuelUiState, AddFuelViewModel, CostChanged, DateChanged, FullTankToggled, GasStationChanged, StateFlow (+10 more)

### Community 11 - "Vehicle Detail Screen"
Cohesion: 0.09
Nodes (18): ScreenState, Empty, Error, Loading, Success, ActionState, ArchiveClicked, ExportClicked (+10 more)

### Community 12 - "Service History ViewModel"
Cohesion: 0.09
Nodes (24): ClearExportUri, ClearFilters, DeleteService, ExportHistory, ExportPassport, StateFlow, ViewModel, Retry (+16 more)

### Community 13 - "Reminder Repository"
Cohesion: 0.14
Nodes (5): Flow, ReminderRepositoryImpl, Reminder, IReminderRepository, Flow

### Community 14 - "Reminder DAO & Mapper"
Cohesion: 0.13
Nodes (5): Flow, ReminderDao, ReminderEntity, toDomain(), toEntity()

### Community 15 - "Service Repository"
Cohesion: 0.15
Nodes (5): Flow, ServiceRepositoryImpl, Service, IServiceRepository, Flow

### Community 16 - "Vehicle DAO & Entity"
Cohesion: 0.16
Nodes (3): Flow, VehicleDao, VehicleEntity

### Community 17 - "Vehicle Detail Charts"
Cohesion: 0.17
Nodes (17): DuePrediction, CostByTypeDonut(), Modifier, Modifier, SpendingTrendChart(), Modifier, PremiumSectionHeader(), SheetState (+9 more)

### Community 18 - "Mileage Log Repository"
Cohesion: 0.17
Nodes (7): toDomain(), toEntity(), Flow, MileageLogRepositoryImpl, MileageLogEntry, IMileageLogRepository, Flow

### Community 19 - "Dashboard Data Use Case"
Cohesion: 0.21
Nodes (8): AppInfo, Activity, ReviewHelper, DashboardData, GetDashboardDataUseCase, Flow, VehicleWithStatus, DashboardViewModelTest

### Community 20 - "Service Status Model"
Cohesion: 0.17
Nodes (15): ServiceStatus, COMPLETED, DUE_SOON, OK, OVERDUE, SNOOZED, UNKNOWN, Modifier (+7 more)

### Community 21 - "Reminder Priority Engine"
Cohesion: 0.15
Nodes (18): DataConfidence, HIGH, INCOMPLETE_DATA, LOW_STALE_MILEAGE, MEDIUM, ReminderExplanation, ReminderUrgency, DUE_SOON (+10 more)

### Community 22 - "Add/Edit Save Flow"
Cohesion: 0.17
Nodes (17): Modifier, SaveButton(), SaveButtonState, Idle, Saving, Success, AddServiceContent(), IntervalPreset (+9 more)

### Community 23 - "Service DAO & Mapper"
Cohesion: 0.19
Nodes (5): Flow, ServiceDao, ServiceEntity, toDomain(), toEntity()

### Community 24 - "Reminder Priority Tests"
Cohesion: 0.20
Nodes (7): ReminderWithStatus, ReminderPriorityEngine, buildUpcomingSubtitle(), Modifier, MaintenanceRow(), UpcomingMaintenanceSection(), ReminderPriorityEngineTest

### Community 25 - "Predict Due Use Case"
Cohesion: 0.15
Nodes (4): OdometerPoint, PredictDueUseCase, PerformanceStressBenchmarkTest, PredictDueUseCaseTest

### Community 26 - "Premium Paywall"
Cohesion: 0.15
Nodes (16): Modifier, PremiumPaywallPlanCard(), Available, Loading, PremiumPriceDisplay, Unavailable, FeatureCheck(), FeatureComparisonTable() (+8 more)

### Community 27 - "Nav Routes"
Cohesion: 0.11
Nodes (18): About, AddFuel, AddReminder, AddService, AddVehicle, Dashboard, EditReminder, EditVehicle (+10 more)

### Community 28 - "Mileage Log DAO"
Cohesion: 0.18
Nodes (4): Flow, MileageLogDao, MileageLogEntity, ManualBackupManagerTest

### Community 29 - "Distance & Date Utilities"
Cohesion: 0.16
Nodes (10): DistanceUtil, AddFuelBentoContent(), FuelStatPill(), Modifier, AddReminderVehicleHeader(), Modifier, EditReminderVehicleHeader(), com (+2 more)

### Community 31 - "Database Module & Migration"
Cohesion: 0.19
Nodes (5): MigrationTest, DatabaseModule, Context, AppDatabase, RoomDatabase

### Community 32 - "Fuel DAO & Mapper"
Cohesion: 0.20
Nodes (5): FuelDao, Flow, FuelEntryEntity, toDomain(), toEntity()

### Community 33 - "Premium Status Style"
Cohesion: 0.18
Nodes (8): Modifier, LoadingState(), Color, Shape, PremiumStatusStyle, Modifier, RemindersDelayedBanner(), Dp

### Community 35 - "PRD Overview"
Cohesion: 0.12
Nodes (17): What ships next (v1.1) — not now, Product identity & strategic positioning, Architecture laws (MVVM + Clean + Offline-First), 30-day build roadmap (P1-P12), Closing statement, Daily engagement / gamification system, Room entities v2 database design, AutoMinder PRD v4.0 (+9 more)

### Community 36 - "Backup Manager"
Cohesion: 0.27
Nodes (8): AutoMinderBackupData, BackupRestoreSummary, FuelEntryBackupDto, ManualBackupManager, MileageLogBackupDto, ReminderBackupDto, ServiceBackupDto, VehicleBackupDto

### Community 37 - "Dashboard Screen"
Cohesion: 0.21
Nodes (12): Modifier, pressScale(), ActiveVehicleCard(), Modifier, DashboardScreen(), HomeContextualHeader(), androidx, Modifier (+4 more)

### Community 39 - "Counter Mode Flow Docs"
Cohesion: 0.18
Nodes (16): Step 1 — Capture, Counter Mode (Quote Auditor UI), Step 4 — Decision Confirm, decisions table (Gate C schema), Counter Mode Flow & State Spec, Gate B (design gate), NavRoutes.CounterMode route, quote_lines table (Gate C schema) (+8 more)

### Community 40 - "CLAUDE.md Governance"
Cohesion: 0.19
Nodes (15): Compose Performance Reviewer Agent, Visual QA Agent, AutoMinder Data & Domain Rules, AutoMinder Android Release Gate Skill, AutoMinder AVD Visual QA Skill, Compose Performance Guardian Skill, AutoMinder UI Diff Review Skill, AutoMinder CI Workflow (+7 more)

### Community 41 - "Motion & Animation Theme"
Cohesion: 0.20
Nodes (7): FuelEfficiencyChart(), Modifier, FormField(), Modifier, Motion, FiniteAnimationSpec, T

### Community 42 - "Vehicle Hero Card"
Cohesion: 0.25
Nodes (13): FormSectionCard(), Modifier, CompactContent(), ExpandedContent(), androidx, Color, Modifier, VehicleAvatar() (+5 more)

### Community 43 - "Dashboard ViewModel"
Cohesion: 0.19
Nodes (10): DashboardUiState, DashboardViewModel, DataWithPrefs, Empty, Error, Activity, StateFlow, ViewModel (+2 more)

### Community 44 - "Home Screen Widget"
Cohesion: 0.30
Nodes (13): AutoMinderWidget, AutoMinderWidgetReceiver, fuelDao(), Context, LargeWidget(), MediumWidget(), reminderDao(), SmallWidget() (+5 more)

### Community 46 - "Main Activity & Theme"
Cohesion: 0.23
Nodes (8): android, com, Provider, MainActivity, VehicleDeepLink, AutoMinderTheme(), Bundle, ComponentActivity

### Community 47 - "Dashboard Recent Activity"
Cohesion: 0.19
Nodes (8): ActivityRow(), Modifier, RecentActivitySection(), ActivityType, FUEL, MILEAGE, SERVICE, HomeActivityItem

### Community 48 - "Nav Graph & Dialogs"
Cohesion: 0.31
Nodes (12): DiscardChangesDialog(), Modifier, NavHostController, NavGraph(), AddFuelScreen(), AddReminderScreen(), EditReminderScreen(), AddServiceScreen() (+4 more)

### Community 49 - "Mileage Log ViewModel"
Cohesion: 0.30
Nodes (4): StateFlow, ViewModel, MileageLogUiState, MileageLogViewModel

### Community 50 - "Edit Reminder ViewModel"
Cohesion: 0.29
Nodes (4): EditReminderUiState, EditReminderViewModel, StateFlow, ViewModel

### Community 51 - "Play Store Checklist"
Cohesion: 0.15
Nodes (14): Play Store full description, "Fleet Health Score" marketing claim, AutoMinder Pro "7-day free trial" claim, Performance gates (S1-S3, Baseline Profile unwired), Ads (AdMob) & consent checklist, Build & signing checklist, AutoMinder Play Store Release Checklist, Pre-submit smoke test (+6 more)

### Community 52 - "Design System Docs"
Cohesion: 0.18
Nodes (14): Bottom nav: Home/Vehicles/Records/Settings, Data display conventions (money cents / distance km), AutoMinder Design System v1.0, AutoMinder Brand & Design System (Figma), Haptics vocabulary, Motion system (springs, Motion.kt), Onboarding doctrine (activation-first), Racing Teal #006B5F (+6 more)

### Community 53 - "README & Gemini Docs"
Cohesion: 0.14
Nodes (14): AdMob ID policy, Active branches convention, Build variants (debug/release AdMob IDs), AutoMinder Project-Specific Overrides (GEMINI.md), Session verification protocol, Release signing policy, Technical foundation / exact versions table, Branch strategy (+6 more)

### Community 54 - "Backup Coordinator"
Cohesion: 0.23
Nodes (7): DispatchersModule, BackupCoordinator, Failed, Partial, Result, Success, CoroutineDispatcher

### Community 55 - "Add Reminder ViewModel"
Cohesion: 0.28
Nodes (5): AddReminderUiState, AddReminderViewModel, DefaultInterval, StateFlow, ViewModel

### Community 56 - "Service Completion Model"
Cohesion: 0.24
Nodes (5): Failed, ServiceCompletion, ServiceCompletionResult, Success, VehicleNotFound

### Community 57 - "AutoMinder App Init"
Cohesion: 0.27
Nodes (5): AutoMinderApp, Application, Provider, Configuration, HiltWorkerFactory

### Community 58 - "Analytics Helper"
Cohesion: 0.23
Nodes (5): AnalyticsModule, AnalyticsEvents, AnalyticsHelper, AnalyticsParams, FirebaseAnalyticsHelper

### Community 59 - "Backup Agent"
Cohesion: 0.27
Nodes (7): AutoMinderBackupAgent, BackupAgentEntryPoint, BackupAgent, BackupDataInput, BackupDataOutput, FullBackupDataOutput, ParcelFileDescriptor

### Community 60 - "Export Service History"
Cohesion: 0.23
Nodes (4): ExportServiceHistoryUseCase, Uri, ExportServiceHistoryUseCaseTest, Context

### Community 61 - "Reminder Check & Status Calc"
Cohesion: 0.21
Nodes (5): StatusCalculator, CoroutineWorker, ReminderCheckWorker, CoroutineWorker, WeeklyDigestWorker

### Community 62 - "Service Detail Screen"
Cohesion: 0.32
Nodes (10): ErrorState(), Modifier, Context, ImageVector, Modifier, ServiceDetailBentoContent(), ServiceDetailScreen(), serviceIconFor() (+2 more)

### Community 63 - "Dashboard Vehicle Status Card"
Cohesion: 0.21
Nodes (9): Modifier, StatusVisual, VehicleStatusCard(), VehicleOperationalStatus, DUE_SOON, HEALTHY, OVERDUE, SETUP_INCOMPLETE (+1 more)

### Community 64 - "Add Reminder ViewModel (dup cluster)"
Cohesion: 0.17
Nodes (12): AddReminderUiEvent, DescriptionChanged, DueDateChanged, DueKmChanged, IntervalDaysChanged, IntervalKmChanged, PermissionRequestHandled, SaveClicked (+4 more)

### Community 65 - "Edit Reminder ViewModel (dup cluster)"
Cohesion: 0.17
Nodes (12): CustomLabelChanged, DeleteClicked, DueDateChanged, DueKmChanged, EditReminderUiEvent, IntervalDaysChanged, IntervalKmChanged, NotesChanged (+4 more)

### Community 66 - "Product Strategy Doc"
Cohesion: 0.17
Nodes (12): AI strategy (drill-down only, evidence-carded), App flow recommendations, Competitor weaknesses to attack (table), Executive product diagnosis (strongest/weakest), AutoMinder Product Strategy Deep Review, Final recommendation, Growth & app store strategy, Mechanic Prep script (+4 more)

### Community 67 - "Premium Components Skill"
Cohesion: 0.18
Nodes (11): Fleet Design Inspiration (behance.net/gallery/250251481 — translated to Racing Teal M3, never copy Fleet's yellow/purple palette or fake content), FormSectionCard spec (title+helper+content+error slot), HealthCockpitCard spec (human-verdict headline, score ring demoted to instrument, never a lone giant '0'), InsightMetricCard spec (eyebrow label + Mono value + unit), Premium Compose Components Skill, PremiumActionGrid spec (2x2 FilledTonal tiles, ≥56dp), PremiumPaywallPlanCard spec (Mono price or loading, badge, selected = tonal lift + 1.02 scale), PremiumSectionHeader spec (title+count badge+trailing action, heading() semantics) (+3 more)

### Community 69 - "Vehicle List Screen"
Cohesion: 0.35
Nodes (9): EmptyState(), ImageVector, Modifier, Modifier, labelRes(), VehicleListContent(), VehicleListRow(), VehicleListScreen() (+1 more)

### Community 70 - "Reminder Detail Sheet"
Cohesion: 0.31
Nodes (8): info(), Color, ReminderDetailSheet(), ServiceTypeInfo, SeverityBadge(), SheetSection(), overdueByText(), OverdueCopy

### Community 71 - "Reminder Staleness Test"
Cohesion: 0.33
Nodes (3): evaluateReminderStaleness(), RemindersDelayedState, ReminderStalenessTest

### Community 72 - "Service Detail ViewModel"
Cohesion: 0.29
Nodes (6): DeleteClicked, StateFlow, ViewModel, ServiceDetailUiEvent, ServiceDetailUiState, ServiceDetailViewModel

### Community 73 - "Vehicle List ViewModel"
Cohesion: 0.27
Nodes (9): Empty, Error, StateFlow, ViewModel, Loading, Success, VehicleListItem, VehicleListUiState (+1 more)

### Community 76 - "PLAN.md Roadmap"
Cohesion: 0.22
Nodes (11): Billing 9.1.0 migration (migration/play-billing-9), AutoMinder 2026 MVP Plan, MVP-ready blocking gates (B1-B8), Per-screen problem/answer table, Platform gates (P1-P6), Execution queue (Step 0-7), Reminder engine cannot keep the promise (correctness risk), Honest risk register (+3 more)

### Community 77 - "UI Slice Skill"
Cohesion: 0.20
Nodes (10): Android UI Architect Agent, Shape Set by Component Family, Never by Status (status-dependent corner radii removed — a list whose radii vary per row reads as broken, not informative), 12 AutoMinder 2026 Premium UI Rules, AutoMinder UI Slice Skill, StatusReminderCard spec (status corner morphing 8/16/28dp via animateDpAsState, 4dp error rail for OVERDUE), Root DESIGN_SYSTEM.md (archived/stale — do not consult), docs/DESIGN_SYSTEM_2026.md (Midnight Cobalt authority), Mobbin Design Blueprint 2026 (exec plan) (+2 more)

### Community 78 - "Mileage Log ViewModel (variant)"
Cohesion: 0.20
Nodes (10): AddClicked, DeleteLog, MileageLogUiEvent, NewNotesChanged, NewOdometerChanged, ResetSuccess, Retry, SelectTag (+2 more)

### Community 80 - "UI Audit 2026 Doc"
Cohesion: 0.22
Nodes (10): Document authority table, Quality bar items (Q1-Q6), Accessibility findings (WCAG 2.1 AA), AutoMinder Complete UI Audit (2026 Standards), Loading-state inconsistency (skeleton vs spinner), Numeric typography drift (JetBrains Mono not applied everywhere), Prioritized v1.1 redesign roadmap, Screen-by-screen findings (+2 more)

### Community 81 - "UI Rules Governance"
Cohesion: 0.28
Nodes (9): Accessibility Reviewer Agent, StatusCalculator: OVERDUE Always Beats SNOOZED; Never Notify GOOD/SNOOZED/DISABLED, Midnight Cobalt Brand Tokens (Racing Teal #006B5F retired — predates the Night Garage commit), Valid ServiceStatus States: OVERDUE/DUE_SOON/SNOOZED/OK/COMPLETED/UNKNOWN — GOOD, DISABLED, NO_DATA, isEnabled, health-score explicitly do not exist, Skeleton Opacity Pulse 0.40→0.70 1000ms (not a translating shimmer sweep — a moving gradient costs frames every skeleton with no ongoing activity), AutoMinder UI Rules (Midnight Cobalt), AutoMinder Accessibility QA Skill, AGENTS.md StatusCalculator Logic incl. GOOD/DISABLED priority order (marked VERIFIED CORRECT, dated 2026-04) (+1 more)

### Community 82 - "Agents & Governance Docs"
Cohesion: 0.22
Nodes (9): Play-Store-Readiness: Products Query Split SUBS/INAPP, AGENTS.md Agent Scope Control Table (one scope per agent session, prevents merge conflicts), AGENTS.md Agent Configuration, AGENTS.md Billing 7.1.1: One-Time Purchase Only, No Subscriptions, AGENTS.md Do NOT Touch Protected Files List, AGENTS.md Pinned Tech Stack Table (Kotlin 2.1.21, AGP 8.9.1, KSP 2.1.21-2.0.1, etc. — 'never change without explicit human approval'), MVVM Clean Architecture Layering Law (UI→ViewModel→UseCase→Repository→DAO), Security & Privacy Law (keystore/local.properties/secrets never touched or logged) (+1 more)

### Community 83 - "Skeleton Loading Component"
Cohesion: 0.64
Nodes (8): DashboardSkeleton(), Modifier, Shape, ListSkeleton(), SkeletonBar(), skeletonFill(), SkeletonScaffold(), VehicleCardSkeleton()

### Community 84 - "Mileage Log Screen"
Cohesion: 0.44
Nodes (7): Modifier, SwipeToDeleteContainer(), Modifier, MileageCockpitCard(), MileageLogScreen(), MileageTimelineCard(), MileageVehicleHeader()

### Community 85 - "Fuel History Screen"
Cohesion: 0.56
Nodes (8): FuelEfficiencyTrendCard(), FuelHistoryScreen(), FuelIntelligenceCockpit(), FuelReceiptCard(), FuelSpendingBars(), FuelSpendingTrendCard(), FuelVehicleHeader(), Modifier

### Community 86 - "Service History Screen"
Cohesion: 0.47
Nodes (8): CategoryFilterRow(), com, Modifier, LifetimeSpendHeroCard(), ServiceHistoryScreen(), ServiceHistoryStream(), ServiceReceiptCard(), VehicleFilterRow()

### Community 87 - "Add Vehicle ViewModel"
Cohesion: 0.22
Nodes (9): AddVehicleUiEvent, BrandChanged, ModelChanged, OdometerChanged, PhotoUriChanged, PlateNumberChanged, SaveClicked, VinChanged (+1 more)

### Community 90 - "Notification Action Receiver"
Cohesion: 0.36
Nodes (6): BroadcastReceiver, Context, Intent, NotificationActionReceiver, ReminderDao, Lesson: DAO Idempotency Before Background Actions

### Community 91 - "About Screen"
Cohesion: 0.57
Nodes (7): AboutActionItem(), AboutFeatureBadge(), AboutFeaturesRow(), AboutHeroCard(), AboutScreen(), ImageVector, Modifier

### Community 92 - "Add Vehicle ViewModel (variant)"
Cohesion: 0.43
Nodes (5): AddVehicleUiState, AddVehicleViewModel, StateFlow, ViewModel, toStringRes()

### Community 93 - "Reminder Alarm Scheduler"
Cohesion: 0.50
Nodes (3): Context, ReminderAlarmScheduler, PendingIntent

### Community 95 - "Handoff Doc"
Cohesion: 0.29
Nodes (8): Beyond-UI release backlog, Premium component kit (ui/components/premium/), 12 AutoMinder 2026 Premium UI Rules, AutoMinder Session Handoff (2026-07-07), Environment recipes (Windows Gradle, AVD, editing traps), Fleet Behance case study (storytelling reference), Slice 8 nit list, UI rescue pipeline (Slices 0-8)

### Community 96 - "Test Runner"
Cohesion: 0.43
Nodes (5): AndroidJUnitRunner, AutoMinderTestRunner, Application, Context, ClassLoader

### Community 97 - "Power Settings"
Cohesion: 0.57
Nodes (3): Context, Intent, PowerSettings

### Community 98 - "Dashboard What's Next Section"
Cohesion: 0.57
Nodes (5): PrioritizedReminder, buildUpcomingSubtitle(), Modifier, WhatsNextRow(), WhatsNextSection()

### Community 99 - "Dashboard Quick Log Section"
Cohesion: 0.67
Nodes (6): androidx, ImageVector, Modifier, PrimaryQuickLogButton(), QuickLogSection(), SecondaryQuickLogButton()

### Community 100 - "Edit Vehicle Screen"
Cohesion: 0.52
Nodes (6): EditVehicleField(), EditVehicleForm(), EditVehiclePhotoHero(), ImageVector, KeyboardType, Modifier

### Community 101 - "Play Launch Scorecard"
Cohesion: 0.29
Nodes (7): AutoMinder US Google Play Launch Scorecard, Policy & privacy (§5), Product quality gate (§1), Ratings & support (§4), Retention & engagement metrics (§3), Staged rollout plan (§6), Store conversion checklist (§2)

### Community 102 - "Release Checklist v1.1"
Cohesion: 0.29
Nodes (7): AutoMinder UI Production-Readiness Checklist, Figma file (design source of truth), Play Store submission gates, Remaining P1 UI work, Session log, Verify this session's changes, Orphaned premium components (built, never wired)

### Community 103 - "Monetization Rules"
Cohesion: 0.47
Nodes (6): Billing Correctness (verified PURCHASED entitlement, prompt ack, restore at startup, no logged tokens), AutoMinder Monetization Rules, UMP Consent Gate (no ad request until canRequestAds is true), Play-Store-Readiness: Billing 7.1.1 Pinned, Do NOT Propose Upgrade (Billing 8 tracked as separate pre-Aug-2026 task), AutoMinder Play Store Readiness Skill, Billing Library 7→9.x Migration Mandatory Before 2026-08-31 Deadline

### Community 105 - "App Lifecycle Observer"
Cohesion: 0.53
Nodes (3): AppLifecycleObserver, DefaultLifecycleObserver, LifecycleOwner

### Community 106 - "Reminder Alarm Receiver"
Cohesion: 0.53
Nodes (4): BroadcastReceiver, Context, Intent, ReminderAlarmReceiver

### Community 107 - "System Event Receiver"
Cohesion: 0.53
Nodes (4): BroadcastReceiver, Context, Intent, SystemEventReceiver

### Community 108 - "Stitch Proxy Script"
Cohesion: 0.33
Nodes (4): cloudsdkConfig, gcloud, proxy, stitchHome

### Community 111 - "Insight Metric Card"
Cohesion: 0.70
Nodes (4): InsightMetricCard(), InsightMetricRow(), ImageVector, Modifier

### Community 112 - "Premium Action Grid"
Cohesion: 0.80
Nodes (4): ActionTile(), Modifier, PremiumAction, PremiumActionGrid()

### Community 114 - "Gradle Wrapper"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 116 - "Bottom Nav Bar"
Cohesion: 0.67
Nodes (3): BottomNavBar(), BottomNavItem, NavHostController

## Ambiguous Edges - Review These
- `Billing Library 7→9.x Migration Mandatory Before 2026-08-31 Deadline` → `Play-Store-Readiness: Billing 7.1.1 Pinned, Do NOT Propose Upgrade (Billing 8 tracked as separate pre-Aug-2026 task)`  [AMBIGUOUS]
  .claude/skills/play-store-readiness/SKILL.md · relation: conceptually_related_to
- `gradle/libs.versions.toml as Sole Dependency-Version Authority` → `AGENTS.md Pinned Tech Stack Table (Kotlin 2.1.21, AGP 8.9.1, KSP 2.1.21-2.0.1, etc. — 'never change without explicit human approval')`  [AMBIGUOUS]
  AGENTS.md · relation: conceptually_related_to
- `Valid ServiceStatus States: OVERDUE/DUE_SOON/SNOOZED/OK/COMPLETED/UNKNOWN — GOOD, DISABLED, NO_DATA, isEnabled, health-score explicitly do not exist` → `StatusCalculator: OVERDUE Always Beats SNOOZED; Never Notify GOOD/SNOOZED/DISABLED`  [AMBIGUOUS]
  .claude/rules/data.md · relation: conceptually_related_to
- `Valid ServiceStatus States: OVERDUE/DUE_SOON/SNOOZED/OK/COMPLETED/UNKNOWN — GOOD, DISABLED, NO_DATA, isEnabled, health-score explicitly do not exist` → `AGENTS.md StatusCalculator Logic incl. GOOD/DISABLED priority order (marked VERIFIED CORRECT, dated 2026-04)`  [AMBIGUOUS]
  AGENTS.md · relation: conceptually_related_to
- `Shape Set by Component Family, Never by Status (status-dependent corner radii removed — a list whose radii vary per row reads as broken, not informative)` → `StatusReminderCard spec (status corner morphing 8/16/28dp via animateDpAsState, 4dp error rail for OVERDUE)`  [AMBIGUOUS]
  .claude/skills/premium-compose-components/SKILL.md · relation: conceptually_related_to
- `AGENTS.md Billing 7.1.1: One-Time Purchase Only, No Subscriptions` → `Play-Store-Readiness: Products Query Split SUBS/INAPP`  [AMBIGUOUS]
  AGENTS.md · relation: conceptually_related_to
- `AutoMinder Brand & Design System (Figma)` → `Figma file (design source of truth)`  [AMBIGUOUS]
  RELEASE_CHECKLIST_v1.1_UI.md · relation: conceptually_related_to
- `Production readiness checklist` → `AutoMinder Production Readiness`  [AMBIGUOUS]
  PRODUCTION_READINESS.md · relation: semantically_similar_to
- `AutoMinder Design System 2026` → `AutoMinder MVP Plan`  [AMBIGUOUS]
  docs/exec-plans/mvp-plan.html · relation: references
- `AutoMinder Privacy Policy (Store)` → `AutoMinder Website Privacy Policy`  [AMBIGUOUS]
  store/PRIVACY_POLICY.md · relation: conceptually_related_to

## Knowledge Gaps
- **312 isolated node(s):** `npx`, `Processing`, `Cancelled`, `Pending`, `InProgress` (+307 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **25 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Billing Library 7→9.x Migration Mandatory Before 2026-08-31 Deadline` and `Play-Store-Readiness: Billing 7.1.1 Pinned, Do NOT Propose Upgrade (Billing 8 tracked as separate pre-Aug-2026 task)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `gradle/libs.versions.toml as Sole Dependency-Version Authority` and `AGENTS.md Pinned Tech Stack Table (Kotlin 2.1.21, AGP 8.9.1, KSP 2.1.21-2.0.1, etc. — 'never change without explicit human approval')`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Valid ServiceStatus States: OVERDUE/DUE_SOON/SNOOZED/OK/COMPLETED/UNKNOWN — GOOD, DISABLED, NO_DATA, isEnabled, health-score explicitly do not exist` and `StatusCalculator: OVERDUE Always Beats SNOOZED; Never Notify GOOD/SNOOZED/DISABLED`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Valid ServiceStatus States: OVERDUE/DUE_SOON/SNOOZED/OK/COMPLETED/UNKNOWN — GOOD, DISABLED, NO_DATA, isEnabled, health-score explicitly do not exist` and `AGENTS.md StatusCalculator Logic incl. GOOD/DISABLED priority order (marked VERIFIED CORRECT, dated 2026-04)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Shape Set by Component Family, Never by Status (status-dependent corner radii removed — a list whose radii vary per row reads as broken, not informative)` and `StatusReminderCard spec (status corner morphing 8/16/28dp via animateDpAsState, 4dp error rail for OVERDUE)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `AGENTS.md Billing 7.1.1: One-Time Purchase Only, No Subscriptions` and `Play-Store-Readiness: Products Query Split SUBS/INAPP`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `AutoMinder Brand & Design System (Figma)` and `Figma file (design source of truth)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._