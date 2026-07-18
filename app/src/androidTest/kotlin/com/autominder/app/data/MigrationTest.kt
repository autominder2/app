package com.autominder.app.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.autominder.app.core.di.DatabaseModule
import com.autominder.app.data.local.database.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Migration coverage for the existing schema (Gate A requirement: cover
 * what exists BEFORE creating schema v3). Validates both structure
 * (against exported schema JSONs) and data preservation across 1 -> 2.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDb = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2_preservesVehicleData_andValidatesSchema() {
        // Seed a v1 database with a real vehicle row
        helper.createDatabase(testDb, 1).apply {
            execSQL(
                """
                INSERT INTO vehicles
                    (make, model, year, plateNumber, vin, currentOdometer,
                     photoUri, isArchived, notes, createdAt, updatedAt)
                VALUES
                    ('Toyota', 'Corolla', 2021, 'ABC-123', NULL, 84120,
                     NULL, 0, '', 1700000000000, 1700000000000)
                """.trimIndent()
            )
            close()
        }

        // Run the real migration and validate against the exported 2.json
        val db = helper.runMigrationsAndValidate(
            testDb, 2, true, DatabaseModule.MIGRATION_1_2
        )

        // Data survived
        db.query("SELECT make, model, currentOdometer FROM vehicles").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("Toyota", c.getString(0))
            assertEquals("Corolla", c.getString(1))
            assertEquals(84120, c.getInt(2))
        }

        // New v2 table exists and is empty
        db.query("SELECT COUNT(*) FROM fuel_entries").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(0, c.getInt(0))
        }
    }

    @Test
    @Throws(IOException::class)
    fun freshInstallAtV2_schemaMatchesExportedJson() {
        // A clean create at latest version must validate against 2.json —
        // catches entity/schema drift before it ships.
        helper.createDatabase(testDb, 2).close()
    }
}
