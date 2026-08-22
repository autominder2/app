package com.autominder.app.ui.screens.fuel

import androidx.lifecycle.SavedStateHandle
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.FuelEntry
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IFuelRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.usecase.CalculateEfficiencyUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class FuelHistoryViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var fuelRepo: IFuelRepository
    private lateinit var vehicleRepo: IVehicleRepository
    private lateinit var userPrefs: UserPreferences
    private lateinit var calculateEfficiency: CalculateEfficiencyUseCase
    private lateinit var savedStateHandle: SavedStateHandle

    private val vehicleId = 0L

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        fuelRepo = mockk(relaxed = true)
        vehicleRepo = mockk(relaxed = true)
        userPrefs = mockk(relaxed = true)
        calculateEfficiency = CalculateEfficiencyUseCase()
        savedStateHandle = SavedStateHandle(mapOf("vehicleId" to vehicleId))

        val vehicle = Vehicle(id = vehicleId, make = "Toyota", model = "Camry", year = 2022, plateNumber = "TOY-123", currentOdometer = 45000)
        every { vehicleRepo.getVehicleById(vehicleId) } returns flowOf(vehicle)
        every { userPrefs.distanceUnit } returns flowOf("km")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = FuelHistoryViewModel(
        fuelRepository = fuelRepo,
        vehicleRepository = vehicleRepo,
        userPreferences = userPrefs,
        calculateEfficiency = calculateEfficiency,
        savedStateHandle = savedStateHandle
    )

    @Test
    fun `loadData calculates total spend, volume, and average price per liter`() = runTest(dispatcher) {
        val f1 = FuelEntry(id = 1L, vehicleId = vehicleId, date = Date(1000L), odometer = 40000, volumeMilliliters = 40000, costCents = 6000L)
        val f2 = FuelEntry(id = 2L, vehicleId = vehicleId, date = Date(2000L), odometer = 40500, volumeMilliliters = 40000, costCents = 6400L)

        every { fuelRepo.getFuelEntriesForVehicle(vehicleId) } returns flowOf(listOf(f1, f2))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(12400L, state.totalFuelCostCents)
        assertEquals(80000L, state.totalVolumeMilliliters)
        // 12400 cents / 80 liters = 155 cents/liter ($1.55/L)
        assertEquals(155.0, state.averagePricePerLiterCents, 0.01)
    }

    @Test
    fun `loadData identifies best and worst tanks accurately`() = runTest(dispatcher) {
        val f1 = FuelEntry(id = 1L, vehicleId = vehicleId, date = Date(1000L), odometer = 10000, volumeMilliliters = 40000, costCents = 6000L)
        val f2 = FuelEntry(id = 2L, vehicleId = vehicleId, date = Date(2000L), odometer = 10400, volumeMilliliters = 40000, costCents = 6000L) // 400 km / 40L = 10 km/L
        val f3 = FuelEntry(id = 3L, vehicleId = vehicleId, date = Date(3000L), odometer = 11000, volumeMilliliters = 40000, costCents = 6000L) // 600 km / 40L = 15 km/L

        every { fuelRepo.getFuelEntriesForVehicle(vehicleId) } returns flowOf(listOf(f1, f2, f3))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.bestTank)
        assertNotNull(state.worstTank)
        assertEquals(15.0, state.bestTank!!.efficiency, 0.01)
        assertEquals(10.0, state.worstTank!!.efficiency, 0.01)
    }

    @Test
    fun `deleteEntry and undoDelete invoke repository methods`() = runTest(dispatcher) {
        val entry = FuelEntry(id = 5L, vehicleId = vehicleId, date = Date(), odometer = 42000, volumeMilliliters = 45000, costCents = 7000L)
        every { fuelRepo.getFuelEntriesForVehicle(vehicleId) } returns flowOf(listOf(entry))

        coEvery { fuelRepo.deleteFuelEntry(any()) } returns Unit
        coEvery { fuelRepo.insertFuelEntry(any()) } returns 5L

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(FuelHistoryUiEvent.DeleteEntry(entry))
        advanceUntilIdle()
        coVerify(exactly = 1) { fuelRepo.deleteFuelEntry(entry) }

        viewModel.onEvent(FuelHistoryUiEvent.UndoDelete(entry))
        advanceUntilIdle()
        coVerify(exactly = 1) { fuelRepo.insertFuelEntry(entry) }
    }
}
