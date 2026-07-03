package com.autominder.app.ui.screens.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IVehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed class VehicleListUiState {
    object Loading : VehicleListUiState()
    object Empty : VehicleListUiState()
    data class Error(val message: String) : VehicleListUiState()
    data class Success(val vehicles: List<Vehicle>) : VehicleListUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VehicleListViewModel @Inject constructor(
    vehicleRepository: IVehicleRepository
) : ViewModel() {

    // A `catch` on a Room flow completes it permanently — retry() re-subscribes
    // so the Error state's retry button actually works.
    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<VehicleListUiState> = retryTrigger
        .flatMapLatest {
            vehicleRepository.getAllVehicles()
                .map { vehicles ->
                    if (vehicles.isEmpty()) VehicleListUiState.Empty
                    else VehicleListUiState.Success(vehicles)
                }
                .onStart<VehicleListUiState> { emit(VehicleListUiState.Loading) }
                .catch { e -> emit(VehicleListUiState.Error(e.message ?: "Failed to load vehicles")) }
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
