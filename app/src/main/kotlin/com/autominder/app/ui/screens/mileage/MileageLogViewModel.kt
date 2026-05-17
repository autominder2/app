package com.autominder.app.ui.screens.mileage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.MileageLogEntry
import com.autominder.app.domain.repository.IMileageLogRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MileageLogUiState(
    val logs: List<MileageLogEntry> = emptyList(),
    val newOdometer: String = "",
    val newNotes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loadError: String? = null
)

sealed class MileageLogUiEvent {
    data class NewOdometerChanged(val odometer: String) : MileageLogUiEvent()
    data class NewNotesChanged(val notes: String) : MileageLogUiEvent()
    object AddClicked : MileageLogUiEvent()
    object Retry : MileageLogUiEvent()
    data class DeleteLog(val log: MileageLogEntry) : MileageLogUiEvent()
}

@HiltViewModel
class MileageLogViewModel @Inject constructor(
    private val mileageLogRepository: IMileageLogRepository,
    private val vehicleRepository: IVehicleRepository,
    private val userPreferences: UserPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.toRoute<NavRoutes.MileageLog>().vehicleId

    private val _uiState = MutableStateFlow(MileageLogUiState())
    val uiState: StateFlow<MileageLogUiState> = _uiState.asStateFlow()

    init {
        loadLogs()
    }

    private fun loadLogs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loadError = null)
            mileageLogRepository.getLogsForVehicle(vehicleId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loadError = e.message ?: "Failed to load mileage logs"
                    )
                }
                .collect { logs ->
                    _uiState.value = _uiState.value.copy(
                        logs = logs.sortedByDescending { it.loggedAt },
                        isLoading = false
                    )
                }
        }
    }

    fun onEvent(event: MileageLogUiEvent) {
        when (event) {
            is MileageLogUiEvent.NewOdometerChanged -> _uiState.value = _uiState.value.copy(newOdometer = event.odometer)
            is MileageLogUiEvent.NewNotesChanged -> _uiState.value = _uiState.value.copy(newNotes = event.notes)
            is MileageLogUiEvent.AddClicked -> addLog()
            is MileageLogUiEvent.Retry -> loadLogs()
            is MileageLogUiEvent.DeleteLog -> deleteLog(event.log)
        }
    }

    private fun addLog() {
        val state = _uiState.value
        val odometerInt = state.newOdometer.toIntOrNull()
        if (odometerInt == null || odometerInt < 0) {
            _uiState.value = state.copy(error = "Please enter a valid odometer reading")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            try {
                val unit = userPreferences.distanceUnit.first()
                val odometerKm = DistanceUtil.displayToKm(odometerInt, unit)
                val vehicle = vehicleRepository.getVehicleById(vehicleId).firstOrNull()
                
                if (vehicle != null && odometerKm < vehicle.currentOdometer) {
                    _uiState.value = _uiState.value.copy(
                        error = "Odometer reading cannot be lower than current (${DistanceUtil.kmToDisplay(vehicle.currentOdometer, unit)})"
                    )
                    return@launch
                }

                val now = System.currentTimeMillis()
                val entry = MileageLogEntry(
                    id = 0,
                    vehicleId = vehicleId,
                    odometer = odometerKm,
                    loggedAt = now,
                    notes = state.newNotes.ifBlank { null }
                )
                mileageLogRepository.insertLog(entry)
                vehicleRepository.updateOdometer(vehicleId, odometerKm)
                _uiState.value = _uiState.value.copy(
                    newOdometer = "",
                    newNotes = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add mileage log"
                )
            }
        }
    }

    private fun deleteLog(log: MileageLogEntry) {
        viewModelScope.launch {
            try {
                mileageLogRepository.deleteLog(log)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete log entry"
                )
            }
        }
    }
}
