package com.autominder.app.ui.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the subscription disclosure required by Google Play's Subscriptions
 * policy: the cost, the billing period and the automatic renewal terms must all
 * be visible without the user taking any further action.
 *
 * Unexpected auto-renewal is also the most common complaint in this app
 * category, so this is a refund-and-one-star risk as much as a policy one.
 *
 * These assertions read `strings.xml` directly rather than rendering the
 * composable. That is deliberate: the failure mode being guarded is a *copy*
 * regression — someone shortening the CTA back to "Continue", dropping the
 * price placeholder, or reinstating a hardcoded discount. A Robolectric render
 * would not catch any of those, and a plain JVM test that reads the resource
 * file does, at no cost.
 */
class PaywallDisclosureTest {

    private val strings: String by lazy {
        val f = File("src/main/res/values/strings.xml")
        require(f.exists()) { "strings.xml not found at ${f.absolutePath}" }
        f.readText()
    }

    private fun value(name: String): String {
        val match = Regex("""<string name="$name">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .find(strings)
        requireNotNull(match) { "string/$name is missing — the paywall disclosure depends on it" }
        return match.groupValues[1]
    }

    @Test
    fun `every call to action carries the price placeholder`() {
        listOf("paywall_cta_subscribe", "paywall_cta_lifetime").forEach { name ->
            assertTrue(
                "string/$name must interpolate Play's localized price. A CTA that " +
                    "hides what it charges violates the Subscriptions policy.",
                value(name).contains("%1\$s")
            )
        }
    }

    @Test
    fun `recurring plans state that they renew automatically`() {
        listOf("paywall_renewal_monthly", "paywall_renewal_yearly").forEach { name ->
            val text = value(name).lowercase()
            assertTrue(
                "string/$name must say the subscription renews automatically",
                text.contains("renews automatically")
            )
            assertTrue(
                "string/$name must tell the user how to cancel",
                text.contains("cancel")
            )
            assertTrue(
                "string/$name must interpolate the price",
                value(name).contains("%1\$s")
            )
        }
    }

    @Test
    fun `monthly and yearly disclose their own billing period`() {
        assertTrue(
            "The monthly line must name a monthly billing period",
            value("paywall_renewal_monthly").lowercase().contains("per month")
        )
        assertTrue(
            "The yearly line must name a yearly billing period",
            value("paywall_renewal_yearly").lowercase().contains("per year")
        )
    }

    @Test
    fun `lifetime is never described as renewing`() {
        val text = value("paywall_renewal_lifetime").lowercase()

        // Check the claim, not the token. The correct copy contains the word
        // "renews" inside the negation "nothing renews", so banning the token
        // outright fails on the very sentence it is meant to require. This
        // assertion originally did exactly that.
        listOf("renews automatically", "renews at", "will renew", "auto-renew").forEach { claim ->
            assertFalse(
                "string/paywall_renewal_lifetime says \"$claim\". Lifetime is a " +
                    "one-off purchase; claiming it renews would be as untrue as " +
                    "hiding that the subscriptions do.",
                text.contains(claim)
            )
        }
        assertTrue(
            "Lifetime must state plainly that it is not a subscription",
            text.contains("not a subscription")
        )
    }

    @Test
    fun `no savings claim states a percentage the app never computed`() {
        // Was "Save 37% · Best value" — a constant, so it was wrong in every
        // market where Play's local pricing does not preserve the monthly:yearly
        // ratio, and wrong the moment either price changed.
        val badge = value("paywall_yearly_best_value")
        assertFalse(
            "string/paywall_yearly_best_value states '$badge'. A discount figure may " +
                "only appear here once it is computed from both products' " +
                "priceAmountMicros at runtime.",
            badge.contains("%")
        )
        assertFalse(
            "A hardcoded 'save' claim is a disclosure risk in any currency",
            badge.lowercase().contains("save")
        )
    }

    @Test
    fun `the old placeholder call to action is gone`() {
        assertFalse(
            "paywall_continue was the bare \"Continue\" button. It must not come back.",
            strings.contains("name=\"paywall_continue\"")
        )
    }
}
