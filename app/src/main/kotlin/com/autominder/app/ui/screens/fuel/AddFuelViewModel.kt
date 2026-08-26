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
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IFuelRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AddFuelUiState(
    val vehicleId: Long,
    val vehicle: Vehicle? = null,
    val latestEntry: FuelEntry? = null,
    val volume: String = "",
    val cost: String = "",
    val pricePerUnit: String = "",
    val odometer: String = "",
    val isFullTank: Boolean = true,
    val date: Long = System.currentTimeMillis(),
    val gasStation: String = "",
    val notes: String = "",
    val distanceUnit: String = "km",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val initialLoadFailed: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val errorArgs: List<Any> = emptyList(),
    // Inline Field Validation Errors
    @StringRes val volumeErrorRes: Int? = null,
    @StringRes val odometerErrorRes: Int? = null,
    @StringRes val costErrorRes: Int? = null,
    // Live Real-Time Telemetry & Economy Preview
    val distanceSinceLastFill: Int? = null,
    val estimatedEfficiency: Double? = null,
    val estimatedCostPerDistance: Double? = null
)

sealed class AddFuelUiEvent {
    data class VolumeChanged(val volume: String) : AddFuelUiEvent()
    data class CostChanged(val cost: String) : AddFuelUiEvent()
    data class PricePerUnitChanged(val price: String) : AddFuelUiEvent()
    data class OdometerChanged(val odometer: String) : AddFuelUiEvent()
    data class QuickCostTapped(val amount: Double) : AddFuelUiEvent()
    data class QuickVolumeTapped(val amount: Double) : AddFuelUiEvent()
    data class QuickOdometerStepTapped(val step: Int) : AddFuelUiEvent()
    data class FullTankToggled(val isFull: Boolean) : AddFuelUiEvent()
    data class DateChanged(val dateMillis: Long) : AddFuelUiEvent()
    data class GasStationChanged(val station: String) : AddFuelUiEvent()
    data class NotesChanged(val notes: String) : AddFuelUiEvent()
    data object ErrorDismissed : AddFuelUiEvent()
    data object SaveClicked : AddFuelUiEvent()
    data object RetryClicked : AddFuelUiEvent()
}

@HiltViewModel
class AddFuelViewModel @Inject constructor(
    private val fuelRepository: IFuelRepository,
    private val vehicleRepository: IVehicleRepository,
    private val userPreferences: UserPreferences,
    private val analyticsHelper: AnalyticsHelper,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.toRoute<NavRoutes.AddFuel>().vehicleId

    companion object {
        private const val KEY_VOLUME = "fuel_volume"
        private const val KEY_COST = "fuel_cost"
        private const val KEY_PRICE = "fuel_price"
        private const val KEY_ODOMETER = "fuel_odometer"
        private const val KEY_FULL_TANK = "fuel_full_tank"
        private const val KEY_STATION = "fuel_station"
        private const val KEY_NOTES = "fuel_notes"
        private const val KEY_DATE = "fuel_date"
    }

    private val _uiState = MutableStateFlow(
        AddFuelUiState(
            vehicleId = vehicleId,
            volume = savedStateHandle[KEY_VOLUME] ?: "",
            cost = savedStateHandle[KEY_COST] ?: "",
            pricePerUnit = savedStateHandle[KEY_PRICE] ?: "",
            odometer = savedStateHandle[KEY_ODOMETER] ?: "",
            isFullTank = savedStateHandle[KEY_FULL_TANK] ?: true,
            gasStation = savedStateHandle[KEY_STATION] ?: "",
            notes = savedStateHandle[KEY_NOTES] ?: "",
            date = savedStateHandle[KEY_DATE] ?: System.currentTimeMillis()
        )
    )
    val uiState: StateFlow<AddFuelUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(event: AddFuelUiEvent) {
        when (event) {
            is AddFuelUiEvent.VolumeChanged -> onVolumeChanged(event.volume)
            is AddFuelUiEvent.CostChanged -> onCostChanged(event.cost)
            is AddFuelUiEvent.PricePerUnitChanged -> onPricePerUnitChanged(event.price)
            is AddFuelUiEvent.OdometerChanged -> onOdometerChanged(event.odometer)
            is AddFuelUiEvent.QuickCostTapped -> onQuickCostTapped(event.amount)
            is AddFuelUiEvent.QuickVolumeTapped -> onQuickVolumeTapped(event.amount)
            is AddFuelUiEvent.QuickOdometerStepTapped -> onQuickOdometerStepTapped(event.step)
            is AddFuelUiEvent.FullTankToggled -> onFullTankToggled(event.isFull)
            is AddFuelUiEvent.DateChanged -> onDateChanged(event.dateMillis)
            is AddFuelUiEvent.GasStationChanged -> onGasStationChanged(event.station)
            is AddFuelUiEvent.NotesChanged -> onNotesChanged(event.notes)
            is AddFuelUiEvent.ErrorDismissed -> _uiState.update { it.copy(errorRes = null) }
            is AddFuelUiEvent.SaveClicked -> saveFuelEntry()
            is AddFuelUiEvent.RetryClicked -> loadData()
        }
    }

    private fun loadData() {
        _uiState.update { it.copy(isLoading = true, errorRes = null, initialLoadFailed = false) }
        viewModelScope.launch {
            try {
                combine(
                    vehicleRepository.getVehicleById(vehicleId),
                    fuelRepository.getLatestFuelEntryForVehicle(vehicleId),
                    userPreferences.distanceUnit
                ) { vehicle, latestEntry, unit ->
                    Triple(vehicle, latestEntry, unit)
                }.collect { (vehicle, latestEntry, unit) ->
                    val initialOdometer = if (_uiState.value.odometer.isEmpty() && vehicle != null) {
                        val displayOdo = DistanceUtil.kmToDisplay(vehicle.currentOdometer, unit).toString()
                        savedStateHandle[KEY_ODOMETER] = displayOdo
                        displayOdo
                    } else {
                        _uiState.value.odometer
                    }

                    _uiState.update { current ->
                        val updated = current.copy(
                            vehicle = vehicle,
                            latestEntry = latestEntry,
                            distanceUnit = unit,
                            odometer = initialOdometer,
                            isLoading = false,
                            initialLoadFailed = false
                        )
                        calculateTelemetry(updated)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load vehicle/fuel data for AddFuel")
                _uiState.update { it.copy(isLoading = false, initialLoadFailed = true, errorRes = R.string.error_load_vehicle_failed) }
            }
        }
    }

    private fun onVolumeChanged(volume: String) {
        val clean = volume.filter { it.isDigit() || it == '.' }
        savedStateHandle[KEY_VOLUME] = clean
        _uiState.update { current ->
            val updated = current.copy(volume = clean, volumeErrorRes = null, errorRes = null)
            // Auto-calculate cost if pricePerUnit is populated
            val volDbl = clean.toDoubleOrNull()
            val priceDbl = current.pricePerUnit.toDoubleOrNull()
            val calculated = if (volDbl != null && priceDbl != null && priceDbl > 0 && current.cost.isBlank()) {
                val computedCost = String.format(Locale.US, "%.2f", volDbl * priceDbl)
                savedStateHandle[KEY_COST] = computedCost
                updated.copy(cost = computedCost)
            } else {
                updated
            }
            calculateTelemetry(calculated)
        }
    }

    private fun onCostChanged(cost: String) {
        val clean = cost.filter { it.isDigit() || it == '.' }
        savedStateHandle[KEY_COST] = clean
        _uiState.update { current ->
            val updated = current.copy(cost = clean, costErrorRes = null, errorRes = null)
            // Auto-calculate volume if pricePerUnit is populated
            val costDbl = clean.toDoubleOrNull()
            val priceDbl = current.pricePerUnit.toDoubleOrNull()
            val calculated = if (costDbl != null && priceDbl != null && priceDbl > 0 && current.volume.isBlank()) {
                val computedVol = String.format(Locale.US, "%.2f", costDbl / priceDbl)
                savedStateHandle[KEY_VOLUME] = computedVol
                updated.copy(volume = computedVol)
            } else {
                updated
            }
            calculateTelemetry(calculated)
        }
    }

    private fun onPricePerUnitChanged(price: String) {
        val clean = price.filter { it.isDigit() || it == '.' }
        savedStateHandle[KEY_PRICE] = clean
        _uiState.update { current ->
            val updated = current.copy(pricePerUnit = clean, errorRes = null)
            val priceDbl = clean.toDoubleOrNull()
            val costDbl = current.cost.toDoubleOrNull()
            val volDbl = current.volume.toDoubleOrNull()

            val calculated = if (priceDbl != null && priceDbl > 0) {
                if (costDbl != null && costDbl > 0 && current.volume.isBlank()) {
                    val computedVol = String.format(Locale.US, "%.2f", costDbl / priceDbl)
                    savedStateHandle[KEY_VOLUME] = computedVol
                    updated.copy(volume = computedVol)
                } else if (volDbl != null && volDbl > 0 && current.cost.isBlank()) {
                    val computedCost = String.format(Locale.US, "%.2f", volDbl * priceDbl)
                    savedStateHandle[KEY_COST] = computedCost
                    updated.copy(cost = computedCost)
                } else {
                    updated
                }
            } else {
                updated
            }
            calculateTelemetry(calculated)
        }
    }

    private fun onOdometerChanged(odometer: String) {
        val clean = odometer.filter { it.isDigit() }
        savedStateHandle[KEY_ODOMETER] = clean
        _uiState.update { current ->
            val updated = current.copy(odometer = clean, odometerErrorRes = null, errorRes = null)
            calculateTelemetry(updated)
        }
    }

    private fun onQuickCostTapped(amount: Double) {
        val costStr = if (amount % 1.0 == 0.0) amount.toInt().toString() else String.format(Locale.US, "%.2f", amount)
        savedStateHandle[KEY_COST] = costStr
        _uiState.update { current ->
            val updated = current.copy(cost = costStr, costErrorRes = null, errorRes = null)
            val priceDbl = current.pricePerUnit.toDoubleOrNull()
            val calculated = if (priceDbl != null && priceDbl > 0) {
                val computedVol = String.format(Locale.US, "%.2f", amount / priceDbl)
                savedStateHandle[KEY_VOLUME] = computedVol
                updated.copy(volume = computedVol, volumeErrorRes = null)
            } else {
                updated
            }
            calculateTelemetry(calculated)
        }
    }

    private fun onQuickVolumeTapped(amount: Double) {
        val volStr = if (amount % 1.0 == 0.0) amount.toInt().toString() else String.format(Locale.US, "%.2f", amount)
        savedStateHandle[KEY_VOLUME] = volStr
        _uiState.update { current ->
            val updated = current.copy(volume = volStr, volumeErrorRes = null, errorRes = null)
            val priceDbl = current.pricePerUnit.toDoubleOrNull()
            val calculated = if (priceDbl != null && priceDbl > 0) {
                val computedCost = String.format(Locale.US, "%.2f", amount * priceDbl)
                savedStateHandle[KEY_COST] = computedCost
                updated.copy(cost = computedCost, costErrorRes = null)
            } else {
                updated
            }
            calculateTelemetry(calculated)
        }
    }

    private fun onQuickOdometerStepTapped(step: Int) {
        val currentOdo = _uiState.value.odometer.toIntOrNull()
            ?: _uiState.value.vehicle?.let { DistanceUtil.kmToDisplay(it.currentOdometer, _uiState.value.distanceUnit) }
            ?: 0
        val newOdo = (currentOdo + step).coerceAtLeast(0).toString()
        savedStateHandle[KEY_ODOMETER] = newOdo
        _uiState.update { current ->
            val updated = current.copy(odometer = newOdo, odometerErrorRes = null, errorRes = null)
            calculateTelemetry(updated)
        }
    }

    private fun onFullTankToggled(isFull: Boolean) {
        savedStateHandle[KEY_FULL_TANK] = isFull
        _uiState.update { it.copy(isFullTank = isFull) }
    }

    private fun onDateChanged(dateMillis: Long) {
        savedStateHandle[KEY_DATE] = dateMillis
        _uiState.update { it.copy(date = dateMillis) }
    }

    private fun onGasStationChanged(station: String) {
        savedStateHandle[KEY_STATION] = station
        _uiState.update { it.copy(gasStation = station) }
    }

    private fun onNotesChanged(notes: String) {
        savedStateHandle[KEY_NOTES] = notes
        _uiState.update { it.copy(notes = notes) }
    }

    /**
     * Compute Real-Time Telemetry Preview
     */
    private fun calculateTelemetry(state: AddFuelUiState): AddFuelUiState {
        val odoInput = state.odometer.toIntOrNull()
        val volumeDbl = state.volume.toDoubleOrNull()
        val costDbl = state.cost.toDoubleOrNull()
        val latestEntry = state.latestEntry

        var distanceDelta: Int? = null
        var efficiency: Double? = null
        var costPerDist: Double? = null

        if (odoInput != null && latestEntry != null) {
            val unit = state.distanceUnit
            val lastOdoDisplay = DistanceUtil.kmToDisplay(latestEntry.odometer, unit)
            val delta = odoInput - lastOdoDisplay
            if (delta > 0) {
                distanceDelta = delta
                if (volumeDbl != null && volumeDbl > 0) {
                    efficiency = delta / volumeDbl
                }
                if (costDbl != null && costDbl > 0) {
                    costPerDist = costDbl / delta
                }
            }
        }

        return state.copy(
            distanceSinceLastFill = distanceDelta,
            estimatedEfficiency = efficiency,
            estimatedCostPerDistance = costPerDist
        )
    }

    fun saveFuelEntry() {
        val state = _uiState.value
        val volumeDbl = state.volume.toDoubleOrNull() ?: 0.0
        val costDbl = state.cost.toDoubleOrNull() ?: 0.0
        val odoInt = state.odometer.toIntOrNull() ?: 0

        var hasError = false
        var volumeErr: Int? = null
        var odoErr: Int? = null
        var costErr: Int? = null

        if (volumeDbl <= 0) {
            volumeErr = R.string.error_invalid_fuel_amount
            hasError = true
        }
        if (odoInt <= 0) {
            odoErr = R.string.error_invalid_odometer
            hasError = true
        }
        if (costDbl < 0) {
            costErr = R.string.error_cost_negative
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    volumeErrorRes = volumeErr,
                    odometerErrorRes = odoErr,
                    costErrorRes = costErr,
                    errorRes = volumeErr ?: odoErr ?: costErr,
                    errorArgs = emptyList()
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isSaving = true,
                errorRes = null,
                volumeErrorRes = null,
                odometerErrorRes = null,
                costErrorRes = null,
                errorArgs = emptyList()
            )
        }

        viewModelScope.launch {
            try {
                val unit = userPreferences.distanceUnit.first()
                val odometerKm = DistanceUtil.displayToKm(odoInt, unit)

                val fullNotes = buildString {
                    if (state.gasStation.isNotBlank()) {
                        append("Station: ${state.gasStation.trim()}")
                    }
                    if (state.notes.isNotBlank()) {
                        if (isNotEmpty()) append(" · ")
                        append(state.notes.trim())
                    }
                }

                val entry = FuelEntry(
                    vehicleId = vehicleId,
                    date = Date(state.date),
                    odometer = odometerKm,
                    volumeMilliliters = (volumeDbl * 1000).toInt(),
                    costCents = (costDbl * 100).toLong(),
                    notes = fullNotes
                )

                // Atomic Room transaction inserts fuel entry & monotonically updates vehicle odometer
                fuelRepository.insertFuelEntry(entry)

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
