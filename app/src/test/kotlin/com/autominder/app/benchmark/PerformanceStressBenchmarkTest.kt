package com.autominder.app.benchmark

import com.autominder.app.data.backup.AutoMinderBackupData
import com.autominder.app.data.backup.FuelEntryBackupDto
import com.autominder.app.data.backup.MileageLogBackupDto
import com.autominder.app.data.backup.ReminderBackupDto
import com.autominder.app.data.backup.ServiceBackupDto
import com.autominder.app.data.backup.VehicleBackupDto
import com.autominder.app.domain.model.FuelEntry
import com.autominder.app.domain.model.MileageLogEntry
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.usecase.CalculateEfficiencyUseCase
import com.autominder.app.domain.usecase.PredictDueUseCase
import com.autominder.app.domain.usecase.StatusCalculator
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date
import kotlin.system.measureTimeMillis

/**
 * High-volume correctness checks for the core computational engines —
 * efficiency math, pace learning, status calculation and backup serialization —
 * at 1,000+ records.
 *
 * These are NOT benchmarks, despite the class name, and the timing assertions
 * are a coarse regression tripwire rather than a performance measurement. Two
 * things were previously claimed here and are not true, so they have been
 * removed rather than left to mislead:
 *
 *  - "zero memory leaks": nothing in this class measures allocation or
 *    retention. A leak would pass every assertion below.
 *  - "< 100ms per 1,000 items": every assertion in this file actually allows
 *    500ms. The individual test names claimed 30ms and 50ms while asserting
 *    500ms — off by up to 16x. Names now match the assertions.
 *
 * A wall-clock assertion inside a JVM unit test is environment-sensitive by
 * construction: `CalculateEfficiencyUseCase ...` failed on 2026-08-26 purely
 * because an Android emulator was saturating the CPU on the same machine, and
 * passed immediately once it was shut down. The threshold is deliberately loose
 * so it catches an algorithmic regression (an accidental O(n^2), a per-item
 * allocation storm) without failing on a loaded CI runner. Real timing work
 * belongs in the macrobenchmark module, on a device.
 */
class PerformanceStressBenchmarkTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    @Test
    fun `CalculateEfficiencyUseCase processes 1,000 sequential fuel entries without an algorithmic regression`() {
        val calculateEfficiency = CalculateEfficiencyUseCase()
        val entries = (1..1000).map { i ->
            FuelEntry(
                id = i.toLong(),
                vehicleId = 1L,
                date = Date(1700000000000L + (i * 86400000L * 7)),
                odometer = 10000 + (i * 450), // 450 km per tank
                volumeMilliliters = 40000,     // 40 L
                costCents = 6000L
            )
        }

        val elapsed = measureTimeMillis {
            val avg = calculateEfficiency.calculateAverage(entries)
            assertTrue("Average efficiency should be positive", avg > 0.0)
        }

        println("⚡ CalculateEfficiencyUseCase 1,000 entries took ${elapsed}ms")
        assertTrue("Execution should be fast (< 500ms on JVM)", elapsed < 500)
    }

    @Test
    fun `PredictDueUseCase processes 1,000 odometer pace observations without an algorithmic regression`() {
        val predictDue = PredictDueUseCase()
        val points = (1..1000).map { i ->
            com.autominder.app.domain.usecase.OdometerPoint(
                odometerKm = 10000 + (i * 40), // 40 km per day
                timestamp = 1700000000000L + (i * 86400000L)
            )
        }

        val reminder = Reminder(
            id = 1L,
            vehicleId = 1L,
            serviceType = ServiceType.OIL_CHANGE,
            intervalKm = 10000,
            intervalDays = 180,
            nextDueOdometer = 60000
        )

        val elapsed = measureTimeMillis {
            val rate = predictDue.dailyKmRate(points)
            assertNotNull(rate)
            assertTrue("Daily pace should be ~40 km/day", rate!! in 38.0..42.0)

            val prediction = predictDue.predict(reminder, 50000, rate)
            assertNotNull(prediction)
            assertEquals(10000, prediction.kmRemaining)
        }

        println("⚡ PredictDueUseCase 1,000 points took ${elapsed}ms")
        assertTrue("Execution should be fast (< 500ms on JVM)", elapsed < 500)
    }

    @Test
    fun `StatusCalculator evaluates 1,000 reminders without an algorithmic regression`() {
        val now = System.currentTimeMillis()
        val elapsed = measureTimeMillis {
            for (i in 1..1000) {
                val status = StatusCalculator.calculate(
                    nowMillis = now,
                    currentOdometer = 50000 + (i * 10),
                    dueDateMillis = now + (i * 86400000L),
                    dueOdometer = 50000 + (i * 10) + 500,
                    snoozeUntilMillis = null,
                    isCompleted = false
                )
                assertNotNull(status)
            }
        }

        println("⚡ StatusCalculator 1,000 evaluations took ${elapsed}ms")
        assertTrue("Status calculation must be near-instantaneous (< 500ms on JVM)", elapsed < 500)
    }

    @Test
    fun `AutoMinderBackupData serializes and parses 1,000 complex entities without an algorithmic regression`() {
        val vehicles = (1..10).map { i ->
            VehicleBackupDto(
                id = i.toLong(),
                make = "Make $i",
                model = "Model $i",
                year = 2020 + (i % 5),
                plateNumber = "ABC-$i",
                currentOdometer = 50000 + (i * 5000)
            )
        }

        val services = (1..500).map { i ->
            ServiceBackupDto(
                id = i.toLong(),
                vehicleId = ((i % 10) + 1).toLong(),
                serviceType = ServiceType.entries[i % ServiceType.entries.size].name,
                odometerAtService = 20000 + (i * 100),
                serviceDate = 1700000000000L + (i * 86400000L),
                costCents = 5000 + (i * 10)
            )
        }

        val fuelEntries = (1..500).map { i ->
            FuelEntryBackupDto(
                id = i.toLong(),
                vehicleId = ((i % 10) + 1).toLong(),
                date = 1700000000000L + (i * 86400000L),
                odometer = 20000 + (i * 100),
                volumeMilliliters = 45000,
                costCents = 6000L
            )
        }

        val backupData = AutoMinderBackupData(
            version = 2,
            exportedAt = System.currentTimeMillis(),
            appVersion = "1.0.0",
            vehicles = vehicles,
            services = services,
            reminders = emptyList(),
            fuelEntries = fuelEntries,
            mileageLogs = emptyList()
        )

        // Warm up serializer once for JVM classloading
        val warmup = AutoMinderBackupData(vehicles = vehicles.take(1))
        json.decodeFromString<AutoMinderBackupData>(json.encodeToString(warmup))

        var encodedJson = ""
        val serializeTime = measureTimeMillis {
            encodedJson = json.encodeToString(backupData)
        }

        var decodedData: AutoMinderBackupData? = null
        val parseTime = measureTimeMillis {
            decodedData = json.decodeFromString<AutoMinderBackupData>(encodedJson)
        }

        println("⚡ JSON Serialization (1,010 entities): ${serializeTime}ms, Parsing: ${parseTime}ms")
        assertNotNull(decodedData)
        assertEquals(10, decodedData!!.vehicles.size)
        assertEquals(500, decodedData!!.services.size)
        assertEquals(500, decodedData!!.fuelEntries.size)
        assertTrue("Total serialization + parsing time should be fast (< 1000ms on JVM)", serializeTime + parseTime < 1000)
    }
}
