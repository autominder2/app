package com.autominder.app.ui.screens.dashboard

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autominder.app.R
import com.autominder.app.core.util.AppInfo
import com.autominder.app.core.util.ReviewHelper
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.FuelEntry
import com.autominder.app.domain.model.MileageLogEntry
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.repository.IFuelRepository
import com.autominder.app.domain.repository.IMileageLogRepository
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.usecase.CalculateEfficiencyUseCase
import com.autominder.app.domain.usecase.DataConfidence
import com.autominder.app.domain.usecase.GetDashboardDataUseCase
import com.autominder.app.domain.usecase.PrioritizedReminder
import com.autominder.app.domain.usecase.ReminderExplanation
import com.autominder.app.domain.usecase.ReminderPriorityEngine
import com.autominder.app.domain.usecase.ReminderWithStatus
import com.autominder.app.domain.usecase.VehicleWithStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class VehicleOperationalStatus {
    HEALTHY,
    UPCOMING,
    DUE_SOON,
    OVERDUE,
    SETUP_INCOMPLETE
}

data class RemindersDelayedState(val lastCheckedAt: Long?)

fun evaluateReminderStaleness(
    lastSuccessfulCheckAt: Long?,
    firstInstallTimeMillis: Long,
    nowMillis: Long = System.currentTimeMillis()
): RemindersDelayedState? {
    val thirtySixHours = 36L * 60 * 60 * 1000
    if (lastSuccessfulCheckAt != null) {
        if (nowMillis - lastSuccessfulCheckAt > thirtySixHours && nowMillis >= lastSuccessfulCheckAt) {
            return RemindersDelayedState(lastSuccessfulCheckAt)
        }
    } else {
        if (nowMillis - firstInstallTimeMillis > thirtySixHours) {
            return RemindersDelayedState(null)
        }
    }
    return null
}

data class HomeActivityItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val timestamp: Long,
    val itemType: ActivityType
) {
    enum class ActivityType {
        SERVICE,
        FUEL,
        MILEAGE
    }
}

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object Empty : DashboardUiState
    data class Success(
        val vehicles: List<VehicleWithStatus>,
        val alertsCount: Int,
        val attentionReminders: List<ReminderWithStatus>,
        val primaryCostPerDistanceCents: Double?,
        val primaryAvgEfficiency: Double?,
        val distanceUnit: String = "km",
        val lastSuccessfulCheckAt: Long? = null,
        val selectedVehicleId: Long? = null,
        val selectedVehicle: VehicleWithStatus? = null,
        val vehicleStatus: VehicleOperationalStatus = VehicleOperationalStatus.HEALTHY,
        val upcomingReminders: List<ReminderWithStatus> = emptyList(),
        val prioritizedReminders: List<PrioritizedReminder> = emptyList(),
        val nextCheck: PrioritizedReminder? = null,
        val recentActivity: List<HomeActivityItem> = emptyList()
    ) : DashboardUiState
    data class Error(val messageRes: Int? = null) : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val fuelRepository: IFuelRepository,
    private val serviceRepository: IServiceRepository,
    private val mileageLogRepository: IMileageLogRepository,
    private val calculateEfficiency: CalculateEfficiencyUseCase,
    private val reminderPriorityEngine: ReminderPriorityEngine,
    private val reviewHelper: ReviewHelper,
    private val userPreferences: UserPreferences,
    private val appInfo: AppInfo
) : ViewModel() {

    private val selectedVehicleIdFlow = MutableStateFlow<Long?>(null)
    private val latestServicesMap = mutableMapOf<ServiceType, Service>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = combine(
        getDashboardDataUseCase(),
        userPreferences.distanceUnit,
        userPreferences.lastSuccessfulCheckAt,
        selectedVehicleIdFlow
    ) { data, distanceUnit, lastCheck, selectedId ->
        DataWithPrefs(data, distanceUnit, lastCheck, selectedId)
    }.flatMapLatest { (data, distanceUnit, lastCheck, selectedId) ->
        if (data.vehiclesWithStatus.isEmpty()) {
            flowOf(DashboardUiState.Empty)
        } else {
            val activeVehicle = if (selectedId != null) {
                data.vehiclesWithStatus.find { it.vehicle.id == selectedId } ?: data.vehiclesWithStatus.first()
            } else {
                data.vehiclesWithStatus.first()
            }
            val activeVehicleId = activeVehicle.vehicle.id

            combine(
                fuelRepository.getFuelEntriesForVehicle(activeVehicleId),
                serviceRepository.getServicesForVehicle(activeVehicleId),
                mileageLogRepository.getLogsForVehicle(activeVehicleId)
            ) { fuels, services, mileageLogs ->
                // Cache latest services for explainability
                services.forEach { s ->
                    if (!latestServicesMap.containsKey(s.serviceType) || (latestServicesMap[s.serviceType]?.serviceDate ?: 0) < s.serviceDate) {
                        latestServicesMap[s.serviceType] = s
                    }
                }

                val avgEfficiency = if (fuels.size >= 2) {
                    calculateEfficiency.calculateAverage(fuels)
                } else null

                val vehicleReminders = data.upcomingReminders.filter { it.reminder.vehicleId == activeVehicleId }

                // Rank with domain priority engine
                val prioritized = reminderPriorityEngine.rankReminders(
                    items = vehicleReminders,
                    lastServices = latestServicesMap
                )

                val vehicleStatus = computeOperationalStatus(
                    activeVehicle = activeVehicle,
                    reminders = vehicleReminders
                )

                val nextCheck = prioritized.firstOrNull { it.remainingKm != null && it.remainingKm > 0 }

                val recentActivity = buildRecentActivity(
                    vehicleName = "${activeVehicle.vehicle.make} ${activeVehicle.vehicle.model}".trim(),
                    services = services,
                    fuels = fuels,
                    mileageLogs = mileageLogs,
                    distanceUnit = distanceUnit
                )

                DashboardUiState.Success(
                    vehicles = data.vehiclesWithStatus,
                    alertsCount = data.alertsCount,
                    attentionReminders = vehicleReminders.filter { it.status == ServiceStatus.OVERDUE || it.status == ServiceStatus.DUE_SOON },
                    primaryCostPerDistanceCents = null,
                    primaryAvgEfficiency = avgEfficiency,
                    distanceUnit = distanceUnit,
                    lastSuccessfulCheckAt = lastCheck,
                    selectedVehicleId = activeVehicleId,
                    selectedVehicle = activeVehicle,
                    vehicleStatus = vehicleStatus,
                    upcomingReminders = vehicleReminders.take(3),
                    prioritizedReminders = prioritized.take(3),
                    nextCheck = nextCheck,
                    recentActivity = recentActivity
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading
    )

    fun selectVehicle(vehicleId: Long) {
        selectedVehicleIdFlow.value = vehicleId
    }

    fun explainReminder(prioritizedItem: PrioritizedReminder): ReminderExplanation {
        val lastService = latestServicesMap[prioritizedItem.reminderWithStatus.reminder.serviceType]
        return reminderPriorityEngine.buildExplanation(
            item = prioritizedItem.reminderWithStatus,
            lastService = lastService
        )
    }

    private fun computeOperationalStatus(
        activeVehicle: VehicleWithStatus,
        reminders: List<ReminderWithStatus>
    ): VehicleOperationalStatus {
        val odo = activeVehicle.vehicle.currentOdometer
        if (odo <= 0) {
            return VehicleOperationalStatus.SETUP_INCOMPLETE
        }
        if (reminders.any { it.status == ServiceStatus.OVERDUE }) {
            return VehicleOperationalStatus.OVERDUE
        }
        if (reminders.any { it.status == ServiceStatus.DUE_SOON }) {
            return VehicleOperationalStatus.DUE_SOON
        }
        if (reminders.isNotEmpty()) {
            return VehicleOperationalStatus.UPCOMING
        }
        return VehicleOperationalStatus.HEALTHY
    }

    private fun buildRecentActivity(
        vehicleName: String,
        services: List<Service>,
        fuels: List<FuelEntry>,
        mileageLogs: List<MileageLogEntry>,
        distanceUnit: String
    ): List<HomeActivityItem> {
        val items = mutableListOf<HomeActivityItem>()

        services.forEach { service ->
            items.add(
                HomeActivityItem(
                    id = "service_${service.id}",
                    title = service.serviceType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    subtitle = "$vehicleName • ${com.autominder.app.ui.util.DateFormatUtil.formatDate(service.serviceDate)}",
                    timestamp = service.serviceDate,
                    itemType = HomeActivityItem.ActivityType.SERVICE
                )
            )
        }

        fuels.forEach { fuel ->
            items.add(
                HomeActivityItem(
                    id = "fuel_${fuel.id}",
                    title = "Fuel",
                    subtitle = "$vehicleName • ${com.autominder.app.ui.util.DateFormatUtil.formatDate(fuel.date.time)}",
                    timestamp = fuel.date.time,
                    itemType = HomeActivityItem.ActivityType.FUEL
                )
            )
        }

        mileageLogs.forEach { log ->
            val displayKm = com.autominder.app.domain.util.DistanceUtil.kmToDisplay(log.odometer, distanceUnit)
            val unitStr = com.autominder.app.domain.util.DistanceUtil.unitLabel(distanceUnit)
            items.add(
                HomeActivityItem(
                    id = "mileage_${log.id}",
                    title = "Mileage updated",
                    subtitle = "${com.autominder.app.ui.util.DistanceFormat.grouped(displayKm)} $unitStr • ${com.autominder.app.ui.util.DateFormatUtil.formatDate(log.loggedAt)}",
                    timestamp = log.loggedAt,
                    itemType = HomeActivityItem.ActivityType.MILEAGE
                )
            )
        }

        return items.sortedByDescending { it.timestamp }.take(3)
    }

    fun requestReviewIfAppropriate(activity: Activity) {
        viewModelScope.launch {
            reviewHelper.requestReviewIfAppropriate(activity)
        }
    }

    fun retry() {
        // Triggers re-flow
    }

    companion object {
        fun getGreetingRes(): Int {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when (hour) {
                in 4..11 -> R.string.home_greeting_morning
                in 12..16 -> R.string.home_greeting_afternoon
                else -> R.string.home_greeting_evening
            }
        }
    }
}

private data class DataWithPrefs(
    val data: com.autominder.app.domain.usecase.DashboardData,
    val distanceUnit: String,
    val lastCheck: Long?,
    val selectedId: Long?
)
