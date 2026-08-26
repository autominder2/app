package com.autominder.app.ui.screens.vehicle

import app.cash.turbine.test
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IReminderRepository
import com.autominder.app.domain.repository.IVehicleRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Note on assertions: [uiState] is a StateFlow whose upstream runs eagerly
 * under [UnconfinedTestDispatcher], so the declared `Loading` initialValue is
 * already superseded by the time Turbine subscribes. These tests therefore
 * assert the *settled* state via `expectMostRecentItem()` rather than
 * asserting an emission sequence that conflation legitimately collapses.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VehicleListViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var vehicleRepo: IVehicleRepository
    private lateinit var reminderRepo: IReminderRepository

    private val now = System.currentTimeMillis()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        vehicleRepo = mockk()
        reminderRepo = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun vehicle(id: Long, odometer: Int = 10_000) = Vehicle(
        id = id,
        make = "Honda",
        model = "Civic",
        year = 2020,
        currentOdometer = odometer
    )

    private fun overdueReminder(vehicleId: Long) = Reminder(
        id = 1,
        vehicleId = vehicleId,
        serviceType = ServiceType.OIL_CHANGE,
        nextDueDate = now - TimeUnit.DAYS.toMillis(1)
    )

    private fun dueSoonReminder(vehicleId: Long) = Reminder(
        id = 2,
        vehicleId = vehicleId,
        serviceType = ServiceType.TIRE_ROTATION,
        nextDueDate = now + TimeUnit.DAYS.toMillis(3)
    )

    private fun farFutureReminder(vehicleId: Long) = Reminder(
        id = 3,
        vehicleId = vehicleId,
        serviceType = ServiceType.BRAKE_SERVICE,
        nextDueDate = now + TimeUnit.DAYS.toMillis(90)
    )

    private fun createViewModel() = VehicleListViewModel(vehicleRepo, reminderRepo, dispatcher)

    @Test
    fun `empty repository produces Empty state`() = runTest {
        every { vehicleRepo.getAllVehicles() } returns flowOf(emptyList())
        every { reminderRepo.getAllPendingReminders() } returns flowOf(emptyList())

        createViewModel().uiState.test {
            assertEquals(VehicleListUiState.Empty, expectMostRecentItem())
        }
    }

    @Test
    fun `worst-status vehicle sorts first and carries its top concern`() = runTest {
        val ok = vehicle(1)
        val overdue = vehicle(2)
        val dueSoon = vehicle(3)
        every { vehicleRepo.getAllVehicles() } returns flowOf(listOf(ok, overdue, dueSoon))
        every { reminderRepo.getAllPendingReminders() } returns flowOf(
            listOf(farFutureReminder(ok.id), overdueReminder(overdue.id), dueSoonReminder(dueSoon.id))
        )

        createViewModel().uiState.test {
            val state = expectMostRecentItem() as VehicleListUiState.Success

            assertEquals(3, state.items.size)
            assertEquals(2, state.attentionCount) // overdue + due-soon, not the far-future OK one

            val (first, second, third) = state.items
            assertEquals(overdue.id, first.vehicle.id)
            assertEquals(ServiceStatus.OVERDUE, first.status)
            assertEquals(ServiceType.OIL_CHANGE, first.topConcern?.serviceType)

            assertEquals(dueSoon.id, second.vehicle.id)
            assertEquals(ServiceStatus.DUE_SOON, second.status)

            assertEquals(ok.id, third.vehicle.id)
            assertEquals(ServiceStatus.OK, third.status)
            assertNull(third.topConcern)
        }
    }

    @Test
    fun `vehicle with no reminders is UNKNOWN - never all-good from absent data`() = runTest {
        val lone = vehicle(1)
        every { vehicleRepo.getAllVehicles() } returns flowOf(listOf(lone))
        every { reminderRepo.getAllPendingReminders() } returns flowOf(emptyList())

        createViewModel().uiState.test {
            val state = expectMostRecentItem() as VehicleListUiState.Success
            assertEquals(1, state.items.size)
            assertEquals(0, state.attentionCount)
            assertEquals(ServiceStatus.UNKNOWN, state.items.first().status)
            assertNull(state.items.first().topConcern)
        }
    }

    @Test
    fun `vehicle with only a far-future reminder is OK with no concern`() = runTest {
        val lone = vehicle(1)
        every { vehicleRepo.getAllVehicles() } returns flowOf(listOf(lone))
        every { reminderRepo.getAllPendingReminders() } returns flowOf(listOf(farFutureReminder(lone.id)))

        createViewModel().uiState.test {
            val state = expectMostRecentItem() as VehicleListUiState.Success
            assertEquals(0, state.attentionCount)
            assertEquals(ServiceStatus.OK, state.items.first().status)
            // OK must never surface a concern line — concernText is only
            // legal for OVERDUE / DUE_SOON / the no-data UNKNOWN copy.
            assertNull(state.items.first().topConcern)
        }
    }

    @Test
    fun `repository failure produces Error state`() = runTest {
        every { vehicleRepo.getAllVehicles() } returns flow { throw RuntimeException("boom") }
        every { reminderRepo.getAllPendingReminders() } returns flowOf(emptyList())

        createViewModel().uiState.test {
            assertTrue(expectMostRecentItem() is VehicleListUiState.Error)
        }
    }

    @Test
    fun `retry re-subscribes after a failure`() = runTest {
        var attempt = 0
        every { vehicleRepo.getAllVehicles() } returns flow {
            attempt++
            if (attempt == 1) throw RuntimeException("boom") else emit(listOf(vehicle(1)))
        }
        every { reminderRepo.getAllPendingReminders() } returns flowOf(emptyList())

        val vm = createViewModel()
        vm.uiState.test {
            assertTrue(expectMostRecentItem() is VehicleListUiState.Error)

            vm.retry()

            // A `catch` completes a flow permanently; retry() must resubscribe
            // for the Error state's retry button to actually work.
            val recovered = expectMostRecentItem() as VehicleListUiState.Success
            assertEquals(1, recovered.items.size)
        }
    }
}
