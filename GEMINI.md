# AutoMinder — Project-Specific Overrides
# Loaded alongside ~/.gemini/GEMINI.md. Keep under 100 lines.
# Project overrides take precedence over global rules.

## Project-Specific Context
- App: AutoMinder | Package: com.autominder.app
- Workspace: d:\Autominder
- Repo: github.com/[owner]/autominder

## Session Protocol
1. Run verification test first: 'What tech stack and rules apply to this project?'
2. Agent MUST respond with: com.autominder.app, Kotlin 2.1.21, KSP not kapt, Long PKs
3. If agent response is wrong → STOP. Fix context loading before any coding.

## Build Variants
- debug: uses DEBUG_ADMOB_ID from local.properties
- release: uses RELEASE_ADMOB_ID from CI env / local.properties (never source)

## Signing (release)
- Keystore path: stored in local.properties as KEYSTORE_PATH
- storePassword / keyPassword: from local.properties (NEVER in source)
- All signing config reads from local.properties only

## AdMob
- App ID: from local.properties → ADMOB_APP_ID
- Test IDs hardcoded in debug only (Google official test IDs are OK)
- Production IDs: ONLY from local.properties / CI secrets

## Active Branches Convention
- main: always compiles, always green
- agent/phase-*: where agents work
- fix/*: quick bug fixes
- feature/*: human-driven features

## Verification Command
./gradlew compileDebugKotlin
