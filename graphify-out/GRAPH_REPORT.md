# Graph Report - Autominder  (2026-08-21)

## Corpus Check
- 258 files · ~161,194 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1974 nodes · 4217 edges · 138 communities (109 shown, 29 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 79 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `04be0705`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- FuelEntry
- OnboardingViewModelTest
- SubscriptionManager
- QuoteAuditorViewModel.kt
- AddServiceUiEvent
- ServiceType
- AdManager.kt
- Vehicle
- AddVehicleViewModel.kt
- AutoMinder MVP Plan
- AddFuelViewModel
- VehicleDetailViewModel.kt
- ServiceHistoryViewModel.kt
- Reminder
- ReminderDao
- Service
- VehicleDao
- FuelHistoryViewModel.kt
- MileageLogEntry
- DashboardViewModelTest.kt
- ServiceStatus
- ReminderUrgency
- AddServiceScreen.kt
- ServiceDao
- ReminderWithStatus
- PredictDueUseCaseTest
- Motion
- NavRoutes
- ManualBackupManagerTest
- LocalDistanceUnit.kt
- ServiceRepositoryImplTest
- AppDatabase
- FuelDao
- PremiumStatusStyle
- ReminderCheckWorkerTest
- AutoMinder PRD v4.0
- ManualBackupManager.kt
- MutableInteractionSource
- ValidatorsTest
- Counter Mode Flow & State Spec
- CLAUDE.md Governance (v6.1)
- .buildPlan
- Composable
- DashboardViewModel.kt
- AutoMinderWidget.kt
- VehicleListViewModelTest
- MainActivity.kt
- DistanceUtil
- ListSkeleton
- OnboardingScreen.kt
- EditReminderViewModel.kt
- AutoMinder Play Store Release Checklist
- AutoMinder Design System v1.0
- AutoMinder README
- Result
- AddReminderViewModel.kt
- ServiceRepositoryImpl
- Timber
- AnalyticsHelper
- DrivingAmount
- QuoteAuditorViewModelTest
- WeeklyDigestWorker.kt
- ServiceDetailScreen.kt
- VehicleOperationalStatus
- AddReminderUiEvent
- AutoMinder 2026 Design Blueprint (Mobbin-Informed)
- AutoMinder Product Strategy Deep Review
- Premium Compose Components Skill
- ServiceCompletionTransactionTest
- AddFuelUiEvent
- ReminderDetailSheet.kt
- evaluateReminderStaleness
- ServiceDetailViewModel.kt
- VehicleListViewModel.kt
- StatusCalculatorTest
- AddServiceViewModelTest
- AutoMinder 2026 MVP Plan
- AutoMinder UI Slice Skill
- OnboardingViewModel.kt
- VehicleCatalogTest
- AutoMinder Complete UI Audit (2026 Standards)
- AutoMinder UI Rules (Midnight Cobalt)
- AGENTS.md Agent Configuration
- ReminderCheckWorker.kt
- UserPreferences
- FuelHistoryScreen.kt
- ServiceHistoryScreen.kt
- VehicleCatalog
- ServiceHistoryViewModelTest
- ReminderAlarmSchedulerTest
- DateFormatUtil
- AboutScreen.kt
- CalculateEfficiencyUseCase
- ServiceChoicePicker.kt
- OverdueCopyTest
- AutoMinder Session Handoff (2026-07-07)
- AutoMinderTestRunner.kt
- QuoteAuditorScreen.kt
- ReminderPriorityEngine
- QuickLogSection.kt
- pressScale
- AutoMinder US Google Play Launch Scorecard
- AutoMinder UI Production-Readiness Checklist
- AutoMinder Play Store Readiness Skill
- NotificationHelper
- AddServiceViewModel.kt
- AutoMinder Marketing Website Home
- VehicleDisplayNameTest
- stitch-proxy.mjs
- ConsentManager.kt
- FuelHistoryViewModelTest
- InsightMetricCard.kt
- AuditQuoteUseCase
- AddVehicleScreen.kt
- gradlew
- UpdateHelper
- .onCreate
- BannerAdView
- PermissionUtils
- AddFuelViewModel.kt
- BaselineProfileGenerator
- firebase
- Play Store short description
- Play Store listing title
- Lesson: Figma MCP Quirks
- AutoMinder Favicon Icon
- QuoteVerdictStatus
- EfficiencyTrend
- ReviewHelper
- ActivityType
- FormSectionCard.kt

## God Nodes (most connected - your core abstractions)
1. `ServiceType` - 85 edges
2. `Vehicle` - 78 edges
3. `Reminder` - 61 edges
4. `Service` - 52 edges
5. `IVehicleRepository` - 47 edges
6. `UserPreferences` - 44 edges
7. `FuelEntry` - 44 edges
8. `ServiceStatus` - 44 edges
9. `DistanceUtil` - 36 edges
10. `ReminderDao` - 34 edges

## Surprising Connections (you probably didn't know these)
- `Product quality gate (§1)` --semantically_similar_to--> `Billing 9.1.0 migration (migration/play-billing-9)`  [INFERRED] [semantically similar]
  PLAY_LAUNCH_SCORECARD.md → PLAN.md
- `Quote Auditor v0 (no OCR, no photo)` --semantically_similar_to--> `Counter Mode (Quote Auditor UI)`  [INFERRED] [semantically similar]
  PRODUCT_STRATEGY_AUTOMINDER.md → COUNTER_MODE_FLOW.md
- `"Fleet Health Score" marketing claim` --semantically_similar_to--> `Contradictions, resolved (table)`  [INFERRED] [semantically similar]
  app/src/main/play/listings/en-US/full-description.txt → PLAN.md
- `Brutally honest risks (10 items)` --semantically_similar_to--> `Billing 9.1.0 migration (migration/play-billing-9)`  [INFERRED] [semantically similar]
  PRODUCT_STRATEGY_AUTOMINDER.md → PLAN.md
- `Orphaned premium components (built, never wired)` --semantically_similar_to--> `Premium component kit (ui/components/premium/)`  [INFERRED] [semantically similar]
  UI_AUDIT_2026.md → HANDOFF.md

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

## Communities (138 total, 29 thin omitted)

### Community 0 - "FuelEntry"
Cohesion: 0.13
Nodes (6): FuelRepositoryImpl, Flow, FuelEntry, IFuelRepository, Flow, CalculateEfficiencyUseCaseTest

### Community 2 - "SubscriptionManager"
Cohesion: 0.06
Nodes (36): Cancelled, Error, Idle, InProgress, Activity, ProductDetails, StateFlow, NotFound (+28 more)

### Community 3 - "QuoteAuditorViewModel.kt"
Cohesion: 0.16
Nodes (13): AddItem, AnalyzeQuote, ClearSavedMessage, StateFlow, ViewModel, QuoteAuditorUiEvent, QuoteAuditorUiState, QuoteAuditorViewModel (+5 more)

### Community 4 - "AddServiceUiEvent"
Cohesion: 0.12
Nodes (16): AddServiceUiEvent, CostChanged, CustomLabelChanged, NotesChanged, OdometerAdjusted, OdometerChanged, QuickCostSelected, QuickDateSelected (+8 more)

### Community 5 - "ServiceType"
Cohesion: 0.10
Nodes (18): Converters, ServiceType, AIR_FILTER, BATTERY, BRAKE_SERVICE, CABIN_FILTER, COOLANT, CUSTOM (+10 more)

### Community 6 - "AdManager.kt"
Cohesion: 0.11
Nodes (14): AdError, AdManager, InterstitialAdLoadCallback, RewardedAdLoadCallback, RewardedInterstitialAdLoadCallback, FullScreenContentCallback, FullScreenContentCallback, FullScreenContentCallback (+6 more)

### Community 7 - "Vehicle"
Cohesion: 0.15
Nodes (5): Flow, VehicleRepositoryImpl, Vehicle, IVehicleRepository, Flow

### Community 8 - "AddVehicleViewModel.kt"
Cohesion: 0.05
Nodes (38): ValidationError, ValidationErrorCode, COST_NEGATIVE, FIELD_REQUIRED, ODOMETER_NEGATIVE, VIN_INVALID_FORMAT, YEAR_TOO_EARLY, YEAR_TOO_LATE (+30 more)

### Community 9 - "AutoMinder MVP Plan"
Cohesion: 0.16
Nodes (19): AdMob ID Production Safety Gate Pattern, Play Billing 7.1.1 to 9.1.0 Migration, Demo Data Credibility Issue, Four Bottom Navigation Tabs (Home/Vehicles/Records/Settings), Fuel Intelligence Algorithm Phases (v1.1+), Health Score Fabrication / Banned Language, Locale Claims Mismatch (Gate B5), AutoMinder MVP Plan Thesis (+11 more)

### Community 11 - "VehicleDetailViewModel.kt"
Cohesion: 0.05
Nodes (39): CostByTypeDonut(), Modifier, Modifier, SpendingTrendChart(), ActionTile(), Modifier, PremiumAction, PremiumActionGrid() (+31 more)

### Community 12 - "ServiceHistoryViewModel.kt"
Cohesion: 0.09
Nodes (24): ClearExportUri, ClearFilters, DeleteService, ExportHistory, ExportPassport, StateFlow, ViewModel, Retry (+16 more)

### Community 13 - "Reminder"
Cohesion: 0.14
Nodes (5): Flow, ReminderRepositoryImpl, Reminder, IReminderRepository, Flow

### Community 14 - "ReminderDao"
Cohesion: 0.13
Nodes (5): Flow, ReminderDao, ReminderEntity, toDomain(), toEntity()

### Community 15 - "Service"
Cohesion: 0.17
Nodes (5): Service, IServiceRepository, Flow, ExportServiceHistoryUseCaseTest, Context

### Community 16 - "VehicleDao"
Cohesion: 0.13
Nodes (5): Flow, VehicleDao, VehicleEntity, toDomain(), toEntity()

### Community 17 - "FuelHistoryViewModel.kt"
Cohesion: 0.19
Nodes (11): DeleteEntry, EfficiencyExtreme, FuelEntryDetailed, FuelHistoryUiEvent, FuelHistoryUiState, FuelHistoryViewModel, FuelMonthlySpend, StateFlow (+3 more)

### Community 18 - "MileageLogEntry"
Cohesion: 0.06
Nodes (24): RepositoryModule, toDomain(), toEntity(), Flow, MileageLogRepositoryImpl, MileageLogEntry, IMileageLogRepository, Flow (+16 more)

### Community 19 - "DashboardViewModelTest.kt"
Cohesion: 0.28
Nodes (6): AppInfo, DashboardData, GetDashboardDataUseCase, Flow, VehicleWithStatus, DashboardViewModelTest

### Community 20 - "ServiceStatus"
Cohesion: 0.17
Nodes (15): ServiceStatus, COMPLETED, DUE_SOON, OK, OVERDUE, SNOOZED, UNKNOWN, Modifier (+7 more)

### Community 21 - "ReminderUrgency"
Cohesion: 0.29
Nodes (7): ReminderUrgency, DUE_SOON, FUTURE, MILEAGE_BASED, OVERDUE, SAFETY_CRITICAL, TIME_SENSITIVE

### Community 22 - "AddServiceScreen.kt"
Cohesion: 0.18
Nodes (15): Modifier, SaveButton(), SaveButtonState, Idle, Saving, Success, AddFuelBentoContent(), FuelStatPill() (+7 more)

### Community 23 - "ServiceDao"
Cohesion: 0.20
Nodes (5): Flow, ServiceDao, ServiceEntity, toDomain(), toEntity()

### Community 25 - "PredictDueUseCaseTest"
Cohesion: 0.14
Nodes (5): DuePrediction, OdometerPoint, PredictDueUseCase, PerformanceStressBenchmarkTest, PredictDueUseCaseTest

### Community 26 - "Motion"
Cohesion: 0.09
Nodes (28): FormField(), Modifier, Modifier, PremiumPaywallPlanCard(), Available, Loading, PremiumPriceDisplay, Unavailable (+20 more)

### Community 27 - "NavRoutes"
Cohesion: 0.11
Nodes (18): About, AddFuel, AddReminder, AddService, AddVehicle, Dashboard, EditReminder, EditVehicle (+10 more)

### Community 28 - "ManualBackupManagerTest"
Cohesion: 0.19
Nodes (4): Flow, MileageLogDao, MileageLogEntity, ManualBackupManagerTest

### Community 29 - "LocalDistanceUnit.kt"
Cohesion: 0.23
Nodes (10): DiscardChangesDialog(), Modifier, ServiceChoice(), ServiceTypeGrid(), AddReminderScreen(), AddReminderVehicleHeader(), Modifier, EditReminderVehicleHeader() (+2 more)

### Community 31 - "AppDatabase"
Cohesion: 0.16
Nodes (5): MigrationTest, DatabaseModule, Context, AppDatabase, RoomDatabase

### Community 32 - "FuelDao"
Cohesion: 0.20
Nodes (5): FuelDao, Flow, FuelEntryEntity, toDomain(), toEntity()

### Community 33 - "PremiumStatusStyle"
Cohesion: 0.36
Nodes (4): Color, Dp, Shape, PremiumStatusStyle

### Community 35 - "AutoMinder PRD v4.0"
Cohesion: 0.12
Nodes (17): What ships next (v1.1) — not now, Product identity & strategic positioning, Architecture laws (MVVM + Clean + Offline-First), 30-day build roadmap (P1-P12), Closing statement, Daily engagement / gamification system, Room entities v2 database design, AutoMinder PRD v4.0 (+9 more)

### Community 36 - "ManualBackupManager.kt"
Cohesion: 0.27
Nodes (8): AutoMinderBackupData, BackupRestoreSummary, FuelEntryBackupDto, ManualBackupManager, MileageLogBackupDto, ReminderBackupDto, ServiceBackupDto, VehicleBackupDto

### Community 37 - "MutableInteractionSource"
Cohesion: 0.15
Nodes (27): PrioritizedReminder, ActiveVehicleCard(), Modifier, ExplainableReminderSheet(), Modifier, ProofRow(), AttentionBanner(), HealthyBanner() (+19 more)

### Community 39 - "Counter Mode Flow & State Spec"
Cohesion: 0.18
Nodes (16): Step 1 — Capture, Counter Mode (Quote Auditor UI), Step 4 — Decision Confirm, decisions table (Gate C schema), Counter Mode Flow & State Spec, Gate B (design gate), NavRoutes.CounterMode route, quote_lines table (Gate C schema) (+8 more)

### Community 40 - "CLAUDE.md Governance (v6.1)"
Cohesion: 0.19
Nodes (15): Compose Performance Reviewer Agent, Visual QA Agent, AutoMinder Data & Domain Rules, AutoMinder Android Release Gate Skill, AutoMinder AVD Visual QA Skill, Compose Performance Guardian Skill, AutoMinder UI Diff Review Skill, AutoMinder CI Workflow (+7 more)

### Community 41 - ".buildPlan"
Cohesion: 0.19
Nodes (4): CreateDefaultRemindersUseCase, PlannedReminder, ReminderTemplate, CreateDefaultRemindersUseCaseTest

### Community 42 - "Composable"
Cohesion: 0.18
Nodes (19): Modifier, LoadingState(), CompactContent(), ExpandedContent(), androidx, Color, Modifier, VehicleAvatar() (+11 more)

### Community 43 - "DashboardViewModel.kt"
Cohesion: 0.19
Nodes (10): DashboardUiState, DashboardViewModel, DataWithPrefs, Empty, Error, Activity, StateFlow, ViewModel (+2 more)

### Community 44 - "AutoMinderWidget.kt"
Cohesion: 0.30
Nodes (13): AutoMinderWidget, AutoMinderWidgetReceiver, fuelDao(), Context, LargeWidget(), MediumWidget(), reminderDao(), SmallWidget() (+5 more)

### Community 46 - "MainActivity.kt"
Cohesion: 0.36
Nodes (6): android, com, Provider, MainActivity, VehicleDeepLink, ComponentActivity

### Community 47 - "DistanceUtil"
Cohesion: 0.22
Nodes (5): DistanceUtil, Modifier, RecentActivityRow(), RecentActivitySection(), HomeActivityItem

### Community 48 - "ListSkeleton"
Cohesion: 0.19
Nodes (23): EmptyState(), ImageVector, Modifier, ErrorState(), Modifier, ListSkeleton(), Modifier, NavHostController (+15 more)

### Community 49 - "OnboardingScreen.kt"
Cohesion: 0.29
Nodes (16): GlowHero(), Color, ImageVector, Modifier, OnboardingProgressBar(), PillarItem, PrimaryCta(), ValuePillarGroup() (+8 more)

### Community 50 - "EditReminderViewModel.kt"
Cohesion: 0.12
Nodes (16): CustomLabelChanged, DeleteClicked, DueDateChanged, DueKmChanged, EditReminderUiEvent, EditReminderUiState, EditReminderViewModel, IntervalDaysChanged (+8 more)

### Community 51 - "AutoMinder Play Store Release Checklist"
Cohesion: 0.15
Nodes (14): Play Store full description, "Fleet Health Score" marketing claim, AutoMinder Pro "7-day free trial" claim, Performance gates (S1-S3, Baseline Profile unwired), Ads (AdMob) & consent checklist, Build & signing checklist, AutoMinder Play Store Release Checklist, Pre-submit smoke test (+6 more)

### Community 52 - "AutoMinder Design System v1.0"
Cohesion: 0.18
Nodes (14): Bottom nav: Home/Vehicles/Records/Settings, Data display conventions (money cents / distance km), AutoMinder Design System v1.0, AutoMinder Brand & Design System (Figma), Haptics vocabulary, Motion system (springs, Motion.kt), Onboarding doctrine (activation-first), Racing Teal #006B5F (+6 more)

### Community 53 - "AutoMinder README"
Cohesion: 0.14
Nodes (14): AdMob ID policy, Active branches convention, Build variants (debug/release AdMob IDs), AutoMinder Project-Specific Overrides (GEMINI.md), Session verification protocol, Release signing policy, Technical foundation / exact versions table, Branch strategy (+6 more)

### Community 54 - "Result"
Cohesion: 0.10
Nodes (16): DispatchersModule, AutoMinderBackupAgent, BackupAgentEntryPoint, BackupCoordinator, Failed, Partial, Result, Success (+8 more)

### Community 55 - "AddReminderViewModel.kt"
Cohesion: 0.28
Nodes (5): AddReminderUiState, AddReminderViewModel, DefaultInterval, StateFlow, ViewModel

### Community 56 - "ServiceRepositoryImpl"
Cohesion: 0.15
Nodes (7): Flow, ServiceRepositoryImpl, Failed, ServiceCompletion, ServiceCompletionResult, Success, VehicleNotFound

### Community 57 - "Timber"
Cohesion: 0.06
Nodes (26): AutoMinderApp, Application, Provider, AppLifecycleObserver, CrashlyticsTree, Context, Intent, PowerSettings (+18 more)

### Community 58 - "AnalyticsHelper"
Cohesion: 0.17
Nodes (9): AnalyticsModule, BroadcastReceiver, Context, Intent, NotificationActionReceiver, AnalyticsHelper, FirebaseAnalyticsHelper, ReminderDao (+1 more)

### Community 59 - "DrivingAmount"
Cohesion: 0.17
Nodes (11): DrivingAmount, HIGH, LOW, TYPICAL, DrivingAmountChips(), DrivingConfig, getDrivingAmountConfig(), Modifier (+3 more)

### Community 60 - "QuoteAuditorViewModelTest"
Cohesion: 0.24
Nodes (6): QuoteAuditResult, QuoteItem, QuoteLineVerdict, SuggestedInterval, SavedStateHandle, QuoteAuditorViewModelTest

### Community 61 - "WeeklyDigestWorker.kt"
Cohesion: 0.33
Nodes (3): StatusCalculator, CoroutineWorker, WeeklyDigestWorker

### Community 62 - "ServiceDetailScreen.kt"
Cohesion: 0.33
Nodes (9): labelRes(), Context, ImageVector, Modifier, ServiceDetailBentoContent(), serviceIconFor(), ServiceTelemetryPill(), shareServiceReceipt() (+1 more)

### Community 63 - "VehicleOperationalStatus"
Cohesion: 0.29
Nodes (6): VehicleOperationalStatus, DUE_SOON, HEALTHY, OVERDUE, SETUP_INCOMPLETE, UPCOMING

### Community 64 - "AddReminderUiEvent"
Cohesion: 0.17
Nodes (12): AddReminderUiEvent, DescriptionChanged, DueDateChanged, DueKmChanged, IntervalDaysChanged, IntervalKmChanged, PermissionRequestHandled, SaveClicked (+4 more)

### Community 65 - "AutoMinder 2026 Design Blueprint (Mobbin-Informed)"
Cohesion: 0.19
Nodes (14): FleetHealthScore, HealthCockpitCard, ImageVector, Modifier, RecordsTimelineCard(), Skeleton.kt (shimmer), Midnight Cobalt Design System, Mobbin Competitive Research Pass 2 (+6 more)

### Community 66 - "AutoMinder Product Strategy Deep Review"
Cohesion: 0.17
Nodes (12): AI strategy (drill-down only, evidence-carded), App flow recommendations, Competitor weaknesses to attack (table), Executive product diagnosis (strongest/weakest), AutoMinder Product Strategy Deep Review, Final recommendation, Growth & app store strategy, Mechanic Prep script (+4 more)

### Community 67 - "Premium Compose Components Skill"
Cohesion: 0.18
Nodes (11): Fleet Design Inspiration (behance.net/gallery/250251481 — translated to Racing Teal M3, never copy Fleet's yellow/purple palette or fake content), FormSectionCard spec (title+helper+content+error slot), HealthCockpitCard spec (human-verdict headline, score ring demoted to instrument, never a lone giant '0'), InsightMetricCard spec (eyebrow label + Mono value + unit), Premium Compose Components Skill, PremiumActionGrid spec (2x2 FilledTonal tiles, ≥56dp), PremiumPaywallPlanCard spec (Mono price or loading, badge, selected = tonal lift + 1.02 scale), PremiumSectionHeader spec (title+count badge+trailing action, heading() semantics) (+3 more)

### Community 69 - "AddFuelUiEvent"
Cohesion: 0.14
Nodes (14): AddFuelUiEvent, CostChanged, DateChanged, FullTankToggled, GasStationChanged, NotesChanged, OdometerChanged, PricePerUnitChanged (+6 more)

### Community 70 - "ReminderDetailSheet.kt"
Cohesion: 0.27
Nodes (9): info(), Color, ReminderDetailSheet(), ServiceTypeInfo, SeverityBadge(), SheetSection(), reminderTiming(), overdueByText() (+1 more)

### Community 71 - "evaluateReminderStaleness"
Cohesion: 0.33
Nodes (3): evaluateReminderStaleness(), RemindersDelayedState, ReminderStalenessTest

### Community 72 - "ServiceDetailViewModel.kt"
Cohesion: 0.29
Nodes (6): DeleteClicked, StateFlow, ViewModel, ServiceDetailUiEvent, ServiceDetailUiState, ServiceDetailViewModel

### Community 73 - "VehicleListViewModel.kt"
Cohesion: 0.29
Nodes (8): Empty, Error, StateFlow, ViewModel, Loading, Success, VehicleListUiState, VehicleListViewModel

### Community 76 - "AutoMinder 2026 MVP Plan"
Cohesion: 0.22
Nodes (11): Billing 9.1.0 migration (migration/play-billing-9), AutoMinder 2026 MVP Plan, MVP-ready blocking gates (B1-B8), Per-screen problem/answer table, Platform gates (P1-P6), Execution queue (Step 0-7), Reminder engine cannot keep the promise (correctness risk), Honest risk register (+3 more)

### Community 77 - "AutoMinder UI Slice Skill"
Cohesion: 0.20
Nodes (10): Android UI Architect Agent, Shape Set by Component Family, Never by Status (status-dependent corner radii removed — a list whose radii vary per row reads as broken, not informative), 12 AutoMinder 2026 Premium UI Rules, AutoMinder UI Slice Skill, StatusReminderCard spec (status corner morphing 8/16/28dp via animateDpAsState, 4dp error rail for OVERDUE), Root DESIGN_SYSTEM.md (archived/stale — do not consult), docs/DESIGN_SYSTEM_2026.md (Midnight Cobalt authority), Mobbin Design Blueprint 2026 (exec plan) (+2 more)

### Community 78 - "OnboardingViewModel.kt"
Cohesion: 0.24
Nodes (4): StateFlow, ViewModel, OnboardingUiState, OnboardingViewModel

### Community 80 - "AutoMinder Complete UI Audit (2026 Standards)"
Cohesion: 0.22
Nodes (10): Document authority table, Quality bar items (Q1-Q6), Accessibility findings (WCAG 2.1 AA), AutoMinder Complete UI Audit (2026 Standards), Loading-state inconsistency (skeleton vs spinner), Numeric typography drift (JetBrains Mono not applied everywhere), Prioritized v1.1 redesign roadmap, Screen-by-screen findings (+2 more)

### Community 81 - "AutoMinder UI Rules (Midnight Cobalt)"
Cohesion: 0.28
Nodes (9): Accessibility Reviewer Agent, StatusCalculator: OVERDUE Always Beats SNOOZED; Never Notify GOOD/SNOOZED/DISABLED, Midnight Cobalt Brand Tokens (Racing Teal #006B5F retired — predates the Night Garage commit), Valid ServiceStatus States: OVERDUE/DUE_SOON/SNOOZED/OK/COMPLETED/UNKNOWN — GOOD, DISABLED, NO_DATA, isEnabled, health-score explicitly do not exist, Skeleton Opacity Pulse 0.40→0.70 1000ms (not a translating shimmer sweep — a moving gradient costs frames every skeleton with no ongoing activity), AutoMinder UI Rules (Midnight Cobalt), AutoMinder Accessibility QA Skill, AGENTS.md StatusCalculator Logic incl. GOOD/DISABLED priority order (marked VERIFIED CORRECT, dated 2026-04) (+1 more)

### Community 82 - "AGENTS.md Agent Configuration"
Cohesion: 0.22
Nodes (9): Play-Store-Readiness: Products Query Split SUBS/INAPP, AGENTS.md Agent Scope Control Table (one scope per agent session, prevents merge conflicts), AGENTS.md Agent Configuration, AGENTS.md Billing 7.1.1: One-Time Purchase Only, No Subscriptions, AGENTS.md Do NOT Touch Protected Files List, AGENTS.md Pinned Tech Stack Table (Kotlin 2.1.21, AGP 8.9.1, KSP 2.1.21-2.0.1, etc. — 'never change without explicit human approval'), MVVM Clean Architecture Layering Law (UI→ViewModel→UseCase→Repository→DAO), Security & Privacy Law (keystore/local.properties/secrets never touched or logged) (+1 more)

### Community 83 - "ReminderCheckWorker.kt"
Cohesion: 0.24
Nodes (5): Context, localizedLabel(), VehicleDisplayName, CoroutineWorker, ReminderCheckWorker

### Community 85 - "FuelHistoryScreen.kt"
Cohesion: 0.38
Nodes (10): FuelEfficiencyChart(), Modifier, FuelEfficiencyTrendCard(), FuelHistoryScreen(), FuelIntelligenceCockpit(), FuelReceiptCard(), FuelSpendingBars(), FuelSpendingTrendCard() (+2 more)

### Community 86 - "ServiceHistoryScreen.kt"
Cohesion: 0.36
Nodes (9): Modifier, SwipeToDeleteContainer(), CategoryFilterRow(), com, Modifier, LifetimeSpendHeroCard(), ServiceHistoryStream(), ServiceReceiptCard() (+1 more)

### Community 87 - "VehicleCatalog"
Cohesion: 0.24
Nodes (6): VehicleCatalog, Modifier, VehiclePickerMode, MAKE, MODEL, VehiclePickerSheet()

### Community 88 - "ServiceHistoryViewModelTest"
Cohesion: 0.21
Nodes (3): ExportServiceHistoryUseCase, Uri, ServiceHistoryViewModelTest

### Community 90 - "DateFormatUtil"
Cohesion: 0.25
Nodes (7): Modifier, RemindersDelayedBanner(), buildUpcomingSubtitle(), Modifier, MaintenanceRow(), UpcomingMaintenanceSection(), DateFormatUtil

### Community 91 - "AboutScreen.kt"
Cohesion: 0.57
Nodes (7): AboutActionItem(), AboutFeatureBadge(), AboutFeaturesRow(), AboutHeroCard(), AboutScreen(), ImageVector, Modifier

### Community 92 - "CalculateEfficiencyUseCase"
Cohesion: 0.24
Nodes (6): CalculateEfficiencyUseCase, EfficiencyUnit, KM_L, L_100KM, MPG_UK, MPG_US

### Community 93 - "ServiceChoicePicker.kt"
Cohesion: 0.36
Nodes (8): AllServicesRow(), ChoiceFlow(), Modifier, SectionLabel(), ServiceChoice(), ServiceChoicePicker(), icon(), ImageVector

### Community 95 - "AutoMinder Session Handoff (2026-07-07)"
Cohesion: 0.29
Nodes (8): Beyond-UI release backlog, Premium component kit (ui/components/premium/), 12 AutoMinder 2026 Premium UI Rules, AutoMinder Session Handoff (2026-07-07), Environment recipes (Windows Gradle, AVD, editing traps), Fleet Behance case study (storytelling reference), Slice 8 nit list, UI rescue pipeline (Slices 0-8)

### Community 96 - "AutoMinderTestRunner.kt"
Cohesion: 0.43
Nodes (5): AndroidJUnitRunner, AutoMinderTestRunner, Application, Context, ClassLoader

### Community 97 - "QuoteAuditorScreen.kt"
Cohesion: 0.51
Nodes (9): HeroVerdictBentoCard(), Color, Modifier, MechanicTalkingPointsCard(), QuickAddChipsSection(), QuoteAuditorContent(), QuoteLineItemCard(), VehicleSelectorRow() (+1 more)

### Community 98 - "ReminderPriorityEngine"
Cohesion: 0.19
Nodes (7): DataConfidence, ESTIMATED, HIGH, INCOMPLETE_DATA, MEDIUM, ReminderExplanation, ReminderPriorityEngine

### Community 99 - "QuickLogSection.kt"
Cohesion: 0.67
Nodes (5): Color, ImageVector, Modifier, QuickLogButton(), QuickLogSection()

### Community 100 - "pressScale"
Cohesion: 0.26
Nodes (9): Modifier, pressScale(), EditVehicleField(), EditVehicleForm(), EditVehiclePhotoHero(), ImageVector, KeyboardType, Modifier (+1 more)

### Community 101 - "AutoMinder US Google Play Launch Scorecard"
Cohesion: 0.29
Nodes (7): AutoMinder US Google Play Launch Scorecard, Policy & privacy (§5), Product quality gate (§1), Ratings & support (§4), Retention & engagement metrics (§3), Staged rollout plan (§6), Store conversion checklist (§2)

### Community 102 - "AutoMinder UI Production-Readiness Checklist"
Cohesion: 0.29
Nodes (7): AutoMinder UI Production-Readiness Checklist, Figma file (design source of truth), Play Store submission gates, Remaining P1 UI work, Session log, Verify this session's changes, Orphaned premium components (built, never wired)

### Community 103 - "AutoMinder Play Store Readiness Skill"
Cohesion: 0.47
Nodes (6): Billing Correctness (verified PURCHASED entitlement, prompt ack, restore at startup, no logged tokens), AutoMinder Monetization Rules, UMP Consent Gate (no ad request until canRequestAds is true), Play-Store-Readiness: Billing 7.1.1 Pinned, Do NOT Propose Upgrade (Billing 8 tracked as separate pre-Aug-2026 task), AutoMinder Play Store Readiness Skill, Billing Library 7→9.x Migration Mandatory Before 2026-08-31 Deadline

### Community 105 - "AddServiceViewModel.kt"
Cohesion: 0.38
Nodes (5): AddServiceUiState, AddServiceViewModel, StateFlow, ViewModel, Job

### Community 106 - "AutoMinder Marketing Website Home"
Cohesion: 0.31
Nodes (6): Privacy Policy / Data Safety Drift, AutoMinder Privacy Policy (Store), AutoMinder Google Play Store Listing, AutoMinder Marketing Website Home, AutoMinder Website Privacy Policy, AutoMinder Website Support Page

### Community 108 - "stitch-proxy.mjs"
Cohesion: 0.33
Nodes (4): cloudsdkConfig, gcloud, proxy, stitchHome

### Community 111 - "InsightMetricCard.kt"
Cohesion: 0.70
Nodes (4): InsightMetricCard(), InsightMetricRow(), ImageVector, Modifier

### Community 112 - "AuditQuoteUseCase"
Cohesion: 0.39
Nodes (3): AuditQuoteUseCase, com, IntRange

### Community 113 - "AddVehicleScreen.kt"
Cohesion: 0.46
Nodes (7): AddVehicleForm(), AddVehicleScreen(), ImageVector, KeyboardType, Modifier, VehicleField(), VehiclePhotoHero()

### Community 114 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 116 - ".onCreate"
Cohesion: 0.29
Nodes (5): BottomNavBar(), BottomNavItem, NavHostController, AutoMinderTheme(), Bundle

### Community 119 - "AddFuelViewModel.kt"
Cohesion: 0.40
Nodes (4): AnalyticsEvents, AnalyticsParams, StateFlow, ViewModel

### Community 132 - "QuoteVerdictStatus"
Cohesion: 0.40
Nodes (5): QuoteVerdictStatus, CAN_WAIT, LEGITIMATE_DUE, LIKELY_UPSELL, VERIFY_FIRST

### Community 133 - "EfficiencyTrend"
Cohesion: 0.40
Nodes (4): EfficiencyTrend, DECLINING, FLAT, IMPROVING

### Community 135 - "ActivityType"
Cohesion: 0.50
Nodes (4): ActivityType, FUEL, MILEAGE, SERVICE

## Ambiguous Edges - Review These
- `Figma file (design source of truth)` → `AutoMinder Brand & Design System (Figma)`  [AMBIGUOUS]
  RELEASE_CHECKLIST_v1.1_UI.md · relation: conceptually_related_to
- `Play-Store-Readiness: Billing 7.1.1 Pinned, Do NOT Propose Upgrade (Billing 8 tracked as separate pre-Aug-2026 task)` → `Billing Library 7→9.x Migration Mandatory Before 2026-08-31 Deadline`  [AMBIGUOUS]
  .claude/skills/play-store-readiness/SKILL.md · relation: conceptually_related_to
- `Production readiness checklist` → `AutoMinder Production Readiness`  [AMBIGUOUS]
  PRODUCTION_READINESS.md · relation: semantically_similar_to
- `AutoMinder Privacy Policy (Store)` → `AutoMinder Website Privacy Policy`  [AMBIGUOUS]
  store/PRIVACY_POLICY.md · relation: conceptually_related_to
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
- `AutoMinder Design System 2026` → `AutoMinder MVP Plan`  [AMBIGUOUS]
  docs/exec-plans/mvp-plan.html · relation: references

## Knowledge Gaps
- **314 isolated node(s):** `npx`, `Processing`, `Cancelled`, `Pending`, `InProgress` (+309 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **29 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Figma file (design source of truth)` and `AutoMinder Brand & Design System (Figma)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Play-Store-Readiness: Billing 7.1.1 Pinned, Do NOT Propose Upgrade (Billing 8 tracked as separate pre-Aug-2026 task)` and `Billing Library 7→9.x Migration Mandatory Before 2026-08-31 Deadline`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Production readiness checklist` and `AutoMinder Production Readiness`?**
  _Edge tagged AMBIGUOUS (relation: semantically_similar_to) - confidence is low._
- **What is the exact relationship between `AutoMinder Privacy Policy (Store)` and `AutoMinder Website Privacy Policy`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Shape Set by Component Family, Never by Status (status-dependent corner radii removed — a list whose radii vary per row reads as broken, not informative)` and `StatusReminderCard spec (status corner morphing 8/16/28dp via animateDpAsState, 4dp error rail for OVERDUE)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `StatusCalculator: OVERDUE Always Beats SNOOZED; Never Notify GOOD/SNOOZED/DISABLED` and `Valid ServiceStatus States: OVERDUE/DUE_SOON/SNOOZED/OK/COMPLETED/UNKNOWN — GOOD, DISABLED, NO_DATA, isEnabled, health-score explicitly do not exist`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Valid ServiceStatus States: OVERDUE/DUE_SOON/SNOOZED/OK/COMPLETED/UNKNOWN — GOOD, DISABLED, NO_DATA, isEnabled, health-score explicitly do not exist` and `AGENTS.md StatusCalculator Logic incl. GOOD/DISABLED priority order (marked VERIFIED CORRECT, dated 2026-04)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._