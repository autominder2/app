package com.autominder.app.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.autominder.app.data.local.dao.ReminderDao
import com.autominder.app.data.local.dao.ServiceDao
import com.autominder.app.data.local.dao.VehicleDao
import com.autominder.app.data.local.database.AppDatabase
import com.autominder.app.data.local.entity.ReminderEntity
import com.autominder.app.data.local.entity.VehicleEntity
import com.autominder.app.data.repository.ServiceRepositoryImpl
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceCompletion
import com.autominder.app.domain.model.ServiceCompletionResult
import com.autominder.app.domain.model.ServiceType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Real-database proof that completing a service is one atomic operation.
 *
 * The unit tests stub [androidx.room.withTransaction] and can therefore only
 * prove call sequencing. This one runs against an in-memory [AppDatabase], so
 * commit and rollback are SQLite's, not a mock's.
 *
 * Rollback is induced the way it would actually happen in production — a
 * constraint violation on a later write in the same transaction — rather than
 * through any test-only hook in production code.
 */
@RunWith(AndroidJUnit4::class)
class ServiceCompletionTransactionTest {

    private lateinit var db: AppDatabase
    private lateinit var serviceDao: ServiceDao
    private lateinit var vehicleDao: VehicleDao
    private lateinit var reminderDao: ReminderDao
    private lateinit var repository: ServiceRepositoryImpl

    private val now = 1_700_000_000_000L
    private val serviceDate = 1_699_000_000_000L
    private var vehicleId = 0L

    @Before
    fun setUp() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .build()
        serviceDao = db.serviceDao()
        vehicleDao = db.vehicleDao()
        reminderDao = db.reminderDao()
        repository = ServiceRepositoryImpl(db, serviceDao, vehicleDao, reminderDao)

        vehicleId = vehicleDao.insertVehicle(
            VehicleEntity(
                make = "Honda",
                model = "Civic",
                year = 2020,
                plateNumber = "ABC-123",
                currentOdometer = 201_430
            )
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun completion(
        odometerKm: Int,
        remindNext: Boolean = true,
        intervalKm: Int? = 10_000,
        intervalDays: Int? = 180,
        vehicle: Long = vehicleId
    ) = ServiceCompletion(
        service = Service(
            vehicleId = vehicle,
            serviceType = ServiceType.OIL_CHANGE,
            odometerAtService = odometerKm,
            serviceDate = serviceDate,
            createdAt = now
        ),
        remindNext = remindNext,
        reminderIntervalKm = intervalKm,
        reminderIntervalDays = intervalDays
    )

    private suspend fun currentOdometer(): Int =
        vehicleDao.getVehicleByIdOnce(vehicleId)!!.currentOdometer

    private suspend fun services() = serviceDao.getServicesForVehicle(vehicleId).first()

    @Test
    fun newerServiceAdvancesTheOdometerAndCreatesTheReminder() = runBlocking {
        val result = repository.completeService(completion(odometerKm = 205_000))

        assertTrue(result is ServiceCompletionResult.Success)
        assertEquals(1, services().size)
        assertEquals(205_000, services().first().odometerAtService)
        assertEquals(205_000, currentOdometer())

        val reminder = reminderDao.findActiveReminderByType(vehicleId, ServiceType.OIL_CHANGE)!!
        assertEquals(215_000, reminder.nextDueOdometer)
        assertEquals(serviceDate + 180L * 86_400_000L, reminder.nextDueDate)
    }

    @Test
    fun historicalServiceIsStoredAndLeavesTheCurrentOdometerAlone() = runBlocking {
        val result = repository.completeService(completion(odometerKm = 180_000))

        assertTrue(result is ServiceCompletionResult.Success)
        assertEquals(180_000, services().single().odometerAtService)
        // The evidence of the 180,000 km service survives; the vehicle still reads 201,430.
        assertEquals(201_430, currentOdometer())
    }

    @Test
    fun existingReminderIsRebasedAndItsSnoozeCleared() = runBlocking {
        reminderDao.insertReminder(
            ReminderEntity(
                vehicleId = vehicleId,
                serviceType = ServiceType.OIL_CHANGE,
                intervalDays = 180,
                intervalKm = 10_000,
                nextDueDate = serviceDate - 1_000L,
                nextDueOdometer = 195_000,
                snoozeUntil = now + 86_400_000L,
                lastNotifiedAt = now - 3_600_000L
            )
        )

        repository.completeService(completion(odometerKm = 205_000))

        val reminder = reminderDao.findActiveReminderByType(vehicleId, ServiceType.OIL_CHANGE)!!
        assertEquals(215_000, reminder.nextDueOdometer)
        assertEquals(serviceDate + 180L * 86_400_000L, reminder.nextDueDate)
        assertNull(reminder.snoozeUntil)
        assertNull(reminder.lastNotifiedAt)
        // Rebased, not duplicated.
        assertEquals(1, reminderDao.getAllRemindersForVehicle(vehicleId).first().size)
    }

    /**
     * The case the historical-maintenance capability makes reachable: logging old
     * history must not destroy a healthy future reminder or the user's snooze.
     */
    @Test
    fun historicalServiceLeavesAHealthyReminderUntouched() = runBlocking {
        val snoozeUntil = now + 86_400_000L
        reminderDao.insertReminder(
            ReminderEntity(
                vehicleId = vehicleId,
                serviceType = ServiceType.OIL_CHANGE,
                intervalDays = 180,
                intervalKm = 10_000,
                nextDueDate = serviceDate + 200L * 86_400_000L,
                nextDueOdometer = 211_430,
                snoozeUntil = snoozeUntil
            )
        )

        val result = repository.completeService(completion(odometerKm = 180_000))

        assertTrue(result is ServiceCompletionResult.Success)
        assertEquals(180_000, services().single().odometerAtService)

        val reminder = reminderDao.findActiveReminderByType(vehicleId, ServiceType.OIL_CHANGE)!!
        assertEquals(211_430, reminder.nextDueOdometer)
        assertEquals(serviceDate + 200L * 86_400_000L, reminder.nextDueDate)
        assertEquals(snoozeUntil, reminder.snoozeUntil)
    }

    @Test
    fun missingVehicleWritesNothing() = runBlocking {
        val absentVehicleId = vehicleId + 9_999

        val result = repository.completeService(completion(odometerKm = 205_000, vehicle = absentVehicleId))

        assertEquals(ServiceCompletionResult.VehicleNotFound, result)
        assertTrue(serviceDao.getAllServices().first().isEmpty())
        assertEquals(201_430, currentOdometer())
        assertTrue(reminderDao.getAllRemindersForVehicle(vehicleId).first().isEmpty())
    }

    /**
     * The rollback case.
     *
     * The reminder step is made to fail at the SQLite level, but only AFTER the
     * service insert and the odometer update have already executed inside the same
     * transaction. If those two writes were not covered by one transaction they
     * would survive this failure — which is precisely the bug this slice removes.
     */
    @Test
    fun aLaterFailureRollsBackTheServiceAndTheOdometer() = runBlocking {
        val poisoned = ServiceCompletion(
            service = Service(
                vehicleId = vehicleId,
                serviceType = ServiceType.OIL_CHANGE,
                odometerAtService = 205_000,
                serviceDate = serviceDate,
                createdAt = now
            ),
            remindNext = true,
            reminderIntervalKm = 10_000,
            reminderIntervalDays = 180
        )

        // Drop the reminders table so the transaction's last step throws while the
        // service row and the odometer update are still pending.
        db.openHelper.writableDatabase.execSQL("DROP TABLE reminders")

        val result = repository.completeService(poisoned)

        assertTrue("expected a rolled-back failure, got $result", result is ServiceCompletionResult.Failed)
        assertTrue("service row survived a rolled-back transaction", services().isEmpty())
        assertEquals("odometer survived a rolled-back transaction", 201_430, currentOdometer())
    }
}
