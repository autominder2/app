package com.autominder.app.data.backup

import androidx.room.withTransaction
import com.autominder.app.core.di.IoDispatcher
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class AutoMinderBackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0.0",
    val vehicles: List<VehicleBackupDto> = emptyList(),
    val services: List<ServiceBackupDto> = emptyList(),
    val reminders: List<ReminderBackupDto> = emptyList(),
    val fuelEntries: List<FuelEntryBackupDto> = emptyList(),
    val mileageLogs: List<MileageLogBackupDto> = emptyList()
)

@Serializable
data class VehicleBackupDto(
    val id: Long,
    val make: String,
    val model: String,
    val year: Int,
    val plateNumber: String = "",
    val vin: String? = null,
    val currentOdometer: Int = 0,
    val photoUri: String? = null,
    val isArchived: Boolean = false,
    val notes: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
data class ServiceBackupDto(
    val id: Long,
    val vehicleId: Long,
    val serviceType: String,
    val customLabel: String? = null,
    val odometerAtService: Int,
    val serviceDate: Long,
    val costCents: Int? = null,
    val shopName: String? = null,
    val notes: String = "",
    val receiptPhotoUri: String? = null,
    val createdAt: Long = 0L
)

@Serializable
data class ReminderBackupDto(
    val id: Long,
    val vehicleId: Long,
    val serviceType: String,
    val customLabel: String? = null,
    val intervalDays: Int? = null,
    val intervalKm: Int? = null,
    val nextDueDate: Long? = null,
    val nextDueOdometer: Int? = null,
    val snoozeUntil: Long? = null,
    val notifyDaysBefore: Int = 7,
    val lastNotifiedAt: Long? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notes: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
data class FuelEntryBackupDto(
    val id: Long,
    val vehicleId: Long,
    val date: Long,
    val odometer: Int,
    val volumeMilliliters: Int,
    val costCents: Long,
    val notes: String = "",
    val createdAt: Long = 0L
)

@Serializable
data class MileageLogBackupDto(
    val id: Long,
    val vehicleId: Long,
    val odometer: Int,
    val loggedAt: Long,
    val notes: String? = null
)

data class BackupRestoreSummary(
    val vehiclesCount: Int,
    val servicesCount: Int,
    val remindersCount: Int,
    val fuelEntriesCount: Int,
    val mileageLogsCount: Int
)

/**
 * Handles JSON-based complete data export and atomic restore.
 * Grants users 100% data sovereignty to backup or migrate between devices
 * without any cloud or vendor lock-in.
 */
@Singleton
class ManualBackupManager @Inject constructor(
    private val database: AppDatabase,
    private val backupCoordinator: BackupCoordinator,
    private val vehicleDao: VehicleDao,
    private val serviceDao: ServiceDao,
    private val reminderDao: ReminderDao,
    private val fuelDao: FuelDao,
    private val mileageLogDao: MileageLogDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Flushes SQLite WAL and exports all records to [outputStream] as formatted JSON.
     */
    suspend fun exportBackup(outputStream: OutputStream): Result<Int> = withContext(ioDispatcher) {
        runCatching {
            // 1. Flush WAL so everything is in the main database
            backupCoordinator.checkpoint()

            // 2. Read all entities
            val vehicles = vehicleDao.getAllVehiclesOnce().map { it.toBackupDto() }
            val services = serviceDao.getAllServicesOnce().map { it.toBackupDto() }
            val reminders = reminderDao.getAllRemindersOnce().map { it.toBackupDto() }
            val fuelEntries = fuelDao.getAllFuelEntriesOnce().map { it.toBackupDto() }
            val mileageLogs = mileageLogDao.getAllLogsOnce().map { it.toBackupDto() }

            val backup = AutoMinderBackupData(
                version = 1,
                exportedAt = System.currentTimeMillis(),
                vehicles = vehicles,
                services = services,
                reminders = reminders,
                fuelEntries = fuelEntries,
                mileageLogs = mileageLogs
            )

            val jsonString = json.encodeToString(backup)
            outputStream.use { out ->
                out.write(jsonString.toByteArray(Charsets.UTF_8))
                out.flush()
            }

            vehicles.size + services.size + reminders.size + fuelEntries.size + mileageLogs.size
        }.onFailure { e ->
            Timber.e(e, "Failed to export manual backup")
        }
    }

    /**
     * Parses JSON from [inputStream] and restores all records atomically within a Room transaction.
     */
    suspend fun importBackup(inputStream: InputStream): Result<BackupRestoreSummary> = withContext(ioDispatcher) {
        runCatching {
            val jsonString = inputStream.use { it.bufferedReader(Charsets.UTF_8).readText() }
            val backup = json.decodeFromString<AutoMinderBackupData>(jsonString)

            database.withTransaction {
                // Insert vehicles first (FK root)
                val vehicleEntities = backup.vehicles.map { it.toEntity() }
                if (vehicleEntities.isNotEmpty()) {
                    vehicleDao.insertVehicles(vehicleEntities)
                }

                // Insert services
                val serviceEntities = backup.services.map { it.toEntity() }
                if (serviceEntities.isNotEmpty()) {
                    serviceDao.insertServices(serviceEntities)
                }

                // Insert reminders
                val reminderEntities = backup.reminders.map { it.toEntity() }
                if (reminderEntities.isNotEmpty()) {
                    reminderDao.insertReminders(reminderEntities)
                }

                // Insert fuel entries
                val fuelEntities = backup.fuelEntries.map { it.toEntity() }
                if (fuelEntities.isNotEmpty()) {
                    fuelDao.insertFuelEntries(fuelEntities)
                }

                // Insert mileage logs
                val mileageEntities = backup.mileageLogs.map { it.toEntity() }
                if (mileageEntities.isNotEmpty()) {
                    mileageLogDao.insertLogs(mileageEntities)
                }
            }

            BackupRestoreSummary(
                vehiclesCount = backup.vehicles.size,
                servicesCount = backup.services.size,
                remindersCount = backup.reminders.size,
                fuelEntriesCount = backup.fuelEntries.size,
                mileageLogsCount = backup.mileageLogs.size
            )
        }.onFailure { e ->
            Timber.e(e, "Failed to import manual backup")
        }
    }

    // ─── DTO ↔ Entity Mappers ───────────────────────────────────────────────

    private fun VehicleEntity.toBackupDto() = VehicleBackupDto(
        id = id,
        make = make,
        model = model,
        year = year,
        plateNumber = plateNumber,
        vin = vin,
        currentOdometer = currentOdometer,
        photoUri = photoUri,
        isArchived = isArchived,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun VehicleBackupDto.toEntity() = VehicleEntity(
        id = id,
        make = make,
        model = model,
        year = year,
        plateNumber = plateNumber,
        vin = vin,
        currentOdometer = currentOdometer,
        photoUri = photoUri,
        isArchived = isArchived,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun ServiceEntity.toBackupDto() = ServiceBackupDto(
        id = id,
        vehicleId = vehicleId,
        serviceType = serviceType.name,
        customLabel = customLabel,
        odometerAtService = odometerAtService,
        serviceDate = serviceDate,
        costCents = costCents,
        shopName = shopName,
        notes = notes,
        receiptPhotoUri = receiptPhotoUri,
        createdAt = createdAt
    )

    private fun ServiceBackupDto.toEntity() = ServiceEntity(
        id = id,
        vehicleId = vehicleId,
        serviceType = runCatching { ServiceType.valueOf(serviceType) }.getOrDefault(ServiceType.CUSTOM),
        customLabel = customLabel,
        odometerAtService = odometerAtService,
        serviceDate = serviceDate,
        costCents = costCents,
        shopName = shopName,
        notes = notes,
        receiptPhotoUri = receiptPhotoUri,
        createdAt = createdAt
    )

    private fun ReminderEntity.toBackupDto() = ReminderBackupDto(
        id = id,
        vehicleId = vehicleId,
        serviceType = serviceType.name,
        customLabel = customLabel,
        intervalDays = intervalDays,
        intervalKm = intervalKm,
        nextDueDate = nextDueDate,
        nextDueOdometer = nextDueOdometer,
        snoozeUntil = snoozeUntil,
        notifyDaysBefore = notifyDaysBefore,
        lastNotifiedAt = lastNotifiedAt,
        isCompleted = isCompleted,
        completedAt = completedAt,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun ReminderBackupDto.toEntity() = ReminderEntity(
        id = id,
        vehicleId = vehicleId,
        serviceType = runCatching { ServiceType.valueOf(serviceType) }.getOrDefault(ServiceType.CUSTOM),
        customLabel = customLabel,
        intervalDays = intervalDays,
        intervalKm = intervalKm,
        nextDueDate = nextDueDate,
        nextDueOdometer = nextDueOdometer,
        snoozeUntil = snoozeUntil,
        notifyDaysBefore = notifyDaysBefore,
        lastNotifiedAt = lastNotifiedAt,
        isCompleted = isCompleted,
        completedAt = completedAt,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun FuelEntryEntity.toBackupDto() = FuelEntryBackupDto(
        id = id,
        vehicleId = vehicleId,
        date = date,
        odometer = odometer,
        volumeMilliliters = volumeMilliliters,
        costCents = costCents,
        notes = notes,
        createdAt = createdAt
    )

    private fun FuelEntryBackupDto.toEntity() = FuelEntryEntity(
        id = id,
        vehicleId = vehicleId,
        date = date,
        odometer = odometer,
        volumeMilliliters = volumeMilliliters,
        costCents = costCents,
        notes = notes,
        createdAt = createdAt
    )

    private fun MileageLogEntity.toBackupDto() = MileageLogBackupDto(
        id = id,
        vehicleId = vehicleId,
        odometer = odometer,
        loggedAt = loggedAt,
        notes = notes
    )

    private fun MileageLogBackupDto.toEntity() = MileageLogEntity(
        id = id,
        vehicleId = vehicleId,
        odometer = odometer,
        loggedAt = loggedAt,
        notes = notes
    )
}
