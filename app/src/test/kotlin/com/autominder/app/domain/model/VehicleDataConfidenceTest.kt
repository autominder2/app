package com.autominder.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class VehicleDataConfidenceTest {

    private val now = 1_700_000_000_000L

    @Test
    fun `zero or negative odometer evaluates to MISSING_MILEAGE`() {
        val confidenceZero = VehicleDataConfidence.evaluate(
            currentOdometer = 0,
            lastOdometerUpdateMillis = now,
            hasServiceHistory = true,
            nowMillis = now
        )
        assertEquals(VehicleDataConfidence.MISSING_MILEAGE, confidenceZero)
        assertFalse(confidenceZero.isTrustworthy)

        val confidenceNegative = VehicleDataConfidence.evaluate(
            currentOdometer = -100,
            lastOdometerUpdateMillis = now,
            hasServiceHistory = true,
            nowMillis = now
        )
        assertEquals(VehicleDataConfidence.MISSING_MILEAGE, confidenceNegative)
        assertFalse(confidenceNegative.isTrustworthy)
    }

    @Test
    fun `recent odometer update with service history evaluates to HIGH confidence`() {
        val recentUpdate = now - TimeUnit.DAYS.toMillis(5)
        val confidence = VehicleDataConfidence.evaluate(
            currentOdometer = 45000,
            lastOdometerUpdateMillis = recentUpdate,
            hasServiceHistory = true,
            nowMillis = now
        )
        assertEquals(VehicleDataConfidence.HIGH, confidence)
        assertTrue(confidence.isTrustworthy)
    }

    @Test
    fun `moderately recent odometer update evaluates to MEDIUM confidence`() {
        val thirtyDaysAgo = now - TimeUnit.DAYS.toMillis(30)
        val confidence = VehicleDataConfidence.evaluate(
            currentOdometer = 45000,
            lastOdometerUpdateMillis = thirtyDaysAgo,
            hasServiceHistory = true,
            nowMillis = now
        )
        assertEquals(VehicleDataConfidence.MEDIUM, confidence)
        assertTrue(confidence.isTrustworthy)
    }

    @Test
    fun `stale odometer update older than 60 days evaluates to ESTIMATED confidence`() {
        val ninetyDaysAgo = now - TimeUnit.DAYS.toMillis(90)
        val confidence = VehicleDataConfidence.evaluate(
            currentOdometer = 45000,
            lastOdometerUpdateMillis = ninetyDaysAgo,
            hasServiceHistory = true,
            nowMillis = now
        )
        assertEquals(VehicleDataConfidence.ESTIMATED, confidence)
        assertFalse(confidence.isTrustworthy)
    }
}
