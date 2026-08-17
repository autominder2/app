package com.autominder.app.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.autominder.app.data.local.dao.ReminderDao
import com.autominder.app.data.local.dao.VehicleDao
import com.autominder.app.data.local.entity.ReminderEntity
import com.autominder.app.data.local.entity.VehicleEntity
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.ServiceType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.autominder.app.core.notifications.NotificationHelper

/**
 * Regression tests for the reminder engine — the feature the whole product
 * exists to deliver, and which had **no** test coverage before 2026-08-16.
 *
 * These cover selection and cooldown logic plus the heartbeat contract. What
 * they deliberately do *not* cover is whether Android actually runs the worker:
 * Doze, App Standby and OEM process killers are device behaviour and can only
 * be verified on hardware. That gap is why the heartbeat exists at all.
 */
class ReminderCheckWorkerTest {

    private val reminderDao = mockk<ReminderDao>(relaxed = true)
    private val vehicleDao = mockk<VehicleDao>(relaxed = true)
    private val prefs = mockk<UserPreferences>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private val params = mockk<WorkerParameters>(relaxed = true)

    private val day = 24 * 60 * 60 * 1000L

    private fun vehicle(odometer: Int = 50_000) = VehicleEntity(
        id = 1L, make = "Suzuki", model = "Alto", year = 2018,
        plateNumber = "ABC-123", currentOdometer = odometer
    )

    private fun reminder(
        id: Long = 1L,
        dueDate: Long? = null,
        dueOdometer: Int? = null,
        lastNotifiedAt: Long? = null,
        snoozeUntil: Long? = null,
        isCompleted: Boolean = false
    ) = ReminderEntity(
        id = id, vehicleId = 1L, serviceType = ServiceType.OIL_CHANGE,
        nextDueDate = dueDate, nextDueOdometer = dueOdometer,
        lastNotifiedAt = lastNotifiedAt, snoozeUntil = snoozeUntil,
        isCompleted = isCompleted
    )

    private fun worker() = ReminderCheckWorker(context, params, reminderDao, vehicleDao, prefs)

    @Before
    fun setUp() {
        mockkObject(NotificationHelper)
        every { NotificationHelper.showReminderNotification(any(), any(), any(), any(), any()) } returns Unit
        every { context.applicationContext } returns context
        every { context.getString(any(), *anyVararg()) } returns "text"
        every { prefs.notificationsEnabled } returns flowOf(true)
        every { prefs.distanceUnit } returns flowOf("km")
        coEvery { vehicleDao.getVehicleByIdOnce(any()) } returns vehicle()
    }

    @After
    fun tearDown() = unmockkAll()

    @Test
    fun `an overdue reminder notifies and stamps lastNotifiedAt`() = runTest {
        val overdue = reminder(dueDate = System.currentTimeMillis() - 5 * day)
        coEvery { reminderDao.getAllPendingRemindersOnce() } returns listOf(overdue)

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 1) {
            NotificationHelper.showReminderNotification(any(), 1L, 1L, any(), any())
        }
        coVerify(exactly = 1) { reminderDao.updateLastNotifiedAt(1L, any()) }
    }

    @Test
    fun `an overdue reminder notified 2 hours ago stays silent - 24h cooldown`() = runTest {
        val now = System.currentTimeMillis()
        val recentlyNotified = reminder(
            dueDate = now - 5 * day,
            lastNotifiedAt = now - 2 * 60 * 60 * 1000L
        )
        coEvery { reminderDao.getAllPendingRemindersOnce() } returns listOf(recentlyNotified)

        worker().doWork()

        coVerify(exactly = 0) {
            NotificationHelper.showReminderNotification(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `an overdue reminder notified 30 hours ago notifies again`() = runTest {
        val now = System.currentTimeMillis()
        val stale = reminder(dueDate = now - 5 * day, lastNotifiedAt = now - 30 * 60 * 60 * 1000L)
        coEvery { reminderDao.getAllPendingRemindersOnce() } returns listOf(stale)

        worker().doWork()

        coVerify(exactly = 1) {
            NotificationHelper.showReminderNotification(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `a snoozed reminder that is not yet due stays silent`() = runTest {
        val now = System.currentTimeMillis()
        // Due in 30 days and snoozed for 3 — genuinely SNOOZED, so no notification.
        val snoozed = reminder(dueDate = now + 30 * day, snoozeUntil = now + 3 * day)
        coEvery { reminderDao.getAllPendingRemindersOnce() } returns listOf(snoozed)

        worker().doWork()

        coVerify(exactly = 0) {
            NotificationHelper.showReminderNotification(any(), any(), any(), any(), any())
        }
    }

    /**
     * Pins a deliberate product invariant that is easy to "fix" into a bug.
     *
     * `StatusCalculator` evaluates OVERDUE *before* SNOOZED — stated in the
     * algorithm itself ("PRD §7.1 invariant"), in `.claude/rules/data.md`
     * ("OVERDUE always beats SNOOZED") and in `DESIGN_SYSTEM_2026 §3`
     * ("OVERDUE ... cannot be snoozed"). A safety item that is already overdue
     * cannot be silenced by snoozing it.
     *
     * The first version of this test asserted the opposite and failed. The
     * expectation was wrong, not the code.
     */
    @Test
    fun `an overdue reminder still notifies even when snoozed - overdue beats snooze`() = runTest {
        val now = System.currentTimeMillis()
        val overdueAndSnoozed = reminder(dueDate = now - 5 * day, snoozeUntil = now + 3 * day)
        coEvery { reminderDao.getAllPendingRemindersOnce() } returns listOf(overdueAndSnoozed)

        worker().doWork()

        coVerify(exactly = 1) {
            NotificationHelper.showReminderNotification(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `a completed reminder never notifies`() = runTest {
        val done = reminder(dueDate = System.currentTimeMillis() - 5 * day, isCompleted = true)
        coEvery { reminderDao.getAllPendingRemindersOnce() } returns listOf(done)

        worker().doWork()

        coVerify(exactly = 0) {
            NotificationHelper.showReminderNotification(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `a reminder due far in the future never notifies`() = runTest {
        val future = reminder(dueDate = System.currentTimeMillis() + 90 * day)
        coEvery { reminderDao.getAllPendingRemindersOnce() } returns listOf(future)

        worker().doWork()

        coVerify(exactly = 0) {
            NotificationHelper.showReminderNotification(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `a reminder whose vehicle is missing is skipped, not crashed on`() = runTest {
        coEvery { vehicleDao.getVehicleByIdOnce(any()) } returns null
        coEvery { reminderDao.getAllPendingRemindersOnce() } returns
            listOf(reminder(dueDate = System.currentTimeMillis() - 5 * day))

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) {
            NotificationHelper.showReminderNotification(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `a clean pass records the heartbeat`() = runTest {
        coEvery { reminderDao.getAllPendingRemindersOnce() } returns emptyList()

        worker().doWork()

        coVerify(exactly = 1) { prefs.setLastSuccessfulCheckAt(any()) }
    }

    @Test
    fun `notifications disabled still records the heartbeat - the engine ran`() = runTest {
        every { prefs.notificationsEnabled } returns flowOf(false)

        worker().doWork()

        coVerify(exactly = 1) { prefs.setLastSuccessfulCheckAt(any()) }
        coVerify(exactly = 0) {
            NotificationHelper.showReminderNotification(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `a failing pass does NOT record the heartbeat - it must not look healthy`() = runTest {
        coEvery { reminderDao.getAllPendingRemindersOnce() } throws IllegalStateException("db gone")

        val result = worker().doWork()

        // Still success: retry-storming on a partial failure is worse than
        // waiting for the next scheduled run.
        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { prefs.setLastSuccessfulCheckAt(any()) }
    }
}
