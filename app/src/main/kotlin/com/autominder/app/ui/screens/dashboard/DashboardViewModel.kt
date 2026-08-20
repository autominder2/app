package com.autominder.app.ui.screens.dashboard

import android.app.Activity
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autominder.app.R
import com.autominder.app.core.util.AppInfo
import com.autominder.app.core.util.ReviewHelper
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.repository.IFuelRepository
import com.autominder.app.domain.usecase.CalculateEfficiencyUseCase
import com.autominder.app.domain.usecase.GetDashboardDataUseCase
import com.autominder.app.domain.usecase.ReminderWithStatus
import com.autominder.app.domain.usecase.VehicleWithStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * State of the dashboard — follows unidirectional data flow (UDF).
 */
sealed class DashboardUiState {
    object Loading : DashboardUiState()
    object Empty : DashboardUiState()
    data class Error(@StringRes val messageRes: Int? = null) : DashboardUiState()
    data class Success(
        val vehicles: List<VehicleWithStatus>,
        val alertsCount: Int,
        /** Top urgent reminders (display-only slice of the use case's sorted list). */
        val attentionReminders: List<ReminderWithStatus> = emptyList(),
        val primaryCostPerDistanceCents: Double? = null,
        val primaryAvgEfficiency: Double? = null,
        val distanceUnit: String = "km"
    ) : DashboardUiState()
}

/**
 * Present only when the background reminder engine has been silent long
 * enough that the user should be told.
 */
data class RemindersDelayedState(val lastCheckedAt: Long?)

private const val STALE_AFTER_MS = 36L * 60 * 60 * 1000

internal fun evaluateReminderStaleness(
    lastSuccessfulCheckAt: Long?,
    firstInstallTimeMillis: Long,
    nowMillis: Long
): RemindersDelayedState? {
    val lastKnownLiveness = lastSuccessfulCheckAt ?: firstInstallTimeMillis
    if (nowMillis - lastKnownLiveness <= STALE_AFTER_MS) return null
    return RemindersDelayedState(lastCheckedAt = lastSuccessfulCheckAt)
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val fuelRepository: IFuelRepository,
    private val calculateEfficiency: CalculateEfficiencyUseCase,
    private val reviewHelper: ReviewHelper,
    private val userPreferences: UserPreferences,
    appInfo: AppInfo
) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(0)

    val remindersDelayed: StateFlow<RemindersDelayedState?> =
        userPreferences.lastSuccessfulCheckAt
            .map { lastCheck ->
                evaluateReminderStaleness(
                    lastSuccessfulCheckAt = lastCheck,
                    firstInstallTimeMillis = appInfo.firstInstallTimeMillis,
                    nowMillis = System.currentTimeMillis()
                )
            }
            .catch { e ->
                Timber.e(e, "Staleness check failed")
                emit(null)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    fun retry() {
        refreshTrigger.value++
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = refreshTrigger.flatMapLatest {
        getDashboardDataUseCase().flatMapLatest { data ->
            if (data.vehiclesWithStatus.isEmpty()) {
                flowOf(DashboardUiState.Empty)
            } else {
                val primaryVehicle = data.vehiclesWithStatus.first().vehicle
                combine(
                    fuelRepository.getFuelEntriesForVehicle(primaryVehicle.id),
                    userPreferences.distanceUnit
                ) { fuelEntries, unit ->
                    val totalCost = fuelEntries.sumOf { it.costCents }
                    val sortedAsc = fuelEntries.sortedBy { it.odometer }
                    val totalDistance = if (sortedAsc.size >= 2) {
                        sortedAsc.last().odometer - sortedAsc.first().odometer
                    } else 0

                    val costPerDist = if (totalDistance > 0) {
                        val displayDist = if (unit == "mi") totalDistance * 0.621371 else totalDistance.toDouble()
                        totalCost.toDouble() / displayDist
                    } else null

                    val avgEff = if (fuelEntries.size >= 2) {
                        val eff = calculateEfficiency.calculateAverage(fuelEntries)
                        if (eff > 0.0) eff else null
                    } else null

                    DashboardUiState.Success(
                        vehicles = data.vehiclesWithStatus,
                        alertsCount = data.alertsCount,
                        attentionReminders = data.upcomingReminders
                            .filter {
                                it.status == ServiceStatus.OVERDUE ||
                                    it.status == ServiceStatus.DUE_SOON
                            }
                            .take(2),
                        primaryCostPerDistanceCents = costPerDist,
                        primaryAvgEfficiency = avgEff,
                        distanceUnit = unit
                    )
                }
            }
        }
    }
        .catch { e ->
            Timber.e(e, "Dashboard failed to load")
            emit(DashboardUiState.Error(R.string.error_unknown))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState.Loading
        )

    fun requestReviewIfAppropriate(activity: Activity) {
        viewModelScope.launch {
            reviewHelper.requestReviewIfAppropriate(activity)
        }
    }
}
