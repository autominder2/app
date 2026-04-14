package com.autominder.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autominder.app.domain.usecase.GetDashboardDataUseCase
import com.autominder.app.domain.usecase.VehicleWithStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

/**
 * State of the dashboard — follows unidirectional data flow (UDF).
 * All 4 states are explicitly defined.
 */
sealed class DashboardUiState {
    object Loading : DashboardUiState()
    object Empty : DashboardUiState()
    data class Error(val message: String? = null) : DashboardUiState()
    data class Success(
        val vehicles: List<VehicleWithStatus>,
        val alertsCount: Int
    ) : DashboardUiState()
}

/**
 * Orchestrates dashboard logic, fetching vehicles and statuses via usecase.
 * Uses stateIn(WhileSubscribed) to keep data hot for 5s — instant on back-nav.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboardDataUseCase: GetDashboardDataUseCase
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
                    alertsCount = data.alertsCount
                )
            }
        }
    }
        .catch { e -> emit(DashboardUiState.Error(e.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState.Loading
        )
}
