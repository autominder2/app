package com.autominder.app.ui.screens.fuel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.autominder.app.R
import com.autominder.app.core.util.AnalyticsEvents
import com.autominder.app.core.util.AnalyticsHelper
import com.autominder.app.core.util.AnalyticsParams
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.FuelEntry
import com.autominder.app.domain.repository.IFuelRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
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
    @StringRes val errorRes: Int? = null,
    val errorArgs: List<Any> = emptyList()
)

@HiltViewModel
class AddFuelViewModel @Inject constructor(
    private val fuelRepository: IFuelRepository,
    private val vehicleRepository: IVehicleRepository,
    private val userPreferences: UserPreferences,
    private val analyticsHelper: AnalyticsHelper,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.toRoute<NavRoutes.AddFuel>().vehicleId

    // Expertise: State keys for persistence across System Process Death
    companion object {
        private const val KEY_VOLUME = "fuel_volume"
        private const val KEY_COST = "fuel_cost"
        private const val KEY_ODOMETER = "fuel_odometer"
        private const val KEY_NOTES = "fuel_notes"
        private const val KEY_DATE = "fuel_date"
    }

    private val _uiState = MutableStateFlow(
        AddFuelUiState(
            vehicleId = vehicleId,
            volume = savedStateHandle[KEY_VOLUME] ?: "",
            cost = savedStateHandle[KEY_COST] ?: "",
            odometer = savedStateHandle[KEY_ODOMETER] ?: "",
            notes = savedStateHandle[KEY_NOTES] ?: "",
            date = savedStateHandle[KEY_DATE] ?: System.currentTimeMillis()
        )
    )
    val uiState: StateFlow<AddFuelUiState> = _uiState.asStateFlow()

    init {
        // Expertise: Optimistic odometer: pre-fill with vehicle's current odometer
        // but only if the field wasn't restored from SavedStateHandle.
        // Converted to the user's display unit — storage is always km.
        viewModelScope.launch {
            val vehicle = vehicleRepository.getVehicleById(vehicleId).firstOrNull()
            if (vehicle != null && _uiState.value.odometer.isEmpty()) {
                val unit = userPreferences.distanceUnit.first()
                val odo = DistanceUtil.kmToDisplay(vehicle.currentOdometer, unit).toString()
                _uiState.update { it.copy(odometer = odo) }
                savedStateHandle[KEY_ODOMETER] = odo
            }
        }
    }

    fun onVolumeChanged(volume: String) {
        _uiState.update { it.copy(volume = volume) }
        savedStateHandle[KEY_VOLUME] = volume
    }

    fun onCostChanged(cost: String) {
        _uiState.update { it.copy(cost = cost) }
        savedStateHandle[KEY_COST] = cost
    }

    fun onOdometerChanged(odometer: String) {
        _uiState.update { it.copy(odometer = odometer) }
        savedStateHandle[KEY_ODOMETER] = odometer
    }

    fun onDateChanged(date: Long) {
        _uiState.update { it.copy(date = date) }
        savedStateHandle[KEY_DATE] = date
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
        savedStateHandle[KEY_NOTES] = notes
    }

    fun saveFuelEntry() {
        val state = _uiState.value
        val volumeDbl = state.volume.toDoubleOrNull() ?: 0.0
        // Cost is optional — logging fuel should take two fields, not a receipt.
        val costDbl = state.cost.toDoubleOrNull() ?: 0.0
        val odoInt = state.odometer.toIntOrNull() ?: 0

        if (volumeDbl <= 0 || odoInt <= 0) {
            _uiState.update { it.copy(errorRes = R.string.error_invalid_fuel_amount, errorArgs = emptyList()) }
            return
        }
        if (costDbl < 0) {
            _uiState.update { it.copy(errorRes = R.string.error_cost_negative, errorArgs = emptyList()) }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorRes = null, errorArgs = emptyList()) }

        viewModelScope.launch {
            try {
                // Input is in the user's display unit — storage is always km.
                val unit = userPreferences.distanceUnit.first()
                val odometerKm = DistanceUtil.displayToKm(odoInt, unit)
                val entry = FuelEntry(
                    vehicleId = vehicleId,
                    date = Date(state.date),
                    odometer = odometerKm,
                    volumeMilliliters = (volumeDbl * 1000).toInt(),
                    costCents = (costDbl * 100).toLong(),
                    notes = state.notes
                )

                // Expertise: Atomic operation chain
                fuelRepository.insertFuelEntry(entry)

                // Safe odometer update — uses SQL-level conditional guard
                vehicleRepository.updateOdometer(vehicleId, odometerKm)

                analyticsHelper.logEvent(
                    "fuel_entry_added",
                    mapOf(AnalyticsParams.ODOMETER_VALUE to odometerKm)
                )

                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                Timber.e(e, "Failed to save fuel entry")
                _uiState.update { it.copy(isSaving = false, errorRes = R.string.error_save_fuel_failed, errorArgs = emptyList()) }
            }
        }
    }
}
