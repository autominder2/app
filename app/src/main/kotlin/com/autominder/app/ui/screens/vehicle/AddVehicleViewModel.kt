package com.autominder.app.ui.screens.vehicle

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
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
import com.autominder.app.domain.validation.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class AddVehicleUiState(
    val brand: String = "",
    val model: String = "",
    val year: String = "",
    val currentOdometer: String = "",
    val plateNumber: String = "",
    val vin: String = "",
    val photoUri: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val errorArgs: List<Any> = emptyList()
)

sealed class AddVehicleUiEvent {
    data class BrandChanged(val brand: String) : AddVehicleUiEvent()
    data class ModelChanged(val model: String) : AddVehicleUiEvent()
    data class YearChanged(val year: String) : AddVehicleUiEvent()
    data class OdometerChanged(val odometer: String) : AddVehicleUiEvent()
    data class PlateNumberChanged(val plateNumber: String) : AddVehicleUiEvent()
    data class VinChanged(val vin: String) : AddVehicleUiEvent()
    data class PhotoUriChanged(val uri: String?) : AddVehicleUiEvent()
    object SaveClicked : AddVehicleUiEvent()
}

@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    private val vehicleRepository: IVehicleRepository,
    private val createDefaultReminders: CreateDefaultRemindersUseCase,
    private val userPreferences: UserPreferences,
    private val analyticsHelper: AnalyticsHelper,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_BRAND = "brand"
        private const val KEY_MODEL = "model"
        private const val KEY_YEAR = "year"
        private const val KEY_ODOMETER = "odometer"
    }

    private val _uiState = MutableStateFlow(
        AddVehicleUiState(
            brand = savedStateHandle[KEY_BRAND] ?: "",
            model = savedStateHandle[KEY_MODEL] ?: "",
            year = savedStateHandle[KEY_YEAR] ?: "",
            currentOdometer = savedStateHandle[KEY_ODOMETER] ?: ""
        )
    )
    val uiState: StateFlow<AddVehicleUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddVehicleUiEvent) {
        when (event) {
            is AddVehicleUiEvent.BrandChanged -> {
                _uiState.value = _uiState.value.copy(brand = event.brand)
                savedStateHandle[KEY_BRAND] = event.brand
            }
            is AddVehicleUiEvent.ModelChanged -> {
                _uiState.value = _uiState.value.copy(model = event.model)
                savedStateHandle[KEY_MODEL] = event.model
            }
            is AddVehicleUiEvent.YearChanged -> {
                _uiState.value = _uiState.value.copy(year = event.year)
                savedStateHandle[KEY_YEAR] = event.year
            }
            is AddVehicleUiEvent.OdometerChanged -> {
                _uiState.value = _uiState.value.copy(currentOdometer = event.odometer)
                savedStateHandle[KEY_ODOMETER] = event.odometer
            }
            is AddVehicleUiEvent.PlateNumberChanged -> _uiState.value = _uiState.value.copy(plateNumber = event.plateNumber)
            is AddVehicleUiEvent.VinChanged -> _uiState.value = _uiState.value.copy(vin = event.vin)
            is AddVehicleUiEvent.PhotoUriChanged -> _uiState.value = _uiState.value.copy(photoUri = event.uri)
            is AddVehicleUiEvent.SaveClicked -> saveVehicle()
        }
    }

    private fun saveVehicle() {
        val state = _uiState.value
        if (state.brand.isBlank() || state.model.isBlank()) {
            _uiState.value = state.copy(errorRes = R.string.error_brand_model_required, errorArgs = emptyList())
            return
        }

        // Year is optional — only validate a value the user actually entered,
        // so "just make + model" can be saved without touching year.
        val yearInt = state.year.toIntOrNull() ?: 0
        if (state.year.isNotBlank()) {
            // Validators.kt returns raw English text (out of scope for this
            // cleanup pass) — passed through error_validation_passthrough.
            Validators.validateYear(yearInt)?.let { errorMsg ->
                _uiState.value = state.copy(
                    errorRes = R.string.error_validation_passthrough,
                    errorArgs = listOf(errorMsg)
                )
                return
            }
        }
        Validators.validateVin(state.vin)?.let { errorMsg ->
            _uiState.value = state.copy(
                errorRes = R.string.error_validation_passthrough,
                errorArgs = listOf(errorMsg)
            )
            return
        }
        val odometerDisplay = state.currentOdometer.toIntOrNull() ?: 0

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorRes = null, errorArgs = emptyList())
            try {
                val unit = userPreferences.distanceUnit.first()
                val odometerKm = DistanceUtil.displayToKm(odometerDisplay, unit)
                val now = System.currentTimeMillis()
                val newVehicle = Vehicle(
                    id = 0,
                    make = state.brand,
                    model = state.model,
                    year = yearInt,
                    currentOdometer = odometerKm,
                    plateNumber = state.plateNumber,
                    vin = state.vin.ifBlank { null },
                    photoUri = state.photoUri,
                    isArchived = false,
                    createdAt = now,
                    updatedAt = now
                )

                // Expertise: Atomic transactional creation of vehicle + reminders
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

                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                Timber.e(e, "Failed to save vehicle")
                _uiState.value = _uiState.value.copy(isLoading = false, errorRes = R.string.error_save_vehicle_failed, errorArgs = emptyList())
            }
        }
    }
}
