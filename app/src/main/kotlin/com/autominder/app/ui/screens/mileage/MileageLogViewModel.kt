package com.autominder.app.ui.screens.mileage

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.autominder.app.R
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.MileageLogEntry
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IMileageLogRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class MileageLogUiState(
    val vehicle: Vehicle? = null,
    val logs: List<MileageLogEntry> = emptyList(),
    val distanceUnit: String = "km",
    val newOdometer: String = "",
    val newNotes: String = "",
    val deltaSinceLastLog: Int? = null,
    val isLoading: Boolean = false,
    val isAddedSuccess: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val errorArgs: List<Any> = emptyList(),
    @StringRes val loadErrorRes: Int? = null
)

sealed class MileageLogUiEvent {
    data class NewOdometerChanged(val odometer: String) : MileageLogUiEvent()
    data class NewNotesChanged(val notes: String) : MileageLogUiEvent()
    data class StepOdometer(val delta: Int) : MileageLogUiEvent()
    data class SelectTag(val tag: String) : MileageLogUiEvent()
    data object AddClicked : MileageLogUiEvent()
    data object Retry : MileageLogUiEvent()
    data object ResetSuccess : MileageLogUiEvent()
    data class DeleteLog(val log: MileageLogEntry) : MileageLogUiEvent()
    data class UndoDelete(val log: MileageLogEntry) : MileageLogUiEvent()
}

@HiltViewModel
class MileageLogViewModel @Inject constructor(
    private val mileageLogRepository: IMileageLogRepository,
    private val vehicleRepository: IVehicleRepository,
    private val userPreferences: UserPreferences,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.toRoute<NavRoutes.MileageLog>().vehicleId

    private val KEY_ODOMETER = "key_mileage_odometer"
    private val KEY_NOTES = "key_mileage_notes"

    private val _uiState = MutableStateFlow(
        MileageLogUiState(
            newOdometer = savedStateHandle.get<String>(KEY_ODOMETER) ?: "",
            newNotes = savedStateHandle.get<String>(KEY_NOTES) ?: ""
        )
    )
    val uiState: StateFlow<MileageLogUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loadErrorRes = null)

            combine(
                vehicleRepository.getVehicleById(vehicleId),
                mileageLogRepository.getLogsForVehicle(vehicleId),
                userPreferences.distanceUnit
            ) { vehicle, logs, unit ->
                Triple(vehicle, logs, unit)
            }.catch { e ->
                Timber.e(e, "Failed to load mileage logs and vehicle")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loadErrorRes = R.string.error_load_mileage_failed
                )
            }.collect { (vehicle, logs, unit) ->
                val sortedLogs = logs.sortedByDescending { it.loggedAt }
                val currentOdo = vehicle?.currentOdometer ?: 0
                val lastLogOdo = sortedLogs.firstOrNull()?.odometer ?: currentOdo

                val currentInputInt = _uiState.value.newOdometer.toIntOrNull()
                val currentInputKm = currentInputInt?.let { DistanceUtil.displayToKm(it, unit) }
                val delta = if (currentInputKm != null && currentInputKm > lastLogOdo) {
                    currentInputKm - lastLogOdo
                } else null

                _uiState.value = _uiState.value.copy(
                    vehicle = vehicle,
                    logs = sortedLogs,
                    distanceUnit = unit,
                    deltaSinceLastLog = delta,
                    isLoading = false
                )
            }
        }
    }

    fun onEvent(event: MileageLogUiEvent) {
        when (event) {
            is MileageLogUiEvent.NewOdometerChanged -> updateOdometer(event.odometer)
            is MileageLogUiEvent.NewNotesChanged -> updateNotes(event.notes)
            is MileageLogUiEvent.StepOdometer -> stepOdometer(event.delta)
            is MileageLogUiEvent.SelectTag -> selectTag(event.tag)
            is MileageLogUiEvent.AddClicked -> addLog()
            is MileageLogUiEvent.Retry -> loadData()
            is MileageLogUiEvent.ResetSuccess -> _uiState.value = _uiState.value.copy(isAddedSuccess = false)
            is MileageLogUiEvent.DeleteLog -> deleteLog(event.log)
            is MileageLogUiEvent.UndoDelete -> undoDelete(event.log)
        }
    }

    private fun updateOdometer(odometer: String) {
        savedStateHandle[KEY_ODOMETER] = odometer
        val odoInt = odometer.toIntOrNull()
        val unit = _uiState.value.distanceUnit
        val lastLogOdo = _uiState.value.logs.firstOrNull()?.odometer ?: _uiState.value.vehicle?.currentOdometer ?: 0
        val delta = if (odoInt != null) {
            val inputKm = DistanceUtil.displayToKm(odoInt, unit)
            if (inputKm > lastLogOdo) inputKm - lastLogOdo else null
        } else null

        _uiState.value = _uiState.value.copy(
            newOdometer = odometer,
            deltaSinceLastLog = delta,
            errorRes = null
        )
    }

    private fun updateNotes(notes: String) {
        savedStateHandle[KEY_NOTES] = notes
        _uiState.value = _uiState.value.copy(newNotes = notes)
    }

    private fun stepOdometer(deltaDisplay: Int) {
        val currentDisplay = _uiState.value.newOdometer.toIntOrNull()
            ?: _uiState.value.vehicle?.let { DistanceUtil.kmToDisplay(it.currentOdometer, _uiState.value.distanceUnit) }
            ?: 0
        val newOdo = (currentDisplay + deltaDisplay).coerceAtLeast(0)
        updateOdometer(newOdo.toString())
    }

    private fun selectTag(tag: String) {
        val currentNotes = _uiState.value.newNotes.trim()
        val updated = if (currentNotes.isEmpty()) {
            tag
        } else if (currentNotes.contains(tag)) {
            currentNotes
        } else {
            "$currentNotes, $tag"
        }
        updateNotes(updated)
    }

    private fun undoDelete(log: MileageLogEntry) {
        viewModelScope.launch {
            try {
                mileageLogRepository.insertLog(log)
            } catch (e: Exception) {
                Timber.e(e, "Failed to restore log entry")
                _uiState.value = _uiState.value.copy(
                    errorRes = R.string.error_restore_mileage_failed,
                    errorArgs = emptyList()
                )
            }
        }
    }

    private fun addLog() {
        val state = _uiState.value
        val odometerInt = state.newOdometer.toIntOrNull()
        if (odometerInt == null || odometerInt < 0) {
            _uiState.value = state.copy(errorRes = R.string.error_invalid_odometer, errorArgs = emptyList())
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorRes = null, errorArgs = emptyList())
            try {
                val unit = state.distanceUnit
                val odometerKm = DistanceUtil.displayToKm(odometerInt, unit)
                val vehicle = state.vehicle ?: vehicleRepository.getVehicleById(vehicleId).firstOrNull()

                if (vehicle != null && odometerKm < vehicle.currentOdometer) {
                    _uiState.value = _uiState.value.copy(
                        errorRes = R.string.error_odometer_below_current,
                        errorArgs = listOf(DistanceUtil.kmToDisplay(vehicle.currentOdometer, unit))
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

                savedStateHandle[KEY_ODOMETER] = ""
                savedStateHandle[KEY_NOTES] = ""
                _uiState.value = _uiState.value.copy(
                    newOdometer = "",
                    newNotes = "",
                    deltaSinceLastLog = null,
                    isAddedSuccess = true
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to add mileage log")
                _uiState.value = _uiState.value.copy(
                    errorRes = R.string.error_add_mileage_failed,
                    errorArgs = emptyList()
                )
            }
        }
    }

    private fun deleteLog(log: MileageLogEntry) {
        viewModelScope.launch {
            try {
                mileageLogRepository.deleteLog(log)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete log entry")
                _uiState.value = _uiState.value.copy(
                    errorRes = R.string.error_delete_mileage_failed,
                    errorArgs = emptyList()
                )
            }
        }
    }
}
