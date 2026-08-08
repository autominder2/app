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
import javax.inject.Inject

data class AddServiceUiState(
    val serviceType: ServiceType = ServiceType.OIL_CHANGE,
    val customLabel: String = "",
    val odometer: String = "",
    val serviceDate: Long? = System.currentTimeMillis(),
    val cost: String = "",
    val shopName: String = "",
    val notes: String = "",
    // "Remind me for the next one" — the log-and-never-forget prompt
    val remindNext: Boolean = true,
    val remindIntervalKm: String = "",
    val remindIntervalMonths: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val errorArgs: List<Any> = emptyList()
)

sealed class AddServiceUiEvent {
    data class ServiceTypeChanged(val type: ServiceType) : AddServiceUiEvent()
    data class CustomLabelChanged(val label: String) : AddServiceUiEvent()
    data class OdometerChanged(val odometer: String) : AddServiceUiEvent()
    data class ServiceDateChanged(val date: Long?) : AddServiceUiEvent()
    data class CostChanged(val cost: String) : AddServiceUiEvent()
    data class ShopNameChanged(val shopName: String) : AddServiceUiEvent()
    data class NotesChanged(val notes: String) : AddServiceUiEvent()
    data class RemindNextToggled(val enabled: Boolean) : AddServiceUiEvent()
    data class RemindKmChanged(val km: String) : AddServiceUiEvent()
    data class RemindMonthsChanged(val months: String) : AddServiceUiEvent()
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
     *
     * This is single-flight, not durable idempotency: a process death between the
     * committed transaction and navigation would restore the form and allow a second
     * save. Closing that gap needs an operation key in the schema, which v1.0 does
     * not have.
     */
    private var saveJob: Job? = null

    // Expertise: State keys for persistence across System Process Death
    companion object {
        private const val KEY_TYPE = "service_type"
        private const val KEY_LABEL = "custom_label"
        private const val KEY_ODOMETER = "odometer"
        private const val KEY_COST = "cost"
        private const val KEY_SHOP = "shop_name"
        private const val KEY_NOTES = "notes"
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
            // Only prefill if the field is currently empty (not restored from SavedStateHandle)
            if (vehicle != null && _uiState.value.odometer.isEmpty()) {
                val unit = userPreferences.distanceUnit.first()
                val displayOdometer = DistanceUtil.kmToDisplay(vehicle.currentOdometer, unit)
                _uiState.value = _uiState.value.copy(odometer = displayOdometer.toString())
                savedStateHandle[KEY_ODOMETER] = displayOdometer.toString()
            }
        }
    }

    /**
     * Pre-fills the "remind me" interval fields with sensible defaults for the
     * given type, converting the km suggestion into the user's distance unit so
     * the number they see matches how they read the odometer.
     */
    private fun seedSuggestedInterval(type: ServiceType) {
        viewModelScope.launch {
            val unit = userPreferences.distanceUnit.first()
            val suggestion = type.suggestedInterval()
            val kmDisplay = suggestion.km?.let { DistanceUtil.kmToDisplay(it, unit).toString() } ?: ""
            _uiState.value = _uiState.value.copy(
                remindIntervalKm = kmDisplay,
                remindIntervalMonths = suggestion.months?.toString() ?: ""
            )
        }
    }

    fun onEvent(event: AddServiceUiEvent) {
        when (event) {
            is AddServiceUiEvent.ServiceTypeChanged -> {
                _uiState.value = _uiState.value.copy(serviceType = event.type)
                savedStateHandle[KEY_TYPE] = event.type
                // Refresh the reminder-interval suggestion to match the new type.
                seedSuggestedInterval(event.type)
            }
            is AddServiceUiEvent.CustomLabelChanged -> {
                _uiState.value = _uiState.value.copy(customLabel = event.label)
                savedStateHandle[KEY_LABEL] = event.label
            }
            is AddServiceUiEvent.OdometerChanged -> {
                _uiState.value = _uiState.value.copy(odometer = event.odometer)
                savedStateHandle[KEY_ODOMETER] = event.odometer
            }
            is AddServiceUiEvent.ServiceDateChanged -> _uiState.value = _uiState.value.copy(serviceDate = event.date)
            is AddServiceUiEvent.CostChanged -> {
                _uiState.value = _uiState.value.copy(cost = event.cost)
                savedStateHandle[KEY_COST] = event.cost
            }
            is AddServiceUiEvent.ShopNameChanged -> {
                _uiState.value = _uiState.value.copy(shopName = event.shopName)
                savedStateHandle[KEY_SHOP] = event.shopName
            }
            is AddServiceUiEvent.NotesChanged -> {
                _uiState.value = _uiState.value.copy(notes = event.notes)
                savedStateHandle[KEY_NOTES] = event.notes
            }
            is AddServiceUiEvent.RemindNextToggled -> _uiState.value = _uiState.value.copy(remindNext = event.enabled)
            is AddServiceUiEvent.RemindKmChanged -> _uiState.value = _uiState.value.copy(remindIntervalKm = event.km)
            is AddServiceUiEvent.RemindMonthsChanged -> _uiState.value = _uiState.value.copy(remindIntervalMonths = event.months)
            is AddServiceUiEvent.SaveClicked -> saveService()
        }
    }

    private fun saveService() {
        // Single-flight Save: a second tap while the first transaction is still in
        // flight — or after it has already succeeded — must not log the service twice.
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

                // Historical maintenance is valid ownership history: a service logged
                // below the vehicle's current reading is accepted and keeps its own
                // odometer. The data layer refuses to roll the vehicle backwards.
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

                // "Remind me for the next one" — the intervals the user chose,
                // converted to km/days. Null when the toggle is off.
                val reminderIntervalKm: Int? = if (state.remindNext) {
                    state.remindIntervalKm.toIntOrNull()?.takeIf { it > 0 }
                        ?.let { DistanceUtil.displayToKm(it, unit) }
                } else null
                val reminderIntervalDays: Int? = if (state.remindNext) {
                    state.remindIntervalMonths.toIntOrNull()?.takeIf { it > 0 }?.let { it * 30 }
                } else null

                // One command, one transaction. Service record, odometer effect and
                // reminder rebase commit together or not at all.
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
                // The screen went away mid-save. Not an error to show anyone, and the
                // cancellation must keep propagating.
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
