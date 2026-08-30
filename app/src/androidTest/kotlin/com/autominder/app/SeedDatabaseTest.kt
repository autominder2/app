package com.autominder.app

import androidx.datastore.preferences.core.edit
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.autominder.app.data.local.database.AppDatabase
import com.autominder.app.data.local.entity.FuelEntryEntity
import com.autominder.app.data.local.entity.MileageLogEntity
import com.autominder.app.data.local.entity.ReminderEntity
import com.autominder.app.data.local.entity.ServiceEntity
import com.autominder.app.data.local.entity.VehicleEntity
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.ServiceType
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SeedDatabaseTest {

    @Test
    fun seedRealisticData() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Open the real app database
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "autominder.db"
        ).build()

        val vehicleDao = db.vehicleDao()
        val reminderDao = db.reminderDao()
        val serviceDao = db.serviceDao()
        val fuelDao = db.fuelDao()
        val mileageLogDao = db.mileageLogDao()

        // Clear existing data safely
        vehicleDao.getAllVehicles().let {
            // Room cascades on vehicle deletion
        }
        
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        // Vehicle 1: 2022 Toyota Camry XSE
        val v1Id = vehicleDao.insertVehicle(
            VehicleEntity(
                id = 0L,
                make = "Toyota",
                model = "Camry XSE",
                year = 2022,
                plateNumber = "7XYZ890",
                vin = "4T1B11HK5NU123456",
                currentOdometer = 45280,
                photoUri = null,
                isArchived = false,
                notes = "Primary daily commuter - pristine condition",
                createdAt = now - 500 * dayMs,
                updatedAt = now
            )
        )

        // Vehicle 2: 2021 Ford F-150 Lariat
        val v2Id = vehicleDao.insertVehicle(
            VehicleEntity(
                id = 0L,
                make = "Ford",
                model = "F-150 Lariat",
                year = 2021,
                plateNumber = "TRK-4421",
                vin = "1FTFW1ED5MFA98765",
                currentOdometer = 62450,
                photoUri = null,
                isArchived = false,
                notes = "Family road trips & utility haul",
                createdAt = now - 700 * dayMs,
                updatedAt = now
            )
        )

        // Vehicle 3: 2020 Honda Civic Touring
        val v3Id = vehicleDao.insertVehicle(
            VehicleEntity(
                id = 0L,
                make = "Honda",
                model = "Civic Touring",
                year = 2020,
                plateNumber = "8KLP123",
                vin = "2HGFC2F76LH543210",
                currentOdometer = 38100,
                photoUri = null,
                isArchived = false,
                notes = "Secondary city car",
                createdAt = now - 900 * dayMs,
                updatedAt = now
            )
        )

        // Reminders for Vehicle 1
        reminderDao.insertReminder(
            ReminderEntity(
                vehicleId = v1Id,
                serviceType = ServiceType.OIL_CHANGE,
                intervalDays = 180,
                intervalKm = 8000,
                nextDueDate = now + 14 * dayMs,
                nextDueOdometer = 46000,
                notes = "0W-16 Synthetic Oil & OEM Filter",
                createdAt = now - 160 * dayMs,
                updatedAt = now
            )
        )

        reminderDao.insertReminder(
            ReminderEntity(
                vehicleId = v1Id,
                serviceType = ServiceType.CABIN_FILTER,
                intervalDays = 365,
                intervalKm = 20000,
                nextDueDate = now - 10 * dayMs,
                nextDueOdometer = 44000,
                notes = "Inspect for pollen & seasonal dust buildup",
                createdAt = now - 375 * dayMs,
                updatedAt = now
            )
        )

        reminderDao.insertReminder(
            ReminderEntity(
                vehicleId = v1Id,
                serviceType = ServiceType.TIRE_ROTATION,
                intervalDays = 180,
                intervalKm = 10000,
                nextDueDate = now + 60 * dayMs,
                nextDueOdometer = 48000,
                notes = "Rotate front to rear & check tread depth",
                createdAt = now - 160 * dayMs,
                updatedAt = now
            )
        )

        reminderDao.insertReminder(
            ReminderEntity(
                vehicleId = v1Id,
                serviceType = ServiceType.BRAKE_SERVICE,
                intervalDays = 730,
                intervalKm = 40000,
                nextDueDate = now + 240 * dayMs,
                nextDueOdometer = 60000,
                notes = "Front and rear pad & rotor inspection",
                createdAt = now - 300 * dayMs,
                updatedAt = now
            )
        )

        reminderDao.insertReminder(
            ReminderEntity(
                vehicleId = v1Id,
                serviceType = ServiceType.REGISTRATION,
                intervalDays = 365,
                intervalKm = null,
                nextDueDate = now + 85 * dayMs,
                nextDueOdometer = null,
                notes = "Annual DMV registration sticker renewal",
                createdAt = now - 280 * dayMs,
                updatedAt = now
            )
        )

        // Reminders for Vehicle 2
        reminderDao.insertReminder(
            ReminderEntity(
                vehicleId = v2Id,
                serviceType = ServiceType.TRANSMISSION,
                intervalDays = 1095,
                intervalKm = 60000,
                nextDueDate = now + 45 * dayMs,
                nextDueOdometer = 65000,
                notes = "Automatic transmission fluid exchange",
                createdAt = now - 500 * dayMs,
                updatedAt = now
            )
        )

        // Services for Vehicle 1
        serviceDao.insertService(
            ServiceEntity(
                vehicleId = v1Id,
                serviceType = ServiceType.OIL_CHANGE,
                odometerAtService = 37500,
                serviceDate = now - 160 * dayMs,
                costCents = 6500,
                shopName = "Toyota Service Center",
                notes = "Full synthetic 0W-16, OEM filter, multi-point safety inspection passed.",
                createdAt = now - 160 * dayMs
            )
        )

        serviceDao.insertService(
            ServiceEntity(
                vehicleId = v1Id,
                serviceType = ServiceType.TIRE_ROTATION,
                odometerAtService = 37500,
                serviceDate = now - 160 * dayMs,
                costCents = 3500,
                shopName = "Toyota Service Center",
                notes = "4-wheel balance and rotation. Set tire pressures to 35 PSI cold.",
                createdAt = now - 160 * dayMs
            )
        )

        serviceDao.insertService(
            ServiceEntity(
                vehicleId = v1Id,
                serviceType = ServiceType.AIR_FILTER,
                odometerAtService = 28000,
                serviceDate = now - 320 * dayMs,
                costCents = 2850,
                shopName = "DIY / Self Service",
                notes = "Replaced engine air filter with genuine Denso OEM unit.",
                createdAt = now - 320 * dayMs
            )
        )

        serviceDao.insertService(
            ServiceEntity(
                vehicleId = v1Id,
                serviceType = ServiceType.INSPECTION,
                odometerAtService = 20150,
                serviceDate = now - 450 * dayMs,
                costCents = 14500,
                shopName = "Toyota Official Dealership",
                notes = "20k factory maintenance inspection & brake fluid moisture check.",
                createdAt = now - 450 * dayMs
            )
        )

        // Fuel entries for Vehicle 1
        fuelDao.insertFuelEntry(
            FuelEntryEntity(
                vehicleId = v1Id,
                date = now - 3 * dayMs,
                odometer = 45280,
                volumeMilliliters = 46200,
                costCents = 4850,
                notes = "Shell V-Power Regular 87",
                createdAt = now - 3 * dayMs
            )
        )

        fuelDao.insertFuelEntry(
            FuelEntryEntity(
                vehicleId = v1Id,
                date = now - 10 * dayMs,
                odometer = 44710,
                volumeMilliliters = 45800,
                costCents = 4790,
                notes = "Costco Wholesale Gas",
                createdAt = now - 10 * dayMs
            )
        )

        fuelDao.insertFuelEntry(
            FuelEntryEntity(
                vehicleId = v1Id,
                date = now - 18 * dayMs,
                odometer = 44150,
                volumeMilliliters = 47100,
                costCents = 4920,
                notes = "Chevron with Techron",
                createdAt = now - 18 * dayMs
            )
        )

        fuelDao.insertFuelEntry(
            FuelEntryEntity(
                vehicleId = v1Id,
                date = now - 26 * dayMs,
                odometer = 43580,
                volumeMilliliters = 46500,
                costCents = 4880,
                notes = "Costco Wholesale Gas",
                createdAt = now - 26 * dayMs
            )
        )

        // Mileage logs for Vehicle 1
        mileageLogDao.insertLog(
            MileageLogEntity(
                vehicleId = v1Id,
                odometer = 45280,
                loggedAt = now - 3 * dayMs,
                notes = "Weekly odometer check"
            )
        )

        mileageLogDao.insertLog(
            MileageLogEntity(
                vehicleId = v1Id,
                odometer = 44710,
                loggedAt = now - 10 * dayMs,
                notes = "Pre-trip reading"
            )
        )

        mileageLogDao.insertLog(
            MileageLogEntity(
                vehicleId = v1Id,
                odometer = 44150,
                loggedAt = now - 18 * dayMs,
                notes = "Commute log"
            )
        )

        val prefs = UserPreferences(context)
        prefs.setHasSeenOnboarding(true)
        prefs.setDistanceUnit("mi")
        
        db.close()
    }
}
