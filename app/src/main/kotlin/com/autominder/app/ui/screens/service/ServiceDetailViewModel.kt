package com.autominder.app.ui.screens.service

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.autominder.app.R
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ServiceDetailUiState(
    val service: Service? = null,
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val errorArgs: List<Any> = emptyList(),
    val isDeleted: Boolean = false
)

sealed class ServiceDetailUiEvent {
    object DeleteClicked : ServiceDetailUiEvent()
}

@HiltViewModel
class ServiceDetailViewModel @Inject constructor(
    private val serviceRepository: IServiceRepository,
    private val vehicleRepository: IVehicleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val serviceId: Long = savedStateHandle.toRoute<NavRoutes.ServiceDetail>().serviceId

    private val _uiState = MutableStateFlow(ServiceDetailUiState())
    val uiState: StateFlow<ServiceDetailUiState> = _uiState.asStateFlow()

    init {
        loadService()
    }

    fun retry() = loadService()

    private fun loadService() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorRes = null, errorArgs = emptyList())
            try {
                serviceRepository.getServiceById(serviceId).collect { service ->
                    if (service != null) {
                        val vehicle = vehicleRepository.getVehicleById(service.vehicleId).firstOrNull()
                        _uiState.value = _uiState.value.copy(
                            service = service,
                            vehicle = vehicle,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorRes = R.string.error_service_not_found
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load service")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorRes = R.string.error_load_service_failed
                )
            }
        }
    }

    fun onEvent(event: ServiceDetailUiEvent) {
        when (event) {
            is ServiceDetailUiEvent.DeleteClicked -> deleteService()
        }
    }

    private fun deleteService() {
        val service = _uiState.value.service ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorRes = null, errorArgs = emptyList())
            try {
                serviceRepository.deleteService(service)
                _uiState.value = _uiState.value.copy(isLoading = false, isDeleted = true)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete service")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorRes = R.string.error_delete_service_failed
                )
            }
        }
    }
}
