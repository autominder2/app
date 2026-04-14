package com.autominder.app.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.autominder.app.data.local.dao.*
import com.autominder.app.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module — PRD Section 7.4.
 *
 * RULES:
 * • NEVER use fallbackToDestructiveMigration()
 * • NEVER use allowMainThreadQueries()
 * • addMigrations() will be added here when version bumps occur (P3+)
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `fuel_entries` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `vehicleId` INTEGER NOT NULL, 
                    `date` INTEGER NOT NULL, 
                    `odometer` INTEGER NOT NULL, 
                    `volumeMilliliters` INTEGER NOT NULL, 
                    `costCents` INTEGER NOT NULL, 
                    `notes` TEXT NOT NULL, 
                    `createdAt` INTEGER NOT NULL, 
                    FOREIGN KEY(`vehicleId`) REFERENCES `vehicles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_fuel_entries_vehicleId` ON `fuel_entries` (`vehicleId`)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "autominder.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    @Singleton
    fun provideVehicleDao(db: AppDatabase): VehicleDao = db.vehicleDao()

    @Provides
    @Singleton
    fun provideReminderDao(db: AppDatabase): ReminderDao = db.reminderDao()

    @Provides
    @Singleton
    fun provideServiceDao(db: AppDatabase): ServiceDao = db.serviceDao()

    @Provides
    @Singleton
    fun provideMileageLogDao(db: AppDatabase): MileageLogDao = db.mileageLogDao()

    @Provides
    @Singleton
    fun provideFuelDao(db: AppDatabase): FuelDao = db.fuelDao()
}
