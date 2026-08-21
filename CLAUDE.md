# AUTOMINDER AGENT CONTEXT — READ FIRST, EVERY SESSION
# CLAUDE.md | Updated: 2026-08-04 | Version: 6.1 | Production-Outcome Governance
# Path-scoped detail: .claude/rules/{ui,data,monetization}.md (auto-load on
# matching files). Procedures: .claude/skills/. Reference: docs/GOVERNANCE_REFERENCE.md.

## IDENTITY
App: AutoMinder — Android car maintenance & fuel intelligence app
Package: com.autominder.app  ← FINAL. LOCKED. NEVER CHANGE. EVER.
Play Store Developer: TikiTaka3D | Workspace: D:\Autominder
Version: 1.0.0 (FEATURE-FROZEN, release hardening) → 1.1.0 post-launch

## CURRENT PHASE — RELEASE HARDENING + MANDATORY BILLING MIGRATION
v1.0 is feature-frozen: bug fixes, stability, security, billing correctness,
localization/lint/test hardening, and Play submission readiness only.

EXTERNAL DEADLINE OVERRIDE (verified 2026-08-04, official deprecation FAQ):
Google Play rejects Billing Library 7 for NEW apps and updates after
2026-08-31 (extension to 2026-11-01 via Console form only). AutoMinder is
unpublished, so this binds the INITIAL submission: Billing migration
(7.1.1 → 9.x, direct path officially supported) is mandatory BEFORE
production submission, on its own migration/* branch, never bundled with
UI/Ads/Room/navigation/release-workflow changes. Paywall polish targets the
migrated API.

Deferred to v1.1+ (STOP and confirm before touching): Figma full-screen
build, garage hero silhouettes, VIN decode/lookup, OBD-II, GPS/geofencing,
cloud sync, receipt OCR, family sharing, Fuel Intelligence phases, major
widget redesign or new widget capabilities.
NOTE: a v1.0 home-screen widget IS shipped (widget/AutoMinderWidget.kt,
Glance). It receives bug/a11y/stability fixes like any shipped surface —
do not duplicate it or treat it as unbuilt.

## SESSION VERIFICATION PROTOCOL — confirm all 5 or STOP
1. Package is com.autominder.app
2. Annotation processing is KSP only (never kapt); KSP version mirrors the
   Kotlin prefix in gradle/libs.versions.toml
3. gradle/libs.versions.toml checked for any version being relied on
4. Files to be touched are listed before any code is written
5. Current phase (incl. Billing deadline) acknowledged before scoping

## SOURCE-OF-TRUTH PRECEDENCE
1. gradle/libs.versions.toml — sole authority for dependency versions.
   Never hardcode or "remember" versions. On drift: report, don't guess.
2. Repository code and git history override HANDOFF.md and session summaries.
3. This file overrides rules and skills on conflict; a task's explicitly
   approved file scope overrides default ownership boundaries.
Pinned invariants: Room exportSchema=true | minSdk 26 | targetSdk 36 |
Java 17 | type-safe @Serializable navigation only.

## ARCHITECTURE LAW (violation = immediate rollback)
MVVM + Clean Architecture + Offline-First + Repository.
UI → ViewModel → UseCase → Repository interface → RepositoryImpl → DAO.
ViewModel NEVER imports DAO. UI composable NEVER imports Repository.
NEVER kapt() — ksp() only. NEVER raw strings in navigate() — NavRoutes.kt
@Serializable sealed types only. Bottom nav: Home | Vehicles | Records |
Settings (never rename Records). Layer/Flow/coroutine/Room detail:
.claude/rules/data.md.

## PRODUCT EXPERIENCE LAW
Health understandable ~3s | mileage update ~5s | routine service log ~15s |
common actions zero-or-minimal typing. Prefill everything known. One primary
action per screen; no equal-weight layouts. Entered data survives errors,
rotation, process death. Truthful states everywhere — never advertise
capabilities that don't exist, never dead-end a CTA. UI specifics (tokens,
a11y, motion, localization, forms): .claude/rules/ui.md + DESIGN_SYSTEM.md.

## SECURITY & PRIVACY LAW
- NEVER touch or print contents of *.keystore, *.jks, local.properties,
  google-services.json. Never commit secrets or signing material.
- Signing: local release from untracked local.properties; CI from protected
  secrets only; missing input fails loudly; values never appear in logs.
- Manifest components declare android:exported explicitly; internal stays
  non-exported. Validate incoming intent/deep-link data. PendingIntent
  mutability explicit and minimal.
- New permissions need documented user need + explicit approval.
- Never log VINs, plates, notes, locations, tokens, personal data.
- No cleartext traffic. Backup rules reviewed when data files change.
- Data Safety / privacy-policy claims must match actual SDK behavior.
- NEVER PowerShell Get-/Set-Content on .kt/.xml (mojibake/BOM) — use proper
  editing tools.

## TESTING LAW
Every bug fix gets a regression test where practical. Changed business rules
cover success + boundary + failure. Never delete/weaken a test or add
retries/exclusions to get green CI. Behavior verified ≠ compiled — state
plainly what was and wasn't verified.

## VERIFICATION GATES — pick the tier the task needs
QUICK (every task):  gradlew compileDebugKotlin
HARDENING (any shipped behavior): gradlew assembleDebug + testDebugUnitTest
  + lintDebug. (clean only for cache diagnosis — not routine.)
RELEASE CANDIDATE (before any Play upload, plus HARDENING): clean
  assembleRelease + bundleRelease | R8 missing-rules review | signed AAB
  verified | device migration tests | billing purchase+restore via Play |
  UMP consent + privacy options | test-ad verification | TalkBack + 2.0x
  font + dark/light | process-death restoration | pre-launch report.
Windows recipes live in scripts/ and HANDOFF §6 — run the script, don't
retype the incantation.
On failure: STOP. Keep the full log on disk; report the failing task and the
first relevant error block with its causal chain — not the whole log, not
just line one. Fix only that failure.

## GIT & WIP DISCIPLINE
main: always green, no direct commits | migration/*: dependency migrations |
fix/* | feature/* | sprint/*: hardening. Before unrelated work, reach a
recoverable checkpoint: clean committed tree, reviewed named stash, or
explicitly documented WIP assigned for reconciliation. NEVER commit
unreviewed work just to get a clean tree; NEVER bury real changes under a
generic "checkpoint" message — it hides finished work from history (this
happened; it cost a full reconciliation session).

## DEFINITION OF DONE — a task is complete only when:
- approved acceptance criteria met, within approved file scope only
- changed behavior has appropriate tests; required gate tier passed
- no test/lint/R8 protection weakened; no protected file touched
- required UI states + accessibility + restoration considered
- what was NOT verified is stated explicitly
Claude never claims "production-ready", "fully tested", "secure", or
"Play-ready" without corresponding release-gate evidence.

## ROUTING
Path rules (auto-load): .claude/rules/ui.md | data.md | monetization.md
Procedures: .claude/skills/ (ui-slice, visual-qa, accessibility-qa,
compose-performance-guardian, ui-diff-review, android-release-gate,
play-store-readiness). Reviewers: .claude/agents/ (read-only).
Reference: docs/GOVERNANCE_REFERENCE.md (StatusCalculator, notification
cooldowns, fuel-intelligence v1.1 spec, AdMob pattern, ownership defaults).
State: HANDOFF.md — verify freshness against git log before trusting.
Code graph: graphify-out/graph.json (~1.9k nodes, ~4.2k edges), rebuilt by a
post-commit hook, which skips commits touching no code — so `built_at_commit`
correctly points at the last CODE commit and lagging HEAD is normal, not
stale. Hand-rebuild only to index uncommitted work: `graphify update .`
(AST only, no LLM, no token cost). Ask the graph BEFORE
grepping for any relational question (what uses X, what breaks if X changes,
where does this flow go); grep stays correct for a single literal in a known
file. Query the BARE SYMBOL, not a verb: the traversal filters edges by the
verb it infers, and `calls` is only 747 of 4217 edges against `imports` 1094
and `references` 942 — "what calls VehicleCatalog" returned nothing while its
three real importers sat in the graph. One symbol also yields several nodes
(class plus each method); the class-level node carries the cross-file edges.
graph.json/graph.html are gitignored: 5.4MB of regenerable output that would
otherwise append to history on every commit.
