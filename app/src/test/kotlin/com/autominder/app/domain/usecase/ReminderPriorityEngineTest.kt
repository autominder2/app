package com.autominder.app.domain.usecase

import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.Vehicle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ReminderPriorityEngineTest {

    private lateinit var priorityEngine: ReminderPriorityEngine

    @Before
    fun setUp() {
        priorityEngine = ReminderPriorityEngine()
    }

    @Test
    fun `rankReminders places overdue items first regardless of date order`() {
        val vehicle = Vehicle(id = 1L, make = "Toyota", model = "RAV4", year = 2022, currentOdometer = 45000, updatedAt = System.currentTimeMillis())

        val futureReminder = ReminderWithStatus(
            reminder = Reminder(id = 1L, vehicleId = 1L, serviceType = ServiceType.OIL_CHANGE, nextDueOdometer = 50000, nextDueDate = System.currentTimeMillis() + 100000000L),
            vehicle = vehicle,
            status = ServiceStatus.OK
        )
        val overdueReminder = ReminderWithStatus(
            reminder = Reminder(id = 2L, vehicleId = 1L, serviceType = ServiceType.BRAKE_SERVICE, nextDueOdometer = 44000, nextDueDate = System.currentTimeMillis() - 10000000L),
            vehicle = vehicle,
            status = ServiceStatus.OVERDUE
        )

        val ranked = priorityEngine.rankReminders(listOf(futureReminder, overdueReminder))

        assertEquals(2, ranked.size)
        assertEquals(2L, ranked.first().reminderWithStatus.reminder.id)
        assertEquals(ReminderUrgency.OVERDUE, ranked.first().urgency)
    }

    @Test
    fun `rankReminders prioritizes safety critical items over general future items`() {
        val vehicle = Vehicle(id = 1L, make = "Toyota", model = "RAV4", year = 2022, currentOdometer = 45000)

        val customReminder = ReminderWithStatus(
            reminder = Reminder(id = 1L, vehicleId = 1L, serviceType = ServiceType.CUSTOM, nextDueOdometer = 60000),
            vehicle = vehicle,
            status = ServiceStatus.OK
        )
        val brakeReminder = ReminderWithStatus(
            reminder = Reminder(id = 2L, vehicleId = 1L, serviceType = ServiceType.BRAKE_SERVICE, nextDueOdometer = 60000),
            vehicle = vehicle,
            status = ServiceStatus.OK
        )

        val ranked = priorityEngine.rankReminders(listOf(customReminder, brakeReminder))

        assertEquals(2L, ranked.first().reminderWithStatus.reminder.id)
        assertEquals(ReminderUrgency.SAFETY_CRITICAL, ranked.first().urgency)
    }

    @Test
    fun `buildExplanation produces exact mathematical proof and standard vocabulary`() {
        val vehicle = Vehicle(id = 1L, make = "Toyota", model = "RAV4", year = 2022, currentOdometer = 45000, updatedAt = System.currentTimeMillis())
        val reminder = ReminderWithStatus(
            reminder = Reminder(id = 1L, vehicleId = 1L, serviceType = ServiceType.OIL_CHANGE, intervalKm = 10000, nextDueOdometer = 50000),
            vehicle = vehicle,
            status = ServiceStatus.OK
        )
        val lastService = Service(
            id = 10L,
            vehicleId = 1L,
            serviceType = ServiceType.OIL_CHANGE,
            odometerAtService = 40000,
            serviceDate = System.currentTimeMillis() - 100000000L
        )

        val explanation = priorityEngine.buildExplanation(reminder, lastService)

        assertEquals("Oil service", explanation.serviceTitle)
        assertEquals(40000, explanation.lastServiceOdometer)
        assertEquals(10000, explanation.intervalKm)
        assertEquals(45000, explanation.currentOdometer)
        assertEquals(50000, explanation.targetDueOdometer)
        assertEquals(5000, explanation.remainingKm)
        assertEquals(DataConfidence.HIGH, explanation.confidence)
    }

    @Test
    fun `determineConfidence marks 0 odometer as INCOMPLETE_DATA`() {
        val vehicle = Vehicle(id = 1L, make = "Toyota", model = "RAV4", year = 2022, currentOdometer = 0)
        val reminder = ReminderWithStatus(
            reminder = Reminder(id = 1L, vehicleId = 1L, serviceType = ServiceType.OIL_CHANGE),
            vehicle = vehicle,
            status = ServiceStatus.OK
        )

        val explanation = priorityEngine.buildExplanation(reminder, null)

        assertEquals(DataConfidence.INCOMPLETE_DATA, explanation.confidence)
    }
}
