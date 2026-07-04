package com.autominder.app.ui.screens.reminder

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
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.repository.IReminderRepository
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

data class AddReminderUiState(
    val title: String = "",
    val description: String = "",
    val dueKm: String = "",
    val dueDateLong: Long? = null,
    val intervalKm: String = "",
    val intervalDays: String = "",
    val serviceType: ServiceType = ServiceType.OIL_CHANGE,
    val isLoading: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val errorArgs: List<Any> = emptyList(),
    val isSaved: Boolean = false,
    val shouldRequestNotificationPermission: Boolean = false
)

sealed class AddReminderUiEvent {
    data class TitleChanged(val title: String) : AddReminderUiEvent()
    data class DescriptionChanged(val description: String) : AddReminderUiEvent()
    data class DueKmChanged(val dueKm: String) : AddReminderUiEvent()
    data class DueDateChanged(val date: Long?) : AddReminderUiEvent()
    data class IntervalKmChanged(val intervalKm: String) : AddReminderUiEvent()
    data class IntervalDaysChanged(val intervalDays: String) : AddReminderUiEvent()
    data class ServiceTypeChanged(val type: ServiceType) : AddReminderUiEvent()
    object SaveClicked : AddReminderUiEvent()
    object PermissionRequestHandled : AddReminderUiEvent()
}

@HiltViewModel
class AddReminderViewModel @Inject constructor(
    private val reminderRepository: IReminderRepository,
    private val vehicleRepository: IVehicleRepository,
    private val userPreferences: UserPreferences,
    private val analyticsHelper: AnalyticsHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.toRoute<NavRoutes.AddReminder>().vehicleId

    private val _uiState = MutableStateFlow(AddReminderUiState())
    val uiState: StateFlow<AddReminderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val vehicle = vehicleRepository.getVehicleById(vehicleId).firstOrNull()
            if (vehicle != null) {
                val unit = userPreferences.distanceUnit.first()
                val defaults = getDefaultIntervals(_uiState.value.serviceType)
                val displayOdometer = DistanceUtil.kmToDisplay(vehicle.currentOdometer, unit)
                _uiState.value = _uiState.value.copy(
                    dueKm = defaults.intervalKm?.let { (displayOdometer + it).toString() } ?: "",
                    dueDateLong = defaults.intervalDays?.let {
                        System.currentTimeMillis() + (it.toLong() * 86_400_000L)
                    },
                    intervalKm = defaults.intervalKm?.toString() ?: "",
                    intervalDays = defaults.intervalDays?.toString() ?: ""
                )
            }
        }
    }

    private fun getDefaultIntervals(type: ServiceType): DefaultInterval = when (type) {
        ServiceType.OIL_CHANGE -> DefaultInterval(8000, 180)
        ServiceType.TIRE_ROTATION -> DefaultInterval(10000, 180)
        ServiceType.BRAKE_SERVICE -> DefaultInterval(40000, 730)
        ServiceType.BATTERY -> DefaultInterval(null, 1095)
        ServiceType.AIR_FILTER -> DefaultInterval(20000, 365)
        ServiceType.CABIN_FILTER -> DefaultInterval(20000, 365)
        ServiceType.TRANSMISSION -> DefaultInterval(60000, 730)
        ServiceType.COOLANT -> DefaultInterval(40000, 730)
        ServiceType.SPARK_PLUGS -> DefaultInterval(50000, 730)
        ServiceType.TIMING_BELT -> DefaultInterval(100000, 1825)
        ServiceType.WIPER_BLADES -> DefaultInterval(null, 365)
        ServiceType.INSURANCE -> DefaultInterval(null, 365)
        ServiceType.REGISTRATION -> DefaultInterval(null, 365)
        ServiceType.INSPECTION -> DefaultInterval(null, 365)
        ServiceType.EMISSIONS_TEST -> DefaultInterval(null, 365)
        ServiceType.CUSTOM -> DefaultInterval(null, null)
    }

    private data class DefaultInterval(val intervalKm: Int?, val intervalDays: Int?)

    fun onEvent(event: AddReminderUiEvent) {
        when (event) {
            is AddReminderUiEvent.TitleChanged -> _uiState.value = _uiState.value.copy(title = event.title)
            is AddReminderUiEvent.DescriptionChanged -> _uiState.value = _uiState.value.copy(description = event.description)
            is AddReminderUiEvent.DueKmChanged -> _uiState.value = _uiState.value.copy(dueKm = event.dueKm)
            is AddReminderUiEvent.DueDateChanged -> _uiState.value = _uiState.value.copy(dueDateLong = event.date)
            is AddReminderUiEvent.IntervalKmChanged -> _uiState.value = _uiState.value.copy(intervalKm = event.intervalKm)
            is AddReminderUiEvent.IntervalDaysChanged -> _uiState.value = _uiState.value.copy(intervalDays = event.intervalDays)
            is AddReminderUiEvent.ServiceTypeChanged -> onServiceTypeChanged(event.type)
            is AddReminderUiEvent.SaveClicked -> saveReminder()
            is AddReminderUiEvent.PermissionRequestHandled -> _uiState.value = _uiState.value.copy(shouldRequestNotificationPermission = false)
        }
    }

    private fun onServiceTypeChanged(type: ServiceType) {
        val defaults = getDefaultIntervals(type)
        viewModelScope.launch {
            val vehicle = vehicleRepository.getVehicleById(vehicleId).firstOrNull()
            val unit = userPreferences.distanceUnit.first()
            val displayOdometer = vehicle?.let { DistanceUtil.kmToDisplay(it.currentOdometer, unit) } ?: 0
            _uiState.value = _uiState.value.copy(
                serviceType = type,
                dueKm = defaults.intervalKm?.let { (displayOdometer + it).toString() } ?: "",
                dueDateLong = defaults.intervalDays?.let {
                    System.currentTimeMillis() + (it.toLong() * 86_400_000L)
                },
                intervalKm = defaults.intervalKm?.toString() ?: "",
                intervalDays = defaults.intervalDays?.toString() ?: ""
            )
        }
    }

    private fun saveReminder() {
        val state = _uiState.value

        if (state.serviceType == ServiceType.CUSTOM && state.title.isBlank()) {
            _uiState.value = _uiState.value.copy(errorRes = R.string.error_custom_reminder_name_required, errorArgs = emptyList())
            return
        }

        if (state.dueDateLong == null && state.dueKm.isBlank()) {
            _uiState.value = _uiState.value.copy(errorRes = R.string.error_reminder_due_required, errorArgs = emptyList())
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorRes = null, errorArgs = emptyList())
            try {
                // Check if this is the first reminder for this vehicle
                val existingReminders = reminderRepository.getAllRemindersForVehicle(vehicleId).first()
                val isFirstReminder = existingReminders.isEmpty()

                val currentTime = System.currentTimeMillis()
                val reminder = Reminder(
                    id = 0,
                    vehicleId = vehicleId,
                    serviceType = state.serviceType,
                    customLabel = if (state.serviceType == ServiceType.CUSTOM) state.title else null,
                    intervalKm = state.intervalKm.toIntOrNull(),
                    intervalDays = state.intervalDays.toIntOrNull(),
                    nextDueDate = state.dueDateLong,
                    nextDueOdometer = state.dueKm.toIntOrNull(),
                    notes = state.description,
                    createdAt = currentTime,
                    updatedAt = currentTime
                )
                reminderRepository.insertReminder(reminder)

                analyticsHelper.logEvent(
                    AnalyticsEvents.REMINDER_CREATED,
                    mapOf(AnalyticsParams.REMINDER_TYPE to state.serviceType.name)
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSaved = true,
                    shouldRequestNotificationPermission = isFirstReminder
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to save reminder")
                _uiState.value = _uiState.value.copy(isLoading = false, errorRes = R.string.error_save_reminder_failed, errorArgs = emptyList())
            }
        }
    }
}
