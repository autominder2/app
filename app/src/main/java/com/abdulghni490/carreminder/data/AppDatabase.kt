package com.abdulghni490.carreminder.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MaintenanceTask::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
