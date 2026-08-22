package com.autominder.app.ui.screens.vehicle

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autominder.app.R
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IReminderRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.usecase.StatusCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject

/**
 * A vehicle plus its calculated status and (if attention is needed) the
 * single most urgent reminder driving that status — reuses the same
 * [StatusCalculator] algorithm as [com.autominder.app.domain.usecase.GetDashboardDataUseCase],
 * scoped to this one vehicle rather than the whole fleet.
 */
data class VehicleListItem(
    val vehicle: Vehicle,
    val status: ServiceStatus,
    val topConcern: Reminder? = null
)

sealed class VehicleListUiState {
    object Loading : VehicleListUiState()
    object Empty : VehicleListUiState()
    data class Error(@StringRes val messageRes: Int) : VehicleListUiState()
    data class Success(
        val items: List<VehicleListItem>,
        val attentionCount: Int
    ) : VehicleListUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VehicleListViewModel @Inject constructor(
    private val vehicleRepository: IVehicleRepository,
    private val reminderRepository: IReminderRepository
) : ViewModel() {

    // A `catch` on a Room flow completes it permanently — retry() re-subscribes
    // so the Error state's retry button actually works.
    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<VehicleListUiState> = retryTrigger
        .flatMapLatest {
            combine(
                vehicleRepository.getAllVehicles(),
                reminderRepository.getAllPendingReminders()
            ) { vehicles, reminders ->
                if (vehicles.isEmpty()) return@combine VehicleListUiState.Empty

                val now = System.currentTimeMillis()
                var attentionCount = 0

                val items = vehicles.map { vehicle ->
                    val vehicleReminders = reminders.filter { it.vehicleId == vehicle.id }
                    // UNKNOWN, not OK: a vehicle with zero reminders has no
                    // data to be "OK" about — never claim all-good from
                    // absent data. First reminder iteration overwrites this.
                    var worstStatus = ServiceStatus.UNKNOWN
                    var worstReminder: Reminder? = null

                    vehicleReminders.forEach { reminder ->
                        val status = StatusCalculator.calculate(
                            nowMillis = now,
                            currentOdometer = vehicle.currentOdometer,
                            dueDateMillis = reminder.nextDueDate,
                            dueOdometer = reminder.nextDueOdometer,
                            snoozeUntilMillis = reminder.snoozeUntil,
                            isCompleted = reminder.isCompleted
                        )
                        if (worstReminder == null || status.severity > worstStatus.severity) {
                            worstStatus = status
                            worstReminder = reminder
                        }
                    }
                    if (worstStatus == ServiceStatus.OVERDUE || worstStatus == ServiceStatus.DUE_SOON) {
                        attentionCount++
                    }

                    VehicleListItem(
                        vehicle = vehicle,
                        status = worstStatus,
                        topConcern = worstReminder.takeIf {
                            worstStatus == ServiceStatus.OVERDUE || worstStatus == ServiceStatus.DUE_SOON
                        }
                    )
                }.sortedByDescending { it.status.severity }

                VehicleListUiState.Success(items = items, attentionCount = attentionCount)
            }
                .onStart<VehicleListUiState> { emit(VehicleListUiState.Loading) }
                .catch { e ->
                    Timber.e(e, "Failed to load vehicles")
                    emit(VehicleListUiState.Error(R.string.error_load_vehicles_failed))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VehicleListUiState.Loading
        )

    fun retry() {
        retryTrigger.value++
    }
}
