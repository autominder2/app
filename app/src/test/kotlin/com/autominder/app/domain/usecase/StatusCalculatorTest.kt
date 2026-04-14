package com.autominder.app.domain.usecase

import com.autominder.app.domain.model.ServiceStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class StatusCalculatorTest {

    private val now = System.currentTimeMillis()

    @Test
    fun `completed reminder returns COMPLETED`() {
        val result = StatusCalculator.calculate(
            nowMillis = now,
            currentOdometer = 50000,
            dueDateMillis = now - TimeUnit.DAYS.toMillis(30),
            dueOdometer = 40000,
            snoozeUntilMillis = null,
            isCompleted = true
        )
        assertEquals(ServiceStatus.COMPLETED, result)
    }

    @Test
    fun `overdue by date returns OVERDUE`() {
        val result = StatusCalculator.calculate(
            nowMillis = now,
            currentOdometer = 50000,
            dueDateMillis = now - TimeUnit.DAYS.toMillis(1),
            dueOdometer = null,
            snoozeUntilMillis = null,
            isCompleted = false
        )
        assertEquals(ServiceStatus.OVERDUE, result)
    }

    @Test
    fun `overdue by odometer returns OVERDUE`() {
        val result = StatusCalculator.calculate(
            nowMillis = now,
            currentOdometer = 50001,
            dueDateMillis = null,
            dueOdometer = 50000,
            snoozeUntilMillis = null,
            isCompleted = false
        )
        assertEquals(ServiceStatus.OVERDUE, result)
    }

    @Test
    fun `overdue takes priority over snoozed`() {
        val result = StatusCalculator.calculate(
            nowMillis = now,
            currentOdometer = 50000,
            dueDateMillis = now - TimeUnit.DAYS.toMillis(1),
            dueOdometer = null,
            snoozeUntilMillis = now + TimeUnit.DAYS.toMillis(7),
            isCompleted = false
        )
        assertEquals(ServiceStatus.OVERDUE, result)
    }

    @Test
    fun `snoozed returns SNOOZED when not overdue`() {
        val result = StatusCalculator.calculate(
            nowMillis = now,
            currentOdometer = 50000,
            dueDateMillis = now + TimeUnit.DAYS.toMillis(30),
            dueOdometer = null,
            snoozeUntilMillis = now + TimeUnit.DAYS.toMillis(7),
            isCompleted = false
        )
        assertEquals(ServiceStatus.SNOOZED, result)
    }

    @Test
    fun `due soon by date returns DUE_SOON`() {
        val result = StatusCalculator.calculate(
            nowMillis = now,
            currentOdometer = 50000,
            dueDateMillis = now + TimeUnit.DAYS.toMillis(10),
            dueOdometer = null,
            snoozeUntilMillis = null,
            isCompleted = false
        )
        assertEquals(ServiceStatus.DUE_SOON, result)
    }

    @Test
    fun `due soon by odometer returns DUE_SOON`() {
        val result = StatusCalculator.calculate(
            nowMillis = now,
            currentOdometer = 49600,
            dueDateMillis = null,
            dueOdometer = 50000,
            snoozeUntilMillis = null,
            isCompleted = false
        )
        assertEquals(ServiceStatus.DUE_SOON, result)
    }

    @Test
    fun `far future date returns OK`() {
        val result = StatusCalculator.calculate(
            nowMillis = now,
            currentOdometer = 50000,
            dueDateMillis = now + TimeUnit.DAYS.toMillis(60),
            dueOdometer = null,
            snoozeUntilMillis = null,
            isCompleted = false
        )
        assertEquals(ServiceStatus.OK, result)
    }

    @Test
    fun `no due conditions returns UNKNOWN`() {
        val result = StatusCalculator.calculate(
            nowMillis = now,
            currentOdometer = 50000,
            dueDateMillis = null,
            dueOdometer = null,
            snoozeUntilMillis = null,
            isCompleted = false
        )
        assertEquals(ServiceStatus.UNKNOWN, result)
    }
}
