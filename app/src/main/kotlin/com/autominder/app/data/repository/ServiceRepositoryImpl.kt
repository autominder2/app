package com.autominder.app.data.repository

import androidx.room.withTransaction
import com.autominder.app.data.local.dao.ReminderDao
import com.autominder.app.data.local.dao.ServiceDao
import com.autominder.app.data.local.dao.VehicleDao
import com.autominder.app.data.local.database.AppDatabase
import com.autominder.app.data.mapper.toDomain
import com.autominder.app.data.mapper.toEntity
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceCompletion
import com.autominder.app.domain.model.ServiceCompletionResult
import com.autominder.app.domain.repository.IServiceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val serviceDao: ServiceDao,
    private val vehicleDao: VehicleDao,
    private val reminderDao: ReminderDao
) : IServiceRepository {

    override fun getAllServices(): Flow<List<Service>> {
        return serviceDao.getAllServices().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getServicesForVehicle(vehicleId: Long): Flow<List<Service>> {
        return serviceDao.getServicesForVehicle(vehicleId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getServiceById(id: Long): Flow<Service?> {
        return serviceDao.getServiceById(id).map { it?.toDomain() }
    }

    override suspend fun insertService(service: Service): Long {
        return serviceDao.insertService(service.toEntity())
    }

    /**
     * Sole transaction owner for "a service was completed".
     *
     * Service row, vehicle odometer and reminder rebase are three tables that must
     * agree. They are written inside one [AppDatabase.withTransaction] block, so a
     * failure at any point leaves none of them changed — the user never ends up with
     * a logged service whose reminder was not reset, or a reminder reset for a
     * service that was never stored.
     *
     * Historical maintenance is first-class: a service logged at 180,000 km on a
     * vehicle now reading 201,430 km keeps its own odometer evidence and leaves the
     * vehicle's current reading alone. [VehicleDao.updateOdometerIfHigher] enforces
     * that in SQL, so there is no read-then-write race.
     */
    override suspend fun completeService(completion: ServiceCompletion): ServiceCompletionResult {
        return try {
            db.withTransaction {
                val service = completion.service
                val vehicleId = service.vehicleId

                // Existence is checked BEFORE any write. If the vehicle was deleted
                // between screen load and Save, the operation ends here having
                // touched nothing at all.
                vehicleDao.getVehicleByIdOnce(vehicleId)
                    ?: return@withTransaction ServiceCompletionResult.VehicleNotFound

                val serviceId = serviceDao.insertService(service.toEntity())

                vehicleDao.updateOdometerIfHigher(
                    id = vehicleId,
                    odometer = service.odometerAtService,
                    nowMillis = service.createdAt
                )

                rebaseReminder(completion)

                ServiceCompletionResult.Success(serviceId)
            }
        } catch (e: CancellationException) {
            // Cancellation is not a persistence failure — it must keep propagating so
            // the caller's scope (and any withTimeout around it) still unwinds.
            throw e
        } catch (e: Exception) {
            // withTransaction never marks the transaction successful when the block
            // throws, so nothing above survives this catch.
            Timber.e(e, "Service completion rolled back")
            ServiceCompletionResult.Failed(e)
        }
    }

    /**
     * Resets the maintenance clock for the service that was just completed.
     *
     * An existing active reminder is rebased from the completed service (not from
     * "now"), keeping its recurrence rules and clearing the snooze/notification
     * state that belonged to the previous cycle. A new reminder is created only when
     * the user explicitly asked to be reminded for the next one.
     */
    private suspend fun rebaseReminder(completion: ServiceCompletion) {
        val service = completion.service
        val existing = reminderDao
            .findActiveReminderByType(service.vehicleId, service.serviceType)
            ?.toDomain()

        when {
            existing != null -> {
                // Interval edits are adopted only when the user left "remind me for
                // the next one" on; otherwise the reminder's own recurrence stands.
                val km = if (completion.remindNext) completion.reminderIntervalKm else existing.intervalKm
                val days = if (completion.remindNext) completion.reminderIntervalDays else existing.intervalDays
                val nextDueOdometer = km?.let { service.odometerAtService + it }
                val nextDueDate = days?.let { service.serviceDate + it.toLong() * MILLIS_PER_DAY }

                // Back-dated maintenance is recorded, but it does not reset the
                // clock. Logging a 180,000 km oil change on a vehicle whose next one
                // is already booked for 211,430 km must not drag that reminder into
                // the past, flip it to OVERDUE, and silently discard the user's
                // snooze. The service still stands as history; the reminder does not
                // move backwards.
                if (!movesForward(existing, nextDueOdometer, nextDueDate)) return

                reminderDao.updateReminder(
                    existing.copy(
                        intervalKm = km,
                        intervalDays = days,
                        nextDueOdometer = nextDueOdometer,
                        nextDueDate = nextDueDate,
                        isCompleted = false,
                        completedAt = null,
                        snoozeUntil = null,
                        lastNotifiedAt = null,
                        updatedAt = service.createdAt
                    ).toEntity()
                )
            }

            completion.remindNext &&
                (completion.reminderIntervalKm != null || completion.reminderIntervalDays != null) -> {
                reminderDao.insertReminder(
                    Reminder(
                        id = 0,
                        vehicleId = service.vehicleId,
                        serviceType = service.serviceType,
                        customLabel = service.customLabel,
                        intervalKm = completion.reminderIntervalKm,
                        intervalDays = completion.reminderIntervalDays,
                        nextDueOdometer = completion.reminderIntervalKm
                            ?.let { service.odometerAtService + it },
                        nextDueDate = completion.reminderIntervalDays
                            ?.let { service.serviceDate + it.toLong() * MILLIS_PER_DAY },
                        createdAt = service.createdAt,
                        updatedAt = service.createdAt
                    ).toEntity()
                )
            }
            // No matching reminder and no request for one: the service stands alone
            // and no unrelated reminder is touched.
        }
    }

    /**
     * True when rebasing would move the reminder's due point forward (or leave it
     * where it is). A candidate that is due *earlier* than what the reminder already
     * carries means the user logged older history, not the newest service.
     *
     * An axis the reminder does not currently use cannot move backwards, so it never
     * blocks the rebase on its own.
     */
    private fun movesForward(existing: Reminder, nextDueOdometer: Int?, nextDueDate: Long?): Boolean {
        val currentOdometer = existing.nextDueOdometer
        val currentDate = existing.nextDueDate
        val odometerOk = nextDueOdometer == null || currentOdometer == null || nextDueOdometer >= currentOdometer
        val dateOk = nextDueDate == null || currentDate == null || nextDueDate >= currentDate
        return odometerOk && dateOk
    }

    override suspend fun updateService(service: Service) {
        serviceDao.updateService(service.toEntity())
    }

    override suspend fun deleteService(service: Service) {
        serviceDao.deleteService(service.toEntity())
    }

    override fun getTotalCostForVehicle(vehicleId: Long): Flow<Int> {
        return serviceDao.getTotalCostForVehicle(vehicleId)
    }

    override fun getCostSince(vehicleId: Long, sinceMillis: Long): Flow<Int> {
        return serviceDao.getCostSince(vehicleId, sinceMillis)
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
    }
}
