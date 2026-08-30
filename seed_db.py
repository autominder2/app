import subprocess
import os
import sqlite3
import time

adb_path = os.path.expandvars(r"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe")
db_local = r"d:\Autominder\autominder_seed.db"

# Force stop app first
subprocess.run([adb_path, "shell", "am", "force-stop", "com.autominder.app"], check=True)

if os.path.exists(db_local):
    os.remove(db_local)

conn = sqlite3.connect(db_local)
c = conn.cursor()

# 1. Setup Room master table
c.execute("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
c.execute("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'd19f8939d4a13e033d9f639169980e75')")

# 2. Setup Tables
c.execute("""
CREATE TABLE IF NOT EXISTS `vehicles` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `make` TEXT NOT NULL,
    `model` TEXT NOT NULL,
    `year` INTEGER NOT NULL,
    `plateNumber` TEXT NOT NULL,
    `vin` TEXT,
    `currentOdometer` INTEGER NOT NULL,
    `photoUri` TEXT,
    `isArchived` INTEGER NOT NULL,
    `notes` TEXT NOT NULL,
    `createdAt` INTEGER NOT NULL,
    `updatedAt` INTEGER NOT NULL
)
""")

c.execute("""
CREATE TABLE IF NOT EXISTS `reminders` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `vehicleId` INTEGER NOT NULL,
    `serviceType` TEXT NOT NULL,
    `customLabel` TEXT,
    `intervalDays` INTEGER,
    `intervalKm` INTEGER,
    `nextDueDate` INTEGER,
    `nextDueOdometer` INTEGER,
    `snoozeUntil` INTEGER,
    `notifyDaysBefore` INTEGER NOT NULL,
    `lastNotifiedAt` INTEGER,
    `isCompleted` INTEGER NOT NULL,
    `completedAt` INTEGER,
    `notes` TEXT NOT NULL,
    `createdAt` INTEGER NOT NULL,
    `updatedAt` INTEGER NOT NULL,
    FOREIGN KEY(`vehicleId`) REFERENCES `vehicles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
)
""")
c.execute("CREATE INDEX IF NOT EXISTS `index_reminders_vehicleId` ON `reminders` (`vehicleId`)")

c.execute("""
CREATE TABLE IF NOT EXISTS `services` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `vehicleId` INTEGER NOT NULL,
    `serviceType` TEXT NOT NULL,
    `customLabel` TEXT,
    `odometerAtService` INTEGER NOT NULL,
    `serviceDate` INTEGER NOT NULL,
    `costCents` INTEGER,
    `shopName` TEXT,
    `notes` TEXT NOT NULL,
    `receiptPhotoUri` TEXT,
    `createdAt` INTEGER NOT NULL,
    FOREIGN KEY(`vehicleId`) REFERENCES `vehicles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
)
""")
c.execute("CREATE INDEX IF NOT EXISTS `index_services_vehicleId` ON `services` (`vehicleId`)")

c.execute("""
CREATE TABLE IF NOT EXISTS `mileage_logs` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `vehicleId` INTEGER NOT NULL,
    `odometer` INTEGER NOT NULL,
    `loggedAt` INTEGER NOT NULL,
    `notes` TEXT,
    FOREIGN KEY(`vehicleId`) REFERENCES `vehicles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
)
""")
c.execute("CREATE INDEX IF NOT EXISTS `index_mileage_logs_vehicleId` ON `mileage_logs` (`vehicleId`)")

c.execute("""
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
""")
c.execute("CREATE INDEX IF NOT EXISTS `index_fuel_entries_vehicleId` ON `fuel_entries` (`vehicleId`)")

now = int(time.time() * 1000)
day_ms = 86400000

# 3. Populate Vehicles
# Vehicle 1: 2022 Toyota Camry XSE
c.execute("""
INSERT INTO vehicles (id, make, model, year, plateNumber, vin, currentOdometer, photoUri, isArchived, notes, createdAt, updatedAt)
VALUES (1, 'Toyota', 'Camry XSE', 2022, '7XYZ890', '4T1B11HK5NU123456', 45280, NULL, 0, 'Primary daily commuter - pristine condition', ?, ?)
""", (now, now))

# Vehicle 2: 2021 Ford F-150 Lariat
c.execute("""
INSERT INTO vehicles (id, make, model, year, plateNumber, vin, currentOdometer, photoUri, isArchived, notes, createdAt, updatedAt)
VALUES (2, 'Ford', 'F-150 Lariat', 2021, 'TRK-4421', '1FTFW1ED5MFA98765', 62450, NULL, 0, 'Family road trips & utility haul', ?, ?)
""", (now, now))

# Vehicle 3: 2020 Honda Civic Touring
c.execute("""
INSERT INTO vehicles (id, make, model, year, plateNumber, vin, currentOdometer, photoUri, isArchived, notes, createdAt, updatedAt)
VALUES (3, 'Honda', 'Civic Touring', 2020, '8KLP123', '2HGFC2F76LH543210', 38100, NULL, 0, 'Secondary vehicle', ?, ?)
""", (now, now))

# 4. Reminders for Vehicle 1 (Toyota Camry)
# Oil Change - DUE SOON (Visual Amber Highlight)
c.execute("""
INSERT INTO reminders (id, vehicleId, serviceType, customLabel, intervalDays, intervalKm, nextDueDate, nextDueOdometer, snoozeUntil, notifyDaysBefore, lastNotifiedAt, isCompleted, completedAt, notes, createdAt, updatedAt)
VALUES (1, 1, 'OIL_CHANGE', NULL, 180, 8000, ?, 46000, NULL, 7, NULL, 0, NULL, '0W-16 Synthetic Oil & OEM Filter', ?, ?)
""", (now + 14 * day_ms, now, now))

# Cabin Air Filter - OVERDUE (Visual Red Critical Highlight)
c.execute("""
INSERT INTO reminders (id, vehicleId, serviceType, customLabel, intervalDays, intervalKm, nextDueDate, nextDueOdometer, snoozeUntil, notifyDaysBefore, lastNotifiedAt, isCompleted, completedAt, notes, createdAt, updatedAt)
VALUES (2, 1, 'CABIN_FILTER', NULL, 365, 20000, ?, 44000, NULL, 7, NULL, 0, NULL, 'Inspect for pollen & seasonal dust buildup', ?, ?)
""", (now - 10 * day_ms, now, now))

# Tire Rotation - OK (Green)
c.execute("""
INSERT INTO reminders (id, vehicleId, serviceType, customLabel, intervalDays, intervalKm, nextDueDate, nextDueOdometer, snoozeUntil, notifyDaysBefore, lastNotifiedAt, isCompleted, completedAt, notes, createdAt, updatedAt)
VALUES (3, 1, 'TIRE_ROTATION', NULL, 180, 10000, ?, 48000, NULL, 7, NULL, 0, NULL, 'Rotate front to rear & check tread depth', ?, ?)
""", (now + 60 * day_ms, now, now))

# Brake Service - OK (Green)
c.execute("""
INSERT INTO reminders (id, vehicleId, serviceType, customLabel, intervalDays, intervalKm, nextDueDate, nextDueOdometer, snoozeUntil, notifyDaysBefore, lastNotifiedAt, isCompleted, completedAt, notes, createdAt, updatedAt)
VALUES (4, 1, 'BRAKE_SERVICE', NULL, 730, 40000, ?, 60000, NULL, 14, NULL, 0, NULL, 'Front and rear pad & rotor inspection', ?, ?)
""", (now + 240 * day_ms, now, now))

# Registration Renewal - OK
c.execute("""
INSERT INTO reminders (id, vehicleId, serviceType, customLabel, intervalDays, intervalKm, nextDueDate, nextDueOdometer, snoozeUntil, notifyDaysBefore, lastNotifiedAt, isCompleted, completedAt, notes, createdAt, updatedAt)
VALUES (5, 1, 'REGISTRATION', NULL, 365, NULL, ?, NULL, NULL, 30, NULL, 0, NULL, 'Annual DMV registration sticker renewal', ?, ?)
""", (now + 85 * day_ms, now, now))

# Reminders for Ford F-150
c.execute("""
INSERT INTO reminders (id, vehicleId, serviceType, customLabel, intervalDays, intervalKm, nextDueDate, nextDueOdometer, snoozeUntil, notifyDaysBefore, lastNotifiedAt, isCompleted, completedAt, notes, createdAt, updatedAt)
VALUES (6, 2, 'TRANSMISSION', NULL, 1095, 60000, ?, 65000, NULL, 14, NULL, 0, NULL, 'Automatic transmission fluid exchange', ?, ?)
""", (now + 45 * day_ms, now, now))

c.execute("""
INSERT INTO reminders (id, vehicleId, serviceType, customLabel, intervalDays, intervalKm, nextDueDate, nextDueOdometer, snoozeUntil, notifyDaysBefore, lastNotifiedAt, isCompleted, completedAt, notes, createdAt, updatedAt)
VALUES (7, 2, 'OIL_CHANGE', NULL, 180, 10000, ?, 64000, NULL, 7, NULL, 0, NULL, 'Motorcraft 5W-30 Synthetic Blend', ?, ?)
""", (now + 35 * day_ms, now, now))

# 5. Completed Service History for Vehicle 1
c.execute("""
INSERT INTO services (id, vehicleId, serviceType, customLabel, odometerAtService, serviceDate, costCents, shopName, notes, receiptPhotoUri, createdAt)
VALUES (1, 1, 'OIL_CHANGE', NULL, 37500, ?, 6500, 'Toyota Service Center', 'Full synthetic 0W-16, OEM filter, multi-point safety inspection passed.', NULL, ?)
""", (now - 160 * day_ms, now - 160 * day_ms))

c.execute("""
INSERT INTO services (id, vehicleId, serviceType, customLabel, odometerAtService, serviceDate, costCents, shopName, notes, receiptPhotoUri, createdAt)
VALUES (2, 1, 'TIRE_ROTATION', NULL, 37500, ?, 3500, 'Toyota Service Center', '4-wheel balance and rotation. Set tire pressures to 35 PSI cold.', NULL, ?)
""", (now - 160 * day_ms, now - 160 * day_ms))

c.execute("""
INSERT INTO services (id, vehicleId, serviceType, customLabel, odometerAtService, serviceDate, costCents, shopName, notes, receiptPhotoUri, createdAt)
VALUES (3, 1, 'AIR_FILTER', NULL, 28000, ?, 2850, 'DIY / Self Service', 'Replaced engine air filter with genuine Denso OEM unit.', NULL, ?)
""", (now - 320 * day_ms, now - 320 * day_ms))

c.execute("""
INSERT INTO services (id, vehicleId, serviceType, customLabel, odometerAtService, serviceDate, costCents, shopName, notes, receiptPhotoUri, createdAt)
VALUES (4, 1, 'INSPECTION', NULL, 20150, ?, 14500, 'Toyota Official Dealership', '20k factory maintenance inspection & brake fluid moisture check.', NULL, ?)
""", (now - 450 * day_ms, now - 450 * day_ms))

# Completed Service for Ford F-150
c.execute("""
INSERT INTO services (id, vehicleId, serviceType, customLabel, odometerAtService, serviceDate, costCents, shopName, notes, receiptPhotoUri, createdAt)
VALUES (5, 2, 'BRAKE_SERVICE', NULL, 58000, ?, 28000, 'Brembo Certified Auto', 'Replaced front ceramic pads and machined rotors to OEM specs.', NULL, ?)
""", (now - 80 * day_ms, now - 80 * day_ms))

# 6. Fuel Entries for Vehicle 1
c.execute("""
INSERT INTO fuel_entries (id, vehicleId, date, odometer, volumeMilliliters, costCents, notes, createdAt)
VALUES (1, 1, ?, 45280, 46200, 4850, 'Shell V-Power Regular 87', ?)
""", (now - 3 * day_ms, now - 3 * day_ms))

c.execute("""
INSERT INTO fuel_entries (id, vehicleId, date, odometer, volumeMilliliters, costCents, notes, createdAt)
VALUES (2, 1, ?, 44710, 45800, 4790, 'Costco Wholesale Gas', ?)
""", (now - 10 * day_ms, now - 10 * day_ms))

c.execute("""
INSERT INTO fuel_entries (id, vehicleId, date, odometer, volumeMilliliters, costCents, notes, createdAt)
VALUES (3, 1, ?, 44150, 47100, 4920, 'Chevron with Techron', ?)
""", (now - 18 * day_ms, now - 18 * day_ms))

c.execute("""
INSERT INTO fuel_entries (id, vehicleId, date, odometer, volumeMilliliters, costCents, notes, createdAt)
VALUES (4, 1, ?, 43580, 46500, 4880, 'Costco Wholesale Gas', ?)
""", (now - 26 * day_ms, now - 26 * day_ms))

# 7. Mileage Logs
c.execute("""
INSERT INTO mileage_logs (id, vehicleId, odometer, loggedAt, notes)
VALUES (1, 1, 45280, ?, 'Weekly odometer check')
""", (now - 3 * day_ms,))

c.execute("""
INSERT INTO mileage_logs (id, vehicleId, odometer, loggedAt, notes)
VALUES (2, 1, 44710, ?, 'Pre-trip reading')
""", (now - 10 * day_ms,))

c.execute("""
INSERT INTO mileage_logs (id, vehicleId, odometer, loggedAt, notes)
VALUES (3, 1, 44150, ?, 'Commute log')
""", (now - 18 * day_ms,))

c.execute("""
INSERT INTO mileage_logs (id, vehicleId, odometer, loggedAt, notes)
VALUES (4, 1, 43580, ?, 'Monthly check')
""", (now - 26 * day_ms,))

conn.commit()
conn.close()

print("Local database populated with complete schema and seed rows.")

with open(db_local, "rb") as f:
    db_bytes = f.read()

# Clear WAL and SHM files on emulator
subprocess.run([adb_path, "shell", "run-as", "com.autominder.app", "rm", "-f", "databases/autominder.db-wal", "databases/autominder.db-shm"], check=True)

# Write back database to app
p = subprocess.Popen([adb_path, "shell", "run-as", "com.autominder.app", "sh", "-c", "cat > databases/autominder.db"], stdin=subprocess.PIPE)
p.communicate(input=db_bytes)
p.wait()

print("Database written back to emulator successfully!")
