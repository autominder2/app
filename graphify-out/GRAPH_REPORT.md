# Graph Report - Autominder  (2026-08-25)

## Corpus Check
- 273 files · ~172,035 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2088 nodes · 4404 edges · 143 communities (112 shown, 31 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 80 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `e84ba65b`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- FuelEntry
- Reminder
- SubscriptionManager
- MileageLogEntry
- OnboardingScreen.kt
- Timber
- QuoteAuditorScreen.kt
- ServiceHistoryViewModel.kt
- OnboardingViewModelTest
- VehicleDetailViewModel.kt
- MutableInteractionSource
- AutoMinder 2026 Design Blueprint (Mobbin-Informed)
- ListSkeleton
- ServiceStatus
- AdManager.kt
- AnalyticsHelper
- AutoMinderBackupAgent.kt
- AddFuelViewModel
- Vehicle
- AddServiceScreen.kt
- MainActivity.kt
- VehicleBodyType
- UserPreferences
- ReminderDao
- DashboardViewModel.kt
- ServiceRepositoryImpl
- ServiceType
- VehicleDisplayName
- LocalDistanceUnit.kt
- ManualBackupManagerTest
- VehicleDao
- DashboardViewModelTest.kt
- ManualBackupManager.kt
- ServiceDao
- ReminderDetailSheet.kt
- ProPaywall.kt
- NavRoutes
- AppDatabase
- ServiceRepositoryImplTest
- AddServiceViewModelTest
- FuelDao
- DistanceUtil
- ServiceTypeGrid.kt
- ReminderCheckWorkerTest
- AutoMinder PRD v4.0
- ReminderPriorityEngine
- AddServiceUiEvent
- ValidatorsTest
- Counter Mode Flow & State Spec
- CLAUDE.md Governance (v6.1)
- AutoMinderWidget.kt
- AutoMinder Play Store Release Checklist
- Brand and Design System
- Project Setup and Policy
- ExportServiceHistoryUseCase
- Service
- Composable
- AddReminderViewModel.kt
- AddServiceViewModel.kt
- AddReminderUiEvent
- VehicleCatalogTest
- .buildPlan
- AutoMinder Product Strategy Deep Review
- Premium Compose Components Skill
- ServiceCompletionTransactionTest
- AddFuelUiEvent
- ReminderWithStatus
- evaluateReminderStaleness
- StatusCalculatorTest
- AutoMinder 2026 MVP Plan
- AutoMinder UI Slice Skill
- EditVehicleViewModel.kt
- AutoMinder Complete UI Audit (2026 Standards)
- AutoMinder UI Rules (Midnight Cobalt)
- AGENTS.md Agent Configuration
- AutoMinder Marketing Website Home
- AddVehicleViewModel.kt
- MileageLogViewModel
- VehicleDisplayNameFormatterTest
- VehicleDisplayNameTest
- ReminderAlarmSchedulerTest
- VehicleDataConfidence
- EditReminderViewModel.kt
- Skeleton.kt
- AboutScreen.kt
- OverdueCopyTest
- AutoMinder Session Handoff (2026-07-07)
- AutoMinderTestRunner.kt
- VehicleListViewModelTest
- OnboardingViewModel.kt
- ServiceChoicePicker.kt
- StatusChip
- ScreenState
- AutoMinder US Google Play Launch Scorecard
- EditReminderUiEvent
- AutoMinder Play Store Readiness Skill
- VehicleDataConfidenceTest
- stitch-proxy.mjs
- VehicleCatalog
- gradlew
- VehicleListViewModel.kt
- PermissionUtils
- localizedLabel
- MileageLogViewModelTest
- EmptyState
- BaselineProfileGenerator
- firebase
- MileageLogUiEvent
- BackupCoordinatorTest
- Result
- NotificationHelper
- Color.kt
- Play Store short description
- Play Store listing title
- Lesson: Figma MCP Quirks
- AutoMinder Favicon Icon
- VehicleDetailScreen.kt
- Motion
- PredictDueUseCaseTest
- ServiceHistoryScreen.kt
- ConsentManager.kt
- CoroutineDispatcher
- InsightMetricCard.kt
- FuelHistoryScreen.kt
- DrivingAmount
- VehicleOperationalStatus
- UpdateHelper
- BannerAdView
- ServiceDetailScreen.kt
- AddVehicleScreen.kt
- EditVehicleScreen.kt
- AutoMinder Market R&D - 2026-08-23
- VehicleDetailUiEvent
- RemindersDelayedBanner.kt

## God Nodes (most connected - your core abstractions)
1. `ServiceType` - 87 edges
2. `Vehicle` - 78 edges
3. `Reminder` - 61 edges
4. `Service` - 52 edges
5. `ServiceStatus` - 48 edges
6. `IVehicleRepository` - 47 edges
7. `UserPreferences` - 45 edges
8. `FuelEntry` - 42 edges
9. `DistanceUtil` - 37 edges
10. `ReminderDao` - 34 edges

## Surprising Connections (you probably didn't know these)
- `Quote Auditor v0 (no OCR, no photo)` --semantically_similar_to--> `Counter Mode (Quote Auditor UI)`  [INFERRED] [semantically similar]
  PRODUCT_STRATEGY_AUTOMINDER.md → COUNTER_MODE_FLOW.md
- `"Fleet Health Score" marketing claim` --semantically_similar_to--> `Contradictions, resolved (table)`  [INFERRED] [semantically similar]
  app/src/main/play/listings/en-US/full-description.txt → PLAN.md
- `Brutally honest risks (10 items)` --semantically_similar_to--> `Billing 9.1.0 migration (migration/play-billing-9)`  [INFERRED] [semantically similar]
  PRODUCT_STRATEGY_AUTOMINDER.md → PLAN.md
- `Product quality gate (§1)` --semantically_similar_to--> `Billing 9.1.0 migration (migration/play-billing-9)`  [INFERRED] [semantically similar]
  PLAY_LAUNCH_SCORECARD.md → PLAN.md
- `Development phases (P1-P7)` --semantically_similar_to--> `30-day build roadmap (P1-P12)`  [INFERRED] [semantically similar]
  README.md → PRD.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **CLAUDE.md Read-Only Reviewer Agents** — _claude_agents_accessibility_reviewer_accessibility_reviewer, _claude_agents_android_ui_architect_android_ui_architect, _claude_agents_compose_performance_reviewer_compose_performance_reviewer, _claude_agents_visual_qa_agent_visual_qa_agent [EXTRACTED 1.00]
- **CLAUDE.md Skill Procedures Group** — _claude_skills_autominder_ui_slice_skill_autominder_ui_slice, _claude_skills_autominder_visual_qa_skill_autominder_visual_qa, _claude_skills_accessibility_qa_skill_accessibility_qa, _claude_skills_compose_performance_guardian_skill_compose_performance_guardian, _claude_skills_ui_diff_review_skill_ui_diff_review, _claude_skills_android_release_gate_skill_android_release_gate, _claude_skills_play_store_readiness_skill_play_store_readiness [EXTRACTED 1.00]
- **PLAN.md supersedes PRD.md, DESIGN_SYSTEM.md and HANDOFF.md where they conflict** — plan_doc, prd_doc, design_system_doc, handoff_doc [EXTRACTED 1.00]
- **Three moments of vulnerability: Quote Auditor, Mechanic Prep, Service Passport** — product_strategy_autominder_quoteauditor, product_strategy_autominder_mechanicprep, product_strategy_autominder_servicepassport [EXTRACTED 1.00]
- **StatusCalculator Status-Model Definitions (GOOD/DISABLED contradiction across docs)** — _claude_rules_ui_service_status, _claude_rules_data_status_calculator, agents_status_calculator [INFERRED 0.75]
- **Baseline Profile plugin applied but never wired — recurring cross-document finding** — plan_performancegates, play_store_checklist_stabilityperformance, product_strategy_autominder_architecture [INFERRED 0.85]
- **AutoMinder Governance Document Hierarchy** — docs_codex_handoff, docs_governance_reference, docs_exec_plans_mvp_plan, docs_exec_plans_mobbin_design_blueprint_2026 [INFERRED 0.85]
- **Health Score Fabrication Evidence Trail** — docs_exec_plans_mobbin_design_blueprint_2026, concept_health_score_fabrication, app_src_main_kotlin_com_autominder_app_ui_components_premium_healthcockpitcard_healthcockpitcard, app_src_main_kotlin_com_autominder_app_ui_components_fleethealthscore_fleethealthscore [INFERRED 0.85]
- **Blocking Release Gates (mvp-plan.html)** — docs_exec_plans_mvp_plan, concept_billing_migration_pbl9, concept_reminder_engine_reliability_risk, concept_locale_claims_mismatch, concept_demo_data_credibility, concept_data_safety_privacy_drift [INFERRED 0.85]

## Communities (143 total, 31 thin omitted)

### Community 0 - "FuelEntry"
Cohesion: 0.05
Nodes (29): FuelRepositoryImpl, Flow, FuelEntry, IFuelRepository, Flow, CalculateEfficiencyUseCase, EfficiencyUnit, KM_L (+21 more)

### Community 1 - "Reminder"
Cohesion: 0.14
Nodes (5): Flow, ReminderRepositoryImpl, Reminder, IReminderRepository, Flow

### Community 2 - "SubscriptionManager"
Cohesion: 0.06
Nodes (36): Cancelled, Error, Idle, InProgress, Activity, ProductDetails, StateFlow, NotFound (+28 more)

### Community 3 - "MileageLogEntry"
Cohesion: 0.15
Nodes (6): RepositoryModule, Flow, MileageLogRepositoryImpl, MileageLogEntry, IMileageLogRepository, Flow

### Community 4 - "OnboardingScreen.kt"
Cohesion: 0.29
Nodes (16): GlowHero(), Color, ImageVector, Modifier, OnboardingProgressBar(), PillarItem, PrimaryCta(), ValuePillarGroup() (+8 more)

### Community 5 - "Timber"
Cohesion: 0.05
Nodes (32): AutoMinderApp, Application, Provider, AppLifecycleObserver, CrashlyticsTree, Context, Intent, PowerSettings (+24 more)

### Community 6 - "QuoteAuditorScreen.kt"
Cohesion: 0.08
Nodes (36): QuoteAuditResult, QuoteItem, QuoteLineVerdict, QuoteVerdictStatus, CAN_WAIT, LEGITIMATE_DUE, LIKELY_UPSELL, VERIFY_FIRST (+28 more)

### Community 7 - "ServiceHistoryViewModel.kt"
Cohesion: 0.09
Nodes (24): ClearExportUri, ClearFilters, DeleteService, ExportHistory, ExportPassport, StateFlow, ViewModel, Retry (+16 more)

### Community 9 - "VehicleDetailViewModel.kt"
Cohesion: 0.16
Nodes (6): ActionState, com, StateFlow, ViewModel, VehicleDetailUiState, VehicleDetailViewModel

### Community 10 - "MutableInteractionSource"
Cohesion: 0.06
Nodes (57): DataConfidence, ESTIMATED, HIGH, INCOMPLETE_DATA, MEDIUM, PrioritizedReminder, ReminderExplanation, ReminderUrgency (+49 more)

### Community 11 - "AutoMinder 2026 Design Blueprint (Mobbin-Informed)"
Cohesion: 0.10
Nodes (33): FleetHealthScore, HealthCockpitCard, ImageVector, Modifier, RecordsTimelineCard(), Skeleton.kt (shimmer), AdMob ID Production Safety Gate Pattern, Play Billing 7.1.1 to 9.1.0 Migration (+25 more)

### Community 12 - "ListSkeleton"
Cohesion: 0.29
Nodes (15): ErrorState(), Modifier, ListSkeleton(), Modifier, NavHostController, NavGraph(), AddFuelScreen(), EditReminderScreen() (+7 more)

### Community 13 - "ServiceStatus"
Cohesion: 0.13
Nodes (14): ServiceStatus, COMPLETED, DUE_SOON, OK, OVERDUE, SNOOZED, UNKNOWN, StatusCalculator (+6 more)

### Community 14 - "AdManager.kt"
Cohesion: 0.11
Nodes (14): AdError, AdManager, InterstitialAdLoadCallback, RewardedAdLoadCallback, RewardedInterstitialAdLoadCallback, FullScreenContentCallback, FullScreenContentCallback, FullScreenContentCallback (+6 more)

### Community 15 - "AnalyticsHelper"
Cohesion: 0.16
Nodes (10): AnalyticsModule, BroadcastReceiver, Context, Intent, NotificationActionReceiver, AnalyticsEvents, AnalyticsHelper, FirebaseAnalyticsHelper (+2 more)

### Community 16 - "AutoMinderBackupAgent.kt"
Cohesion: 0.27
Nodes (7): AutoMinderBackupAgent, BackupAgentEntryPoint, BackupAgent, BackupDataInput, BackupDataOutput, FullBackupDataOutput, ParcelFileDescriptor

### Community 17 - "AddFuelViewModel"
Cohesion: 0.21
Nodes (5): AnalyticsParams, AddFuelUiState, AddFuelViewModel, StateFlow, ViewModel

### Community 18 - "Vehicle"
Cohesion: 0.12
Nodes (7): toDomain(), toEntity(), Flow, VehicleRepositoryImpl, Vehicle, IVehicleRepository, Flow

### Community 19 - "AddServiceScreen.kt"
Cohesion: 0.18
Nodes (15): Modifier, SaveButton(), SaveButtonState, Idle, Saving, Success, AddFuelBentoContent(), FuelStatPill() (+7 more)

### Community 20 - "MainActivity.kt"
Cohesion: 0.18
Nodes (12): android, com, Provider, MainActivity, VehicleDeepLink, WidgetDeepLink, BottomNavBar(), BottomNavItem (+4 more)

### Community 21 - "VehicleBodyType"
Cohesion: 0.08
Nodes (12): VehicleBodyType, CONVERTIBLE, COUPE, HATCHBACK, MINIVAN, MOTORCYCLE, SEDAN, SUV (+4 more)

### Community 22 - "UserPreferences"
Cohesion: 0.15
Nodes (4): Activity, ReviewHelper, Flow, UserPreferences

### Community 23 - "ReminderDao"
Cohesion: 0.14
Nodes (5): Flow, ReminderDao, ReminderEntity, toDomain(), toEntity()

### Community 24 - "DashboardViewModel.kt"
Cohesion: 0.19
Nodes (10): DashboardUiState, DashboardViewModel, DataWithPrefs, Empty, Error, Activity, StateFlow, ViewModel (+2 more)

### Community 25 - "ServiceRepositoryImpl"
Cohesion: 0.16
Nodes (7): Flow, ServiceRepositoryImpl, Failed, ServiceCompletion, ServiceCompletionResult, Success, VehicleNotFound

### Community 26 - "ServiceType"
Cohesion: 0.10
Nodes (18): Converters, ServiceType, AIR_FILTER, BATTERY, BRAKE_SERVICE, CABIN_FILTER, COOLANT, CUSTOM (+10 more)

### Community 28 - "LocalDistanceUnit.kt"
Cohesion: 0.19
Nodes (8): VehicleDisplayNameFormatter, DiscardChangesDialog(), AddReminderScreen(), AddReminderVehicleHeader(), Modifier, EditReminderVehicleHeader(), com, Modifier

### Community 29 - "ManualBackupManagerTest"
Cohesion: 0.15
Nodes (6): Flow, MileageLogDao, MileageLogEntity, toDomain(), toEntity(), ManualBackupManagerTest

### Community 30 - "VehicleDao"
Cohesion: 0.16
Nodes (3): Flow, VehicleDao, VehicleEntity

### Community 31 - "DashboardViewModelTest.kt"
Cohesion: 0.28
Nodes (6): AppInfo, DashboardData, GetDashboardDataUseCase, Flow, VehicleWithStatus, DashboardViewModelTest

### Community 32 - "ManualBackupManager.kt"
Cohesion: 0.27
Nodes (8): AutoMinderBackupData, BackupRestoreSummary, FuelEntryBackupDto, ManualBackupManager, MileageLogBackupDto, ReminderBackupDto, ServiceBackupDto, VehicleBackupDto

### Community 33 - "ServiceDao"
Cohesion: 0.19
Nodes (5): Flow, ServiceDao, ServiceEntity, toDomain(), toEntity()

### Community 34 - "ReminderDetailSheet.kt"
Cohesion: 0.27
Nodes (10): DuePrediction, info(), Color, ReminderDetailSheet(), ServiceTypeInfo, SeverityBadge(), SheetSection(), reminderTiming() (+2 more)

### Community 35 - "ProPaywall.kt"
Cohesion: 0.15
Nodes (16): Modifier, PremiumPaywallPlanCard(), Available, Loading, PremiumPriceDisplay, Unavailable, FeatureCheck(), FeatureComparisonTable() (+8 more)

### Community 36 - "NavRoutes"
Cohesion: 0.11
Nodes (18): About, AddFuel, AddReminder, AddService, AddVehicle, Dashboard, EditReminder, EditVehicle (+10 more)

### Community 37 - "AppDatabase"
Cohesion: 0.18
Nodes (5): MigrationTest, DatabaseModule, Context, AppDatabase, RoomDatabase

### Community 40 - "FuelDao"
Cohesion: 0.20
Nodes (5): FuelDao, Flow, FuelEntryEntity, toDomain(), toEntity()

### Community 41 - "DistanceUtil"
Cohesion: 0.16
Nodes (8): DistanceUtil, com, FleetHeroSummaryCard(), Modifier, labelRes(), VehicleListContent(), VehicleListRow(), DateFormatUtil

### Community 42 - "ServiceTypeGrid.kt"
Cohesion: 0.48
Nodes (5): Modifier, ServiceChoice(), ServiceTypeGrid(), icon(), ImageVector

### Community 44 - "AutoMinder PRD v4.0"
Cohesion: 0.14
Nodes (14): Product identity & strategic positioning, Architecture laws (MVVM + Clean + Offline-First), 30-day build roadmap (P1-P12), Closing statement, Room entities v2 database design, AutoMinder PRD v4.0, Folder structure specification, Maintenance interval algorithm (dual-trigger) (+6 more)

### Community 46 - "AddServiceUiEvent"
Cohesion: 0.11
Nodes (19): AddServiceUiEvent, CostBreakdownToggled, CostChanged, CustomLabelChanged, LaborCostChanged, NotesChanged, OdometerAdjusted, OdometerChanged (+11 more)

### Community 48 - "Counter Mode Flow & State Spec"
Cohesion: 0.18
Nodes (16): Step 1 — Capture, Counter Mode (Quote Auditor UI), Step 4 — Decision Confirm, decisions table (Gate C schema), Counter Mode Flow & State Spec, Gate B (design gate), NavRoutes.CounterMode route, quote_lines table (Gate C schema) (+8 more)

### Community 49 - "CLAUDE.md Governance (v6.1)"
Cohesion: 0.19
Nodes (15): Compose Performance Reviewer Agent, Visual QA Agent, AutoMinder Data & Domain Rules, AutoMinder Android Release Gate Skill, AutoMinder AVD Visual QA Skill, Compose Performance Guardian Skill, AutoMinder UI Diff Review Skill, AutoMinder CI Workflow (+7 more)

### Community 50 - "AutoMinderWidget.kt"
Cohesion: 0.08
Nodes (37): AutoMinderWidget, AutoMinderWidgetReceiver, createActionIntent(), createMainIntent(), Context, Intent, LargeWidgetContent(), MediumWidgetContent() (+29 more)

### Community 51 - "AutoMinder Play Store Release Checklist"
Cohesion: 0.17
Nodes (13): Play Store full description, "Fleet Health Score" marketing claim, AutoMinder Pro "7-day free trial" claim, Performance gates (S1-S3, Baseline Profile unwired), Ads (AdMob) & consent checklist, Build & signing checklist, AutoMinder Play Store Release Checklist, Pre-submit smoke test (+5 more)

### Community 52 - "Brand and Design System"
Cohesion: 0.18
Nodes (14): Bottom nav: Home/Vehicles/Records/Settings, Data display conventions (money cents / distance km), AutoMinder Design System v1.0, AutoMinder Brand & Design System (Figma), Haptics vocabulary, Motion system (springs, Motion.kt), Onboarding doctrine (activation-first), Racing Teal #006B5F (+6 more)

### Community 53 - "Project Setup and Policy"
Cohesion: 0.14
Nodes (14): AdMob ID policy, Active branches convention, Build variants (debug/release AdMob IDs), AutoMinder Project-Specific Overrides (GEMINI.md), Session verification protocol, Release signing policy, Technical foundation / exact versions table, Branch strategy (+6 more)

### Community 54 - "ExportServiceHistoryUseCase"
Cohesion: 0.23
Nodes (4): ExportServiceHistoryUseCase, Uri, ExportServiceHistoryUseCaseTest, Context

### Community 55 - "Service"
Cohesion: 0.15
Nodes (4): Service, IServiceRepository, Flow, ServiceHistoryViewModelTest

### Community 56 - "Composable"
Cohesion: 0.20
Nodes (16): Modifier, LoadingState(), FormSectionCard(), Modifier, CompactContent(), ExpandedContent(), androidx, Color (+8 more)

### Community 57 - "AddReminderViewModel.kt"
Cohesion: 0.28
Nodes (5): AddReminderUiState, AddReminderViewModel, DefaultInterval, StateFlow, ViewModel

### Community 58 - "AddServiceViewModel.kt"
Cohesion: 0.29
Nodes (6): SuggestedInterval, AddServiceUiState, AddServiceViewModel, StateFlow, ViewModel, Job

### Community 59 - "AddReminderUiEvent"
Cohesion: 0.17
Nodes (12): AddReminderUiEvent, DescriptionChanged, DueDateChanged, DueKmChanged, IntervalDaysChanged, IntervalKmChanged, PermissionRequestHandled, SaveClicked (+4 more)

### Community 61 - ".buildPlan"
Cohesion: 0.19
Nodes (4): CreateDefaultRemindersUseCase, PlannedReminder, ReminderTemplate, CreateDefaultRemindersUseCaseTest

### Community 62 - "AutoMinder Product Strategy Deep Review"
Cohesion: 0.14
Nodes (14): Fuel Intelligence Algorithm (4-phase Bayesian+EMA), AI strategy (drill-down only, evidence-carded), App flow recommendations, Competitor weaknesses to attack (table), Executive product diagnosis (strongest/weakest), AutoMinder Product Strategy Deep Review, Final recommendation, Growth & app store strategy (+6 more)

### Community 63 - "Premium Compose Components Skill"
Cohesion: 0.18
Nodes (11): Fleet Design Inspiration (behance.net/gallery/250251481 — translated to Racing Teal M3, never copy Fleet's yellow/purple palette or fake content), FormSectionCard spec (title+helper+content+error slot), HealthCockpitCard spec (human-verdict headline, score ring demoted to instrument, never a lone giant '0'), InsightMetricCard spec (eyebrow label + Mono value + unit), Premium Compose Components Skill, PremiumActionGrid spec (2x2 FilledTonal tiles, ≥56dp), PremiumPaywallPlanCard spec (Mono price or loading, badge, selected = tonal lift + 1.02 scale), PremiumSectionHeader spec (title+count badge+trailing action, heading() semantics) (+3 more)

### Community 65 - "AddFuelUiEvent"
Cohesion: 0.13
Nodes (15): AddFuelUiEvent, CostChanged, DateChanged, ErrorDismissed, FullTankToggled, GasStationChanged, NotesChanged, OdometerChanged (+7 more)

### Community 66 - "ReminderWithStatus"
Cohesion: 0.27
Nodes (6): ReminderWithStatus, buildUpcomingSubtitle(), Modifier, MaintenanceRow(), UpcomingMaintenanceSection(), ReminderPriorityEngineTest

### Community 67 - "evaluateReminderStaleness"
Cohesion: 0.33
Nodes (3): evaluateReminderStaleness(), RemindersDelayedState, ReminderStalenessTest

### Community 69 - "AutoMinder 2026 MVP Plan"
Cohesion: 0.18
Nodes (13): Billing 9.1.0 migration (migration/play-billing-9), AutoMinder 2026 MVP Plan, MVP-ready blocking gates (B1-B8), Per-screen problem/answer table, Platform gates (P1-P6), Execution queue (Step 0-7), Reminder engine cannot keep the promise (correctness risk), Honest risk register (+5 more)

### Community 70 - "AutoMinder UI Slice Skill"
Cohesion: 0.20
Nodes (10): Android UI Architect Agent, Shape Set by Component Family, Never by Status (status-dependent corner radii removed — a list whose radii vary per row reads as broken, not informative), 12 AutoMinder 2026 Premium UI Rules, AutoMinder UI Slice Skill, StatusReminderCard spec (status corner morphing 8/16/28dp via animateDpAsState, 4dp error rail for OVERDUE), Root DESIGN_SYSTEM.md (archived/stale — do not consult), docs/DESIGN_SYSTEM_2026.md (Midnight Cobalt authority), Mobbin Design Blueprint 2026 (exec plan) (+2 more)

### Community 71 - "EditVehicleViewModel.kt"
Cohesion: 0.08
Nodes (24): ValidationError, ValidationErrorCode, COST_NEGATIVE, FIELD_REQUIRED, ODOMETER_NEGATIVE, VIN_INVALID_FORMAT, YEAR_TOO_EARLY, YEAR_TOO_LATE (+16 more)

### Community 72 - "AutoMinder Complete UI Audit (2026 Standards)"
Cohesion: 0.22
Nodes (10): Document authority table, Quality bar items (Q1-Q6), Accessibility findings (WCAG 2.1 AA), AutoMinder Complete UI Audit (2026 Standards), Loading-state inconsistency (skeleton vs spinner), Numeric typography drift (JetBrains Mono not applied everywhere), Prioritized v1.1 redesign roadmap, Screen-by-screen findings (+2 more)

### Community 73 - "AutoMinder UI Rules (Midnight Cobalt)"
Cohesion: 0.28
Nodes (9): Accessibility Reviewer Agent, StatusCalculator: OVERDUE Always Beats SNOOZED; Never Notify GOOD/SNOOZED/DISABLED, Midnight Cobalt Brand Tokens (Racing Teal #006B5F retired — predates the Night Garage commit), Valid ServiceStatus States: OVERDUE/DUE_SOON/SNOOZED/OK/COMPLETED/UNKNOWN — GOOD, DISABLED, NO_DATA, isEnabled, health-score explicitly do not exist, Skeleton Opacity Pulse 0.40→0.70 1000ms (not a translating shimmer sweep — a moving gradient costs frames every skeleton with no ongoing activity), AutoMinder UI Rules (Midnight Cobalt), AutoMinder Accessibility QA Skill, AGENTS.md StatusCalculator Logic incl. GOOD/DISABLED priority order (marked VERIFIED CORRECT, dated 2026-04) (+1 more)

### Community 74 - "AGENTS.md Agent Configuration"
Cohesion: 0.22
Nodes (9): Play-Store-Readiness: Products Query Split SUBS/INAPP, AGENTS.md Agent Scope Control Table (one scope per agent session, prevents merge conflicts), AGENTS.md Agent Configuration, AGENTS.md Billing 7.1.1: One-Time Purchase Only, No Subscriptions, AGENTS.md Do NOT Touch Protected Files List, AGENTS.md Pinned Tech Stack Table (Kotlin 2.1.21, AGP 8.9.1, KSP 2.1.21-2.0.1, etc. — 'never change without explicit human approval'), MVVM Clean Architecture Layering Law (UI→ViewModel→UseCase→Repository→DAO), Security & Privacy Law (keystore/local.properties/secrets never touched or logged) (+1 more)

### Community 75 - "AutoMinder Marketing Website Home"
Cohesion: 0.31
Nodes (6): Privacy Policy / Data Safety Drift, AutoMinder Privacy Policy (Store), AutoMinder Google Play Store Listing, AutoMinder Marketing Website Home, AutoMinder Website Privacy Policy, AutoMinder Website Support Page

### Community 76 - "AddVehicleViewModel.kt"
Cohesion: 0.16
Nodes (14): AddVehicleUiEvent, AddVehicleUiState, AddVehicleViewModel, BrandChanged, StateFlow, ViewModel, ModelChanged, OdometerChanged (+6 more)

### Community 77 - "MileageLogViewModel"
Cohesion: 0.30
Nodes (4): StateFlow, ViewModel, MileageLogUiState, MileageLogViewModel

### Community 81 - "VehicleDataConfidence"
Cohesion: 0.25
Nodes (6): VehicleDataConfidence, ESTIMATED, HIGH, INCOMPLETE, MEDIUM, MISSING_MILEAGE

### Community 82 - "EditReminderViewModel.kt"
Cohesion: 0.29
Nodes (4): EditReminderUiState, EditReminderViewModel, StateFlow, ViewModel

### Community 83 - "Skeleton.kt"
Cohesion: 0.68
Nodes (7): DashboardSkeleton(), Modifier, Shape, SkeletonBar(), skeletonFill(), SkeletonScaffold(), VehicleCardSkeleton()

### Community 84 - "AboutScreen.kt"
Cohesion: 0.57
Nodes (7): AboutActionItem(), AboutFeatureBadge(), AboutFeaturesRow(), AboutHeroCard(), AboutScreen(), ImageVector, Modifier

### Community 86 - "AutoMinder Session Handoff (2026-07-07)"
Cohesion: 0.14
Nodes (15): Beyond-UI release backlog, Premium component kit (ui/components/premium/), 12 AutoMinder 2026 Premium UI Rules, AutoMinder Session Handoff (2026-07-07), Environment recipes (Windows Gradle, AVD, editing traps), Fleet Behance case study (storytelling reference), Slice 8 nit list, UI rescue pipeline (Slices 0-8) (+7 more)

### Community 87 - "AutoMinderTestRunner.kt"
Cohesion: 0.43
Nodes (5): AndroidJUnitRunner, AutoMinderTestRunner, Application, Context, ClassLoader

### Community 89 - "OnboardingViewModel.kt"
Cohesion: 0.24
Nodes (4): StateFlow, ViewModel, OnboardingUiState, OnboardingViewModel

### Community 90 - "ServiceChoicePicker.kt"
Cohesion: 0.57
Nodes (6): AllServicesRow(), ChoiceFlow(), Modifier, SectionLabel(), ServiceChoice(), ServiceChoicePicker()

### Community 91 - "StatusChip"
Cohesion: 0.27
Nodes (8): Modifier, MaintenanceVerdictCard(), Modifier, ProactiveAttentionCard(), Modifier, StatusReminderCard(), Modifier, StatusChip()

### Community 92 - "ScreenState"
Cohesion: 0.40
Nodes (5): ScreenState, Empty, Error, Loading, Success

### Community 93 - "AutoMinder US Google Play Launch Scorecard"
Cohesion: 0.29
Nodes (7): AutoMinder US Google Play Launch Scorecard, Policy & privacy (§5), Product quality gate (§1), Ratings & support (§4), Retention & engagement metrics (§3), Staged rollout plan (§6), Store conversion checklist (§2)

### Community 94 - "EditReminderUiEvent"
Cohesion: 0.17
Nodes (12): CustomLabelChanged, DeleteClicked, DueDateChanged, DueKmChanged, EditReminderUiEvent, IntervalDaysChanged, IntervalKmChanged, NotesChanged (+4 more)

### Community 95 - "AutoMinder Play Store Readiness Skill"
Cohesion: 0.47
Nodes (6): Billing Correctness (verified PURCHASED entitlement, prompt ack, restore at startup, no logged tokens), AutoMinder Monetization Rules, UMP Consent Gate (no ad request until canRequestAds is true), Play-Store-Readiness: Billing 7.1.1 Pinned, Do NOT Propose Upgrade (Billing 8 tracked as separate pre-Aug-2026 task), AutoMinder Play Store Readiness Skill, Billing Library 7→9.x Migration Mandatory Before 2026-08-31 Deadline

### Community 97 - "stitch-proxy.mjs"
Cohesion: 0.33
Nodes (4): cloudsdkConfig, gcloud, proxy, stitchHome

### Community 98 - "VehicleCatalog"
Cohesion: 0.24
Nodes (6): VehicleCatalog, Modifier, VehiclePickerMode, MAKE, MODEL, VehiclePickerSheet()

### Community 99 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 100 - "VehicleListViewModel.kt"
Cohesion: 0.27
Nodes (9): Empty, Error, StateFlow, ViewModel, Loading, Success, VehicleListItem, VehicleListUiState (+1 more)

### Community 102 - "localizedLabel"
Cohesion: 0.27
Nodes (6): Context, labelRes(), localizedLabel(), localizedLabel(), CoroutineWorker, ReminderCheckWorker

### Community 104 - "EmptyState"
Cohesion: 0.40
Nodes (8): EmptyState(), ImageVector, Modifier, Modifier, MileageCockpitCard(), MileageLogScreen(), MileageTimelineCard(), MileageVehicleHeader()

### Community 109 - "MileageLogUiEvent"
Cohesion: 0.20
Nodes (10): AddClicked, DeleteLog, MileageLogUiEvent, NewNotesChanged, NewOdometerChanged, ResetSuccess, Retry, SelectTag (+2 more)

### Community 111 - "Result"
Cohesion: 0.39
Nodes (5): BackupCoordinator, Failed, Partial, Result, Success

### Community 113 - "Color.kt"
Cohesion: 0.60
Nodes (4): AutoMinderServiceStatusBadge(), AutoMinderStatusBadge(), Modifier, StatusBadgeConfig

### Community 125 - "VehicleDetailScreen.kt"
Cohesion: 0.14
Nodes (21): CostByTypeDonut(), Modifier, Modifier, SpendingTrendChart(), ActionTile(), Modifier, PremiumAction, PremiumActionGrid() (+13 more)

### Community 126 - "Motion"
Cohesion: 0.20
Nodes (7): FuelEfficiencyChart(), Modifier, FormField(), Modifier, Motion, FiniteAnimationSpec, T

### Community 127 - "PredictDueUseCaseTest"
Cohesion: 0.15
Nodes (4): OdometerPoint, PredictDueUseCase, PerformanceStressBenchmarkTest, PredictDueUseCaseTest

### Community 128 - "ServiceHistoryScreen.kt"
Cohesion: 0.36
Nodes (9): Modifier, SwipeToDeleteContainer(), CategoryFilterRow(), com, Modifier, LifetimeSpendHeroCard(), ServiceHistoryStream(), ServiceReceiptCard() (+1 more)

### Community 131 - "InsightMetricCard.kt"
Cohesion: 0.70
Nodes (4): InsightMetricCard(), InsightMetricRow(), ImageVector, Modifier

### Community 132 - "FuelHistoryScreen.kt"
Cohesion: 0.56
Nodes (8): FuelEfficiencyTrendCard(), FuelHistoryScreen(), FuelIntelligenceCockpit(), FuelReceiptCard(), FuelSpendingBars(), FuelSpendingTrendCard(), FuelVehicleHeader(), Modifier

### Community 133 - "DrivingAmount"
Cohesion: 0.17
Nodes (11): DrivingAmount, HIGH, LOW, TYPICAL, DrivingAmountChips(), DrivingConfig, getDrivingAmountConfig(), Modifier (+3 more)

### Community 134 - "VehicleOperationalStatus"
Cohesion: 0.25
Nodes (6): VehicleOperationalStatus, DUE_SOON, HEALTHY, OVERDUE, SETUP_INCOMPLETE, UPCOMING

### Community 137 - "ServiceDetailScreen.kt"
Cohesion: 0.46
Nodes (7): Context, ImageVector, Modifier, ServiceDetailBentoContent(), serviceIconFor(), ServiceTelemetryPill(), shareServiceReceipt()

### Community 138 - "AddVehicleScreen.kt"
Cohesion: 0.36
Nodes (6): AddVehicleForm(), ImageVector, KeyboardType, Modifier, VehicleField(), VehiclePhotoHero()

### Community 139 - "EditVehicleScreen.kt"
Cohesion: 0.52
Nodes (6): EditVehicleField(), EditVehicleForm(), EditVehiclePhotoHero(), ImageVector, KeyboardType, Modifier

### Community 140 - "AutoMinder Market R&D - 2026-08-23"
Cohesion: 0.25
Nodes (7): AutoMinder Market R&D - 2026-08-23, Competitive Pattern, Executive Verdict, Launch Positioning, Market Scan, Product Strategy, Release Plan Impact

### Community 141 - "VehicleDetailUiEvent"
Cohesion: 0.29
Nodes (7): ArchiveClicked, ExportClicked, ExportConsumed, MarkReminderComplete, SnoozeReminder, UpdateOdometer, VehicleDetailUiEvent

## Ambiguous Edges - Review These
- `AutoMinder Design System 2026` → `AutoMinder MVP Plan`  [AMBIGUOUS]
  docs/exec-plans/mvp-plan.html · relation: references
- `Production readiness checklist` → `AutoMinder Production Readiness`  [AMBIGUOUS]
  PRODUCTION_READINESS.md · relation: semantically_similar_to
- `AutoMinder Brand & Design System (Figma)` → `Figma file (design source of truth)`  [AMBIGUOUS]
  RELEASE_CHECKLIST_v1.1_UI.md · relation: conceptually_related_to
- `Shape Set by Component Family, Never by Status (status-dependent corner radii removed — a list whose radii vary per row reads as broken, not informative)` → `StatusReminderCard spec (status corner morphing 8/16/28dp via animateDpAsState, 4dp error rail for OVERDUE)`  [AMBIGUOUS]
  .claude/skills/premium-compose-components/SKILL.md · relation: conceptually_related_to
- `StatusCalculator: OVERDUE Always Beats SNOOZED; Never Notify GOOD/SNOOZED/DISABLED` → `Valid ServiceStatus States: OVERDUE/DUE_SOON/SNOOZED/OK/COMPLETED/UNKNOWN — GOOD, DISABLED, NO_DATA, isEnabled, health-score explicitly do not exist`  [AMBIGUOUS]
  .claude/rules/data.md · relation: conceptually_related_to
- `Valid ServiceStatus States: OVERDUE/DUE_SOON/SNOOZED/OK/COMPLETED/UNKNOWN — GOOD, DISABLED, NO_DATA, isEnabled, health-score explicitly do not exist` → `AGENTS.md StatusCalculator Logic incl. GOOD/DISABLED priority order (marked VERIFIED CORRECT, dated 2026-04)`  [AMBIGUOUS]
  AGENTS.md · relation: conceptually_related_to
- `Play-Store-Readiness: Products Query Split SUBS/INAPP` → `AGENTS.md Billing 7.1.1: One-Time Purchase Only, No Subscriptions`  [AMBIGUOUS]
  AGENTS.md · relation: conceptually_related_to
- `AGENTS.md Pinned Tech Stack Table (Kotlin 2.1.21, AGP 8.9.1, KSP 2.1.21-2.0.1, etc. — 'never change without explicit human approval')` → `gradle/libs.versions.toml as Sole Dependency-Version Authority`  [AMBIGUOUS]
  AGENTS.md · relation: conceptually_related_to
- `AutoMinder Privacy Policy (Store)` → `AutoMinder Website Privacy Policy`  [AMBIGUOUS]
  store/PRIVACY_POLICY.md · relation: conceptually_related_to
- `Play-Store-Readiness: Billing 7.1.1 Pinned, Do NOT Propose Upgrade (Billing 8 tracked as separate pre-Aug-2026 task)` → `Billing Library 7→9.x Migration Mandatory Before 2026-08-31 Deadline`  [AMBIGUOUS]
  .claude/skills/play-store-readiness/SKILL.md · relation: conceptually_related_to

## Knowledge Gaps
- **344 isolated node(s):** `npx`, `Processing`, `Cancelled`, `Pending`, `InProgress` (+339 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **31 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `AutoMinder Design System 2026` and `AutoMinder MVP Plan`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **What is the exact relationship between `Production readiness checklist` and `AutoMinder Production Readiness`?**
  _Edge tagged AMBIGUOUS (relation: semantically_similar_to) - confidence is low._
- **What is the exact relationship between `AutoMinder Brand & Design System (Figma)` and `Figma file (design source of truth)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Shape Set by Component Family, Never by Status (status-dependent corner radii removed — a list whose radii vary per row reads as broken, not informative)` and `StatusReminderCard spec (status corner morphing 8/16/28dp via animateDpAsState, 4dp error rail for OVERDUE)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `StatusCalculator: OVERDUE Always Beats SNOOZED; Never Notify GOOD/SNOOZED/DISABLED` and `Valid ServiceStatus States: OVERDUE/DUE_SOON/SNOOZED/OK/COMPLETED/UNKNOWN — GOOD, DISABLED, NO_DATA, isEnabled, health-score explicitly do not exist`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Valid ServiceStatus States: OVERDUE/DUE_SOON/SNOOZED/OK/COMPLETED/UNKNOWN — GOOD, DISABLED, NO_DATA, isEnabled, health-score explicitly do not exist` and `AGENTS.md StatusCalculator Logic incl. GOOD/DISABLED priority order (marked VERIFIED CORRECT, dated 2026-04)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Play-Store-Readiness: Products Query Split SUBS/INAPP` and `AGENTS.md Billing 7.1.1: One-Time Purchase Only, No Subscriptions`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._