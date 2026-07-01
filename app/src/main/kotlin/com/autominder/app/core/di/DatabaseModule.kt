package com.autominder.app.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
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
 * Hilt module for database dependencies.
 * Implements professional-grade health management.
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
            // WAL improves read/write concurrency. Set it through the builder —
            // NOT via execSQL("PRAGMA journal_mode = WAL"), which returns a row
            // and makes execSQL throw "Queries can be performed using
            // SQLiteDatabase query or rawQuery methods only" on every open.
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
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
