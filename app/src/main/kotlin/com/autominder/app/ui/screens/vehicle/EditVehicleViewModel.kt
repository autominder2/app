package com.autominder.app.ui.screens.vehicle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.autominder.app.domain.validation.Validators
import javax.inject.Inject

data class EditVehicleUiState(
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val plateNumber: String = "",
    val vin: String = "",
    val currentOdometer: String = "",
    val notes: String = "",
    val photoUri: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

sealed class EditVehicleUiEvent {
    data class MakeChanged(val make: String) : EditVehicleUiEvent()
    data class ModelChanged(val model: String) : EditVehicleUiEvent()
    data class YearChanged(val year: String) : EditVehicleUiEvent()
    data class PlateNumberChanged(val plateNumber: String) : EditVehicleUiEvent()
    data class VinChanged(val vin: String) : EditVehicleUiEvent()
    data class OdometerChanged(val odometer: String) : EditVehicleUiEvent()
    data class NotesChanged(val notes: String) : EditVehicleUiEvent()
    data class PhotoUriChanged(val uri: String?) : EditVehicleUiEvent()
    object SaveClicked : EditVehicleUiEvent()
}

@HiltViewModel
class EditVehicleViewModel @Inject constructor(
    private val vehicleRepository: IVehicleRepository,
    private val userPreferences: UserPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.toRoute<NavRoutes.EditVehicle>().vehicleId

    private val _uiState = MutableStateFlow(EditVehicleUiState())
    val uiState: StateFlow<EditVehicleUiState> = _uiState.asStateFlow()

    init {
        loadVehicle()
    }

    private fun loadVehicle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val vehicle = vehicleRepository.getVehicleById(vehicleId).firstOrNull()
                if (vehicle != null) {
                    val unit = userPreferences.distanceUnit.first()
                    _uiState.value = _uiState.value.copy(
                        make = vehicle.make,
                        model = vehicle.model,
                        year = vehicle.year.toString(),
                        plateNumber = vehicle.plateNumber,
                        vin = vehicle.vin ?: "",
                        currentOdometer = DistanceUtil.kmToDisplay(vehicle.currentOdometer, unit).toString(),
                        notes = vehicle.notes,
                        photoUri = vehicle.photoUri,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Vehicle not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load vehicle"
                )
            }
        }
    }

    fun onEvent(event: EditVehicleUiEvent) {
        when (event) {
            is EditVehicleUiEvent.MakeChanged -> _uiState.value = _uiState.value.copy(make = event.make)
            is EditVehicleUiEvent.ModelChanged -> _uiState.value = _uiState.value.copy(model = event.model)
            is EditVehicleUiEvent.YearChanged -> _uiState.value = _uiState.value.copy(year = event.year)
            is EditVehicleUiEvent.PlateNumberChanged -> _uiState.value = _uiState.value.copy(plateNumber = event.plateNumber)
            is EditVehicleUiEvent.VinChanged -> _uiState.value = _uiState.value.copy(vin = event.vin)
            is EditVehicleUiEvent.OdometerChanged -> _uiState.value = _uiState.value.copy(currentOdometer = event.odometer)
            is EditVehicleUiEvent.NotesChanged -> _uiState.value = _uiState.value.copy(notes = event.notes)
            is EditVehicleUiEvent.PhotoUriChanged -> _uiState.value = _uiState.value.copy(photoUri = event.uri)
            is EditVehicleUiEvent.SaveClicked -> saveVehicle()
        }
    }

    private fun saveVehicle() {
        val state = _uiState.value
        if (state.make.isBlank() || state.model.isBlank()) {
            _uiState.value = state.copy(error = "Make and Model are required")
            return
        }

        val yearInt = state.year.toIntOrNull() ?: 0
        Validators.validateYear(yearInt)?.let { errorMsg ->
            _uiState.value = state.copy(error = errorMsg)
            return
        }
        val odometerDisplay = state.currentOdometer.toIntOrNull() ?: 0
        val vinValue = state.vin.ifBlank { null }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val unit = userPreferences.distanceUnit.first()
                val odometerKm = DistanceUtil.displayToKm(odometerDisplay, unit)
                val existing = vehicleRepository.getVehicleById(vehicleId).firstOrNull()
                if (existing != null) {
                    val updated = existing.copy(
                        make = state.make,
                        model = state.model,
                        year = yearInt,
                        plateNumber = state.plateNumber,
                        vin = vinValue,
                        currentOdometer = odometerKm,
                        notes = state.notes,
                        photoUri = state.photoUri,
                        updatedAt = System.currentTimeMillis()
                    )
                    vehicleRepository.updateVehicle(updated)
                    _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Vehicle not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save vehicle"
                )
            }
        }
    }
}
