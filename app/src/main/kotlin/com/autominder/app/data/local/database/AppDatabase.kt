package com.autominder.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.autominder.app.data.local.dao.*
import com.autominder.app.data.local.entity.*

/**
 * Room database — PRD Section 7.3.
 *
 * RULES:
 * • version = 1 at P0 (increment on every schema change)
 * • exportSchema = true (stored in app/schemas/ for migration audit)
 * • NEVER use fallbackToDestructiveMigration() in ANY build variant
 */
@Database(
    entities = [
        VehicleEntity::class,
        ReminderEntity::class,
        ServiceEntity::class,
        MileageLogEntity::class,
        FuelEntryEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun reminderDao(): ReminderDao
    abstract fun serviceDao(): ServiceDao
    abstract fun mileageLogDao(): MileageLogDao
    abstract fun fuelDao(): FuelDao
}
