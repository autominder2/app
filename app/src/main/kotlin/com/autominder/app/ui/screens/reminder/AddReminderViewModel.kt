package com.autominder.app.ui.screens.reminder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.repository.IReminderRepository
import com.autominder.app.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    val error: String? = null,
    val isSaved: Boolean = false
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
}

@HiltViewModel
class AddReminderViewModel @Inject constructor(
    private val reminderRepository: IReminderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.toRoute<NavRoutes.AddReminder>().vehicleId

    private val _uiState = MutableStateFlow(AddReminderUiState())
    val uiState: StateFlow<AddReminderUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddReminderUiEvent) {
        when (event) {
            is AddReminderUiEvent.TitleChanged -> _uiState.value = _uiState.value.copy(title = event.title)
            is AddReminderUiEvent.DescriptionChanged -> _uiState.value = _uiState.value.copy(description = event.description)
            is AddReminderUiEvent.DueKmChanged -> _uiState.value = _uiState.value.copy(dueKm = event.dueKm)
            is AddReminderUiEvent.DueDateChanged -> _uiState.value = _uiState.value.copy(dueDateLong = event.date)
            is AddReminderUiEvent.IntervalKmChanged -> _uiState.value = _uiState.value.copy(intervalKm = event.intervalKm)
            is AddReminderUiEvent.IntervalDaysChanged -> _uiState.value = _uiState.value.copy(intervalDays = event.intervalDays)
            is AddReminderUiEvent.ServiceTypeChanged -> _uiState.value = _uiState.value.copy(serviceType = event.type)
            is AddReminderUiEvent.SaveClicked -> saveReminder()
        }
    }

    private fun saveReminder() {
        val state = _uiState.value
        
        if (state.serviceType == ServiceType.CUSTOM && state.title.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please specify a name for your custom reminder")
            return
        }

        if (state.dueDateLong == null && state.dueKm.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please specify a due date or due odometer")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
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
                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to save reminder")
            }
        }
    }
}
