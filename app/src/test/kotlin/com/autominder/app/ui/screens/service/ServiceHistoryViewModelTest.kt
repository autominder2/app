package com.autominder.app.ui.screens.service

import android.net.Uri
import com.autominder.app.data.export.ExportServiceHistoryUseCase
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IServiceRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceHistoryViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var serviceRepo: IServiceRepository
    private lateinit var vehicleRepo: IVehicleRepository
    private lateinit var userPrefs: UserPreferences
    private lateinit var exportUseCase: ExportServiceHistoryUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        serviceRepo = mockk(relaxed = true)
        vehicleRepo = mockk(relaxed = true)
        userPrefs = mockk(relaxed = true)
        exportUseCase = mockk(relaxed = true)

        val vehicle = Vehicle(id = 1L, make = "Honda", model = "Civic", year = 2021, plateNumber = "HND-101", currentOdometer = 45000)
        every { vehicleRepo.getAllVehiclesIncludingArchived() } returns flowOf(listOf(vehicle))
        every { serviceRepo.getAllServices() } returns flowOf(emptyList())
        every { serviceRepo.getServicesForVehicle(any()) } returns flowOf(emptyList())
        every { userPrefs.distanceUnit } returns flowOf("km")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ServiceHistoryViewModel(
        serviceRepository = serviceRepo,
        vehicleRepository = vehicleRepo,
        userPreferences = userPrefs,
        exportServiceHistory = exportUseCase,
        defaultDispatcher = dispatcher
    )

    @Test
    fun `loadData aggregates total spend and groups by month`() = runTest(dispatcher) {
        val s1 = Service(id = 1L, vehicleId = 1L, serviceType = ServiceType.OIL_CHANGE, odometerAtService = 30000, serviceDate = 1700000000000L, costCents = 6000)
        val s2 = Service(id = 2L, vehicleId = 1L, serviceType = ServiceType.TIRE_ROTATION, odometerAtService = 35000, serviceDate = 1705000000000L, costCents = 4000)

        every { serviceRepo.getAllServices() } returns flowOf(listOf(s1, s2))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.serviceCount)
        assertEquals(10000, state.totalSpendCents)
        assertEquals(5000, state.averageCostCents)
        assertTrue(state.groups.isNotEmpty())
    }

    @Test
    fun `SelectCategory filters records to only selected category`() = runTest(dispatcher) {
        val s1 = Service(id = 1L, vehicleId = 1L, serviceType = ServiceType.OIL_CHANGE, odometerAtService = 30000, serviceDate = 1700000000000L, costCents = 6000)
        val s2 = Service(id = 2L, vehicleId = 1L, serviceType = ServiceType.TIRE_ROTATION, odometerAtService = 35000, serviceDate = 1705000000000L, costCents = 4000)

        every { serviceRepo.getAllServices() } returns flowOf(listOf(s1, s2))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(ServiceHistoryUiEvent.SelectCategory(ServiceType.OIL_CHANGE))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(ServiceType.OIL_CHANGE, state.selectedCategory)
        assertEquals(1, state.serviceCount)
        assertEquals(6000, state.totalSpendCents)
    }

    @Test
    fun `ExportHistory triggers CSV export use case and populates exportUri`() = runTest(dispatcher) {
        val mockUri = mockk<Uri>(relaxed = true)
        coEvery { exportUseCase(any()) } returns mockUri

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(ServiceHistoryUiEvent.ExportHistory(1L))
        advanceUntilIdle()

        assertEquals(mockUri, viewModel.uiState.value.exportUri)
        coVerify(exactly = 1) { exportUseCase(1L) }
    }

    @Test
    fun `ExportPassport triggers passport export and populates exportUri`() = runTest(dispatcher) {
        val mockUri = mockk<Uri>(relaxed = true)
        coEvery { exportUseCase.exportPassport(any()) } returns mockUri

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(ServiceHistoryUiEvent.ExportPassport(1L))
        advanceUntilIdle()

        assertEquals(mockUri, viewModel.uiState.value.exportUri)
        coVerify(exactly = 1) { exportUseCase.exportPassport(1L) }
    }

    @Test
    fun `DeleteService and UndoDelete call repository methods`() = runTest(dispatcher) {
        val service = Service(id = 1L, vehicleId = 1L, serviceType = ServiceType.OIL_CHANGE, odometerAtService = 30000, serviceDate = 1700000000000L, costCents = 6000)
        coEvery { serviceRepo.deleteService(any()) } returns Unit
        coEvery { serviceRepo.insertService(any()) } returns 1L

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(ServiceHistoryUiEvent.DeleteService(service))
        advanceUntilIdle()
        coVerify(exactly = 1) { serviceRepo.deleteService(service) }

        viewModel.onEvent(ServiceHistoryUiEvent.UndoDelete(service))
        advanceUntilIdle()
        coVerify(exactly = 1) { serviceRepo.insertService(service) }
    }
}
