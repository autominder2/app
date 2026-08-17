package com.autominder.app.domain.usecase

import com.autominder.app.domain.model.DrivingAmount
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.usecase.CreateDefaultRemindersUseCase.Companion.buildPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Deterministic seeded-plan coverage: the plan the onboarding reveal shows
 * is exactly the plan that gets saved, so these tests pin its behavior.
 */
class CreateDefaultRemindersUseCaseTest {

    private val now = 1_750_000_000_000L // fixed clock — determinism by construction

    @Test
    fun `same inputs always produce the same plan`() {
        val a = buildPlan(50_000, DrivingAmount.TYPICAL, now)
        val b = buildPlan(50_000, DrivingAmount.TYPICAL, now)
        assertEquals(a, b)
    }

    @Test
    fun `plan is sorted soonest-first so index zero is what's first`() {
        val plan = buildPlan(20_000, DrivingAmount.TYPICAL, now)
        assertEquals(
            plan.sortedWith(compareBy({ it.nextDueDate }, { it.nextDueOdometer ?: Int.MAX_VALUE })),
            plan
        )
    }

    @Test
    fun `oil change leads the plan for a typical driver`() {
        val plan = buildPlan(0, DrivingAmount.TYPICAL, now)
        assertEquals(ServiceType.OIL_CHANGE, plan.first().serviceType)
    }

    @Test
    fun `driving amount materially changes the date axis`() {
        val low = buildPlan(30_000, DrivingAmount.LOW, now)
        val high = buildPlan(30_000, DrivingAmount.HIGH, now)
        val lowOil = low.first { it.serviceType == ServiceType.OIL_CHANGE }
        val highOil = high.first { it.serviceType == ServiceType.OIL_CHANGE }
        assertTrue(
            "HIGH driver must see a sooner oil date than LOW driver",
            highOil.nextDueDate < lowOil.nextDueDate
        )
    }

    @Test
    fun `driving amount never changes the km axis`() {
        val low = buildPlan(30_000, DrivingAmount.LOW, now)
        val high = buildPlan(30_000, DrivingAmount.HIGH, now)
        val lowKms = low.associate { it.serviceType to it.nextDueOdometer }
        high.forEach { assertEquals(lowKms[it.serviceType], it.nextDueOdometer) }
    }

    @Test
    fun `date never exceeds the template calendar interval`() {
        // A LOW driver still gets the calendar cap, not an absurd far date.
        val plan = buildPlan(0, DrivingAmount.LOW, now)
        val oil = plan.first { it.serviceType == ServiceType.OIL_CHANGE }
        assertEquals(now + 180L * 86_400_000L, oil.nextDueDate)
    }

    /**
     * Replaces an earlier test that asserted the opposite — that a vehicle
     * with no mileage got `nextDueOdometer = 8_000`. That assertion encoded a
     * defect rather than a requirement.
     *
     * `currentOdometer == 0` is this app's sentinel for "mileage not added"
     * (`VehicleListScreen`), not a reading of zero. Anchoring the distance
     * axis to it meant a car whose owner skipped the odometer field got
     * reminders due at 8,000km; the day they entered their real 201,000km,
     * all seven services became overdue at once — observed on a device as
     * "Overdue by 173,000 km" repeated down the dashboard.
     *
     * The plan is still created, and the calendar axis still drives it.
     */
    @Test
    fun `a vehicle with no mileage gets a date-only plan, not one anchored to zero`() {
        val plan = buildPlan(0, DrivingAmount.TYPICAL, now)

        assertEquals(6, plan.size)
        plan.forEach {
            assertNull(
                "no odometer reading means nothing to anchor distance to",
                it.nextDueOdometer
            )
            assertTrue("the calendar axis must still carry the plan", it.nextDueDate > now)
        }
    }

    @Test
    fun `a real reading still anchors the distance axis`() {
        val plan = buildPlan(20_000, DrivingAmount.TYPICAL, now)
        val oil = plan.first { it.serviceType == ServiceType.OIL_CHANGE }
        assertEquals(28_000, oil.nextDueOdometer)
    }

    /**
     * One kilometre is a real reading, however implausible. The sentinel is
     * exactly zero, and the boundary must not drift into "small values are
     * probably unset" guesswork.
     */
    @Test
    fun `one kilometre is a reading, not a sentinel`() {
        val plan = buildPlan(1, DrivingAmount.TYPICAL, now)
        val oil = plan.first { it.serviceType == ServiceType.OIL_CHANGE }
        assertEquals(8_001, oil.nextDueOdometer)
    }

    @Test
    fun `high-mileage augmentation thresholds are exact`() {
        assertEquals(6, buildPlan(79_999, DrivingAmount.TYPICAL, now).size)
        val at80k = buildPlan(80_000, DrivingAmount.TYPICAL, now)
        assertEquals(7, at80k.size)
        assertTrue(at80k.any { it.serviceType == ServiceType.COOLANT })
        val at100k = buildPlan(100_000, DrivingAmount.TYPICAL, now)
        assertEquals(8, at100k.size)
        assertTrue(at100k.any { it.serviceType == ServiceType.TRANSMISSION })
    }

    @Test
    fun `unusually high but plausible mileage still yields a valid plan`() {
        val plan = buildPlan(400_000, DrivingAmount.HIGH, now)
        assertTrue(plan.isNotEmpty())
        plan.forEach {
            assertTrue(it.nextDueOdometer!! > 400_000)
            assertTrue(it.nextDueDate > now)
        }
    }
}
