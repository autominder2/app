package com.autominder.app.ui.screens.fuel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.autominder.app.domain.model.FuelEntry
import com.autominder.app.domain.repository.IFuelRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class AddFuelUiState(
    val vehicleId: Long,
    val volume: String = "",
    val cost: String = "",
    val odometer: String = "",
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddFuelViewModel @Inject constructor(
    private val fuelRepository: IFuelRepository,
    private val vehicleRepository: IVehicleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.toRoute<NavRoutes.AddFuel>().vehicleId
    private val _uiState = MutableStateFlow(AddFuelUiState(vehicleId = vehicleId))
    val uiState: StateFlow<AddFuelUiState> = _uiState.asStateFlow()

    init {
        // Optimistic odometer: pre-fill with vehicle's current odometer
        viewModelScope.launch {
            vehicleRepository.getVehicleById(vehicleId).collect { vehicle ->
                vehicle?.let { v ->
                    if (_uiState.value.odometer.isEmpty()) {
                        _uiState.update { it.copy(odometer = v.currentOdometer.toString()) }
                    }
                }
            }
        }
    }

    fun onVolumeChanged(volume: String) {
        _uiState.update { it.copy(volume = volume) }
    }

    fun onCostChanged(cost: String) {
        _uiState.update { it.copy(cost = cost) }
    }

    fun onOdometerChanged(odometer: String) {
        _uiState.update { it.copy(odometer = odometer) }
    }

    fun onDateChanged(date: Long) {
        _uiState.update { it.copy(date = date) }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun saveFuelEntry() {
        val state = _uiState.value
        val volumeDbl = state.volume.toDoubleOrNull() ?: 0.0
        val costDbl = state.cost.toDoubleOrNull() ?: 0.0
        val odoInt = state.odometer.toIntOrNull() ?: 0

        if (volumeDbl <= 0 || costDbl <= 0 || odoInt <= 0) {
            _uiState.update { it.copy(error = "Please fill all fields with valid numbers") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val entry = FuelEntry(
                    vehicleId = vehicleId,
                    date = Date(state.date),
                    odometer = odoInt,
                    volumeMilliliters = (volumeDbl * 1000).toInt(),
                    costCents = (costDbl * 100).toLong(),
                    notes = state.notes
                )
                fuelRepository.insertFuelEntry(entry)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save") }
            }
        }
    }
}
