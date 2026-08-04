package com.autominder.app.ui.screens.onboarding

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autominder.app.R
import com.autominder.app.core.util.AnalyticsEvents
import com.autominder.app.core.util.AnalyticsHelper
import com.autominder.app.core.util.AnalyticsParams
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.DrivingAmount
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.usecase.CreateDefaultRemindersUseCase
import com.autominder.app.domain.usecase.PlannedReminder
import com.autominder.app.domain.util.DistanceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class OnboardingUiState(
    val brand: String = "",
    val model: String = "",
    val odometer: String = "",
    val drivingAmount: DrivingAmount = DrivingAmount.TYPICAL,
    /** Seeded plan preview — non-empty means the reveal step has real content. */
    val plan: List<PlannedReminder> = emptyList(),
    /** Odometer in km captured at preview time; save consumes exactly this. */
    val planOdometerKm: Int? = null,
    val isSaving: Boolean = false,
    val vehicleSaved: Boolean = false,
    @StringRes val errorRes: Int? = null
) {
    val planReady: Boolean get() = plan.isNotEmpty() && planOdometerKm != null
}

/**
 * Activation-first onboarding with a plan reveal BEFORE the notification
 * permission ask. Invariant enforced here (and unit-tested): [saveVehicle]
 * refuses to run until [previewPlan] has produced a plan — so the reveal
 * step structurally precedes both the save and the permission request.
 *
 * Form fields live in [SavedStateHandle] so rotation, backgrounding, and
 * ordinary process recreation all restore the user's input.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userPreferences: UserPreferences,
    private val vehicleRepository: IVehicleRepository,
    private val createDefaultReminders: CreateDefaultRemindersUseCase,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OnboardingUiState(
            brand = savedStateHandle[KEY_BRAND] ?: "",
            model = savedStateHandle[KEY_MODEL] ?: "",
            odometer = savedStateHandle[KEY_ODOMETER] ?: "",
            drivingAmount = DrivingAmount.fromNameOrDefault(savedStateHandle[KEY_DRIVING])
        )
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        analyticsHelper.logEvent(AnalyticsEvents.ONBOARDING_STARTED)
        if (savedStateHandle.get<Boolean>(KEY_PLAN_REVEALED) == true) {
            previewPlan(savedStateHandle.get<String>(KEY_DISPLAY_UNIT) ?: "km")
        }
    }

    fun onBrandChanged(value: String) {
        savedStateHandle[KEY_BRAND] = value
        invalidatePlan()
        _uiState.value = _uiState.value.copy(brand = value, errorRes = null, plan = emptyList(), planOdometerKm = null)
    }

    fun onModelChanged(value: String) {
        savedStateHandle[KEY_MODEL] = value
        invalidatePlan()
        _uiState.value = _uiState.value.copy(model = value, errorRes = null, plan = emptyList(), planOdometerKm = null)
    }

    fun onOdometerChanged(value: String) {
        savedStateHandle[KEY_ODOMETER] = value
        invalidatePlan()
        _uiState.value = _uiState.value.copy(
            odometer = value,
            errorRes = null,
            plan = emptyList(),
            planOdometerKm = null
        )
    }

    fun onDrivingAmountChanged(value: DrivingAmount) {
        savedStateHandle[KEY_DRIVING] = value.name
        invalidatePlan()
        _uiState.value = _uiState.value.copy(
            drivingAmount = value,
            errorRes = null,
            plan = emptyList(),
            planOdometerKm = null
        )
    }

    private fun invalidatePlan() {
        savedStateHandle[KEY_PLAN_REVEALED] = false
    }

    /**
     * Pure, synchronous plan computation from current inputs. Returns true
     * when a plan is ready (caller advances to the reveal step). Mileage may
     * be revised from the reveal — call again and the plan recomputes.
     *
     * @param displayUnit the user's display unit ("km"/"mi") supplied by the
     *   UI so this stays free of async preference reads.
     */
    fun previewPlan(displayUnit: String): Boolean {
        val state = _uiState.value
        if (state.brand.isBlank() || state.model.isBlank()) {
            _uiState.value = state.copy(errorRes = R.string.onboarding_error_brand_model)
            return false
        }
        val odometerDisplay = state.odometer.trim().let {
            if (it.isEmpty()) 0 else it.toIntOrNull()
        }
        if (odometerDisplay == null || odometerDisplay < 0) {
            _uiState.value = state.copy(errorRes = R.string.onboarding_error_invalid_mileage)
            return false
        }
        if (odometerDisplay > MAX_PLAUSIBLE_DISPLAY) {
            _uiState.value = state.copy(errorRes = R.string.onboarding_error_mileage_too_high)
            return false
        }
        val odometerKm = DistanceUtil.displayToKm(odometerDisplay, displayUnit)
        val plan = CreateDefaultRemindersUseCase.buildPlan(
            currentOdometerKm = odometerKm,
            drivingAmount = state.drivingAmount,
            nowMillis = System.currentTimeMillis()
        )
        savedStateHandle[KEY_PLAN_REVEALED] = true
        savedStateHandle[KEY_DISPLAY_UNIT] = displayUnit
        _uiState.value = state.copy(plan = plan, planOdometerKm = odometerKm, errorRes = null)
        return true
    }

    /**
     * Saves the vehicle plus exactly the previewed plan. Refuses to run
     * before [previewPlan] — the reveal must come first (tested invariant).
     */
    fun saveVehicle() {
        val state = _uiState.value
        if (state.isSaving || state.vehicleSaved) return
        val odometerKm = state.planOdometerKm
        if (!state.planReady || odometerKm == null) {
            _uiState.value = state.copy(errorRes = R.string.onboarding_error_plan_required)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorRes = null)
            try {
                val now = System.currentTimeMillis()
                val newVehicle = Vehicle(
                    id = 0,
                    make = state.brand.trim(),
                    model = state.model.trim(),
                    year = 0,
                    currentOdometer = odometerKm,
                    createdAt = now,
                    updatedAt = now
                )

                vehicleRepository.insertVehicleWithInitialState(newVehicle) { vehicleId ->
                    createDefaultReminders.persistPlan(
                        vehicleId = vehicleId,
                        plan = state.plan,
                        createdAt = now
                    )
                }

                analyticsHelper.logEvent(
                    AnalyticsEvents.VEHICLE_ADDED,
                    mapOf(
                        AnalyticsParams.VEHICLE_MAKE to state.brand,
                        AnalyticsParams.VEHICLE_MODEL to state.model
                    )
                )

                _uiState.value = _uiState.value.copy(isSaving = false, vehicleSaved = true)
            } catch (e: Exception) {
                Timber.e(e, "Failed to save vehicle during onboarding")
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorRes = R.string.onboarding_error_save_failed
                )
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.setHasSeenOnboarding(true)
            analyticsHelper.logEvent(AnalyticsEvents.ONBOARDING_COMPLETED)
        }
    }

    companion object {
        private const val KEY_BRAND = "onb_brand"
        private const val KEY_MODEL = "onb_model"
        private const val KEY_ODOMETER = "onb_odometer"
        private const val KEY_DRIVING = "onb_driving"
        private const val KEY_PLAN_REVEALED = "onb_plan_revealed"
        private const val KEY_DISPLAY_UNIT = "onb_display_unit"

        /** 1,000,000 display units — beyond any plausible odometer. */
        const val MAX_PLAUSIBLE_DISPLAY = 1_000_000
    }
}
