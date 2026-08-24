# Graph Report - Autominder  (2026-08-23)

## Corpus Check
- 185 files · ~166,844 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2083 nodes · 4293 edges · 125 communities (98 shown, 27 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 76 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Community 0
- Community 1
- Community 2
- Community 3
- Community 4
- Community 5
- Community 6
- Community 7
- Community 8
- Community 9
- Community 10
- Community 11
- Community 12
- Community 13
- Community 14
- Community 15
- Community 16
- Community 17
- Community 18
- Community 19
- Community 20
- Community 21
- Community 22
- Community 23
- Community 24
- Community 25
- Community 26
- Community 27
- Community 28
- Community 29
- Community 30
- Community 31
- Community 32
- Community 33
- Community 34
- Community 35
- Community 36
- Community 37
- Community 38
- Community 39
- Community 40
- Community 41
- Community 42
- Community 43
- Community 44
- Community 45
- Community 46
- Community 47
- Community 48
- Community 49
- Community 50
- Community 51
- Community 52
- Community 53
- Community 54
- Community 55
- Community 56
- Community 57
- Community 58
- Community 59
- Community 60
- Community 61
- Community 62
- Community 63
- Community 64
- Community 65
- Community 66
- Community 67
- Community 68
- Community 69
- Community 70
- Community 71
- Community 72
- Community 73
- Community 74
- Community 75
- Community 76
- Community 77
- Community 78
- Community 79
- Community 80
- Community 81
- Community 82
- Community 83
- Community 84
- Community 85
- Community 86
- Community 87
- Community 88
- Community 89
- Community 90
- Community 91
- Community 92
- Community 93
- Community 94
- Community 95
- Community 96
- Community 97
- Community 98
- Community 99
- Community 100
- Community 101
- Community 102
- Community 103
- Community 104
- Community 105
- Community 106
- Community 109
- Community 110
- Community 111
- Community 112
- Community 113
- Community 116
- Community 117
- Community 120
- Community 124

## God Nodes (most connected - your core abstractions)
1. `ServiceType` - 85 edges
2. `Vehicle` - 78 edges
3. `Reminder` - 61 edges
4. `Service` - 52 edges
5. `ServiceStatus` - 46 edges
6. `FuelEntry` - 44 edges
7. `DistanceUtil` - 36 edges
8. `ReminderDao` - 34 edges
9. `VehicleDao` - 32 edges
10. `IServiceRepository` - 32 edges

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

## Communities (125 total, 27 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.06
Nodes (33): FuelRepositoryImpl, Flow, FuelEntry, IFuelRepository, Flow, FuelEfficiencyTrendCard(), FuelHistoryScreen(), FuelIntelligenceCockpit() (+25 more)

### Community 1 - "Community 1"
Cohesion: 0.05
Nodes (19): toDomain(), toEntity(), Flow, ReminderRepositoryImpl, Reminder, IReminderRepository, Flow, Empty (+11 more)

### Community 2 - "Community 2"
Cohesion: 0.05
Nodes (35): ConsentManager, Activity, Cancelled, Error, Idle, InProgress, Activity, ProductDetails (+27 more)

### Community 3 - "Community 3"
Cohesion: 0.06
Nodes (26): IVehicleRepository, RepositoryModule, Flow, MileageLogRepositoryImpl, MileageLogEntry, IMileageLogRepository, Flow, AddClicked (+18 more)

### Community 4 - "Community 4"
Cohesion: 0.07
Nodes (40): VehicleCatalog, InsightMetricCard(), InsightMetricRow(), ImageVector, Modifier, DrivingAmountChips(), DrivingConfig, getDrivingAmountConfig() (+32 more)

### Community 5 - "Community 5"
Cohesion: 0.06
Nodes (28): AutoMinderApp, Application, Provider, AppLifecycleObserver, CrashlyticsTree, Context, Intent, PowerSettings (+20 more)

### Community 6 - "Community 6"
Cohesion: 0.07
Nodes (37): QuoteAuditResult, QuoteItem, QuoteLineVerdict, QuoteVerdictStatus, CAN_WAIT, LEGITIMATE_DUE, LIKELY_UPSELL, VERIFY_FIRST (+29 more)

### Community 7 - "Community 7"
Cohesion: 0.05
Nodes (35): Activity, ReviewHelper, Flow, UserPreferences, CategoryFilterRow(), com, Modifier, LifetimeSpendHeroCard() (+27 more)

### Community 8 - "Community 8"
Cohesion: 0.07
Nodes (13): DrivingAmount, HIGH, LOW, TYPICAL, CreateDefaultRemindersUseCase, PlannedReminder, ReminderTemplate, CreateDefaultRemindersUseCaseTest (+5 more)

### Community 9 - "Community 9"
Cohesion: 0.06
Nodes (26): OdometerPoint, PredictDueUseCase, CostByTypeDonut(), Modifier, Modifier, SpendingTrendChart(), FormField(), Modifier (+18 more)

### Community 10 - "Community 10"
Cohesion: 0.10
Nodes (39): PrioritizedReminder, Modifier, pressScale(), ActiveVehicleCard(), Modifier, ExplainableReminderSheet(), Modifier, ProofRow() (+31 more)

### Community 11 - "Community 11"
Cohesion: 0.08
Nodes (37): FleetHealthScore, HealthCockpitCard, Color, Dp, Shape, PremiumStatusStyle, ImageVector, Modifier (+29 more)

### Community 12 - "Community 12"
Cohesion: 0.11
Nodes (32): FuelEfficiencyChart(), Modifier, EmptyState(), ImageVector, Modifier, ErrorState(), ListSkeleton(), Modifier (+24 more)

### Community 13 - "Community 13"
Cohesion: 0.09
Nodes (30): ServiceStatus, COMPLETED, DUE_SOON, OK, OVERDUE, SNOOZED, UNKNOWN, Modifier (+22 more)

### Community 14 - "Community 14"
Cohesion: 0.11
Nodes (14): AdError, AdManager, InterstitialAdLoadCallback, RewardedAdLoadCallback, RewardedInterstitialAdLoadCallback, FullScreenContentCallback, FullScreenContentCallback, FullScreenContentCallback (+6 more)

### Community 15 - "Community 15"
Cohesion: 0.09
Nodes (23): android, BannerAdView(), Modifier, com, UserPreferences, MainActivity, VehicleDeepLink, BottomNavBar() (+15 more)

### Community 16 - "Community 16"
Cohesion: 0.10
Nodes (16): DispatchersModule, AutoMinderBackupAgent, BackupAgentEntryPoint, BackupCoordinator, Failed, Partial, Result, Success (+8 more)

### Community 17 - "Community 17"
Cohesion: 0.11
Nodes (18): AddFuelUiEvent, AddFuelUiState, AddFuelViewModel, CostChanged, DateChanged, FullTankToggled, GasStationChanged, StateFlow (+10 more)

### Community 18 - "Community 18"
Cohesion: 0.12
Nodes (7): toDomain(), toEntity(), Flow, VehicleRepositoryImpl, Vehicle, IVehicleRepository, Flow

### Community 19 - "Community 19"
Cohesion: 0.11
Nodes (23): Modifier, SaveButton(), SaveButtonState, Idle, Saving, Success, AddFuelBentoContent(), FuelStatPill() (+15 more)

### Community 20 - "Community 20"
Cohesion: 0.11
Nodes (16): BackupOpState, Error, ExportSuccess, Idle, ImportSuccess, InProgress, Activity, StateFlow (+8 more)

### Community 21 - "Community 21"
Cohesion: 0.08
Nodes (12): VehicleBodyType, CONVERTIBLE, COUPE, HATCHBACK, MINIVAN, MOTORCYCLE, SEDAN, SUV (+4 more)

### Community 22 - "Community 22"
Cohesion: 0.12
Nodes (16): CustomLabelChanged, DeleteClicked, DueDateChanged, DueKmChanged, EditReminderUiEvent, EditReminderUiState, EditReminderViewModel, IntervalDaysChanged (+8 more)

### Community 23 - "Community 23"
Cohesion: 0.15
Nodes (3): Flow, ReminderDao, ReminderEntity

### Community 24 - "Community 24"
Cohesion: 0.11
Nodes (16): VehicleOperationalStatus, DUE_SOON, HEALTHY, OVERDUE, SETUP_INCOMPLETE, UPCOMING, DashboardUiState, DashboardViewModel (+8 more)

### Community 25 - "Community 25"
Cohesion: 0.14
Nodes (7): Flow, ServiceRepositoryImpl, Failed, ServiceCompletion, ServiceCompletionResult, Success, VehicleNotFound

### Community 26 - "Community 26"
Cohesion: 0.10
Nodes (18): Converters, ServiceType, AIR_FILTER, BATTERY, BRAKE_SERVICE, CABIN_FILTER, COOLANT, CUSTOM (+10 more)

### Community 27 - "Community 27"
Cohesion: 0.13
Nodes (8): Context, NotificationHelper, StatusCalculator, VehicleDisplayName, CoroutineWorker, ReminderCheckWorker, CoroutineWorker, WeeklyDigestWorker

### Community 28 - "Community 28"
Cohesion: 0.16
Nodes (16): labelRes(), AddServiceContent(), AddServiceScreen(), IntervalPreset, isToday(), isYesterday(), Modifier, Context (+8 more)

### Community 29 - "Community 29"
Cohesion: 0.15
Nodes (6): Flow, MileageLogDao, MileageLogEntity, toDomain(), toEntity(), ManualBackupManagerTest

### Community 30 - "Community 30"
Cohesion: 0.16
Nodes (3): Flow, VehicleDao, VehicleEntity

### Community 31 - "Community 31"
Cohesion: 0.23
Nodes (8): AppInfo, DashboardData, GetDashboardDataUseCase, Flow, VehicleWithStatus, DashboardViewModelTest, UserPreferences, ReviewHelper

### Community 32 - "Community 32"
Cohesion: 0.21
Nodes (9): AutoMinderBackupData, BackupRestoreSummary, FuelEntryBackupDto, ManualBackupManager, MileageLogBackupDto, ReminderBackupDto, ServiceBackupDto, VehicleBackupDto (+1 more)

### Community 33 - "Community 33"
Cohesion: 0.20
Nodes (5): Flow, ServiceDao, ServiceEntity, toDomain(), toEntity()

### Community 34 - "Community 34"
Cohesion: 0.16
Nodes (10): DuePrediction, DistanceUtil, info(), Color, ReminderDetailSheet(), ServiceTypeInfo, SeverityBadge(), SheetSection() (+2 more)

### Community 35 - "Community 35"
Cohesion: 0.15
Nodes (16): Modifier, PremiumPaywallPlanCard(), Available, Loading, PremiumPriceDisplay, Unavailable, FeatureCheck(), FeatureComparisonTable() (+8 more)

### Community 36 - "Community 36"
Cohesion: 0.11
Nodes (18): About, AddFuel, AddReminder, AddService, AddVehicle, Dashboard, EditReminder, EditVehicle (+10 more)

### Community 37 - "Community 37"
Cohesion: 0.18
Nodes (5): MigrationTest, DatabaseModule, Context, AppDatabase, RoomDatabase

### Community 39 - "Community 39"
Cohesion: 0.22
Nodes (4): AddServiceViewModelTest, AnalyticsHelper, IVehicleRepository, UserPreferences

### Community 40 - "Community 40"
Cohesion: 0.20
Nodes (5): FuelDao, Flow, FuelEntryEntity, toDomain(), toEntity()

### Community 41 - "Community 41"
Cohesion: 0.21
Nodes (11): VehicleDisplayNameFormatter, AutoMinderServiceStatusBadge(), AutoMinderStatusBadge(), Modifier, StatusBadgeConfig, Modifier, labelRes(), VehicleListContent() (+3 more)

### Community 42 - "Community 42"
Cohesion: 0.18
Nodes (12): Modifier, Modifier, LoadingState(), Modifier, RemindersDelayedBanner(), Modifier, ServiceChoice(), ServiceTypeGrid() (+4 more)

### Community 44 - "Community 44"
Cohesion: 0.12
Nodes (17): What ships next (v1.1) — not now, Product identity & strategic positioning, Architecture laws (MVVM + Clean + Offline-First), 30-day build roadmap (P1-P12), Closing statement, Daily engagement / gamification system, Room entities v2 database design, AutoMinder PRD v4.0 (+9 more)

### Community 45 - "Community 45"
Cohesion: 0.19
Nodes (7): DataConfidence, ESTIMATED, HIGH, INCOMPLETE_DATA, MEDIUM, ReminderExplanation, ReminderPriorityEngine

### Community 46 - "Community 46"
Cohesion: 0.12
Nodes (16): AddServiceUiEvent, CostChanged, CustomLabelChanged, NotesChanged, OdometerAdjusted, OdometerChanged, QuickCostSelected, QuickDateSelected (+8 more)

### Community 48 - "Community 48"
Cohesion: 0.18
Nodes (16): Step 1 — Capture, Counter Mode (Quote Auditor UI), Step 4 — Decision Confirm, decisions table (Gate C schema), Counter Mode Flow & State Spec, Gate B (design gate), NavRoutes.CounterMode route, quote_lines table (Gate C schema) (+8 more)

### Community 49 - "Community 49"
Cohesion: 0.19
Nodes (15): Compose Performance Reviewer Agent, Visual QA Agent, AutoMinder Data & Domain Rules, AutoMinder Android Release Gate Skill, AutoMinder AVD Visual QA Skill, Compose Performance Guardian Skill, AutoMinder UI Diff Review Skill, AutoMinder CI Workflow (+7 more)

### Community 50 - "Community 50"
Cohesion: 0.30
Nodes (13): AutoMinderWidget, AutoMinderWidgetReceiver, fuelDao(), Context, LargeWidget(), MediumWidget(), reminderDao(), SmallWidget() (+5 more)

### Community 51 - "Community 51"
Cohesion: 0.15
Nodes (14): Play Store full description, "Fleet Health Score" marketing claim, AutoMinder Pro "7-day free trial" claim, Performance gates (S1-S3, Baseline Profile unwired), Ads (AdMob) & consent checklist, Build & signing checklist, AutoMinder Play Store Release Checklist, Pre-submit smoke test (+6 more)

### Community 52 - "Community 52"
Cohesion: 0.18
Nodes (14): Bottom nav: Home/Vehicles/Records/Settings, Data display conventions (money cents / distance km), AutoMinder Design System v1.0, AutoMinder Brand & Design System (Figma), Haptics vocabulary, Motion system (springs, Motion.kt), Onboarding doctrine (activation-first), Racing Teal #006B5F (+6 more)

### Community 53 - "Community 53"
Cohesion: 0.14
Nodes (14): AdMob ID policy, Active branches convention, Build variants (debug/release AdMob IDs), AutoMinder Project-Specific Overrides (GEMINI.md), Session verification protocol, Release signing policy, Technical foundation / exact versions table, Branch strategy (+6 more)

### Community 54 - "Community 54"
Cohesion: 0.22
Nodes (5): ExportServiceHistoryUseCase, Uri, ExportServiceHistoryUseCaseTest, Context, IVehicleRepository

### Community 55 - "Community 55"
Cohesion: 0.29
Nodes (3): Service, IServiceRepository, Flow

### Community 56 - "Community 56"
Cohesion: 0.33
Nodes (12): CompactContent(), ExpandedContent(), androidx, Color, com, Modifier, VehicleAvatar(), VehicleHeroCard() (+4 more)

### Community 57 - "Community 57"
Cohesion: 0.28
Nodes (5): AddReminderUiState, AddReminderViewModel, DefaultInterval, StateFlow, ViewModel

### Community 58 - "Community 58"
Cohesion: 0.29
Nodes (6): SuggestedInterval, AddServiceUiState, AddServiceViewModel, StateFlow, ViewModel, Job

### Community 59 - "Community 59"
Cohesion: 0.17
Nodes (12): AddReminderUiEvent, DescriptionChanged, DueDateChanged, DueKmChanged, IntervalDaysChanged, IntervalKmChanged, PermissionRequestHandled, SaveClicked (+4 more)

### Community 61 - "Community 61"
Cohesion: 0.27
Nodes (3): IVehicleRepository, UserPreferences, ServiceHistoryViewModelTest

### Community 62 - "Community 62"
Cohesion: 0.17
Nodes (12): AI strategy (drill-down only, evidence-carded), App flow recommendations, Competitor weaknesses to attack (table), Executive product diagnosis (strongest/weakest), AutoMinder Product Strategy Deep Review, Final recommendation, Growth & app store strategy, Mechanic Prep script (+4 more)

### Community 63 - "Community 63"
Cohesion: 0.18
Nodes (11): Fleet Design Inspiration (behance.net/gallery/250251481 — translated to Racing Teal M3, never copy Fleet's yellow/purple palette or fake content), FormSectionCard spec (title+helper+content+error slot), HealthCockpitCard spec (human-verdict headline, score ring demoted to instrument, never a lone giant '0'), InsightMetricCard spec (eyebrow label + Mono value + unit), Premium Compose Components Skill, PremiumActionGrid spec (2x2 FilledTonal tiles, ≥56dp), PremiumPaywallPlanCard spec (Mono price or loading, badge, selected = tonal lift + 1.02 scale), PremiumSectionHeader spec (title+count badge+trailing action, heading() semantics) (+3 more)

### Community 65 - "Community 65"
Cohesion: 0.22
Nodes (6): CalculateEfficiencyUseCase, EfficiencyUnit, KM_L, L_100KM, MPG_UK, MPG_US

### Community 66 - "Community 66"
Cohesion: 0.27
Nodes (6): ReminderWithStatus, buildUpcomingSubtitle(), Modifier, MaintenanceRow(), UpcomingMaintenanceSection(), ReminderPriorityEngineTest

### Community 67 - "Community 67"
Cohesion: 0.33
Nodes (3): evaluateReminderStaleness(), RemindersDelayedState, ReminderStalenessTest

### Community 69 - "Community 69"
Cohesion: 0.22
Nodes (11): Billing 9.1.0 migration (migration/play-billing-9), AutoMinder 2026 MVP Plan, MVP-ready blocking gates (B1-B8), Per-screen problem/answer table, Platform gates (P1-P6), Execution queue (Step 0-7), Reminder engine cannot keep the promise (correctness risk), Honest risk register (+3 more)

### Community 70 - "Community 70"
Cohesion: 0.20
Nodes (10): Android UI Architect Agent, Shape Set by Component Family, Never by Status (status-dependent corner radii removed — a list whose radii vary per row reads as broken, not informative), 12 AutoMinder 2026 Premium UI Rules, AutoMinder UI Slice Skill, StatusReminderCard spec (status corner morphing 8/16/28dp via animateDpAsState, 4dp error rail for OVERDUE), Root DESIGN_SYSTEM.md (archived/stale — do not consult), docs/DESIGN_SYSTEM_2026.md (Midnight Cobalt authority), Mobbin Design Blueprint 2026 (exec plan) (+2 more)

### Community 71 - "Community 71"
Cohesion: 0.20
Nodes (10): EditVehicleUiEvent, MakeChanged, ModelChanged, NotesChanged, OdometerChanged, PhotoUriChanged, PlateNumberChanged, SaveClicked (+2 more)

### Community 72 - "Community 72"
Cohesion: 0.22
Nodes (10): Document authority table, Quality bar items (Q1-Q6), Accessibility findings (WCAG 2.1 AA), AutoMinder Complete UI Audit (2026 Standards), Loading-state inconsistency (skeleton vs spinner), Numeric typography drift (JetBrains Mono not applied everywhere), Prioritized v1.1 redesign roadmap, Screen-by-screen findings (+2 more)

### Community 73 - "Community 73"
Cohesion: 0.28
Nodes (9): Accessibility Reviewer Agent, StatusCalculator: OVERDUE Always Beats SNOOZED; Never Notify GOOD/SNOOZED/DISABLED, Midnight Cobalt Brand Tokens (Racing Teal #006B5F retired — predates the Night Garage commit), Valid ServiceStatus States: OVERDUE/DUE_SOON/SNOOZED/OK/COMPLETED/UNKNOWN — GOOD, DISABLED, NO_DATA, isEnabled, health-score explicitly do not exist, Skeleton Opacity Pulse 0.40→0.70 1000ms (not a translating shimmer sweep — a moving gradient costs frames every skeleton with no ongoing activity), AutoMinder UI Rules (Midnight Cobalt), AutoMinder Accessibility QA Skill, AGENTS.md StatusCalculator Logic incl. GOOD/DISABLED priority order (marked VERIFIED CORRECT, dated 2026-04) (+1 more)

### Community 74 - "Community 74"
Cohesion: 0.22
Nodes (9): Play-Store-Readiness: Products Query Split SUBS/INAPP, AGENTS.md Agent Scope Control Table (one scope per agent session, prevents merge conflicts), AGENTS.md Agent Configuration, AGENTS.md Billing 7.1.1: One-Time Purchase Only, No Subscriptions, AGENTS.md Do NOT Touch Protected Files List, AGENTS.md Pinned Tech Stack Table (Kotlin 2.1.21, AGP 8.9.1, KSP 2.1.21-2.0.1, etc. — 'never change without explicit human approval'), MVVM Clean Architecture Layering Law (UI→ViewModel→UseCase→Repository→DAO), Security & Privacy Law (keystore/local.properties/secrets never touched or logged) (+1 more)

### Community 75 - "Community 75"
Cohesion: 0.31
Nodes (6): Privacy Policy / Data Safety Drift, AutoMinder Privacy Policy (Store), AutoMinder Google Play Store Listing, AutoMinder Marketing Website Home, AutoMinder Website Privacy Policy, AutoMinder Website Support Page

### Community 76 - "Community 76"
Cohesion: 0.22
Nodes (9): AddVehicleUiEvent, BrandChanged, ModelChanged, OdometerChanged, PhotoUriChanged, PlateNumberChanged, SaveClicked, VinChanged (+1 more)

### Community 77 - "Community 77"
Cohesion: 0.36
Nodes (5): EditVehicleUiState, EditVehicleViewModel, StateFlow, ViewModel, toStringRes()

### Community 81 - "Community 81"
Cohesion: 0.25
Nodes (6): VehicleDataConfidence, ESTIMATED, HIGH, INCOMPLETE, MEDIUM, MISSING_MILEAGE

### Community 83 - "Community 83"
Cohesion: 0.68
Nodes (7): DashboardSkeleton(), Modifier, SkeletonBar(), skeletonFill(), SkeletonScaffold(), VehicleCardSkeleton(), Shape

### Community 84 - "Community 84"
Cohesion: 0.57
Nodes (7): AboutActionItem(), AboutFeatureBadge(), AboutFeaturesRow(), AboutHeroCard(), AboutScreen(), ImageVector, Modifier

### Community 86 - "Community 86"
Cohesion: 0.29
Nodes (8): Beyond-UI release backlog, Premium component kit (ui/components/premium/), 12 AutoMinder 2026 Premium UI Rules, AutoMinder Session Handoff (2026-07-07), Environment recipes (Windows Gradle, AVD, editing traps), Fleet Behance case study (storytelling reference), Slice 8 nit list, UI rescue pipeline (Slices 0-8)

### Community 87 - "Community 87"
Cohesion: 0.43
Nodes (5): AndroidJUnitRunner, AutoMinderTestRunner, Application, Context, ClassLoader

### Community 88 - "Community 88"
Cohesion: 0.29
Nodes (7): ReminderUrgency, DUE_SOON, FUTURE, MILEAGE_BASED, OVERDUE, SAFETY_CRITICAL, TIME_SENSITIVE

### Community 89 - "Community 89"
Cohesion: 0.29
Nodes (7): ValidationErrorCode, COST_NEGATIVE, FIELD_REQUIRED, ODOMETER_NEGATIVE, VIN_INVALID_FORMAT, YEAR_TOO_EARLY, YEAR_TOO_LATE

### Community 90 - "Community 90"
Cohesion: 0.57
Nodes (6): AllServicesRow(), ChoiceFlow(), Modifier, SectionLabel(), ServiceChoice(), ServiceChoicePicker()

### Community 91 - "Community 91"
Cohesion: 0.48
Nodes (5): AddVehicleUiState, AddVehicleViewModel, StateFlow, ViewModel, toStringRes()

### Community 92 - "Community 92"
Cohesion: 0.29
Nodes (6): ScreenState, Empty, Error, Loading, Success, VehicleDetailUiState

### Community 93 - "Community 93"
Cohesion: 0.29
Nodes (7): AutoMinder US Google Play Launch Scorecard, Policy & privacy (§5), Product quality gate (§1), Ratings & support (§4), Retention & engagement metrics (§3), Staged rollout plan (§6), Store conversion checklist (§2)

### Community 94 - "Community 94"
Cohesion: 0.29
Nodes (7): AutoMinder UI Production-Readiness Checklist, Figma file (design source of truth), Play Store submission gates, Remaining P1 UI work, Session log, Verify this session's changes, Orphaned premium components (built, never wired)

### Community 95 - "Community 95"
Cohesion: 0.47
Nodes (6): Billing Correctness (verified PURCHASED entitlement, prompt ack, restore at startup, no logged tokens), AutoMinder Monetization Rules, UMP Consent Gate (no ad request until canRequestAds is true), Play-Store-Readiness: Billing 7.1.1 Pinned, Do NOT Propose Upgrade (Billing 8 tracked as separate pre-Aug-2026 task), AutoMinder Play Store Readiness Skill, Billing Library 7→9.x Migration Mandatory Before 2026-08-31 Deadline

### Community 97 - "Community 97"
Cohesion: 0.33
Nodes (4): cloudsdkConfig, gcloud, proxy, stitchHome

### Community 98 - "Community 98"
Cohesion: 0.80
Nodes (4): ActionTile(), Modifier, PremiumAction, PremiumActionGrid()

### Community 99 - "Community 99"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 100 - "Community 100"
Cohesion: 0.50
Nodes (4): ActivityType, FUEL, MILEAGE, SERVICE

## Ambiguous Edges - Review These
- `Play-Store-Readiness: Billing 7.1.1 Pinned, Do NOT Propose Upgrade (Billing 8 tracked as separate pre-Aug-2026 task)` → `Billing Library 7→9.x Migration Mandatory Before 2026-08-31 Deadline`  [AMBIGUOUS]
  .claude/skills/play-store-readiness/SKILL.md · relation: conceptually_related_to
- `Figma file (design source of truth)` → `AutoMinder Brand & Design System (Figma)`  [AMBIGUOUS]
  RELEASE_CHECKLIST_v1.1_UI.md · relation: conceptually_related_to
- `AutoMinder Design System 2026` → `AutoMinder MVP Plan`  [AMBIGUOUS]
  docs/exec-plans/mvp-plan.html · relation: references
- `Production readiness checklist` → `AutoMinder Production Readiness`  [AMBIGUOUS]
  PRODUCTION_READINESS.md · relation: semantically_similar_to
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

## Knowledge Gaps
- **327 isolated node(s):** `ArchiveClicked`, `ExportClicked`, `ExportConsumed`, `MarkReminderComplete`, `SnoozeReminder` (+322 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **27 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Play-Store-Readiness: Billing 7.1.1 Pinned, Do NOT Propose Upgrade (Billing 8 tracked as separate pre-Aug-2026 task)` and `Billing Library 7→9.x Migration Mandatory Before 2026-08-31 Deadline`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Figma file (design source of truth)` and `AutoMinder Brand & Design System (Figma)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `AutoMinder Design System 2026` and `AutoMinder MVP Plan`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **What is the exact relationship between `Production readiness checklist` and `AutoMinder Production Readiness`?**
  _Edge tagged AMBIGUOUS (relation: semantically_similar_to) - confidence is low._
- **What is the exact relationship between `Shape Set by Component Family, Never by Status (status-dependent corner radii removed — a list whose radii vary per row reads as broken, not informative)` and `StatusReminderCard spec (status corner morphing 8/16/28dp via animateDpAsState, 4dp error rail for OVERDUE)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `StatusCalculator: OVERDUE Always Beats SNOOZED; Never Notify GOOD/SNOOZED/DISABLED` and `Valid ServiceStatus States: OVERDUE/DUE_SOON/SNOOZED/OK/COMPLETED/UNKNOWN — GOOD, DISABLED, NO_DATA, isEnabled, health-score explicitly do not exist`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Valid ServiceStatus States: OVERDUE/DUE_SOON/SNOOZED/OK/COMPLETED/UNKNOWN — GOOD, DISABLED, NO_DATA, isEnabled, health-score explicitly do not exist` and `AGENTS.md StatusCalculator Logic incl. GOOD/DISABLED priority order (marked VERIFIED CORRECT, dated 2026-04)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._