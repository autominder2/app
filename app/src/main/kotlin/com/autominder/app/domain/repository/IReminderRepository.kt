package com.autominder.app.domain.repository

import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.ServiceType
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository for maintenance reminders.
 */
interface IReminderRepository {
    fun getActiveRemindersForVehicle(vehicleId: Long): Flow<List<Reminder>>
    fun getAllRemindersForVehicle(vehicleId: Long): Flow<List<Reminder>>
    fun getAllPendingReminders(): Flow<List<Reminder>>
    fun getReminderById(id: Long): Flow<Reminder?>
    suspend fun findActiveReminderByType(vehicleId: Long, serviceType: ServiceType): Reminder?
    suspend fun insertReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun snoozeReminder(id: Long, snoozeUntilMillis: Long)
    suspend fun markCompleted(id: Long)
    suspend fun deleteReminder(reminder: Reminder)
}
