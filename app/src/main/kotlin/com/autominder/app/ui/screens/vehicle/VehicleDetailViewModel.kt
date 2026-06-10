package com.autominder.app.ui.screens.vehicle

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.autominder.app.domain.model.MileageLogEntry
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IMileageLogRepository
import com.autominder.app.domain.repository.IReminderRepository
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.repository.IFuelRepository
import com.autominder.app.domain.usecase.CalculateEfficiencyUseCase
import com.autominder.app.data.export.ExportServiceHistoryUseCase
import com.autominder.app.domain.usecase.StatusCalculator
import com.autominder.app.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.Calendar
import java.text.NumberFormat
import javax.inject.Inject

data class VehicleDetailUiState(
    val vehicle: Vehicle? = null,
    val reminders: List<Reminder> = emptyList(),
    val reminderStatuses: Map<Long, ServiceStatus> = emptyMap(),
    val totalCostCents: Int = 0,
    val yearCostCents: Int = 0,
    val averageEfficiency: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isArchived: Boolean = false,
    val exportUri: Uri? = null
)

sealed class VehicleDetailUiEvent {
    object ArchiveClicked : VehicleDetailUiEvent()
    object ExportClicked : VehicleDetailUiEvent()
    object ExportConsumed : VehicleDetailUiEvent()
    data class MarkReminderComplete(val reminderId: Long) : VehicleDetailUiEvent()
    data class SnoozeReminder(val reminderId: Long) : VehicleDetailUiEvent()
    data class UpdateOdometer(val odometerKm: Int) : VehicleDetailUiEvent()
}

@HiltViewModel
class VehicleDetailViewModel @Inject constructor(
    private val vehicleRepository: IVehicleRepository,
    private val reminderRepository: IReminderRepository,
    private val serviceRepository: IServiceRepository,
    private val fuelRepository: IFuelRepository,
    private val mileageLogRepository: IMileageLogRepository,
    private val exportServiceHistory: ExportServiceHistoryUseCase,
    private val calculateEfficiency: CalculateEfficiencyUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.toRoute<NavRoutes.VehicleDetail>().vehicleId

    private val _actionState = MutableStateFlow(ActionState())

    private val startOfYear: Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val refreshTrigger = MutableStateFlow(0)

    fun retry() {
        refreshTrigger.value++
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<VehicleDetailUiState> = refreshTrigger.flatMapLatest {
        combine(
            vehicleRepository.getVehicleById(vehicleId),
            reminderRepository.getActiveRemindersForVehicle(vehicleId),
            serviceRepository.getTotalCostForVehicle(vehicleId),
            serviceRepository.getCostSince(vehicleId, startOfYear),
            fuelRepository.getFuelEntriesForVehicle(vehicleId),
            _actionState
        ) { args ->
            val vehicle = args[0] as? com.autominder.app.domain.model.Vehicle
            val reminders = args[1] as List<com.autominder.app.domain.model.Reminder>
            val totalCost = args[2] as Int
            val yearCost = args[3] as Int
            val fuelEntries = args[4] as List<com.autominder.app.domain.model.FuelEntry>
            val action = args[5] as ActionState

        if (action.isArchived) {
            VehicleDetailUiState(isArchived = true)
        } else if (vehicle != null) {
            val now = System.currentTimeMillis()
            val statuses = reminders.associate { r ->
                r.id to StatusCalculator.calculate(
                    nowMillis = now,
                    currentOdometer = vehicle.currentOdometer,
                    dueDateMillis = r.nextDueDate,
                    dueOdometer = r.nextDueOdometer,
                    snoozeUntilMillis = r.snoozeUntil,
                    isCompleted = r.isCompleted
                )
            }
            VehicleDetailUiState(
                vehicle = vehicle,
                reminders = reminders,
                reminderStatuses = statuses,
                totalCostCents = totalCost,
                yearCostCents = yearCost,
                averageEfficiency = calculateEfficiency.calculateAverage(fuelEntries),
                error = action.error,
                exportUri = action.exportUri
            )
        } else {
            VehicleDetailUiState(error = "Vehicle not found")
        }
    }
    }
        .catch { e -> emit(VehicleDetailUiState(error = e.message ?: "Failed to load vehicle")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VehicleDetailUiState(isLoading = true)
        )

    fun onEvent(event: VehicleDetailUiEvent) {
        when (event) {
            is VehicleDetailUiEvent.ArchiveClicked -> archiveVehicle()
            is VehicleDetailUiEvent.ExportClicked -> exportHistory()
            is VehicleDetailUiEvent.ExportConsumed -> _actionState.update { it.copy(exportUri = null) }
            is VehicleDetailUiEvent.MarkReminderComplete -> markReminderComplete(event.reminderId)
            is VehicleDetailUiEvent.SnoozeReminder -> snoozeReminder(event.reminderId)
            is VehicleDetailUiEvent.UpdateOdometer -> updateOdometer(event.odometerKm)
        }
    }

    private fun updateOdometer(odometerKm: Int) {
        viewModelScope.launch {
            try {
                vehicleRepository.updateOdometer(vehicleId, odometerKm)
                mileageLogRepository.insertLog(
                    MileageLogEntry(
                        id = 0,
                        vehicleId = vehicleId,
                        odometer = odometerKm,
                        loggedAt = System.currentTimeMillis(),
                        notes = null
                    )
                )
            } catch (e: Exception) {
                _actionState.update { it.copy(error = e.message ?: "Failed to update odometer") }
            }
        }
    }

    private fun exportHistory() {
        viewModelScope.launch {
            try {
                val uri = exportServiceHistory(vehicleId)
                _actionState.update { it.copy(exportUri = uri) }
            } catch (e: Exception) {
                _actionState.update { it.copy(error = e.message ?: "Export failed") }
            }
        }
    }

    private fun archiveVehicle() {
        val vehicle = uiState.value.vehicle ?: return
        viewModelScope.launch {
            try {
                vehicleRepository.updateVehicle(vehicle.copy(isArchived = true))
                _actionState.value = _actionState.value.copy(isArchived = true)
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(error = e.message ?: "Failed to archive vehicle")
            }
        }
    }

    private fun markReminderComplete(reminderId: Long) {
        viewModelScope.launch {
            try {
                val reminder = reminderRepository.getReminderById(reminderId).firstOrNull()
                reminderRepository.markCompleted(reminderId)
                scheduleNextOccurrence(reminder)
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(error = e.message ?: "Failed to complete reminder")
            }
        }
    }

    private suspend fun scheduleNextOccurrence(reminder: Reminder?) {
        if (reminder == null) return
        val hasInterval = reminder.intervalKm != null || reminder.intervalDays != null
        if (!hasInterval) return

        val now = System.currentTimeMillis()
        val vehicle = vehicleRepository.getVehicleById(vehicleId).firstOrNull()
        val currentOdometer = vehicle?.currentOdometer ?: 0

        val nextReminder = Reminder(
            id = 0,
            vehicleId = reminder.vehicleId,
            serviceType = reminder.serviceType,
            customLabel = reminder.customLabel,
            intervalKm = reminder.intervalKm,
            intervalDays = reminder.intervalDays,
            nextDueOdometer = reminder.intervalKm?.let { currentOdometer + it },
            nextDueDate = reminder.intervalDays?.let { now + (it.toLong() * 86_400_000L) },
            notifyDaysBefore = reminder.notifyDaysBefore,
            notes = reminder.notes,
            createdAt = now,
            updatedAt = now
        )
        reminderRepository.insertReminder(nextReminder)
    }

    private fun snoozeReminder(reminderId: Long) {
        viewModelScope.launch {
            try {
                val snoozeUntil = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
                reminderRepository.snoozeReminder(reminderId, snoozeUntil)
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(error = e.message ?: "Failed to snooze reminder")
            }
        }
    }

    companion object {
        fun formatCost(cents: Int): String {
            return NumberFormat.getCurrencyInstance().format(cents / 100.0)
        }
    }
}

private data class ActionState(
    val isArchived: Boolean = false,
    val error: String? = null,
    val exportUri: Uri? = null
)
