package com.autominder.app.ui.screens.onboarding

import androidx.lifecycle.SavedStateHandle
import com.autominder.app.core.util.AnalyticsHelper
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.DrivingAmount
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.repository.IReminderRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.usecase.CreateDefaultRemindersUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * State-machine proof for Phase 1: the plan reveal structurally precedes
 * both the save and (therefore) the notification-permission step, because
 * saveVehicle refuses to run until previewPlan has produced a plan, and the
 * NOTIFY step is only reachable via vehicleSaved.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private lateinit var vehicleRepo: IVehicleRepository
    private lateinit var reminderRepo: IReminderRepository
    private lateinit var prefs: UserPreferences
    private lateinit var analytics: AnalyticsHelper
    private lateinit var vm: OnboardingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        vehicleRepo = mockk()
        reminderRepo = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        analytics = mockk(relaxed = true)

        val initialState = slot<suspend (Long) -> Unit>()
        coEvery {
            vehicleRepo.insertVehicleWithInitialState(any(), capture(initialState))
        } coAnswers {
            initialState.captured.invoke(42L)
            42L
        }

        vm = OnboardingViewModel(
            savedStateHandle = SavedStateHandle(),
            userPreferences = prefs,
            vehicleRepository = vehicleRepo,
            createDefaultReminders = CreateDefaultRemindersUseCase(reminderRepo),
            analyticsHelper = analytics
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fillValidForm() {
        vm.onBrandChanged("Toyota")
        vm.onModelChanged("Corolla")
        vm.onOdometerChanged("50000")
    }

    // ── Reveal-before-save invariant ────────────────────────────────────────

    @Test
    fun `saveVehicle refuses to run before previewPlan`() = runTest {
        fillValidForm()
        vm.saveVehicle() // no preview happened

        assertFalse(vm.uiState.value.vehicleSaved)
        assertNotNull(vm.uiState.value.errorRes)
        coVerify(exactly = 0) { vehicleRepo.insertVehicleWithInitialState(any(), any()) }
    }

    @Test
    fun `preview then save persists vehicle and exactly the previewed plan`() = runTest {
        fillValidForm()
        assertTrue(vm.previewPlan("mi"))
        val planSize = vm.uiState.value.plan.size
        assertTrue(planSize > 0)

        vm.saveVehicle()

        assertTrue(vm.uiState.value.vehicleSaved)
        assertNull(vm.uiState.value.errorRes)
        coVerify(exactly = 1) { vehicleRepo.insertVehicleWithInitialState(any(), any()) }
        coVerify(exactly = planSize) { reminderRepo.insertReminder(any()) }
    }

    // ── Mileage validation boundaries ───────────────────────────────────────

    @Test
    fun `blank mileage is allowed and treated as zero`() {
        vm.onBrandChanged("Toyota"); vm.onModelChanged("Corolla")
        vm.onOdometerChanged("")
        assertTrue(vm.previewPlan("mi"))
        assertEquals(0, vm.uiState.value.planOdometerKm)
    }

    @Test
    fun `non-numeric mileage is rejected with an error`() {
        fillValidForm()
        vm.onOdometerChanged("about 50k")
        assertFalse(vm.previewPlan("mi"))
        assertNotNull(vm.uiState.value.errorRes)
        assertTrue(vm.uiState.value.plan.isEmpty())
    }

    @Test
    fun `negative mileage is rejected`() {
        fillValidForm()
        vm.onOdometerChanged("-5")
        assertFalse(vm.previewPlan("mi"))
        assertNotNull(vm.uiState.value.errorRes)
    }

    @Test
    fun `implausibly high mileage is rejected`() {
        fillValidForm()
        vm.onOdometerChanged("1000001")
        assertFalse(vm.previewPlan("mi"))
        assertNotNull(vm.uiState.value.errorRes)
    }

    @Test
    fun `missing brand blocks preview`() {
        vm.onModelChanged("Corolla")
        vm.onOdometerChanged("50000")
        assertFalse(vm.previewPlan("mi"))
        assertNotNull(vm.uiState.value.errorRes)
    }

    // ── Revision from the reveal without restarting ────────────────────────

    @Test
    fun `changing driving amount recomputes the plan dates`() {
        fillValidForm()
        assertTrue(vm.previewPlan("mi"))
        val typicalOil = vm.uiState.value.plan
            .first { it.serviceType == ServiceType.OIL_CHANGE }.nextDueDate

        vm.onDrivingAmountChanged(DrivingAmount.HIGH)
        assertTrue(vm.previewPlan("mi"))
        val highOil = vm.uiState.value.plan
            .first { it.serviceType == ServiceType.OIL_CHANGE }.nextDueDate

        assertTrue("HIGH must move the oil date sooner", highOil < typicalOil)
    }

    @Test
    fun `changing mileage from the reveal recomputes the km axis`() {
        fillValidForm()
        assertTrue(vm.previewPlan("mi"))
        val before = vm.uiState.value.plan.first().nextDueOdometer

        vm.onOdometerChanged("80000")
        assertTrue(vm.previewPlan("mi"))
        val after = vm.uiState.value.plan.first().nextDueOdometer

        assertTrue(after > before)
    }

    // ── Regression: default-reminder creation path still runs in-transaction ──

    @Test
    fun `reminders are created inside the vehicle insert transaction lambda`() = runTest {
        fillValidForm()
        vm.previewPlan("mi")
        vm.saveVehicle()
        // The mock invokes the initialState lambda with id 42 — reminders must
        // land there, proving the atomic vehicle+plan path is preserved.
        coVerify(atLeast = 1) { reminderRepo.insertReminder(match { it.vehicleId == 42L }) }
    }
}
