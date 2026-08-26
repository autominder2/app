package com.autominder.app.domain.usecase.cockpit

import com.autominder.app.domain.intelligence.ConfidenceSignal
import com.autominder.app.domain.intelligence.ConfidenceState
import com.autominder.app.domain.intelligence.VehicleConfidenceEngine
import com.autominder.app.domain.model.MileageLogEntry
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.Vehicle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the maintenance-status engine against re-growing a health score.
 *
 * Until 2026-08-26 this engine emitted a 0-100 `score` and a four-way verdict.
 * Its worst behaviour is pinned below as
 * `unknown vehicle is never reported as healthy`: a vehicle with no reminders
 * and no history used to default to `score = 100, verdict = EXCELLENT`, so the
 * app was most confident about the car it knew least about.
 *
 * If a future change reintroduces a number, these tests should be the thing
 * that stops it — not a code review.
 */
class CalculateConfidenceUseCaseTest {

    private val now = 1_756_000_000_000L
    private val engine = VehicleConfidenceEngine()
    private val useCase = CalculateConfidenceUseCase(engine)

    private val vehicle = Vehicle(
        id = 1L,
        make = "Toyota",
        model = "RAV4",
        year = 2025,
        currentOdometer = 15_000,
        createdAt = now - 100_000L,
        updatedAt = now - DAY_MILLIS
    )

    private fun reminder(id: Long, type: ServiceType, label: String? = null) = Reminder(
        id = id,
        vehicleId = 1L,
        serviceType = type,
        customLabel = label,
        intervalKm = 8_000,
        nextDueOdometer = 23_000
    )

    private fun evaluate(
        reminders: List<Reminder> = emptyList(),
        statuses: Map<Long, ServiceStatus> = emptyMap(),
        mileageLogs: List<MileageLogEntry> = emptyList(),
        vehicle: Vehicle = this.vehicle
    ) = useCase.execute(
        vehicle = vehicle,
        reminders = reminders,
        statuses = statuses,
        mileageLogs = mileageLogs,
        services = emptyList(),
        nowMillis = now
    )

    @Test
    fun `unknown vehicle is never reported as healthy`() {
        val result = evaluate()

        assertEquals(
            "A vehicle with no reminders and no history must report NEEDS_SETUP. " +
                "The removed scoring engine returned EXCELLENT here.",
            ConfidenceState.NEEDS_SETUP,
            result.state
        )
        assertTrue(
            "An unknown vehicle must not claim nothing is overdue",
            result.factors.none { it.signal == ConfidenceSignal.NOTHING_OVERDUE }
        )
        assertTrue(
            "The absence of a schedule is itself a reportable signal",
            result.factors.any { it.signal == ConfidenceSignal.NO_SCHEDULE && !it.isPositive }
        )
    }

    @Test
    fun `state is read off reminder statuses, not computed from weights`() {
        val oil = reminder(101L, ServiceType.OIL_CHANGE)
        val tires = reminder(102L, ServiceType.TIRE_ROTATION)
        val brakes = reminder(103L, ServiceType.BRAKE_SERVICE)

        val allOk = evaluate(
            reminders = listOf(oil, tires, brakes),
            statuses = mapOf(oil.id to ServiceStatus.OK, tires.id to ServiceStatus.OK, brakes.id to ServiceStatus.OK)
        )
        assertEquals(ConfidenceState.UP_TO_DATE, allOk.state)
        assertEquals(0, allOk.overdueCount)
        assertEquals(0, allOk.dueSoonCount)

        val oneDueSoon = evaluate(
            reminders = listOf(oil, tires, brakes),
            statuses = mapOf(oil.id to ServiceStatus.DUE_SOON, tires.id to ServiceStatus.OK, brakes.id to ServiceStatus.OK)
        )
        assertEquals(ConfidenceState.DUE_SOON, oneDueSoon.state)
        assertEquals(1, oneDueSoon.dueSoonCount)

        val oneOverdue = evaluate(
            reminders = listOf(oil, tires, brakes),
            statuses = mapOf(oil.id to ServiceStatus.DUE_SOON, tires.id to ServiceStatus.OVERDUE, brakes.id to ServiceStatus.OK)
        )
        assertEquals(
            "One overdue item outranks any number of due-soon items",
            ConfidenceState.OVERDUE,
            oneOverdue.state
        )
        assertEquals(1, oneOverdue.overdueCount)
        assertEquals(1, oneOverdue.dueSoonCount)
    }

    @Test
    fun `a single low-severity overdue item is not downgraded away`() {
        // The removed engine weighted WIPER_BLADES at a 6-point penalty, so one
        // overdue wiper on an otherwise complete schedule still scored ~90 and
        // rendered as EXCELLENT. Overdue is now overdue regardless of item.
        val wipers = reminder(201L, ServiceType.WIPER_BLADES)

        val result = evaluate(
            reminders = listOf(wipers),
            statuses = mapOf(wipers.id to ServiceStatus.OVERDUE)
        )

        assertEquals(ConfidenceState.OVERDUE, result.state)
        assertTrue(
            result.factors.any { it.signal == ConfidenceSignal.ITEM_OVERDUE && it.serviceType == ServiceType.WIPER_BLADES }
        )
    }

    @Test
    fun `item factors carry typed labels so the UI never renders a raw enum name`() {
        val custom = reminder(301L, ServiceType.CUSTOM, label = "Winter tyre swap")
        val canonical = reminder(302L, ServiceType.TIMING_BELT)

        val result = evaluate(
            reminders = listOf(custom, canonical),
            statuses = mapOf(custom.id to ServiceStatus.OVERDUE, canonical.id to ServiceStatus.DUE_SOON)
        )

        val overdueFactor = result.factors.single { it.signal == ConfidenceSignal.ITEM_OVERDUE }
        assertEquals("Winter tyre swap", overdueFactor.customLabel)

        val dueSoonFactor = result.factors.single { it.signal == ConfidenceSignal.ITEM_DUE_SOON }
        assertEquals(
            "Canonical types must travel as a ServiceType so the UI can localize them",
            ServiceType.TIMING_BELT,
            dueSoonFactor.serviceType
        )
        assertEquals(null, dueSoonFactor.customLabel)
    }

    @Test
    fun `overdue item is surfaced as next ahead of a due-soon item`() {
        val dueSoon = reminder(401L, ServiceType.AIR_FILTER)
        val overdue = reminder(402L, ServiceType.BRAKE_SERVICE)

        val result = evaluate(
            reminders = listOf(dueSoon, overdue),
            statuses = mapOf(dueSoon.id to ServiceStatus.DUE_SOON, overdue.id to ServiceStatus.OVERDUE)
        )

        assertEquals(ServiceType.BRAKE_SERVICE, result.nextServiceType)
    }

    @Test
    fun `odometer freshness reports the mileage log, not an unrelated vehicle edit`() {
        val staleVehicle = vehicle.copy(updatedAt = now - 90 * DAY_MILLIS)
        val recentLog = MileageLogEntry(
            id = 1L,
            vehicleId = 1L,
            odometer = 15_000,
            loggedAt = now - 3 * DAY_MILLIS
        )

        val result = evaluate(mileageLogs = listOf(recentLog), vehicle = staleVehicle)

        val factor = result.factors.single {
            it.signal == ConfidenceSignal.ODOMETER_RECENT || it.signal == ConfidenceSignal.ODOMETER_STALE
        }
        assertEquals(ConfidenceSignal.ODOMETER_RECENT, factor.signal)
        assertEquals(3, factor.count)
        assertTrue(factor.isPositive)
    }

    @Test
    fun `a stale odometer is reported as stale`() {
        val log = MileageLogEntry(
            id = 1L,
            vehicleId = 1L,
            odometer = 15_000,
            loggedAt = now - 75 * DAY_MILLIS
        )

        val result = evaluate(mileageLogs = listOf(log))

        val factor = result.factors.single { it.signal == ConfidenceSignal.ODOMETER_STALE }
        assertEquals(75, factor.count)
        assertFalse(factor.isPositive)
    }

    private companion object {
        const val DAY_MILLIS = 1000L * 60 * 60 * 24
    }
}
