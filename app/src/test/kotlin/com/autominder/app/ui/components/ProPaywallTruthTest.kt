package com.autominder.app.ui.components

import com.autominder.app.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the paywall against advertising capabilities a free user already has.
 *
 * Before 2026-08-25 the table listed five Pro-exclusive rows while only two
 * entitlement gates existed in the codebase. A user could pay and receive
 * nothing beyond ad removal and two cards — a refund generator and a Play
 * Store misrepresentation risk.
 *
 * If you add a row as `proOnly`, you must first add a real gate and record it
 * in `gateEvidence`. If you remove a gate, flip the row back to shared.
 */
class ProPaywallTruthTest {

    /**
     * Every entitlement gate that exists in v1.0, verified by reading the code:
     *  - MainActivity.kt:274        `if (!isProUser) { BannerAdView(...) }`
     *  - VehicleDetailScreen.kt:681 `ProFeatureGate` around the cost deck
     *  - VehicleDetailScreen.kt:765 `ProFeatureGate` around the efficiency deck
     *
     * The two VehicleDetail gates are surfaced to the user as one paywall row.
     */
    private val knownGatedFeatures = setOf(
        R.string.paywall_feature_cost_analytics,
        R.string.paywall_feature_ad_free
    )

    @Test
    fun `pro-only rows correspond exactly to real entitlement gates`() {
        val proOnly = PAYWALL_FEATURES.filter { it.proOnly }.map { it.labelRes }.toSet()

        assertEquals(
            "A paywall row is marked Pro-only without a matching gate, or a gate " +
                "lost its row. Add the gate before advertising it — see the KDoc " +
                "on PAYWALL_FEATURES.",
            knownGatedFeatures,
            proOnly
        )
    }

    @Test
    fun `every pro-only row cites where its gate lives`() {
        PAYWALL_FEATURES.filter { it.proOnly }.forEach { feature ->
            assertNotNull(
                "Pro-only row ${feature.labelRes} must cite its gate in gateEvidence " +
                    "so the claim stays auditable.",
                feature.gateEvidence
            )
            assertTrue(
                "gateEvidence should be a file:line reference, got '${feature.gateEvidence}'",
                feature.gateEvidence!!.contains(".kt:")
            )
        }
    }

    @Test
    fun `shared rows do not claim a gate`() {
        PAYWALL_FEATURES.filterNot { it.proOnly }.forEach { feature ->
            assertNull(
                "Row ${feature.labelRes} is available to free users, so it must not " +
                    "cite an entitlement gate.",
                feature.gateEvidence
            )
        }
    }

    @Test
    fun `table is not empty and has no duplicate rows`() {
        assertTrue("Paywall feature table is empty", PAYWALL_FEATURES.isNotEmpty())
        assertEquals(
            "Duplicate feature row in PAYWALL_FEATURES",
            PAYWALL_FEATURES.size,
            PAYWALL_FEATURES.map { it.labelRes }.toSet().size
        )
    }
}
