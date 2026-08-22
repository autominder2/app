---
paths:
  - "app/src/main/kotlin/com/autominder/app/billing/**/*.kt"
  - "app/src/main/kotlin/com/autominder/app/ads/**/*.kt"
  - "app/build.gradle.kts"
---

# AutoMinder Monetization Rules (loads when billing/ads/build files are touched)

## Billing deadline (see CLAUDE.md CURRENT PHASE — binding)
Play rejects Billing 7 for new apps/updates after 2026-08-31. Migration
7.1.1 → 9.x happens on its own migration/* branch, never bundled with UI,
Ads, Room, navigation, or release-workflow changes.

## Billing correctness
- Entitlement only from verified PURCHASED state — never PENDING, never UI
  state alone. Acknowledge eligible purchases promptly (unacknowledged
  purchases are auto-refunded by Play).
- Restore entitlement at startup and via the explicit Restore action.
- ITEM_ALREADY_OWNED → re-query purchases.
- Real ProductDetails prices only — no hardcoded or fake prices. Loading,
  available, unavailable, pending, cancelled are distinct UI states
  (PremiumPriceDisplay sealed states exist — use them).
- Never log purchase tokens or BillingResult debug messages.
- Any billing change is tested via Play license testers before shipping.

## Ads & consent (UMP)
- No ad request until canRequestAds is true. Refresh consent info each
  launch; show the privacy-options entry point whenever UMP requires it.
- Debug/tests: Google test IDs only (hardcoded in the debug block — the
  sanctioned pattern). Release IDs come from CI env / local.properties.
- The Production Safety Gate in app/build.gradle.kts (~L216) fails any
  assembleRelease/bundleRelease on a missing or PLACEHOLDER ID. Never
  weaken it; never add a test-ID fallback to the release block.
- No ads on loading/error/warning/purchase surfaces or inside forms; no
  placements inviting accidental taps. Pro disables all ads immediately.
- The free app remains fully usable with no ad fill and no consent.

## Build variants
debug: ENABLE_ADS=false, test IDs, debuggable, no minify.
release: ENABLE_ADS=true, real IDs, R8 minify+shrink. Never hardcode
AdMob/Firebase IDs in source. Never touch signing config without approval.
