package com.autominder.app.ui.screens.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.autominder.app.domain.validation.Validators
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
    val error: String? = null
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
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddVehicleUiState())
    val uiState: StateFlow<AddVehicleUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddVehicleUiEvent) {
        when (event) {
            is AddVehicleUiEvent.BrandChanged -> _uiState.value = _uiState.value.copy(brand = event.brand)
            is AddVehicleUiEvent.ModelChanged -> _uiState.value = _uiState.value.copy(model = event.model)
            is AddVehicleUiEvent.YearChanged -> _uiState.value = _uiState.value.copy(year = event.year)
            is AddVehicleUiEvent.OdometerChanged -> _uiState.value = _uiState.value.copy(currentOdometer = event.odometer)
            is AddVehicleUiEvent.PlateNumberChanged -> _uiState.value = _uiState.value.copy(plateNumber = event.plateNumber)
            is AddVehicleUiEvent.VinChanged -> _uiState.value = _uiState.value.copy(vin = event.vin)
            is AddVehicleUiEvent.PhotoUriChanged -> _uiState.value = _uiState.value.copy(photoUri = event.uri)
            is AddVehicleUiEvent.SaveClicked -> saveVehicle()
        }
    }

    private fun saveVehicle() {
        val state = _uiState.value
        if (state.brand.isBlank() || state.model.isBlank()) {
            _uiState.value = state.copy(error = "Brand and Model are required")
            return
        }

        val yearInt = state.year.toIntOrNull() ?: 0
        Validators.validateYear(yearInt)?.let { errorMsg ->
            _uiState.value = state.copy(error = errorMsg)
            return
        }
        val odometerDisplay = state.currentOdometer.toIntOrNull() ?: 0

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
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
                val newVehicleId = vehicleRepository.insertVehicle(newVehicle)
                createDefaultReminders(newVehicleId, odometerKm)
                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to save vehicle")
            }
        }
    }
}
