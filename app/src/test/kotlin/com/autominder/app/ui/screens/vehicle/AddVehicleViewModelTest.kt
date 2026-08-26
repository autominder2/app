package com.autominder.app.ui.screens.vehicle

import androidx.lifecycle.SavedStateHandle
import com.autominder.app.core.util.AnalyticsHelper
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.repository.IReminderRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.usecase.CreateDefaultRemindersUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddVehicleViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val vehicleRepository = mockk<IVehicleRepository>(relaxed = true)
    private val reminderRepository = mockk<IReminderRepository>(relaxed = true)
    private val userPreferences = mockk<UserPreferences>(relaxed = true)
    private val analyticsHelper = mockk<AnalyticsHelper>(relaxed = true)
    private lateinit var createDefaultReminders: CreateDefaultRemindersUseCase
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        createDefaultReminders = CreateDefaultRemindersUseCase(reminderRepository)
        savedStateHandle = SavedStateHandle()
        coEvery { userPreferences.distanceUnit } returns flowOf("km")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AddVehicleViewModel {
        return AddVehicleViewModel(
            vehicleRepository = vehicleRepository,
            createDefaultReminders = createDefaultReminders,
            userPreferences = userPreferences,
            analyticsHelper = analyticsHelper,
            savedStateHandle = savedStateHandle,
            defaultDispatcher = testDispatcher
        )
    }

    @Test
    fun `initial state starts at DISCOVERY step with popular vehicle suggestions`() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertEquals(AddVehicleStep.DISCOVERY, state.currentStep)
        assertTrue(state.suggestions.any { it.make == "Toyota" && it.model == "RAV4" })
        assertTrue(state.suggestions.any { it.make == "Honda" && it.model == "Civic" })
        assertFalse(state.isSaved)
    }

    @Test
    fun `selecting vehicle suggestion sets brand and model and advances to IDENTITY step`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(AddVehicleUiEvent.SuggestionSelected(VehicleSuggestion("Toyota", "RAV4", "SUV")))
        assertEquals("Toyota", viewModel.uiState.value.brand)
        assertEquals("RAV4", viewModel.uiState.value.model)
        assertEquals(AddVehicleStep.IDENTITY, viewModel.uiState.value.currentStep)
    }

    @Test
    fun `universal search query filters suggestions accurately`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(AddVehicleUiEvent.SearchQueryChanged("civic"))
        val suggestions = viewModel.uiState.value.suggestions
        assertTrue(suggestions.any { it.model.equals("Civic", ignoreCase = true) })
    }

    @Test
    fun `step navigation moves forward and backward correctly`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(AddVehicleUiEvent.SuggestionSelected(VehicleSuggestion("Honda", "Civic", "Sedan")))
        assertEquals(AddVehicleStep.IDENTITY, viewModel.uiState.value.currentStep)

        viewModel.onEvent(AddVehicleUiEvent.YearChanged("2024"))
        viewModel.onEvent(AddVehicleUiEvent.NextStepClicked)
        assertEquals(AddVehicleStep.SCHEDULE, viewModel.uiState.value.currentStep)

        viewModel.onEvent(AddVehicleUiEvent.PreviousStepClicked)
        assertEquals(AddVehicleStep.IDENTITY, viewModel.uiState.value.currentStep)
    }

    @Test
    fun `adjusting odometer updates state and preview reminders`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(AddVehicleUiEvent.OdometerChanged("10000"))
        assertEquals("10000", viewModel.uiState.value.currentOdometer)

        viewModel.onEvent(AddVehicleUiEvent.OdometerAdjusted(500))
        assertEquals("10500", viewModel.uiState.value.currentOdometer)
        assertTrue(viewModel.uiState.value.previewReminders.isNotEmpty())
    }

    @Test
    fun `saving vehicle calls repository and marks state as saved`() = runTest {
        coEvery {
            vehicleRepository.insertVehicleWithInitialState(any(), any())
        } coAnswers {
            val block = secondArg<suspend (Long) -> Unit>()
            block(1L)
            1L
        }

        val viewModel = createViewModel()
        viewModel.onEvent(AddVehicleUiEvent.SuggestionSelected(VehicleSuggestion("Tesla", "Model 3", "EV")))
        viewModel.onEvent(AddVehicleUiEvent.YearChanged("2025"))
        viewModel.onEvent(AddVehicleUiEvent.RoleChanged("Daily Driver"))
        viewModel.onEvent(AddVehicleUiEvent.OdometerChanged("5000"))

        viewModel.onEvent(AddVehicleUiEvent.SaveClicked)

        assertTrue(viewModel.uiState.value.isSaved)
        coVerify { vehicleRepository.insertVehicleWithInitialState(any(), any()) }
    }
}
