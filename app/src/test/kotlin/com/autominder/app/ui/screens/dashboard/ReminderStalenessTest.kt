package com.autominder.app.ui.screens.dashboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Covers the staleness rule behind the "Reminders may be delayed" banner.
 *
 * The interesting cases are not the obvious ones. A warning like this fails in
 * two directions — firing at a user nothing is wrong with, and staying silent
 * for the user whose engine has never run at all — and both failures are
 * invisible without these boundaries pinned.
 */
class ReminderStalenessTest {

    private val hour = 60 * 60 * 1000L
    private val now = 1_770_000_000_000L
    private val installedLongAgo = now - 90L * 24 * hour

    @Test
    fun `a recent check is not stale`() {
        val result = evaluateReminderStaleness(
            lastSuccessfulCheckAt = now - 5 * hour,
            firstInstallTimeMillis = installedLongAgo,
            nowMillis = now
        )
        assertNull(result)
    }

    @Test
    fun `exactly 36 hours is not yet stale - the boundary is exclusive`() {
        val result = evaluateReminderStaleness(
            lastSuccessfulCheckAt = now - 36 * hour,
            firstInstallTimeMillis = installedLongAgo,
            nowMillis = now
        )
        assertNull(result)
    }

    @Test
    fun `just past 36 hours is stale and reports when we last succeeded`() {
        val lastCheck = now - 36 * hour - 1
        val result = evaluateReminderStaleness(
            lastSuccessfulCheckAt = lastCheck,
            firstInstallTimeMillis = installedLongAgo,
            nowMillis = now
        )
        assertEquals(RemindersDelayedState(lastCheckedAt = lastCheck), result)
    }

    /**
     * The false-alarm case. A worker that has genuinely never had a chance to
     * run must not be reported as broken, or every new install opens to a
     * warning about a failure that has not happened.
     */
    @Test
    fun `a fresh install with no check yet is not stale`() {
        val result = evaluateReminderStaleness(
            lastSuccessfulCheckAt = null,
            firstInstallTimeMillis = now - 2 * hour,
            nowMillis = now
        )
        assertNull(result)
    }

    /**
     * The missed-alarm case, and the reason install time is the fallback
     * rather than simply treating null as healthy: an engine that has never
     * once completed a pass is the *worst* outcome this banner exists to
     * catch, and it would otherwise be the one case that stays silent forever.
     */
    @Test
    fun `an install older than 36 hours with no check ever is stale`() {
        val result = evaluateReminderStaleness(
            lastSuccessfulCheckAt = null,
            firstInstallTimeMillis = now - 40 * hour,
            nowMillis = now
        )
        assertEquals(RemindersDelayedState(lastCheckedAt = null), result)
    }

    @Test
    fun `a clock jumped backwards reads as healthy, not stale`() {
        val result = evaluateReminderStaleness(
            lastSuccessfulCheckAt = now + 10 * hour,
            firstInstallTimeMillis = installedLongAgo,
            nowMillis = now
        )
        assertNull(result)
    }

    /**
     * A recorded check always wins over install time, including when the
     * install is ancient. Otherwise a long-installed app would look stale
     * every time the fallback leaked into the comparison.
     */
    @Test
    fun `a recent check on an old install is not stale`() {
        val result = evaluateReminderStaleness(
            lastSuccessfulCheckAt = now - hour,
            firstInstallTimeMillis = installedLongAgo,
            nowMillis = now
        )
        assertNull(result)
    }
}
