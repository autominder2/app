package com.autominder.app.ui.screens.vehicle

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autominder.app.R
import com.autominder.app.core.di.DefaultDispatcher
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IReminderRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.usecase.StatusCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

/**
 * A vehicle plus its calculated status, role badge, and next upcoming service forecast.
 */
@Immutable
data class VehicleListItem(
    val vehicle: Vehicle,
    val status: ServiceStatus,
    val isPrimary: Boolean = false,
    val roleLabel: String? = null,
    val topConcern: Reminder? = null,
    val nextServiceLabel: String? = null,
    val nextServiceRemainingKm: Int? = null,
    val nextServiceDueDate: Long? = null
)

sealed class VehicleListUiState {
    object Loading : VehicleListUiState()
    object Empty : VehicleListUiState()
    data class Error(@StringRes val messageRes: Int) : VehicleListUiState()
    data class Success(
        val items: List<VehicleListItem>,
        val totalVehiclesCount: Int,
        val healthyCount: Int,
        val attentionCount: Int,
        val fleetUrgentVehicleName: String? = null,
        val fleetUrgentReminderLabel: String? = null,
        val fleetUrgentVehicleId: Long? = null
    ) : VehicleListUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VehicleListViewModel @Inject constructor(
    private val vehicleRepository: IVehicleRepository,
    private val reminderRepository: IReminderRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

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
                var healthyCount = 0

                var fleetUrgentVehicleName: String? = null
                var fleetUrgentReminderLabel: String? = null
                var fleetUrgentVehicleId: Long? = null
                var highestFleetSeverity = -1

                val items = vehicles.mapIndexed { index, vehicle ->
                    val vehicleReminders = reminders.filter { it.vehicleId == vehicle.id }
                    val isPrimary = index == 0

                    val roleLabel = when {
                        vehicle.notes.contains("Daily", ignoreCase = true) -> "Daily Driver"
                        vehicle.notes.contains("Family", ignoreCase = true) -> "Family Hauler"
                        vehicle.notes.contains("Weekend", ignoreCase = true) -> "Weekend Toy"
                        vehicle.notes.contains("Work", ignoreCase = true) -> "Work Horse"
                        isPrimary -> "Daily Driver"
                        index == 1 -> "Family"
                        index == 2 -> "Weekend"
                        else -> null
                    }

                    var worstStatus = ServiceStatus.UNKNOWN
                    var worstReminder: Reminder? = null
                    var soonestReminder: Reminder? = null
                    var soonestRemainingKm: Int? = null

                    vehicleReminders.forEach { reminder ->
                        val status = StatusCalculator.calculate(
                            nowMillis = now,
                            currentOdometer = vehicle.currentOdometer,
                            dueDateMillis = reminder.nextDueDate,
                            dueOdometer = reminder.nextDueOdometer,
                            snoozeUntilMillis = reminder.snoozeUntil,
                            isCompleted = reminder.isCompleted
                        )

                        val remainingKm = if (reminder.nextDueOdometer != null && vehicle.currentOdometer > 0) {
                            reminder.nextDueOdometer - vehicle.currentOdometer
                        } else null

                        if (worstReminder == null || status.severity > worstStatus.severity) {
                            worstStatus = status
                            worstReminder = reminder
                        }

                        if (soonestReminder == null) {
                            soonestReminder = reminder
                            soonestRemainingKm = remainingKm
                        } else {
                            val curDate = soonestReminder?.nextDueDate
                            val newDate = reminder.nextDueDate
                            if (newDate != null && (curDate == null || newDate < curDate)) {
                                soonestReminder = reminder
                                soonestRemainingKm = remainingKm
                            }
                        }
                    }

                    if (worstStatus == ServiceStatus.OVERDUE || worstStatus == ServiceStatus.DUE_SOON) {
                        attentionCount++
                        if (worstStatus.severity > highestFleetSeverity) {
                            highestFleetSeverity = worstStatus.severity
                            fleetUrgentVehicleName = "${vehicle.make} ${vehicle.model}"
                            fleetUrgentReminderLabel = worstReminder?.customLabel ?: worstReminder?.serviceType?.label
                            fleetUrgentVehicleId = vehicle.id
                        }
                    } else if (worstStatus == ServiceStatus.OK) {
                        healthyCount++
                    }

                    val nextServiceLabel = (worstReminder?.takeIf { worstStatus == ServiceStatus.OVERDUE || worstStatus == ServiceStatus.DUE_SOON }
                        ?: soonestReminder)?.let { it.customLabel ?: it.serviceType.label }

                    VehicleListItem(
                        vehicle = vehicle,
                        status = worstStatus,
                        isPrimary = isPrimary,
                        roleLabel = roleLabel,
                        topConcern = worstReminder.takeIf {
                            worstStatus == ServiceStatus.OVERDUE || worstStatus == ServiceStatus.DUE_SOON
                        },
                        nextServiceLabel = nextServiceLabel,
                        nextServiceRemainingKm = soonestRemainingKm,
                        nextServiceDueDate = soonestReminder?.nextDueDate
                    )
                }.sortedWith(
                    compareByDescending<VehicleListItem> { it.status.severity }
                        .thenByDescending { it.isPrimary }
                )

                VehicleListUiState.Success(
                    items = items,
                    totalVehiclesCount = vehicles.size,
                    healthyCount = healthyCount,
                    attentionCount = attentionCount,
                    fleetUrgentVehicleName = fleetUrgentVehicleName,
                    fleetUrgentReminderLabel = fleetUrgentReminderLabel,
                    fleetUrgentVehicleId = fleetUrgentVehicleId
                )
            }
                .flowOn(defaultDispatcher)
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
