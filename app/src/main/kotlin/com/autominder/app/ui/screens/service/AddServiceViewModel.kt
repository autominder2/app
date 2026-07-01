package com.autominder.app.ui.screens.service

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.autominder.app.core.util.AnalyticsEvents
import com.autominder.app.core.util.AnalyticsHelper
import com.autominder.app.core.util.AnalyticsParams
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.suggestedInterval
import com.autominder.app.domain.repository.IReminderRepository
import com.autominder.app.domain.repository.IServiceRepository
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
    val error: String? = null
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
    private val reminderRepository: IReminderRepository,
    private val userPreferences: UserPreferences,
    private val analyticsHelper: AnalyticsHelper,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.toRoute<NavRoutes.AddService>().vehicleId

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
        val state = _uiState.value

        val odometerInt = state.odometer.toIntOrNull()
        if (odometerInt == null || odometerInt < 0) {
            _uiState.value = state.copy(error = "Please enter a valid odometer reading")
            return
        }

        if (state.serviceDate == null) {
            _uiState.value = state.copy(error = "Please select a service date")
            return
        }

        if (state.serviceType == ServiceType.CUSTOM && state.customLabel.isBlank()) {
            _uiState.value = state.copy(error = "Please specify a name for the custom service")
            return
        }

        val costCents = if (state.cost.isBlank()) {
            null
        } else {
            val costDouble = state.cost.toDoubleOrNull()
            if (costDouble == null || costDouble < 0) {
                _uiState.value = state.copy(error = "Please enter a valid cost")
                return
            }
            (costDouble * 100).toInt()
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val unit = userPreferences.distanceUnit.first()
                val odometerKm = DistanceUtil.displayToKm(odometerInt, unit)
                val vehicle = vehicleRepository.getVehicleById(vehicleId).firstOrNull()

                if (vehicle != null && odometerKm < vehicle.currentOdometer) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Odometer reading cannot be lower than the current vehicle odometer (${DistanceUtil.kmToDisplay(vehicle.currentOdometer, unit)})"
                    )
                    return@launch
                }

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

                // Expertise: Multi-operation logic is scoped to ensure consistency
                serviceRepository.insertService(service)
                vehicleRepository.updateOdometer(vehicleId, odometerKm)

                userPreferences.incrementServiceLogCount()

                analyticsHelper.logEvent(
                    AnalyticsEvents.SERVICE_LOGGED,
                    mapOf(AnalyticsParams.SERVICE_TYPE to state.serviceType.name)
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

                val matchingReminder = reminderRepository.findActiveReminderByType(
                    vehicleId = vehicleId,
                    serviceType = state.serviceType
                )
                when {
                    // A reminder already exists: reset its due window. If the user
                    // kept the toggle on, adopt any interval edits they made.
                    matchingReminder != null -> {
                        val km = if (state.remindNext) reminderIntervalKm else matchingReminder.intervalKm
                        val days = if (state.remindNext) reminderIntervalDays else matchingReminder.intervalDays
                        reminderRepository.updateReminder(
                            matchingReminder.copy(
                                intervalKm = km,
                                intervalDays = days,
                                nextDueOdometer = km?.let { odometerKm + it },
                                nextDueDate = days?.let { state.serviceDate + (it.toLong() * 86_400_000L) },
                                isCompleted = false,
                                completedAt = null,
                                snoozeUntil = null,
                                lastNotifiedAt = null,
                                updatedAt = now
                            )
                        )
                    }
                    // No reminder yet: create one so the user never has to remember.
                    state.remindNext && (reminderIntervalKm != null || reminderIntervalDays != null) -> {
                        reminderRepository.insertReminder(
                            Reminder(
                                id = 0,
                                vehicleId = vehicleId,
                                serviceType = state.serviceType,
                                customLabel = if (state.serviceType == ServiceType.CUSTOM) state.customLabel else null,
                                intervalKm = reminderIntervalKm,
                                intervalDays = reminderIntervalDays,
                                nextDueOdometer = reminderIntervalKm?.let { odometerKm + it },
                                nextDueDate = reminderIntervalDays?.let { state.serviceDate + (it.toLong() * 86_400_000L) },
                                createdAt = now,
                                updatedAt = now
                            )
                        )
                    }
                }

                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                Timber.e(e, "Failed to save service")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save service"
                )
            }
        }
    }
}
