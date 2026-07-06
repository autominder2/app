package com.autominder.app.ui.screens.dashboard

import android.app.Activity
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autominder.app.R
import com.autominder.app.core.util.ReviewHelper
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

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val reviewHelper: ReviewHelper
) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(0)

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
