package com.autominder.app.ui.screens.mileage

import androidx.lifecycle.SavedStateHandle
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.MileageLogEntry
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IMileageLogRepository
import com.autominder.app.domain.repository.IVehicleRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MileageLogViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var mileageLogRepo: IMileageLogRepository
    private lateinit var vehicleRepo: IVehicleRepository
    private lateinit var userPrefs: UserPreferences
    private lateinit var savedStateHandle: SavedStateHandle

    private val vehicleId = 0L // Decodes to 0L on JVM stub

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        mileageLogRepo = mockk(relaxed = true)
        vehicleRepo = mockk(relaxed = true)
        userPrefs = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle(mapOf("vehicleId" to vehicleId))

        val vehicle = Vehicle(id = vehicleId, make = "Subaru", model = "Outback", year = 2021, plateNumber = "SUB-444", currentOdometer = 50000)
        every { vehicleRepo.getVehicleById(vehicleId) } returns flowOf(vehicle)
        every { mileageLogRepo.getLogsForVehicle(vehicleId) } returns flowOf(emptyList())
        every { userPrefs.distanceUnit } returns flowOf("km")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = MileageLogViewModel(
        mileageLogRepository = mileageLogRepo,
        vehicleRepository = vehicleRepo,
        userPreferences = userPrefs,
        savedStateHandle = savedStateHandle
    )

    @Test
    fun `NewOdometerChanged updates newOdometer and computes trip delta`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(MileageLogUiEvent.NewOdometerChanged("50250"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("50250", state.newOdometer)
        assertEquals(250, state.deltaSinceLastLog)
    }

    @Test
    fun `StepOdometer increments odometer from vehicle base and calculates delta`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(MileageLogUiEvent.StepOdometer(100))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("50100", state.newOdometer)
        assertEquals(100, state.deltaSinceLastLog)
    }

    @Test
    fun `SelectTag appends tag to notes`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(MileageLogUiEvent.SelectTag("Commute"))
        advanceUntilIdle()
        assertEquals("Commute", viewModel.uiState.value.newNotes)

        viewModel.onEvent(MileageLogUiEvent.SelectTag("Roadtrip"))
        advanceUntilIdle()
        assertEquals("Commute, Roadtrip", viewModel.uiState.value.newNotes)
    }

    @Test
    fun `AddClicked inserts log entry and updates vehicle odometer`() = runTest(dispatcher) {
        coEvery { mileageLogRepo.insertLog(any()) } returns 1L
        coEvery { vehicleRepo.updateOdometer(any(), any()) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(MileageLogUiEvent.NewOdometerChanged("50300"))
        viewModel.onEvent(MileageLogUiEvent.NewNotesChanged("Highway trip"))
        viewModel.onEvent(MileageLogUiEvent.AddClicked)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            mileageLogRepo.insertLog(match {
                it.vehicleId == vehicleId && it.odometer == 50300 && it.notes == "Highway trip"
            })
        }
        coVerify(exactly = 1) {
            vehicleRepo.updateOdometer(vehicleId, 50300)
        }
        assertTrue(viewModel.uiState.value.isAddedSuccess)
    }

    @Test
    fun `AddClicked with lower odometer shows validation error`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(MileageLogUiEvent.NewOdometerChanged("49000")) // Lower than 50000
        viewModel.onEvent(MileageLogUiEvent.AddClicked)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorRes)
        coVerify(exactly = 0) { mileageLogRepo.insertLog(any()) }
    }

    @Test
    fun `DeleteLog and UndoDelete invoke repository methods`() = runTest(dispatcher) {
        val log = MileageLogEntry(id = 10L, vehicleId = vehicleId, odometer = 50200, loggedAt = 1700000000000L)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(MileageLogUiEvent.DeleteLog(log))
        advanceUntilIdle()
        coVerify(exactly = 1) { mileageLogRepo.deleteLog(log) }

        viewModel.onEvent(MileageLogUiEvent.UndoDelete(log))
        advanceUntilIdle()
        coVerify(exactly = 1) { mileageLogRepo.insertLog(log) }
    }
}
