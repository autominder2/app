package com.autominder.app.data.repository

import androidx.room.withTransaction
import com.autominder.app.data.local.dao.ReminderDao
import com.autominder.app.data.local.dao.ServiceDao
import com.autominder.app.data.local.dao.VehicleDao
import com.autominder.app.data.local.database.AppDatabase
import com.autominder.app.data.local.entity.ReminderEntity
import com.autominder.app.data.local.entity.ServiceEntity
import com.autominder.app.data.local.entity.VehicleEntity
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceCompletion
import com.autominder.app.domain.model.ServiceCompletionResult
import com.autominder.app.domain.model.ServiceType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Behaviour of the single transaction owner for "a service was completed".
 *
 * These tests prove the *contract* — what is written, what is deliberately not
 * written, and in which order. They deliberately do NOT claim to prove SQLite
 * rollback: [AppDatabase.withTransaction] is stubbed here to run the block
 * inline. Real commit/rollback behaviour is covered by the instrumented test
 * `ServiceCompletionTransactionTest`, which runs against an in-memory database.
 */
class ServiceRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var serviceDao: ServiceDao
    private lateinit var vehicleDao: VehicleDao
    private lateinit var reminderDao: ReminderDao
    private lateinit var repository: ServiceRepositoryImpl

    private val vehicleId = 7L
    private val now = 1_700_000_000_000L
    private val serviceDate = 1_699_000_000_000L
    private val newServiceId = 42L

    @Before
    fun setUp() {
        db = mockk()
        serviceDao = mockk(relaxed = true)
        vehicleDao = mockk(relaxed = true)
        reminderDao = mockk(relaxed = true)

        // Run the transaction body inline so this test exercises the real
        // sequencing logic rather than Room's engine.
        mockkStatic("androidx.room.RoomDatabaseKt")
        val block = slot<suspend () -> ServiceCompletionResult>()
        coEvery { db.withTransaction(capture(block)) } coAnswers { block.captured.invoke() }

        coEvery { serviceDao.insertService(any()) } returns newServiceId
        coEvery { vehicleDao.getVehicleByIdOnce(vehicleId) } returns vehicleEntity(currentOdometer = 201_430)
        coEvery { reminderDao.findActiveReminderByType(any(), any()) } returns null

        repository = ServiceRepositoryImpl(db, serviceDao, vehicleDao, reminderDao)
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    private fun vehicleEntity(currentOdometer: Int) = VehicleEntity(
        id = vehicleId,
        make = "Honda",
        model = "Civic",
        year = 2020,
        plateNumber = "ABC-123",
        currentOdometer = currentOdometer
    )

    private fun service(odometerKm: Int) = Service(
        id = 0,
        vehicleId = vehicleId,
        serviceType = ServiceType.OIL_CHANGE,
        odometerAtService = odometerKm,
        serviceDate = serviceDate,
        createdAt = now
    )

    private fun completion(
        odometerKm: Int = 205_000,
        remindNext: Boolean = true,
        intervalKm: Int? = 10_000,
        intervalDays: Int? = 180
    ) = ServiceCompletion(
        service = service(odometerKm),
        remindNext = remindNext,
        reminderIntervalKm = intervalKm,
        reminderIntervalDays = intervalDays
    )

    private fun existingReminder(
        intervalKm: Int? = 10_000,
        intervalDays: Int? = 180
    ) = ReminderEntity(
        id = 3,
        vehicleId = vehicleId,
        serviceType = ServiceType.OIL_CHANGE,
        intervalDays = intervalDays,
        intervalKm = intervalKm,
        nextDueDate = serviceDate - 1_000L,
        nextDueOdometer = 195_000,
        snoozeUntil = now + 86_400_000L,
        lastNotifiedAt = now - 3_600_000L,
        createdAt = now - 100,
        updatedAt = now - 100
    )

    // ---------------------------------------------------------------- A. normal

    @Test
    fun `normal completion stores the service and advances the odometer`() = runTest {
        val result = repository.completeService(completion(odometerKm = 205_000))

        assertEquals(ServiceCompletionResult.Success(newServiceId), result)
        // Existence is checked before anything is written, and the odometer follows
        // the service row — not the other way round.
        coVerifyOrder {
            vehicleDao.getVehicleByIdOnce(vehicleId)
            serviceDao.insertService(match<ServiceEntity> { it.odometerAtService == 205_000 })
            vehicleDao.updateOdometerIfHigher(vehicleId, 205_000, now)
        }
        // Conditional update: SQL refuses to lower the stored reading.
        coVerify(exactly = 1) { vehicleDao.updateOdometerIfHigher(vehicleId, 205_000, now) }
        coVerify(exactly = 0) { vehicleDao.updateOdometer(any(), any(), any()) }
    }

    @Test
    fun `matching reminder is rebased from the completed service and its snooze cleared`() = runTest {
        coEvery { reminderDao.findActiveReminderByType(vehicleId, ServiceType.OIL_CHANGE) } returns existingReminder()

        repository.completeService(completion(odometerKm = 205_000, intervalKm = 10_000, intervalDays = 180))

        val saved = slot<ReminderEntity>()
        coVerify(exactly = 1) { reminderDao.updateReminder(capture(saved)) }
        with(saved.captured) {
            assertEquals(3L, id)
            // Rebased from the SERVICE, not from "now".
            assertEquals(215_000, nextDueOdometer)
            assertEquals(serviceDate + 180L * 86_400_000L, nextDueDate)
            assertNull(snoozeUntil)
            assertNull(lastNotifiedAt)
            assertNull(completedAt)
            assertEquals(false, isCompleted)
            assertEquals(now, updatedAt)
        }
        coVerify(exactly = 0) { reminderDao.insertReminder(any()) }
    }

    @Test
    fun `reminder keeps its own recurrence when the user turned remind-next off`() = runTest {
        coEvery { reminderDao.findActiveReminderByType(vehicleId, ServiceType.OIL_CHANGE) } returns
            existingReminder(intervalKm = 15_000, intervalDays = 365)

        repository.completeService(
            completion(odometerKm = 205_000, remindNext = false, intervalKm = null, intervalDays = null)
        )

        val saved = slot<ReminderEntity>()
        coVerify(exactly = 1) { reminderDao.updateReminder(capture(saved)) }
        assertEquals(15_000, saved.captured.intervalKm)
        assertEquals(365, saved.captured.intervalDays)
        assertEquals(220_000, saved.captured.nextDueOdometer)
    }

    // ------------------------------------------------------------ B. historical

    @Test
    fun `historical service keeps its own odometer and never lowers the vehicle`() = runTest {
        // Vehicle currently reads 201,430 km; the user logs a 180,000 km oil change.
        val result = repository.completeService(completion(odometerKm = 180_000))

        assertTrue(result is ServiceCompletionResult.Success)
        coVerify(exactly = 1) {
            serviceDao.insertService(match<ServiceEntity> { it.odometerAtService == 180_000 })
        }
        // The conditional update is still issued, but its WHERE clause is what
        // guarantees 201,430 survives — no unconditional write is ever made.
        coVerify(exactly = 1) { vehicleDao.updateOdometerIfHigher(vehicleId, 180_000, now) }
        coVerify(exactly = 0) { vehicleDao.updateOdometer(any(), any(), any()) }
        coVerify(exactly = 0) { vehicleDao.updateVehicle(any()) }
    }

    @Test
    fun `historical service never drags an existing reminder backwards`() = runTest {
        // Next oil change is already booked for 205,000 km / a future date.
        coEvery { reminderDao.findActiveReminderByType(vehicleId, ServiceType.OIL_CHANGE) } returns
            existingReminder().copy(
                nextDueOdometer = 205_000,
                nextDueDate = serviceDate + 200L * 86_400_000L
            )

        // The user logs an oil change they had done at 180,000 km, two years ago.
        val result = repository.completeService(completion(odometerKm = 180_000))

        assertTrue(result is ServiceCompletionResult.Success)
        coVerify(exactly = 1) { serviceDao.insertService(any()) }
        // Rebasing to 190,000 km would flip a healthy reminder to OVERDUE and wipe
        // the user's snooze. History is recorded; the reminder is left alone.
        coVerify(exactly = 0) { reminderDao.updateReminder(any()) }
        coVerify(exactly = 0) { reminderDao.insertReminder(any()) }
    }

    @Test
    fun `a service newer than the reminder's due point still rebases it`() = runTest {
        coEvery { reminderDao.findActiveReminderByType(vehicleId, ServiceType.OIL_CHANGE) } returns
            existingReminder().copy(
                nextDueOdometer = 205_000,
                nextDueDate = serviceDate - 1_000L
            )

        repository.completeService(completion(odometerKm = 205_000))

        val saved = slot<ReminderEntity>()
        coVerify(exactly = 1) { reminderDao.updateReminder(capture(saved)) }
        assertEquals(215_000, saved.captured.nextDueOdometer)
    }

    // --------------------------------------------------------- C. missing vehicle

    @Test
    fun `missing vehicle writes nothing at all`() = runTest {
        coEvery { vehicleDao.getVehicleByIdOnce(vehicleId) } returns null

        val result = repository.completeService(completion())

        assertEquals(ServiceCompletionResult.VehicleNotFound, result)
        coVerify(exactly = 0) { serviceDao.insertService(any()) }
        coVerify(exactly = 0) { vehicleDao.updateOdometerIfHigher(any(), any(), any()) }
        coVerify(exactly = 0) { vehicleDao.updateOdometer(any(), any(), any()) }
        coVerify(exactly = 0) { reminderDao.updateReminder(any()) }
        coVerify(exactly = 0) { reminderDao.insertReminder(any()) }
    }

    // ------------------------------------------------------ D. no matching reminder

    @Test
    fun `no matching reminder and no request leaves every reminder untouched`() = runTest {
        val result = repository.completeService(
            completion(remindNext = false, intervalKm = null, intervalDays = null)
        )

        assertTrue(result is ServiceCompletionResult.Success)
        coVerify(exactly = 1) { serviceDao.insertService(any()) }
        coVerify(exactly = 0) { reminderDao.insertReminder(any()) }
        coVerify(exactly = 0) { reminderDao.updateReminder(any()) }
        coVerify(exactly = 0) { reminderDao.markCompleted(any(), any()) }
    }

    // ------------------------------------------------------------ E. remind next

    @Test
    fun `a new reminder is created only when the user asked for one`() = runTest {
        repository.completeService(completion(odometerKm = 205_000, remindNext = true, intervalKm = 10_000, intervalDays = 180))

        val created = slot<ReminderEntity>()
        coVerify(exactly = 1) { reminderDao.insertReminder(capture(created)) }
        with(created.captured) {
            assertEquals(this@ServiceRepositoryImplTest.vehicleId, vehicleId)
            assertEquals(215_000, nextDueOdometer)
            assertEquals(serviceDate + 180L * 86_400_000L, nextDueDate)
            assertEquals(now, createdAt)
        }
    }

    @Test
    fun `remind-next on with no usable interval creates nothing`() = runTest {
        repository.completeService(completion(remindNext = true, intervalKm = null, intervalDays = null))

        coVerify(exactly = 0) { reminderDao.insertReminder(any()) }
    }

    // ---------------------------------------------------------------- failure

    @Test
    fun `a persistence failure is reported as Failed rather than thrown`() = runTest {
        coEvery { serviceDao.insertService(any()) } throws RuntimeException("disk full")

        val result = repository.completeService(completion())

        assertTrue(result is ServiceCompletionResult.Failed)
        assertEquals("disk full", (result as ServiceCompletionResult.Failed).cause.message)
    }
}
