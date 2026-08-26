package com.autominder.app.ui.screens.vehicle

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.autominder.app.R
import com.autominder.app.core.util.AnalyticsEvents
import com.autominder.app.core.util.AnalyticsHelper
import com.autominder.app.core.util.AnalyticsParams
import com.autominder.app.data.export.ExportServiceHistoryUseCase
import com.autominder.app.domain.model.FuelEntry
import com.autominder.app.domain.model.MileageLogEntry
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IFuelRepository
import com.autominder.app.domain.repository.IMileageLogRepository
import com.autominder.app.domain.repository.IReminderRepository
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.usecase.CalculateEfficiencyUseCase
import com.autominder.app.domain.usecase.DuePrediction
import com.autominder.app.domain.usecase.PredictDueUseCase
import com.autominder.app.domain.usecase.StatusCalculator
import com.autominder.app.domain.usecase.cockpit.CalculateConfidenceUseCase
import com.autominder.app.domain.usecase.cockpit.CalculateDrivingPatternUseCase
import com.autominder.app.domain.usecase.cockpit.CalculateOwnershipCostUseCase
import com.autominder.app.domain.usecase.cockpit.DrivingPattern
import com.autominder.app.domain.usecase.cockpit.MonthlySpend
import com.autominder.app.domain.usecase.cockpit.OwnershipCostSummary
import com.autominder.app.domain.usecase.cockpit.TypeSpend
import com.autominder.app.domain.usecase.cockpit.VehicleConfidence
import com.autominder.app.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.NumberFormat
import java.util.Calendar
import javax.inject.Inject

typealias TypeSpend = com.autominder.app.domain.usecase.cockpit.TypeSpend
typealias MonthlySpend = com.autominder.app.domain.usecase.cockpit.MonthlySpend

@Immutable
data class VehicleDetailUiState(
    val vehicle: Vehicle? = null,
    val reminders: List<Reminder> = emptyList(),
    val reminderStatuses: Map<Long, ServiceStatus> = emptyMap(),
    val reminderPredictions: Map<Long, DuePrediction> = emptyMap(),
    val confidence: VehicleConfidence = VehicleConfidence(),
    val drivingPattern: DrivingPattern = DrivingPattern(),
    val costSummary: OwnershipCostSummary = OwnershipCostSummary(),
    val recentServices: List<Service> = emptyList(),
    val averageEfficiency: Double = 0.0,
    val efficiencySeries: List<Double> = emptyList(),
    val isLoading: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val errorArgs: List<Any> = emptyList(),
    val isArchived: Boolean = false,
    val exportUri: Uri? = null
)

sealed class VehicleDetailUiEvent {
    data object ArchiveClicked : VehicleDetailUiEvent()
    data object ExportClicked : VehicleDetailUiEvent()
    data object ExportConsumed : VehicleDetailUiEvent()
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
    private val predictDue: PredictDueUseCase,
    private val calculateConfidence: CalculateConfidenceUseCase,
    private val calculateDrivingPattern: CalculateDrivingPatternUseCase,
    private val calculateOwnershipCost: CalculateOwnershipCostUseCase,
    private val analyticsHelper: AnalyticsHelper,
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
            serviceRepository.getServicesForVehicle(vehicleId),
            mileageLogRepository.getLogsForVehicle(vehicleId),
            _actionState
        ) { args ->
            val vehicle = args[0] as? Vehicle
            @Suppress("UNCHECKED_CAST")
            val reminders = args[1] as List<Reminder>
            val totalCost = args[2] as Int
            val yearCost = args[3] as Int
            @Suppress("UNCHECKED_CAST")
            val fuelEntries = args[4] as List<FuelEntry>
            @Suppress("UNCHECKED_CAST")
            val services = args[5] as List<Service>
            @Suppress("UNCHECKED_CAST")
            val mileageLogs = args[6] as List<MileageLogEntry>
            val action = args[7] as ActionState

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

                val drivingPattern = calculateDrivingPattern.execute(mileageLogs, fuelEntries)
                val predictions = reminders.associate { r ->
                    r.id to predictDue.predict(r, vehicle.currentOdometer, drivingPattern.dailyKmRate, now)
                }

                val confidence = calculateConfidence.execute(
                    vehicle = vehicle,
                    reminders = reminders,
                    statuses = statuses,
                    mileageLogs = mileageLogs,
                    services = services,
                    nowMillis = now
                )

                val costSummary = calculateOwnershipCost.execute(
                    totalCostCents = totalCost,
                    yearCostCents = yearCost,
                    services = services,
                    fuelEntries = fuelEntries,
                    currentOdometer = vehicle.currentOdometer,
                    nowMillis = now
                )

                VehicleDetailUiState(
                    vehicle = vehicle,
                    reminders = reminders,
                    reminderStatuses = statuses,
                    reminderPredictions = predictions,
                    confidence = confidence,
                    drivingPattern = drivingPattern,
                    costSummary = costSummary,
                    recentServices = services.sortedByDescending { it.serviceDate }.take(5),
                    averageEfficiency = calculateEfficiency.calculateAverage(fuelEntries),
                    efficiencySeries = computeEfficiencySeries(fuelEntries),
                    errorRes = action.errorRes,
                    errorArgs = action.errorArgs,
                    exportUri = action.exportUri
                )
            } else {
                VehicleDetailUiState(errorRes = R.string.error_vehicle_not_found)
            }
        }
    }
        .catch { e ->
            Timber.e(e, "Failed to load vehicle")
            emit(VehicleDetailUiState(errorRes = R.string.error_load_vehicle_failed))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VehicleDetailUiState(isLoading = true)
        )

    private fun computeEfficiencySeries(
        fuelEntries: List<FuelEntry>
    ): List<Double> {
        val sorted = fuelEntries.sortedBy { it.odometer }
        return sorted.mapIndexedNotNull { index, entry ->
            val previous = sorted.getOrNull(index - 1) ?: return@mapIndexedNotNull null
            val eff = calculateEfficiency.calculate(entry, previous)
            if (eff > 0) eff else null
        }
    }

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
                analyticsHelper.logEvent(
                    AnalyticsEvents.ODOMETER_UPDATED,
                    mapOf(AnalyticsParams.ODOMETER_VALUE to odometerKm)
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to update odometer")
                _actionState.update { it.copy(errorRes = R.string.error_update_odometer_failed, errorArgs = emptyList()) }
            }
        }
    }

    private fun exportHistory() {
        viewModelScope.launch {
            try {
                val uri = exportServiceHistory(vehicleId)
                _actionState.update { it.copy(exportUri = uri) }
                analyticsHelper.logEvent(AnalyticsEvents.HISTORY_EXPORTED)
            } catch (e: Exception) {
                Timber.e(e, "Export failed")
                _actionState.update { it.copy(errorRes = R.string.error_export_failed, errorArgs = emptyList()) }
            }
        }
    }

    private fun archiveVehicle() {
        val vehicle = uiState.value.vehicle ?: return
        viewModelScope.launch {
            try {
                vehicleRepository.updateVehicle(vehicle.copy(isArchived = true))
                analyticsHelper.logEvent(
                    AnalyticsEvents.VEHICLE_ARCHIVED,
                    mapOf(
                        AnalyticsParams.VEHICLE_MAKE to vehicle.make,
                        AnalyticsParams.VEHICLE_MODEL to vehicle.model
                    )
                )
                _actionState.value = _actionState.value.copy(isArchived = true)
            } catch (e: Exception) {
                Timber.e(e, "Failed to archive vehicle")
                _actionState.value = _actionState.value.copy(errorRes = R.string.error_archive_vehicle_failed, errorArgs = emptyList())
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
                Timber.e(e, "Failed to complete reminder")
                _actionState.value = _actionState.value.copy(errorRes = R.string.error_complete_reminder_failed, errorArgs = emptyList())
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
                Timber.e(e, "Failed to snooze reminder")
                _actionState.value = _actionState.value.copy(errorRes = R.string.error_snooze_reminder_failed, errorArgs = emptyList())
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
    @StringRes val errorRes: Int? = null,
    val errorArgs: List<Any> = emptyList(),
    val exportUri: Uri? = null
)
