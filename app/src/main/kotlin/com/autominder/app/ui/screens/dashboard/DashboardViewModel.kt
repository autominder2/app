package com.autominder.app.ui.screens.dashboard

import android.app.Activity
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autominder.app.R
import com.autominder.app.core.util.AppInfo
import com.autominder.app.core.util.ReviewHelper
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.usecase.GetDashboardDataUseCase
import com.autominder.app.domain.usecase.ReminderWithStatus
import com.autominder.app.domain.usecase.VehicleWithStatus
import com.autominder.app.domain.model.ServiceStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        val attentionReminders: List<ReminderWithStatus> = emptyList()
    ) : DashboardUiState()
}

/**
 * Present only when the background reminder engine has been silent long
 * enough that the user should be told.
 *
 * [lastCheckedAt] is null when no check has *ever* completed, which reads
 * differently to the user than "we last managed this on Tuesday" and gets its
 * own copy.
 */
data class RemindersDelayedState(val lastCheckedAt: Long?)

/**
 * Six missed passes of the 6-hour schedule.
 *
 * One or two skipped runs is ordinary Doze behaviour and warning about it
 * would train the user to dismiss the banner. A day and a half of silence is
 * a pattern, not a deferral.
 */
private const val STALE_AFTER_MS = 36L * 60 * 60 * 1000

/**
 * Decides whether the reminder engine looks silenced.
 *
 * A pure function taking `now` as a parameter, deliberately: the same logic
 * evaluated inside a composable via `derivedStateOf` would be worse than
 * useless. `derivedStateOf` recomputes only when a snapshot `State` it read
 * changes, and the system clock is not snapshot state — so the result would
 * be pinned to whenever composition happened to run first, and would vary
 * with unrelated recompositions.
 *
 * Falling back to install time when nothing has ever been recorded closes
 * both holes at once: a fresh install stays quiet for its first 36 hours, and
 * an install whose worker has *never* run still surfaces after them.
 */
internal fun evaluateReminderStaleness(
    lastSuccessfulCheckAt: Long?,
    firstInstallTimeMillis: Long,
    nowMillis: Long
): RemindersDelayedState? {
    val lastKnownLiveness = lastSuccessfulCheckAt ?: firstInstallTimeMillis
    // A backwards clock jump yields a negative age and is treated as healthy;
    // SystemEventReceiver re-runs the check on TIME_SET, so it self-corrects.
    if (nowMillis - lastKnownLiveness <= STALE_AFTER_MS) return null
    return RemindersDelayedState(lastCheckedAt = lastSuccessfulCheckAt)
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val reviewHelper: ReviewHelper,
    userPreferences: UserPreferences,
    appInfo: AppInfo
) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(0)

    /**
     * Null while the engine looks healthy.
     *
     * Kept separate from [uiState] rather than folded into
     * [DashboardUiState.Success]: engine liveness is orthogonal to whether
     * vehicle data loaded, and merging them would make a data error hide a
     * reliability warning.
     *
     * `WhileSubscribed(5_000)` gives this its refresh cadence — DataStore
     * re-emits on every new subscription, so the check is re-evaluated
     * against a fresh clock each time the dashboard comes back into view.
     * That is the right moment: a banner that materialises while the user is
     * looking at a static screen would be startling and no more truthful.
     */
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
                // Preference read failures must never take down the dashboard.
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
        getDashboardDataUseCase()
            .map { data ->
            if (data.vehiclesWithStatus.isEmpty()) {
                DashboardUiState.Empty
            } else {
                DashboardUiState.Success(
                    vehicles = data.vehiclesWithStatus,
                    alertsCount = data.alertsCount,
                    attentionReminders = data.upcomingReminders
                        .filter {
                            it.status == ServiceStatus.OVERDUE ||
                                it.status == ServiceStatus.DUE_SOON
                        }
                        .take(2)
                )
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

    /**
     * Attempts to trigger the Google Play In-App Review flow if the user
     * has reached the required milestones (3+ service logs).
     */
    fun requestReviewIfAppropriate(activity: Activity) {
        viewModelScope.launch {
            reviewHelper.requestReviewIfAppropriate(activity)
        }
    }
}
