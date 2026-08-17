package com.autominder.app.ui.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the point at which an overdue distance stops informing the driver.
 *
 * The case that produced this rule was real, seen on a device: a car added at
 * 20,000 km and updated to 201,000 km rendered "Overdue by 173,000 km" seven
 * times over. Every number was arithmetically correct and the screen was
 * unusable.
 */
class OverdueCopyTest {

    private val oilChangeInterval = 8_000

    @Test
    fun `a small overshoot still shows the exact distance`() {
        assertTrue(OverdueCopy.showsExactDistance(overdueKm = 500, intervalKm = oilChangeInterval))
    }

    @Test
    fun `exactly one interval past due still shows the distance - the boundary is inclusive`() {
        assertTrue(OverdueCopy.showsExactDistance(overdueKm = 8_000, intervalKm = oilChangeInterval))
    }

    @Test
    fun `just beyond one interval switches to the action`() {
        assertFalse(OverdueCopy.showsExactDistance(overdueKm = 8_001, intervalKm = oilChangeInterval))
    }

    @Test
    fun `the device case is suppressed`() {
        assertFalse(OverdueCopy.showsExactDistance(overdueKm = 173_000, intervalKm = oilChangeInterval))
    }

    /**
     * A one-off reminder ("due at 210,000 km") carries no interval, so there is
     * no basis for a ceiling. Inventing one would be fabricating a value, which
     * the UI rules forbid outright — the honest output is the real number.
     */
    @Test
    fun `no interval recorded keeps the exact distance`() {
        assertTrue(OverdueCopy.showsExactDistance(overdueKm = 173_000, intervalKm = null))
    }

    @Test
    fun `a zero or negative interval is treated as no interval`() {
        assertTrue(OverdueCopy.showsExactDistance(overdueKm = 173_000, intervalKm = 0))
        assertTrue(OverdueCopy.showsExactDistance(overdueKm = 173_000, intervalKm = -1))
    }
}
