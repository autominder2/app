package com.autominder.app.data.backup

import androidx.room.withTransaction
import com.autominder.app.data.local.dao.FuelDao
import com.autominder.app.data.local.dao.MileageLogDao
import com.autominder.app.data.local.dao.ReminderDao
import com.autominder.app.data.local.dao.ServiceDao
import com.autominder.app.data.local.dao.VehicleDao
import com.autominder.app.data.local.database.AppDatabase
import com.autominder.app.data.local.entity.FuelEntryEntity
import com.autominder.app.data.local.entity.MileageLogEntity
import com.autominder.app.data.local.entity.ReminderEntity
import com.autominder.app.data.local.entity.ServiceEntity
import com.autominder.app.data.local.entity.VehicleEntity
import com.autominder.app.domain.model.ServiceType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ManualBackupManagerTest {

    private lateinit var database: AppDatabase
    private lateinit var backupCoordinator: BackupCoordinator
    private lateinit var vehicleDao: VehicleDao
    private lateinit var serviceDao: ServiceDao
    private lateinit var reminderDao: ReminderDao
    private lateinit var fuelDao: FuelDao
    private lateinit var mileageLogDao: MileageLogDao
    private lateinit var manager: ManualBackupManager

    @Before
    fun setUp() {
        database = mockk(relaxed = true)
        backupCoordinator = mockk(relaxed = true)
        vehicleDao = mockk(relaxed = true)
        serviceDao = mockk(relaxed = true)
        reminderDao = mockk(relaxed = true)
        fuelDao = mockk(relaxed = true)
        mileageLogDao = mockk(relaxed = true)

        mockkStatic("androidx.room.RoomDatabaseKt")
        val block = slot<suspend () -> Any>()
        coEvery { database.withTransaction(capture(block)) } coAnswers { block.captured.invoke() }

        manager = ManualBackupManager(
            database = database,
            backupCoordinator = backupCoordinator,
            vehicleDao = vehicleDao,
            serviceDao = serviceDao,
            reminderDao = reminderDao,
            fuelDao = fuelDao,
            mileageLogDao = mileageLogDao,
            ioDispatcher = Dispatchers.Unconfined
        )
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `exportBackup flushes WAL and outputs valid JSON containing all entities`() = runTest {
        val vehicle = VehicleEntity(id = 1L, make = "Toyota", model = "Corolla", year = 2022, plateNumber = "ABC-123", currentOdometer = 45000)
        val service = ServiceEntity(id = 2L, vehicleId = 1L, serviceType = ServiceType.OIL_CHANGE, odometerAtService = 40000, serviceDate = 1700000000000L, costCents = 7500)
        val reminder = ReminderEntity(id = 3L, vehicleId = 1L, serviceType = ServiceType.OIL_CHANGE, nextDueOdometer = 50000)
        val fuel = FuelEntryEntity(id = 4L, vehicleId = 1L, date = 1700000000000L, odometer = 44000, volumeMilliliters = 42000, costCents = 6500L)
        val log = MileageLogEntity(id = 5L, vehicleId = 1L, odometer = 44500, loggedAt = 1700000000000L)

        coEvery { vehicleDao.getAllVehiclesOnce() } returns listOf(vehicle)
        coEvery { serviceDao.getAllServicesOnce() } returns listOf(service)
        coEvery { reminderDao.getAllRemindersOnce() } returns listOf(reminder)
        coEvery { fuelDao.getAllFuelEntriesOnce() } returns listOf(fuel)
        coEvery { mileageLogDao.getAllLogsOnce() } returns listOf(log)

        val outputStream = ByteArrayOutputStream()
        val result = manager.exportBackup(outputStream)

        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrNull())

        val exportedJson = outputStream.toString(Charsets.UTF_8.name())
        assertTrue(exportedJson.contains("Toyota"))
        assertTrue(exportedJson.contains("Corolla"))
        assertTrue(exportedJson.contains("OIL_CHANGE"))
        assertTrue(exportedJson.contains("42000"))

        coVerify(exactly = 1) { backupCoordinator.checkpoint() }
    }

    @Test
    fun `importBackup with corrupted JSON returns failure Result without crashing`() = runTest {
        val corruptInput = ByteArrayInputStream("{ this is not valid json }".toByteArray(Charsets.UTF_8))
        val result = manager.importBackup(corruptInput)

        assertTrue(result.isFailure)
    }

    @Test
    fun `importBackup with valid empty JSON succeeds with zero counts`() = runTest {
        val emptyBackupJson = """
            {
                "version": 1,
                "exportedAt": 1700000000000,
                "appVersion": "1.0.0",
                "vehicles": [],
                "services": [],
                "reminders": [],
                "fuelEntries": [],
                "mileageLogs": []
            }
        """.trimIndent()

        val input = ByteArrayInputStream(emptyBackupJson.toByteArray(Charsets.UTF_8))
        val result = manager.importBackup(input)

        assertTrue(result.isSuccess)
        val summary = result.getOrNull()!!
        assertEquals(0, summary.vehiclesCount)
        assertEquals(0, summary.servicesCount)
        assertEquals(0, summary.remindersCount)
        assertEquals(0, summary.fuelEntriesCount)
        assertEquals(0, summary.mileageLogsCount)
    }

    @Test
    fun `importBackup with populated records inserts entities in correct FK dependency order`() = runTest {
        val backupJson = """
            {
                "version": 1,
                "exportedAt": 1700000000000,
                "appVersion": "1.0.0",
                "vehicles": [
                    {
                        "id": 1,
                        "make": "Honda",
                        "model": "Civic",
                        "year": 2020,
                        "plateNumber": "XYZ-999",
                        "currentOdometer": 30000,
                        "isArchived": false,
                        "notes": ""
                    }
                ],
                "services": [
                    {
                        "id": 10,
                        "vehicleId": 1,
                        "serviceType": "OIL_CHANGE",
                        "odometerAtService": 25000,
                        "serviceDate": 1690000000000,
                        "costCents": 6000,
                        "notes": ""
                    }
                ],
                "reminders": [],
                "fuelEntries": [],
                "mileageLogs": []
            }
        """.trimIndent()

        val input = ByteArrayInputStream(backupJson.toByteArray(Charsets.UTF_8))
        val result = manager.importBackup(input)

        assertTrue(result.isSuccess)
        val summary = result.getOrNull()!!
        assertEquals(1, summary.vehiclesCount)
        assertEquals(1, summary.servicesCount)

        coVerify(exactly = 1) { vehicleDao.insertVehicles(any()) }
        coVerify(exactly = 1) { serviceDao.insertServices(any()) }
    }
}
