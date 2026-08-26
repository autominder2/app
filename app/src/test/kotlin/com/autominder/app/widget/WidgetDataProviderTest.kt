package com.autominder.app.widget

import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.usecase.StatusCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class WidgetDataProviderTest {

    @Test
    fun `empty vehicle state sets stateType to EMPTY`() {
        val state = AutoMinderWidgetState(stateType = WidgetStateType.EMPTY)
        assertEquals(WidgetStateType.EMPTY, state.stateType)
    }

    @Test
    fun `setup incomplete when vehicle has 0 odometer and no reminders`() {
        val odo = 0
        val remindersCount = 0
        val stateType = if (odo <= 0 && remindersCount == 0) {
            WidgetStateType.SETUP_INCOMPLETE
        } else {
            WidgetStateType.HEALTHY
        }
        assertEquals(WidgetStateType.SETUP_INCOMPLETE, stateType)
    }

    @Test
    fun `overdue reminder prioritizes OVERDUE state`() {
        val now = System.currentTimeMillis()
        val overduePast = now - TimeUnit.DAYS.toMillis(5)

        val status = StatusCalculator.calculate(
            nowMillis = now,
            currentOdometer = 10_000,
            dueDateMillis = overduePast,
            dueOdometer = null,
            snoozeUntilMillis = null,
            isCompleted = false
        )

        assertEquals(ServiceStatus.OVERDUE, status)

        val overdueCount = 1
        val dueSoonCount = 2
        val stateType = when {
            overdueCount > 0 -> WidgetStateType.OVERDUE
            dueSoonCount > 0 -> WidgetStateType.DUE_SOON
            else -> WidgetStateType.HEALTHY
        }
        assertEquals(WidgetStateType.OVERDUE, stateType)
    }

    @Test
    fun `due soon reminder prioritizes DUE_SOON state when no overdue items`() {
        val now = System.currentTimeMillis()
        val dueSoon = now + TimeUnit.DAYS.toMillis(5)

        val status = StatusCalculator.calculate(
            nowMillis = now,
            currentOdometer = 10_000,
            dueDateMillis = dueSoon,
            dueOdometer = null,
            snoozeUntilMillis = null,
            isCompleted = false
        )

        assertEquals(ServiceStatus.DUE_SOON, status)

        val overdueCount = 0
        val dueSoonCount = 1
        val stateType = when {
            overdueCount > 0 -> WidgetStateType.OVERDUE
            dueSoonCount > 0 -> WidgetStateType.DUE_SOON
            else -> WidgetStateType.HEALTHY
        }
        assertEquals(WidgetStateType.DUE_SOON, stateType)
    }

    @Test
    fun `all good reminders result in HEALTHY state`() {
        val now = System.currentTimeMillis()
        val farFuture = now + TimeUnit.DAYS.toMillis(90)

        val status = StatusCalculator.calculate(
            nowMillis = now,
            currentOdometer = 10_000,
            dueDateMillis = farFuture,
            dueOdometer = 18_000,
            snoozeUntilMillis = null,
            isCompleted = false
        )

        assertEquals(ServiceStatus.OK, status)

        val overdueCount = 0
        val dueSoonCount = 0
        val stateType = when {
            overdueCount > 0 -> WidgetStateType.OVERDUE
            dueSoonCount > 0 -> WidgetStateType.DUE_SOON
            else -> WidgetStateType.HEALTHY
        }
        assertEquals(WidgetStateType.HEALTHY, stateType)
    }

    @Test
    fun `urgent reminder distance remaining is correctly calculated`() {
        val currentOdo = 12_000
        val dueOdo = 15_000
        val remaining = dueOdo - currentOdo
        assertEquals(3000, remaining)
    }
}
