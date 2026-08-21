package com.autominder.app.ui.screens.onboarding

import androidx.lifecycle.SavedStateHandle
import com.autominder.app.core.util.AnalyticsHelper
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.DrivingAmount
import com.autominder.app.domain.model.Reminder
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

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val prefs: UserPreferences = mockk(relaxed = true)
    private val vehicleRepo: IVehicleRepository = mockk(relaxed = true)
    private val reminderRepo: IReminderRepository = mockk(relaxed = true)
    private val analytics: AnalyticsHelper = mockk(relaxed = true)

    private lateinit var vm: OnboardingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        val initialState = slot<suspend (Long) -> Unit>()
        coEvery {
            vehicleRepo.insertVehicleWithInitialState(any(), capture(initialState))
        } coAnswers {
            initialState.captured.invoke(42L)
            42L
        }

        vm = createViewModel()
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ) = OnboardingViewModel(
            savedStateHandle = savedStateHandle,
            userPreferences = prefs,
            vehicleRepository = vehicleRepo,
            createDefaultReminders = CreateDefaultRemindersUseCase(reminderRepo),
            analyticsHelper = analytics
        )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fillValidForm() {
        vm.onBrandChanged("Toyota")
        vm.onModelChanged("Corolla")
        vm.onOdometerChanged("50000")
    }

    // ─── Reveal-before-save invariant ──────────────────────────────────────────

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
        val previewedPlan = vm.uiState.value.plan
        val insertedReminders = mutableListOf<Reminder>()
        coEvery { reminderRepo.insertReminder(capture(insertedReminders)) } answers {
            insertedReminders.size.toLong()
        }
        assertTrue(previewedPlan.isNotEmpty())

        vm.saveVehicle()

        assertTrue(vm.uiState.value.vehicleSaved)
        assertNull(vm.uiState.value.errorRes)
        coVerify(exactly = 1) { vehicleRepo.insertVehicleWithInitialState(any(), any()) }
        assertEquals(previewedPlan.size, insertedReminders.size)
        previewedPlan.zip(insertedReminders).forEach { (planned, persisted) ->
            assertEquals(planned.serviceType, persisted.serviceType)
            assertEquals(planned.intervalKm, persisted.intervalKm)
            assertEquals(planned.intervalDays, persisted.intervalDays)
            assertEquals(planned.nextDueOdometer, persisted.nextDueOdometer)
            assertEquals(planned.nextDueDate, persisted.nextDueDate)
        }
    }

    // ─── Mileage validation boundaries ─────────────────────────────────────────

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
    fun `invalid edit after preview clears stale plan and blocks save`() = runTest {
        fillValidForm()
        assertTrue(vm.previewPlan("mi"))
        assertTrue(vm.uiState.value.planReady)

        vm.onOdometerChanged("not a number")
        assertFalse(vm.previewPlan("mi"))

        assertFalse(vm.uiState.value.planReady)
        assertTrue(vm.uiState.value.plan.isEmpty())
        vm.saveVehicle()
        coVerify(exactly = 0) { vehicleRepo.insertVehicleWithInitialState(any(), any()) }
    }

    @Test
    fun `missing brand blocks preview`() {
        vm.onModelChanged("Corolla")
        vm.onOdometerChanged("50000")
        assertFalse(vm.previewPlan("mi"))
        assertNotNull(vm.uiState.value.errorRes)
    }

    // ─── Revision from the reveal without restarting ───────────────────────────

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
    fun `changing driving amount invalidates preview until recomputed`() {
        fillValidForm()
        assertTrue(vm.previewPlan("mi"))

        vm.onDrivingAmountChanged(DrivingAmount.HIGH)

        assertFalse(vm.uiState.value.planReady)
        assertTrue(vm.uiState.value.plan.isEmpty())
        assertTrue(vm.previewPlan("mi"))
        assertTrue(vm.uiState.value.planReady)
    }

    @Test
    fun `changing mileage from the reveal recomputes the km axis`() {
        fillValidForm()
        assertTrue(vm.previewPlan("mi"))
        // Both forms carry a real reading, so both anchor the distance axis —
        // a null here would mean the sentinel path was taken by mistake.
        val before = requireNotNull(vm.uiState.value.plan.first().nextDueOdometer)

        vm.onOdometerChanged("80000")
        assertTrue(vm.previewPlan("mi"))
        val after = requireNotNull(vm.uiState.value.plan.first().nextDueOdometer)

        assertTrue(after > before)
    }

    // ─── Regression: default-reminder creation path still runs in-transaction ──

    @Test
    fun `reminders are created inside the vehicle insert transaction lambda`() = runTest {
        fillValidForm()
        vm.previewPlan("mi")
        vm.saveVehicle()
        // The mock invokes the initialState lambda with id 42 — reminders must
        // land there, proving the atomic vehicle+plan path is preserved.
        coVerify(atLeast = 1) { reminderRepo.insertReminder(match { it.vehicleId == 42L }) }
    }

    @Test
    fun `revealed plan is reconstructed from saved state after process recreation`() {
        val restored = createViewModel(
            SavedStateHandle(
                mapOf(
                    "onb_brand" to "Toyota",
                    "onb_model" to "Corolla",
                    "onb_odometer" to "50000",
                    "onb_driving" to DrivingAmount.HIGH.name,
                    "onb_plan_revealed" to true,
                    "onb_display_unit" to "mi"
                )
            )
        )

        assertTrue(restored.uiState.value.planReady)
        assertEquals(DrivingAmount.HIGH, restored.uiState.value.drivingAmount)
    }

    // ─── VehicleCatalog integration tests ─────────────────────────────────────

    @Test
    fun `suggestedModels now sourced from VehicleCatalog and covers makes beyond the old 14-brand map`() {
        vm.onBrandChanged("Audi")
        assertTrue(vm.uiState.value.suggestedModels.contains("A4"))
        assertTrue(vm.uiState.value.suggestedModels.contains("Q5"))

        vm.onBrandChanged("Porsche")
        assertTrue(vm.uiState.value.suggestedModels.contains("911"))
        assertTrue(vm.uiState.value.suggestedModels.contains("Cayenne"))
    }

    @Test
    fun `unrecognized or custom make typed manually yields empty suggestedModels without error`() {
        vm.onBrandChanged("CustomUniqueKitCar")
        assertTrue(vm.uiState.value.suggestedModels.isEmpty())
        assertNull(vm.uiState.value.errorRes)
    }
}
