package com.autominder.app.data.repository

import com.autominder.app.data.local.dao.ReminderDao
import com.autominder.app.data.mapper.toDomain
import com.autominder.app.data.mapper.toEntity
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.repository.IReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : IReminderRepository {

    override fun getActiveRemindersForVehicle(vehicleId: Long): Flow<List<Reminder>> {
        return reminderDao.getActiveRemindersForVehicle(vehicleId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllRemindersForVehicle(vehicleId: Long): Flow<List<Reminder>> {
        return reminderDao.getAllRemindersForVehicle(vehicleId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllPendingReminders(): Flow<List<Reminder>> {
        return reminderDao.getAllPendingReminders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getReminderById(id: Long): Flow<Reminder?> {
        return reminderDao.getReminderById(id).map { it?.toDomain() }
    }

    override suspend fun findActiveReminderByType(vehicleId: Long, serviceType: ServiceType): Reminder? {
        return reminderDao.findActiveReminderByType(vehicleId, serviceType)?.toDomain()
    }

    override suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder.toEntity())
    }

    override suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder.toEntity())
    }

    override suspend fun snoozeReminder(id: Long, snoozeUntilMillis: Long) {
        reminderDao.snoozeReminder(id, snoozeUntilMillis)
    }

    override suspend fun markCompleted(id: Long) {
        reminderDao.markCompleted(id)
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder.toEntity())
    }
}
