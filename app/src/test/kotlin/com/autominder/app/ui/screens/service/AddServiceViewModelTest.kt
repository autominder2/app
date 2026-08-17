package com.autominder.app.ui.screens.service

import androidx.lifecycle.SavedStateHandle
import com.autominder.app.R
import com.autominder.app.core.util.AnalyticsHelper
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceCompletion
import com.autominder.app.domain.model.ServiceCompletionResult
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.repository.IVehicleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Save-path behaviour of the Log Service screen.
 *
 * The ViewModel validates input and builds one [ServiceCompletion] command; it
 * must not sequence persistence itself. What is asserted here is the command it
 * produces, how it reports each outcome, and that Save is single-flight.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddServiceViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var serviceRepo: IServiceRepository
    private lateinit var vehicleRepo: IVehicleRepository
    private lateinit var prefs: UserPreferences
    private lateinit var analytics: AnalyticsHelper

    /**
     * Route arguments decode through a stubbed [android.os.Bundle] on the JVM, so
     * the id the ViewModel resolves is the default rather than whatever is seeded
     * into the handle. That is fine for this suite — the subject is the save path,
     * not route decoding, which the navigation library owns and tests itself.
     */
    private val vehicleId = 0L

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        serviceRepo = mockk()
        vehicleRepo = mockk()
        prefs = mockk(relaxed = true)
        analytics = mockk(relaxed = true)

        every { prefs.distanceUnit } returns flowOf("km")
        every { vehicleRepo.getVehicleById(vehicleId) } returns flowOf(
            Vehicle(
                id = vehicleId,
                make = "Honda",
                model = "Civic",
                year = 2020,
                currentOdometer = 201_430
            )
        )
        // The fast path reads this vehicle's history to build "Recently used".
        every { serviceRepo.getServicesForVehicle(any()) } returns flowOf(emptyList())
        coEvery { serviceRepo.completeService(any()) } returns ServiceCompletionResult.Success(1L)
    }

    @Test
    fun `recently used is built from this vehicle's history, newest first, distinct`() = runTest(dispatcher) {
        every { serviceRepo.getServicesForVehicle(any()) } returns flowOf(
            listOf(
                service(ServiceType.OIL_CHANGE, dayOffset = 0),
                service(ServiceType.TIRE_ROTATION, dayOffset = -10),
                service(ServiceType.OIL_CHANGE, dayOffset = -40),   // duplicate, older
                service(ServiceType.BRAKE_SERVICE, dayOffset = -90),
                service(ServiceType.COOLANT, dayOffset = -200)      // beyond the cap
            )
        )

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(
            listOf(ServiceType.OIL_CHANGE, ServiceType.TIRE_ROTATION, ServiceType.BRAKE_SERVICE),
            vm.uiState.value.recentTypes
        )
    }

    @Test
    fun `a vehicle with no history offers no recent shortcuts`() = runTest(dispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.recentTypes.isEmpty())
    }

    private fun service(type: ServiceType, dayOffset: Int) = Service(
        vehicleId = vehicleId,
        serviceType = type,
        odometerAtService = 200_000,
        serviceDate = 1_700_000_000_000L + dayOffset * 86_400_000L
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AddServiceViewModel(
        serviceRepository = serviceRepo,
        vehicleRepository = vehicleRepo,
        userPreferences = prefs,
        analyticsHelper = analytics,
        savedStateHandle = SavedStateHandle(mapOf("vehicleId" to vehicleId))
    )

    /** Enters a valid service with the given odometer reading, then taps Save once. */
    private fun AddServiceViewModel.enterAndSave(odometer: String) {
        onEvent(AddServiceUiEvent.OdometerChanged(odometer))
        onEvent(AddServiceUiEvent.SaveClicked)
    }

    @Test
    fun `historical service below the current odometer is accepted`() = runTest(dispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()

        // Vehicle reads 201,430 km; the user logs a 180,000 km oil change.
        vm.enterAndSave("180000")
        advanceUntilIdle()

        val command = slot<ServiceCompletion>()
        coVerify(exactly = 1) { serviceRepo.completeService(capture(command)) }
        // The reading reaches the data layer untouched — the screen no longer decides
        // whether a low odometer is legitimate history.
        assertEquals(180_000, command.captured.service.odometerAtService)

        val state = vm.uiState.value
        assertTrue(state.isSaved)
        assertNull(state.errorRes)
    }

    @Test
    fun `double tap on Save runs one completion`() = runTest(dispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(AddServiceUiEvent.OdometerChanged("205000"))
        // Two taps before the first transaction gets a chance to run.
        vm.onEvent(AddServiceUiEvent.SaveClicked)
        vm.onEvent(AddServiceUiEvent.SaveClicked)
        advanceUntilIdle()

        coVerify(exactly = 1) { serviceRepo.completeService(any()) }
        assertTrue(vm.uiState.value.isSaved)
    }

    @Test
    fun `a further tap after a successful save does nothing`() = runTest(dispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.enterAndSave("205000")
        advanceUntilIdle()
        vm.onEvent(AddServiceUiEvent.SaveClicked)
        advanceUntilIdle()

        coVerify(exactly = 1) { serviceRepo.completeService(any()) }
    }

    @Test
    fun `a deleted vehicle surfaces a recoverable error and no saved state`() = runTest(dispatcher) {
        coEvery { serviceRepo.completeService(any()) } returns ServiceCompletionResult.VehicleNotFound
        val vm = createViewModel()
        advanceUntilIdle()

        vm.enterAndSave("205000")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isSaved)
        assertFalse(state.isLoading)
        assertEquals(R.string.error_vehicle_not_found, state.errorRes)
    }

    @Test
    fun `a rolled-back transaction surfaces the save failure and can be retried`() = runTest(dispatcher) {
        coEvery { serviceRepo.completeService(any()) } returns
            ServiceCompletionResult.Failed(RuntimeException("disk full"))
        val vm = createViewModel()
        advanceUntilIdle()

        vm.enterAndSave("205000")
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isSaved)
        assertEquals(R.string.error_save_service_failed, vm.uiState.value.errorRes)

        // Not saved means not locked out — the user can fix the cause and try again.
        coEvery { serviceRepo.completeService(any()) } returns ServiceCompletionResult.Success(1L)
        vm.onEvent(AddServiceUiEvent.SaveClicked)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isSaved)
        coVerify(exactly = 2) { serviceRepo.completeService(any()) }
    }

    @Test
    fun `an invalid odometer never reaches the data layer`() = runTest(dispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.enterAndSave("not a number")
        advanceUntilIdle()

        assertEquals(R.string.error_invalid_odometer, vm.uiState.value.errorRes)
        coVerify(exactly = 0) { serviceRepo.completeService(any()) }
    }
}
