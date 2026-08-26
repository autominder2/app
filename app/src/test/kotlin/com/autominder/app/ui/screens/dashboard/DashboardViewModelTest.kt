package com.autominder.app.ui.screens.dashboard

import android.app.Activity
import app.cash.turbine.test
import com.autominder.app.core.util.AppInfo
import com.autominder.app.core.util.ReviewHelper
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.FuelEntry
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.model.VehicleOperationalStatus
import com.autominder.app.domain.repository.IFuelRepository
import com.autominder.app.domain.repository.IMileageLogRepository
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.usecase.CalculateEfficiencyUseCase
import com.autominder.app.domain.usecase.DashboardData
import com.autominder.app.domain.usecase.GetDashboardDataUseCase
import com.autominder.app.domain.usecase.ReminderPriorityEngine
import com.autominder.app.domain.usecase.ReminderWithStatus
import com.autominder.app.domain.usecase.VehicleWithStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private lateinit var getDashboardDataUseCase: GetDashboardDataUseCase
    private lateinit var fuelRepo: IFuelRepository
    private lateinit var serviceRepo: IServiceRepository
    private lateinit var mileageRepo: IMileageLogRepository
    private lateinit var calculateEfficiency: CalculateEfficiencyUseCase
    private lateinit var reminderPriorityEngine: ReminderPriorityEngine
    private lateinit var reviewHelper: ReviewHelper
    private lateinit var userPrefs: UserPreferences
    private lateinit var appInfo: AppInfo

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        getDashboardDataUseCase = mockk(relaxed = true)
        fuelRepo = mockk(relaxed = true)
        serviceRepo = mockk(relaxed = true)
        mileageRepo = mockk(relaxed = true)
        calculateEfficiency = CalculateEfficiencyUseCase()
        reminderPriorityEngine = ReminderPriorityEngine()
        reviewHelper = mockk(relaxed = true)
        userPrefs = mockk(relaxed = true)
        appInfo = mockk(relaxed = true) {
            every { firstInstallTimeMillis } returns 1000L
        }

        every { userPrefs.lastSuccessfulCheckAt } returns flowOf(System.currentTimeMillis())
        every { userPrefs.distanceUnit } returns flowOf("km")
        every { serviceRepo.getServicesForVehicle(any()) } returns flowOf(emptyList())
        every { mileageRepo.getLogsForVehicle(any()) } returns flowOf(emptyList())
        every { fuelRepo.getFuelEntriesForVehicle(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = DashboardViewModel(
        getDashboardDataUseCase = getDashboardDataUseCase,
        fuelRepository = fuelRepo,
        serviceRepository = serviceRepo,
        mileageLogRepository = mileageRepo,
        calculateEfficiency = calculateEfficiency,
        reminderPriorityEngine = reminderPriorityEngine,
        reviewHelper = reviewHelper,
        userPreferences = userPrefs,
        appInfo = appInfo,
        defaultDispatcher = dispatcher
    )

    @Test
    fun `emits Empty state when no vehicles exist`() = runTest(dispatcher) {
        every { getDashboardDataUseCase() } returns flowOf(
            DashboardData(
                vehiclesWithStatus = emptyList(),
                alertsCount = 0,
                upcomingReminders = emptyList()
            )
        )

        val viewModel = createViewModel()
        viewModel.uiState.test {
            assertEquals(DashboardUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Success state with computed driving telemetry when vehicle and fuel entries exist`() = runTest(dispatcher) {
        val vehicle = Vehicle(id = 1L, make = "Honda", model = "Civic", year = 2021, plateNumber = "HND-101", currentOdometer = 50000)
        val vehicleWithStatus = VehicleWithStatus(
            vehicle = vehicle,
            status = ServiceStatus.OK,
            overdueCount = 0,
            dueSoonCount = 0
        )
        val reminderOverdue = ReminderWithStatus(
            reminder = Reminder(id = 10L, vehicleId = 1L, serviceType = ServiceType.OIL_CHANGE),
            vehicle = vehicle,
            status = ServiceStatus.OVERDUE
        )

        every { getDashboardDataUseCase() } returns flowOf(
            DashboardData(
                vehiclesWithStatus = listOf(vehicleWithStatus),
                alertsCount = 1,
                upcomingReminders = listOf(reminderOverdue)
            )
        )

        val f1 = FuelEntry(id = 1L, vehicleId = 1L, date = Date(1000L), odometer = 49000, volumeMilliliters = 40000, costCents = 5000L)
        val f2 = FuelEntry(id = 2L, vehicleId = 1L, date = Date(2000L), odometer = 49500, volumeMilliliters = 40000, costCents = 5500L)
        every { fuelRepo.getFuelEntriesForVehicle(1L) } returns flowOf(listOf(f1, f2))

        val viewModel = createViewModel()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is DashboardUiState.Success)
            val success = state as DashboardUiState.Success
            assertEquals(1, success.vehicles.size)
            assertEquals(1, success.alertsCount)
            assertEquals(1, success.attentionReminders.size)
            assertEquals(ServiceStatus.OVERDUE, success.attentionReminders.first().status)
            assertNotNull(success.primaryAvgEfficiency)
            assertEquals(1, success.prioritizedReminders.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits SetupIncomplete status when vehicle odometer is zero`() = runTest(dispatcher) {
        val vehicle = Vehicle(id = 1L, make = "Toyota", model = "RAV4", year = 2023, currentOdometer = 0)
        val vehicleWithStatus = VehicleWithStatus(
            vehicle = vehicle,
            status = ServiceStatus.OK,
            overdueCount = 0,
            dueSoonCount = 0
        )

        every { getDashboardDataUseCase() } returns flowOf(
            DashboardData(
                vehiclesWithStatus = listOf(vehicleWithStatus),
                alertsCount = 0,
                upcomingReminders = emptyList()
            )
        )

        val viewModel = createViewModel()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is DashboardUiState.Success)
            val success = state as DashboardUiState.Success
            assertEquals(VehicleOperationalStatus.SETUP_INCOMPLETE, success.vehicleStatus)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `explainReminder produces accurate explanation from viewModel`() = runTest(dispatcher) {
        val vehicle = Vehicle(id = 1L, make = "Toyota", model = "RAV4", year = 2022, currentOdometer = 45000)
        val vehicleWithStatus = VehicleWithStatus(vehicle = vehicle, status = ServiceStatus.OK, overdueCount = 0, dueSoonCount = 0)
        val reminder = ReminderWithStatus(
            reminder = Reminder(id = 5L, vehicleId = 1L, serviceType = ServiceType.OIL_CHANGE, intervalKm = 10000, nextDueOdometer = 50000),
            vehicle = vehicle,
            status = ServiceStatus.OK
        )

        every { getDashboardDataUseCase() } returns flowOf(
            DashboardData(
                vehiclesWithStatus = listOf(vehicleWithStatus),
                alertsCount = 0,
                upcomingReminders = listOf(reminder)
            )
        )

        val viewModel = createViewModel()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is DashboardUiState.Success)
            val success = state as DashboardUiState.Success
            val prioritized = success.prioritizedReminders.first()
            val explanation = viewModel.explainReminder(prioritized)

            assertEquals(45000, explanation.currentOdometer)
            assertEquals(50000, explanation.targetDueOdometer)
            assertEquals(5000, explanation.remainingKm)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `requestReviewIfAppropriate forwards to reviewHelper`() = runTest(dispatcher) {
        val activity = mockk<Activity>(relaxed = true)
        coEvery { reviewHelper.requestReviewIfAppropriate(any()) } returns Unit

        val viewModel = createViewModel()
        viewModel.requestReviewIfAppropriate(activity)

        coVerify(exactly = 1) { reviewHelper.requestReviewIfAppropriate(activity) }
    }
}
