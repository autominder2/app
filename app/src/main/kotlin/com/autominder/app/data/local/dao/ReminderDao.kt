package com.autominder.app.data.local.dao

import androidx.room.*
import com.autominder.app.data.local.entity.ReminderEntity
import com.autominder.app.domain.model.ServiceType
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE vehicleId = :vehicleId AND isCompleted = 0 ORDER BY nextDueDate ASC")
    fun getActiveRemindersForVehicle(vehicleId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE vehicleId = :vehicleId ORDER BY createdAt DESC")
    fun getAllRemindersForVehicle(vehicleId: Long): Flow<List<ReminderEntity>>

    /**
     * All overdue/due-soon reminders across ALL vehicles — used by ReminderCheckWorker
     * and Dashboard summary card.
     */
    @Query("""
        SELECT * FROM reminders
        WHERE isCompleted = 0
          AND (nextDueDate IS NOT NULL OR nextDueOdometer IS NOT NULL)
        ORDER BY nextDueDate ASC
    """)
    fun getAllPendingReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getReminderById(id: Long): Flow<ReminderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isCompleted = 1, completedAt = :nowMillis, updatedAt = :nowMillis WHERE id = :id")
    suspend fun markCompleted(id: Long, nowMillis: Long = System.currentTimeMillis())

    @Query("UPDATE reminders SET snoozeUntil = :snoozeUntilMillis, updatedAt = :nowMillis WHERE id = :id")
    suspend fun snoozeReminder(id: Long, snoozeUntilMillis: Long, nowMillis: Long = System.currentTimeMillis())

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND (nextDueDate IS NOT NULL OR nextDueOdometer IS NOT NULL)")
    suspend fun getAllPendingRemindersOnce(): List<ReminderEntity>

    @Query("UPDATE reminders SET lastNotifiedAt = :nowMillis WHERE id = :id")
    suspend fun updateLastNotifiedAt(id: Long, nowMillis: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    /**
     * Returns the single most urgent pending reminder across all vehicles,
     * ordered by overdue first, then soonest due date.
     * Used by the home screen widget.
     */
    @Query("""
        SELECT * FROM reminders
        WHERE isCompleted = 0
          AND (snoozeUntil IS NULL OR snoozeUntil < :now)
        ORDER BY
            CASE WHEN nextDueDate IS NOT NULL AND nextDueDate < :now THEN 0 ELSE 1 END,
            nextDueDate ASC
        LIMIT 1
    """)
    suspend fun getMostUrgentReminder(now: Long = System.currentTimeMillis()): ReminderEntity?

    @Query("""
        SELECT * FROM reminders
        WHERE vehicleId = :vehicleId
          AND serviceType = :serviceType
          AND isCompleted = 0
        LIMIT 1
    """)
    suspend fun findActiveReminderByType(vehicleId: Long, serviceType: ServiceType): ReminderEntity?

    @Query("""
        SELECT COUNT(*) FROM reminders
        WHERE isCompleted = 0
          AND nextDueDate IS NOT NULL
          AND nextDueDate < :now
          AND (snoozeUntil IS NULL OR snoozeUntil < :now)
    """)
    suspend fun getOverdueCount(now: Long = System.currentTimeMillis()): Int

    @Query("""
        SELECT COUNT(*) FROM reminders
        WHERE isCompleted = 0
          AND nextDueDate IS NOT NULL
          AND nextDueDate >= :now
          AND nextDueDate < :soon
          AND (snoozeUntil IS NULL OR snoozeUntil < :now)
    """)
    suspend fun getDueSoonCount(
        now: Long = System.currentTimeMillis(),
        soon: Long = System.currentTimeMillis() + 7L * 86_400_000L
    ): Int
}
