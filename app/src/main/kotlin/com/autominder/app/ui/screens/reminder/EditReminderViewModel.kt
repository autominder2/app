package com.autominder.app.ui.screens.reminder

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.autominder.app.R
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IReminderRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.usecase.StatusCalculator
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
import java.util.Calendar
import javax.inject.Inject

data class EditReminderUiState(
    val vehicle: Vehicle? = null,
    val distanceUnit: String = "km",
    val serviceType: ServiceType = ServiceType.OIL_CHANGE,
    val customLabel: String = "",
    val dueKm: String = "",
    val dueDateLong: Long? = null,
    val intervalKm: String = "",
    val intervalDays: String = "",
    val notes: String = "",
    val currentStatus: ServiceStatus = ServiceStatus.OK,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val errorArgs: List<Any> = emptyList()
)

sealed class EditReminderUiEvent {
    data class ServiceTypeChanged(val type: ServiceType) : EditReminderUiEvent()
    data class CustomLabelChanged(val label: String) : EditReminderUiEvent()
    data class DueKmChanged(val dueKm: String) : EditReminderUiEvent()
    data class DueDateChanged(val date: Long?) : EditReminderUiEvent()
    data class IntervalKmChanged(val intervalKm: String) : EditReminderUiEvent()
    data class IntervalDaysChanged(val intervalDays: String) : EditReminderUiEvent()
    data class NotesChanged(val notes: String) : EditReminderUiEvent()
    data class StepMonths(val months: Int) : EditReminderUiEvent()
    data class StepDueKm(val delta: Int) : EditReminderUiEvent()
    data object SaveClicked : EditReminderUiEvent()
    data object DeleteClicked : EditReminderUiEvent()
}

@HiltViewModel
class EditReminderViewModel @Inject constructor(
    private val reminderRepository: IReminderRepository,
    private val vehicleRepository: IVehicleRepository,
    private val userPreferences: UserPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reminderId: Long = savedStateHandle.toRoute<NavRoutes.EditReminder>().reminderId

    private val _uiState = MutableStateFlow(EditReminderUiState())
    val uiState: StateFlow<EditReminderUiState> = _uiState.asStateFlow()

    private var originalReminder: Reminder? = null

    init {
        loadReminder()
    }

    private fun loadReminder() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorRes = null, errorArgs = emptyList())
            try {
                val reminder = reminderRepository.getReminderById(reminderId).firstOrNull()
                if (reminder != null) {
                    originalReminder = reminder
                    val vehicle = vehicleRepository.getVehicleById(reminder.vehicleId).firstOrNull()
                    val unit = userPreferences.distanceUnit.first()

                    val status = StatusCalculator.calculate(
                        nowMillis = System.currentTimeMillis(),
                        currentOdometer = vehicle?.currentOdometer ?: 0,
                        dueDateMillis = reminder.nextDueDate,
                        dueOdometer = reminder.nextDueOdometer,
                        snoozeUntilMillis = reminder.snoozeUntil,
                        isCompleted = reminder.isCompleted
                    )

                    val dueKmDisplay = reminder.nextDueOdometer?.let { DistanceUtil.kmToDisplay(it, unit) }
                    val intervalKmDisplay = reminder.intervalKm?.let { DistanceUtil.kmToDisplay(it, unit) }

                    _uiState.value = _uiState.value.copy(
                        vehicle = vehicle,
                        distanceUnit = unit,
                        serviceType = reminder.serviceType,
                        customLabel = reminder.customLabel ?: "",
                        dueKm = dueKmDisplay?.toString() ?: "",
                        dueDateLong = reminder.nextDueDate,
                        intervalKm = intervalKmDisplay?.toString() ?: "",
                        intervalDays = reminder.intervalDays?.toString() ?: "",
                        notes = reminder.notes,
                        currentStatus = status,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorRes = R.string.error_reminder_not_found
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load reminder")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorRes = R.string.error_load_reminder_failed
                )
            }
        }
    }

    fun onEvent(event: EditReminderUiEvent) {
        when (event) {
            is EditReminderUiEvent.ServiceTypeChanged -> _uiState.value = _uiState.value.copy(serviceType = event.type)
            is EditReminderUiEvent.CustomLabelChanged -> _uiState.value = _uiState.value.copy(customLabel = event.label)
            is EditReminderUiEvent.DueKmChanged -> updateDueKm(event.dueKm)
            is EditReminderUiEvent.DueDateChanged -> updateDueDate(event.date)
            is EditReminderUiEvent.IntervalKmChanged -> _uiState.value = _uiState.value.copy(intervalKm = event.intervalKm)
            is EditReminderUiEvent.IntervalDaysChanged -> _uiState.value = _uiState.value.copy(intervalDays = event.intervalDays)
            is EditReminderUiEvent.NotesChanged -> _uiState.value = _uiState.value.copy(notes = event.notes)
            is EditReminderUiEvent.StepMonths -> stepMonths(event.months)
            is EditReminderUiEvent.StepDueKm -> stepDueKm(event.delta)
            is EditReminderUiEvent.SaveClicked -> saveReminder()
            is EditReminderUiEvent.DeleteClicked -> deleteReminder()
        }
    }

    private fun updateDueKm(dueKm: String) {
        _uiState.value = _uiState.value.copy(dueKm = dueKm, errorRes = null)
        recalculateLiveStatus()
    }

    private fun updateDueDate(date: Long?) {
        _uiState.value = _uiState.value.copy(dueDateLong = date, errorRes = null)
        recalculateLiveStatus()
    }

    private fun stepMonths(months: Int) {
        val cal = Calendar.getInstance()
        _uiState.value.dueDateLong?.let { cal.timeInMillis = it }
        cal.add(Calendar.MONTH, months)
        updateDueDate(cal.timeInMillis)
    }

    private fun stepDueKm(delta: Int) {
        val currentDisplay = _uiState.value.dueKm.toIntOrNull()
            ?: _uiState.value.vehicle?.let { DistanceUtil.kmToDisplay(it.currentOdometer, _uiState.value.distanceUnit) }
            ?: 0
        val newTarget = currentDisplay + delta
        updateDueKm(newTarget.toString())
    }

    private fun recalculateLiveStatus() {
        val state = _uiState.value
        val vehicle = state.vehicle ?: return
        val currentOdo = vehicle.currentOdometer
        val unit = state.distanceUnit

        val dueKmDisplay = state.dueKm.toIntOrNull()
        val dueKmKm = dueKmDisplay?.let { DistanceUtil.displayToKm(it, unit) }

        val newStatus = StatusCalculator.calculate(
            nowMillis = System.currentTimeMillis(),
            currentOdometer = currentOdo,
            dueDateMillis = state.dueDateLong,
            dueOdometer = dueKmKm,
            snoozeUntilMillis = originalReminder?.snoozeUntil,
            isCompleted = originalReminder?.isCompleted ?: false
        )
        _uiState.value = _uiState.value.copy(currentStatus = newStatus)
    }

    private fun saveReminder() {
        val state = _uiState.value

        if (state.serviceType == ServiceType.CUSTOM && state.customLabel.isBlank()) {
            _uiState.value = state.copy(errorRes = R.string.error_custom_reminder_name_required, errorArgs = emptyList())
            return
        }

        if (state.dueDateLong == null && state.dueKm.isBlank()) {
            _uiState.value = state.copy(errorRes = R.string.error_reminder_due_required, errorArgs = emptyList())
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorRes = null, errorArgs = emptyList())
            try {
                val existing = originalReminder
                if (existing != null) {
                    val dueKmDisplay = state.dueKm.toIntOrNull()
                    val dueKmInternal = dueKmDisplay?.let { DistanceUtil.displayToKm(it, state.distanceUnit) }

                    val intervalKmDisplay = state.intervalKm.toIntOrNull()
                    val intervalKmInternal = intervalKmDisplay?.let { DistanceUtil.displayToKm(it, state.distanceUnit) }

                    val updated = existing.copy(
                        serviceType = state.serviceType,
                        customLabel = if (state.serviceType == ServiceType.CUSTOM) state.customLabel else null,
                        intervalKm = intervalKmInternal,
                        intervalDays = state.intervalDays.toIntOrNull(),
                        nextDueOdometer = dueKmInternal,
                        nextDueDate = state.dueDateLong,
                        notes = state.notes,
                        updatedAt = System.currentTimeMillis()
                    )
                    reminderRepository.updateReminder(updated)
                    _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorRes = R.string.error_reminder_not_found
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to save reminder")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorRes = R.string.error_save_reminder_failed
                )
            }
        }
    }

    private fun deleteReminder() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorRes = null, errorArgs = emptyList())
            try {
                val existing = originalReminder
                if (existing != null) {
                    reminderRepository.deleteReminder(existing)
                    _uiState.value = _uiState.value.copy(isLoading = false, isDeleted = true)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete reminder")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorRes = R.string.error_delete_reminder_failed
                )
            }
        }
    }
}
