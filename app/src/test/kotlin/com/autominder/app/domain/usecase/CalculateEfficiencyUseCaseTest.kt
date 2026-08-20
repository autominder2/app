package com.autominder.app.domain.usecase

import com.autominder.app.domain.model.FuelEntry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Date

class CalculateEfficiencyUseCaseTest {

    private lateinit var useCase: CalculateEfficiencyUseCase

    @Before
    fun setUp() {
        useCase = CalculateEfficiencyUseCase()
    }

    @Test
    fun `calculate single entry with no previous returns 0_0`() {
        val current = FuelEntry(
            id = 1L,
            vehicleId = 10L,
            date = Date(),
            odometer = 50000,
            volumeMilliliters = 40000,
            costCents = 6000L
        )

        val result = useCase.calculate(current, null)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `calculate sequential entries returns accurate km per liter`() {
        val previous = FuelEntry(
            id = 1L,
            vehicleId = 10L,
            date = Date(System.currentTimeMillis() - 86400000L * 7),
            odometer = 50000,
            volumeMilliliters = 40000,
            costCents = 6000L
        )
        val current = FuelEntry(
            id = 2L,
            vehicleId = 10L,
            date = Date(),
            odometer = 50500, // 500 km driven
            volumeMilliliters = 40000, // 40.0 L
            costCents = 6200L
        )

        // 500 km / 40 L = 12.5 km/L
        val result = useCase.calculate(current, previous, CalculateEfficiencyUseCase.EfficiencyUnit.KM_L)
        assertEquals(12.5, result, 0.001)
    }

    @Test
    fun `calculate sequential entries in L per 100km`() {
        val previous = FuelEntry(
            id = 1L,
            vehicleId = 10L,
            date = Date(System.currentTimeMillis() - 86400000L * 7),
            odometer = 50000,
            volumeMilliliters = 40000,
            costCents = 6000L
        )
        val current = FuelEntry(
            id = 2L,
            vehicleId = 10L,
            date = Date(),
            odometer = 50500, // 500 km
            volumeMilliliters = 40000, // 40 L
            costCents = 6200L
        )

        // (40 L / 500 km) * 100 = 8.0 L/100km
        val result = useCase.calculate(current, previous, CalculateEfficiencyUseCase.EfficiencyUnit.L_100KM)
        assertEquals(8.0, result, 0.001)
    }

    @Test
    fun `calculate sequential entries in MPG US`() {
        val previous = FuelEntry(
            id = 1L,
            vehicleId = 10L,
            date = Date(),
            odometer = 10000,
            volumeMilliliters = 37854,
            costCents = 5000L
        )
        val current = FuelEntry(
            id = 2L,
            vehicleId = 10L,
            date = Date(),
            odometer = 10400,
            volumeMilliliters = 37854,
            costCents = 5000L
        )

        val result = useCase.calculate(current, previous, CalculateEfficiencyUseCase.EfficiencyUnit.MPG_US)
        // distance: 400 km * 0.621371 = 248.5484 miles
        // liters: 37.854 L * 0.264172 = 10.0 gallons
        // result ≈ 24.85 MPG
        assertEquals(24.85, result, 0.1)
    }

    @Test
    fun `calculate sequential entries with zero or backwards odometer returns 0_0`() {
        val previous = FuelEntry(
            id = 1L,
            vehicleId = 10L,
            date = Date(),
            odometer = 50000,
            volumeMilliliters = 40000,
            costCents = 6000L
        )
        val current = FuelEntry(
            id = 2L,
            vehicleId = 10L,
            date = Date(),
            odometer = 49900,
            volumeMilliliters = 40000,
            costCents = 6000L
        )

        val result = useCase.calculate(current, previous)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `calculateAverage with fewer than 2 entries returns 0_0`() {
        val entries = listOf(
            FuelEntry(
                id = 1L,
                vehicleId = 10L,
                date = Date(),
                odometer = 50000,
                volumeMilliliters = 40000,
                costCents = 6000L
            )
        )

        assertEquals(0.0, useCase.calculateAverage(entries), 0.001)
        assertEquals(0.0, useCase.calculateAverage(emptyList()), 0.001)
    }

    @Test
    fun `calculateAverage over multi-fill history handles unsorted entries correctly`() {
        val e1 = FuelEntry(id = 1L, vehicleId = 10L, date = Date(1000L), odometer = 10000, volumeMilliliters = 30000, costCents = 4000L)
        val e2 = FuelEntry(id = 2L, vehicleId = 10L, date = Date(2000L), odometer = 10300, volumeMilliliters = 20000, costCents = 3000L)
        val e3 = FuelEntry(id = 3L, vehicleId = 10L, date = Date(3000L), odometer = 10700, volumeMilliliters = 30000, costCents = 4500L)

        // Total distance from e1 to e3: (10300-10000) + (10700-10300) = 700 km
        // Total liters filled after e1: 20 L + 30 L = 50 L
        // Avg = 700 / 50 = 14.0 km/L
        val unsortedList = listOf(e3, e1, e2)
        val result = useCase.calculateAverage(unsortedList, CalculateEfficiencyUseCase.EfficiencyUnit.KM_L)
        assertEquals(14.0, result, 0.001)
    }
}
