package com.autominder.app.ui.screens.vehicle

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autominder.app.R
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
import timber.log.Timber
import javax.inject.Inject

sealed class VehicleListUiState {
    object Loading : VehicleListUiState()
    object Empty : VehicleListUiState()
    data class Error(@StringRes val messageRes: Int) : VehicleListUiState()
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
