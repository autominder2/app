package com.autominder.app.ui.screens.onboarding

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autominder.app.R
import com.autominder.app.core.util.AnalyticsEvents
import com.autominder.app.core.util.AnalyticsHelper
import com.autominder.app.core.util.AnalyticsParams
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.usecase.CreateDefaultRemindersUseCase
import com.autominder.app.domain.util.DistanceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class OnboardingUiState(
    val brand: String = "",
    val model: String = "",
    val odometer: String = "",
    val isSaving: Boolean = false,
    val vehicleSaved: Boolean = false,
    @StringRes val errorRes: Int? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val vehicleRepository: IVehicleRepository,
    private val createDefaultReminders: CreateDefaultRemindersUseCase,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        analyticsHelper.logEvent(AnalyticsEvents.ONBOARDING_STARTED)
    }

    fun onBrandChanged(value: String) {
        _uiState.value = _uiState.value.copy(brand = value, errorRes = null)
    }

    fun onModelChanged(value: String) {
        _uiState.value = _uiState.value.copy(model = value, errorRes = null)
    }

    fun onOdometerChanged(value: String) {
        _uiState.value = _uiState.value.copy(odometer = value, errorRes = null)
    }

    /**
     * Activation-first onboarding: saves the user's first vehicle inline,
     * mirroring AddVehicleViewModel's proven path — atomic vehicle +
     * default-reminder creation, odometer converted from the display unit.
     */
    fun saveVehicle() {
        val state = _uiState.value
        if (state.isSaving || state.vehicleSaved) return
        if (state.brand.isBlank() || state.model.isBlank()) {
            _uiState.value = state.copy(errorRes = R.string.onboarding_error_brand_model)
            return
        }
        val odometerDisplay = state.odometer.toIntOrNull() ?: 0

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorRes = null)
            try {
                val unit = userPreferences.distanceUnit.first()
                val odometerKm = DistanceUtil.displayToKm(odometerDisplay, unit)
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
                    createDefaultReminders(vehicleId, odometerKm)
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
}
