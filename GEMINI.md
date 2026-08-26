# AutoMinder — Antigravity / Gemini entry point
# Updated: 2026-08-25 | Deliberately THIN: this file points, it does not duplicate.
#
# WHY: three instruction files (CLAUDE.md, AGENTS.md, this one) previously carried
# overlapping content and drifted — this file sat 128 days behind CLAUDE.md and still
# declared "overrides take precedence", while asserting a branch convention that no
# longer exists. Duplicated rules drift; pointers cannot. Everything that changes lives
# in ONE place below. Only never-changing invariants are restated here.

## Authority chain — read in this order, later wins on conflict
1. `CLAUDE.md` — **the source of truth.** Current phase, architecture law, security law,
   testing law, verification gates, git discipline, definition of done.
2. `AGENTS.md` — agent scope control and ownership boundaries.
3. `.claude/rules/{ui,data,monetization}.md` — path-scoped detail; load the one matching
   the files you are touching.
4. `docs/GOVERNANCE_REFERENCE.md` — StatusCalculator, notification cooldowns, AdMob pattern.
5. This file — machine-local invariants only (below).

**Do not restate anything from those files here.** If you need a rule, read the file that
owns it. If this file ever contradicts `CLAUDE.md`, `CLAUDE.md` wins.

## Hard invariants (safe to restate — these are locked and never change)
- Package: `com.autominder.app` — FINAL, never change.
- Annotation processing: **KSP only, never kapt.**
- Versions come from `gradle/libs.versions.toml` — the sole authority. Never hardcode or
  recall a version number; on drift, report rather than guess.
- Room `exportSchema=true` | minSdk 26 | targetSdk 36 | Java 17.
- Navigation: type-safe `@Serializable` routes only, never raw strings in `navigate()`.
- Bottom nav: Home | Vehicles | Records | Settings — never rename Records.
- Branches: `main` (always green, no direct commits) · `migration/*` · `fix/*` ·
  `feature/*` · `sprint/*`.
- Never read or print: `*.keystore`, `*.jks`, `local.properties`, `google-services.json`.
  All signing and ad IDs come from `local.properties` or CI secrets, never source.

## Machine-local build note (Windows)
Gradle needs a short Unix-domain-socket path on this machine. `_JAVA_OPTIONS` is now set
globally in `~/.claude/settings.json`, so a plain invocation works:
```
gradlew.bat compileDebugKotlin --no-configuration-cache --no-daemon
```
Use `gradlew.bat`, not `./gradlew`. Ignore the `Picked up _JAVA_OPTIONS` line on stderr —
it is not an error. Trust the exit code. Gate tiers are defined in `CLAUDE.md`.

## Code graph
`graphify-out/` holds a queryable structural graph. Query it before reading files or
fanning out agents; it answers where/what-connects, never whether the logic is correct.
Refresh with `graphify update .` (AST only, no LLM). Committed work memory lives in
`graphify-out/memory/` and `reflections/LESSONS.md` — read the lessons first.

## Session self-check
Before writing code, confirm you can state: the package name, that KSP replaces kapt, the
current phase from `CLAUDE.md`, and the files you intend to touch. If you cannot, stop and
load `CLAUDE.md` before proceeding.
