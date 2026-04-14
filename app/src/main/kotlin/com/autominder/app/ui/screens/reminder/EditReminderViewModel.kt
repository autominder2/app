package com.autominder.app.ui.screens.reminder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.repository.IReminderRepository
import com.autominder.app.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditReminderUiState(
    val serviceType: ServiceType = ServiceType.OIL_CHANGE,
    val customLabel: String = "",
    val dueKm: String = "",
    val dueDateLong: Long? = null,
    val intervalKm: String = "",
    val intervalDays: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)

sealed class EditReminderUiEvent {
    data class ServiceTypeChanged(val type: ServiceType) : EditReminderUiEvent()
    data class CustomLabelChanged(val label: String) : EditReminderUiEvent()
    data class DueKmChanged(val dueKm: String) : EditReminderUiEvent()
    data class DueDateChanged(val date: Long?) : EditReminderUiEvent()
    data class IntervalKmChanged(val intervalKm: String) : EditReminderUiEvent()
    data class IntervalDaysChanged(val intervalDays: String) : EditReminderUiEvent()
    data class NotesChanged(val notes: String) : EditReminderUiEvent()
    object SaveClicked : EditReminderUiEvent()
    object DeleteClicked : EditReminderUiEvent()
}

@HiltViewModel
class EditReminderViewModel @Inject constructor(
    private val reminderRepository: IReminderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reminderId: Long = savedStateHandle.toRoute<NavRoutes.EditReminder>().reminderId

    private val _uiState = MutableStateFlow(EditReminderUiState())
    val uiState: StateFlow<EditReminderUiState> = _uiState.asStateFlow()

    private var originalReminder: com.autominder.app.domain.model.Reminder? = null

    init {
        loadReminder()
    }

    private fun loadReminder() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val reminder = reminderRepository.getReminderById(reminderId).firstOrNull()
                if (reminder != null) {
                    originalReminder = reminder
                    _uiState.value = _uiState.value.copy(
                        serviceType = reminder.serviceType,
                        customLabel = reminder.customLabel ?: "",
                        dueKm = reminder.nextDueOdometer?.toString() ?: "",
                        dueDateLong = reminder.nextDueDate,
                        intervalKm = reminder.intervalKm?.toString() ?: "",
                        intervalDays = reminder.intervalDays?.toString() ?: "",
                        notes = reminder.notes,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Reminder not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load reminder"
                )
            }
        }
    }

    fun onEvent(event: EditReminderUiEvent) {
        when (event) {
            is EditReminderUiEvent.ServiceTypeChanged -> _uiState.value = _uiState.value.copy(serviceType = event.type)
            is EditReminderUiEvent.CustomLabelChanged -> _uiState.value = _uiState.value.copy(customLabel = event.label)
            is EditReminderUiEvent.DueKmChanged -> _uiState.value = _uiState.value.copy(dueKm = event.dueKm)
            is EditReminderUiEvent.DueDateChanged -> _uiState.value = _uiState.value.copy(dueDateLong = event.date)
            is EditReminderUiEvent.IntervalKmChanged -> _uiState.value = _uiState.value.copy(intervalKm = event.intervalKm)
            is EditReminderUiEvent.IntervalDaysChanged -> _uiState.value = _uiState.value.copy(intervalDays = event.intervalDays)
            is EditReminderUiEvent.NotesChanged -> _uiState.value = _uiState.value.copy(notes = event.notes)
            is EditReminderUiEvent.SaveClicked -> saveReminder()
            is EditReminderUiEvent.DeleteClicked -> deleteReminder()
        }
    }

    private fun saveReminder() {
        val state = _uiState.value

        if (state.serviceType == ServiceType.CUSTOM && state.customLabel.isBlank()) {
            _uiState.value = state.copy(error = "Please specify a name for your custom reminder")
            return
        }

        if (state.dueDateLong == null && state.dueKm.isBlank()) {
            _uiState.value = state.copy(error = "Please specify a due date or due odometer")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val existing = originalReminder
                if (existing != null) {
                    val updated = existing.copy(
                        serviceType = state.serviceType,
                        customLabel = if (state.serviceType == ServiceType.CUSTOM) state.customLabel else null,
                        intervalKm = state.intervalKm.toIntOrNull(),
                        intervalDays = state.intervalDays.toIntOrNull(),
                        nextDueOdometer = state.dueKm.toIntOrNull(),
                        nextDueDate = state.dueDateLong,
                        notes = state.notes,
                        updatedAt = System.currentTimeMillis()
                    )
                    reminderRepository.updateReminder(updated)
                    _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Reminder not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save reminder"
                )
            }
        }
    }

    private fun deleteReminder() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val existing = originalReminder
                if (existing != null) {
                    reminderRepository.deleteReminder(existing)
                    _uiState.value = _uiState.value.copy(isLoading = false, isDeleted = true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to delete reminder"
                )
            }
        }
    }
}
