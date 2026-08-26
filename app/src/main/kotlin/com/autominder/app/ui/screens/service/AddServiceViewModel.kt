package com.autominder.app.ui.screens.service

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
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceCompletion
import com.autominder.app.domain.model.ServiceCompletionResult
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.suggestedInterval
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.navigation.NavRoutes
import com.autominder.app.ui.util.DateFormatUtil
import com.autominder.app.ui.util.DistanceFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

data class AddServiceUiState(
    val serviceType: ServiceType = ServiceType.OIL_CHANGE,
    val customLabel: String = "",
    val odometer: String = "",
    val serviceDate: Long? = System.currentTimeMillis(),
    val cost: String = "",
    val partsCost: String = "",
    val laborCost: String = "",
    val isCostBreakdownExpanded: Boolean = false,
    val shopName: String = "",
    val notes: String = "",
    // "Remind me for the next one" — the log-and-never-forget prompt
    val remindNext: Boolean = true,
    val remindIntervalKm: String = "",
    val remindIntervalMonths: String = "",
    /** Quiet context line — which vehicle this is being logged against. */
    val vehicleName: String = "",
    val vehicleOdometerDisplay: String = "",
    /** Live predicted next service forecast strings */
    val predictedNextDueOdometerDisplay: String = "",
    val predictedNextDueDateFormatted: String = "",
    /**
     * Last few distinct service types performed on this vehicle, newest first.
     * Derived from existing history — no schema, no ranking model.
     */
    val recentTypes: List<ServiceType> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val errorArgs: List<Any> = emptyList()
)

sealed class AddServiceUiEvent {
    data class ServiceTypeChanged(val type: ServiceType) : AddServiceUiEvent()
    data class CustomLabelChanged(val label: String) : AddServiceUiEvent()
    data class OdometerChanged(val odometer: String) : AddServiceUiEvent()
    data class OdometerAdjusted(val delta: Int) : AddServiceUiEvent()
    data class ServiceDateChanged(val date: Long?) : AddServiceUiEvent()
    data class QuickDateSelected(val daysAgo: Int) : AddServiceUiEvent()
    data class CostChanged(val cost: String) : AddServiceUiEvent()
    data class PartsCostChanged(val cost: String) : AddServiceUiEvent()
    data class LaborCostChanged(val cost: String) : AddServiceUiEvent()
    data class CostBreakdownToggled(val expanded: Boolean) : AddServiceUiEvent()
    data class QuickCostSelected(val amount: String) : AddServiceUiEvent()
    data class ShopNameChanged(val shopName: String) : AddServiceUiEvent()
    data class NotesChanged(val notes: String) : AddServiceUiEvent()
    data class RemindNextToggled(val enabled: Boolean) : AddServiceUiEvent()
    data class RemindKmChanged(val km: String) : AddServiceUiEvent()
    data class RemindMonthsChanged(val months: String) : AddServiceUiEvent()
    data class QuickIntervalPresetSelected(val months: Int, val kmDisplay: Int) : AddServiceUiEvent()
    object SaveClicked : AddServiceUiEvent()
}

@HiltViewModel
class AddServiceViewModel @Inject constructor(
    private val serviceRepository: IServiceRepository,
    private val vehicleRepository: IVehicleRepository,
    private val userPreferences: UserPreferences,
    private val analyticsHelper: AnalyticsHelper,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.toRoute<NavRoutes.AddService>().vehicleId

    /**
     * Guards Save against double submission for the lifetime of this ViewModel.
     */
    private var saveJob: Job? = null

    companion object {
        private const val KEY_TYPE = "service_type"
        private const val KEY_LABEL = "custom_label"
        private const val KEY_ODOMETER = "odometer"
        private const val KEY_COST = "cost"
        private const val KEY_SHOP = "shop_name"
        private const val KEY_NOTES = "notes"

        /** Two or three recent choices is a shortcut; more is another wall. */
        private const val MAX_RECENT_TYPES = 3
        private const val MILLIS_PER_DAY = 86_400_000L
    }

    private val _uiState = MutableStateFlow(
        AddServiceUiState(
            serviceType = savedStateHandle[KEY_TYPE] ?: ServiceType.OIL_CHANGE,
            customLabel = savedStateHandle[KEY_LABEL] ?: "",
            odometer = savedStateHandle[KEY_ODOMETER] ?: "",
            cost = savedStateHandle[KEY_COST] ?: "",
            shopName = savedStateHandle[KEY_SHOP] ?: "",
            notes = savedStateHandle[KEY_NOTES] ?: ""
        )
    )
    val uiState: StateFlow<AddServiceUiState> = _uiState.asStateFlow()

    init {
        // Seed the reminder-interval suggestion for the initial service type.
        seedSuggestedInterval(_uiState.value.serviceType)
        viewModelScope.launch {
            val vehicle = vehicleRepository.getVehicleById(vehicleId).firstOrNull()
            if (vehicle != null) {
                val unit = userPreferences.distanceUnit.first()
                val displayOdometer = DistanceUtil.kmToDisplay(vehicle.currentOdometer, unit)
                _uiState.value = _uiState.value.copy(
                    vehicleName = listOf(vehicle.make, vehicle.model)
                        .filter { it.isNotBlank() }
                        .joinToString(" "),
                    vehicleOdometerDisplay = displayOdometer.toString()
                )
                // Only prefill if the field is empty (not restored from SavedStateHandle)
                if (_uiState.value.odometer.isEmpty()) {
                    _uiState.value = _uiState.value.copy(odometer = displayOdometer.toString())
                    savedStateHandle[KEY_ODOMETER] = displayOdometer.toString()
                }
                recomputeForecast()
            }
        }

        viewModelScope.launch {
            val history = serviceRepository.getServicesForVehicle(vehicleId).firstOrNull().orEmpty()
            val recent = history
                .sortedByDescending { it.serviceDate }
                .map { it.serviceType }
                .distinct()
                .filterNot { it == ServiceType.CUSTOM }
                .take(MAX_RECENT_TYPES)
            if (recent.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(recentTypes = recent)
            }
        }
    }

    private fun seedSuggestedInterval(type: ServiceType) {
        viewModelScope.launch {
            val unit = userPreferences.distanceUnit.first()
            val suggestion = type.suggestedInterval()
            val kmDisplay = suggestion.km?.let { DistanceUtil.kmToDisplay(it, unit).toString() } ?: ""
            _uiState.value = _uiState.value.copy(
                remindIntervalKm = kmDisplay,
                remindIntervalMonths = suggestion.months?.toString() ?: ""
            )
            recomputeForecast()
        }
    }

    private fun recomputeForecast() {
        val state = _uiState.value
        val baseOdo = state.odometer.toIntOrNull() ?: 0
        val intervalDistance = state.remindIntervalKm.toIntOrNull()
        val nextOdo = intervalDistance?.let { baseOdo + it }

        val baseDate = state.serviceDate ?: System.currentTimeMillis()
        val intervalMonths = state.remindIntervalMonths.toIntOrNull()
        val nextDate = intervalMonths?.let { baseDate + (it.toLong() * 30L * MILLIS_PER_DAY) }

        _uiState.value = state.copy(
            predictedNextDueOdometerDisplay = nextOdo?.let { DistanceFormat.grouped(it) } ?: "",
            predictedNextDueDateFormatted = nextDate?.let { DateFormatUtil.formatDate(it) } ?: ""
        )
    }

    fun onEvent(event: AddServiceUiEvent) {
        when (event) {
            is AddServiceUiEvent.ServiceTypeChanged -> {
                _uiState.value = _uiState.value.copy(serviceType = event.type)
                savedStateHandle[KEY_TYPE] = event.type
                seedSuggestedInterval(event.type)
            }
            is AddServiceUiEvent.CustomLabelChanged -> {
                _uiState.value = _uiState.value.copy(customLabel = event.label)
                savedStateHandle[KEY_LABEL] = event.label
            }
            is AddServiceUiEvent.OdometerChanged -> {
                _uiState.value = _uiState.value.copy(odometer = event.odometer)
                savedStateHandle[KEY_ODOMETER] = event.odometer
                recomputeForecast()
            }
            is AddServiceUiEvent.OdometerAdjusted -> {
                val current = _uiState.value.odometer.toIntOrNull() ?: 0
                val adjusted = (current + event.delta).coerceAtLeast(0).toString()
                _uiState.value = _uiState.value.copy(odometer = adjusted)
                savedStateHandle[KEY_ODOMETER] = adjusted
                recomputeForecast()
            }
            is AddServiceUiEvent.ServiceDateChanged -> {
                _uiState.value = _uiState.value.copy(serviceDate = event.date)
                recomputeForecast()
            }
            is AddServiceUiEvent.QuickDateSelected -> {
                val targetDate = System.currentTimeMillis() - (event.daysAgo.toLong() * MILLIS_PER_DAY)
                _uiState.value = _uiState.value.copy(serviceDate = targetDate)
                recomputeForecast()
            }
            is AddServiceUiEvent.CostChanged -> {
                _uiState.value = _uiState.value.copy(cost = event.cost)
                savedStateHandle[KEY_COST] = event.cost
            }
            is AddServiceUiEvent.PartsCostChanged -> {
                val parts = event.cost
                val labor = _uiState.value.laborCost
                val partsD = parts.toDoubleOrNull() ?: 0.0
                val laborD = labor.toDoubleOrNull() ?: 0.0
                val totalStr = if (partsD + laborD > 0) String.format(Locale.US, "%.2f", partsD + laborD).trimEnd('0').trimEnd('.') else ""
                _uiState.value = _uiState.value.copy(partsCost = parts, cost = totalStr)
                savedStateHandle[KEY_COST] = totalStr
            }
            is AddServiceUiEvent.LaborCostChanged -> {
                val labor = event.cost
                val parts = _uiState.value.partsCost
                val partsD = parts.toDoubleOrNull() ?: 0.0
                val laborD = labor.toDoubleOrNull() ?: 0.0
                val totalStr = if (partsD + laborD > 0) String.format(Locale.US, "%.2f", partsD + laborD).trimEnd('0').trimEnd('.') else ""
                _uiState.value = _uiState.value.copy(laborCost = labor, cost = totalStr)
                savedStateHandle[KEY_COST] = totalStr
            }
            is AddServiceUiEvent.CostBreakdownToggled -> {
                _uiState.value = _uiState.value.copy(isCostBreakdownExpanded = event.expanded)
            }
            is AddServiceUiEvent.QuickCostSelected -> {
                _uiState.value = _uiState.value.copy(cost = event.amount, partsCost = "", laborCost = "")
                savedStateHandle[KEY_COST] = event.amount
            }
            is AddServiceUiEvent.ShopNameChanged -> {
                _uiState.value = _uiState.value.copy(shopName = event.shopName)
                savedStateHandle[KEY_SHOP] = event.shopName
            }
            is AddServiceUiEvent.NotesChanged -> {
                _uiState.value = _uiState.value.copy(notes = event.notes)
                savedStateHandle[KEY_NOTES] = event.notes
            }
            is AddServiceUiEvent.RemindNextToggled -> {
                _uiState.value = _uiState.value.copy(remindNext = event.enabled)
                recomputeForecast()
            }
            is AddServiceUiEvent.RemindKmChanged -> {
                _uiState.value = _uiState.value.copy(remindIntervalKm = event.km)
                recomputeForecast()
            }
            is AddServiceUiEvent.RemindMonthsChanged -> {
                _uiState.value = _uiState.value.copy(remindIntervalMonths = event.months)
                recomputeForecast()
            }
            is AddServiceUiEvent.QuickIntervalPresetSelected -> {
                _uiState.value = _uiState.value.copy(
                    remindIntervalMonths = event.months.toString(),
                    remindIntervalKm = event.kmDisplay.toString()
                )
                recomputeForecast()
            }
            is AddServiceUiEvent.SaveClicked -> saveService()
        }
    }

    private fun saveService() {
        if (saveJob?.isActive == true || _uiState.value.isSaved) return

        val state = _uiState.value

        val odometerInt = state.odometer.toIntOrNull()
        if (odometerInt == null || odometerInt < 0) {
            _uiState.value = state.copy(errorRes = R.string.error_invalid_odometer, errorArgs = emptyList())
            return
        }

        if (state.serviceDate == null) {
            _uiState.value = state.copy(errorRes = R.string.error_select_service_date, errorArgs = emptyList())
            return
        }

        if (state.serviceType == ServiceType.CUSTOM && state.customLabel.isBlank()) {
            _uiState.value = state.copy(errorRes = R.string.error_custom_service_name_required, errorArgs = emptyList())
            return
        }

        val costCents = if (state.cost.isBlank()) {
            null
        } else {
            val costDouble = state.cost.toDoubleOrNull()
            if (costDouble == null || costDouble < 0) {
                _uiState.value = state.copy(errorRes = R.string.error_invalid_cost, errorArgs = emptyList())
                return
            }
            (costDouble * 100).toInt()
        }

        saveJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorRes = null, errorArgs = emptyList())
            try {
                val unit = userPreferences.distanceUnit.first()
                val odometerKm = DistanceUtil.displayToKm(odometerInt, unit)

                val now = System.currentTimeMillis()
                val service = Service(
                    id = 0,
                    vehicleId = vehicleId,
                    serviceType = state.serviceType,
                    customLabel = if (state.serviceType == ServiceType.CUSTOM) state.customLabel else null,
                    odometerAtService = odometerKm,
                    serviceDate = state.serviceDate,
                    costCents = costCents,
                    shopName = state.shopName.ifBlank { null },
                    notes = state.notes,
                    createdAt = now
                )

                val reminderIntervalKm: Int? = if (state.remindNext) {
                    state.remindIntervalKm.toIntOrNull()?.takeIf { it > 0 }
                        ?.let { DistanceUtil.displayToKm(it, unit) }
                } else null
                val reminderIntervalDays: Int? = if (state.remindNext) {
                    state.remindIntervalMonths.toIntOrNull()?.takeIf { it > 0 }?.let { it * 30 }
                } else null

                val result = serviceRepository.completeService(
                    ServiceCompletion(
                        service = service,
                        remindNext = state.remindNext,
                        reminderIntervalKm = reminderIntervalKm,
                        reminderIntervalDays = reminderIntervalDays
                    )
                )

                when (result) {
                    is ServiceCompletionResult.Success -> {
                        userPreferences.incrementServiceLogCount()
                        analyticsHelper.logEvent(
                            AnalyticsEvents.SERVICE_LOGGED,
                            mapOf(AnalyticsParams.SERVICE_TYPE to state.serviceType.name)
                        )
                        _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
                    }

                    ServiceCompletionResult.VehicleNotFound -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorRes = R.string.error_vehicle_not_found,
                            errorArgs = emptyList()
                        )
                    }

                    is ServiceCompletionResult.Failed -> {
                        Timber.e(result.cause, "Failed to save service")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorRes = R.string.error_save_service_failed,
                            errorArgs = emptyList()
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to save service")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorRes = R.string.error_save_service_failed,
                    errorArgs = emptyList()
                )
            }
        }
    }
}
