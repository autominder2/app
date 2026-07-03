package com.autominder.app.ui.screens.service

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServiceDetailUiState(
    val service: Service? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false
)

sealed class ServiceDetailUiEvent {
    object DeleteClicked : ServiceDetailUiEvent()
}

@HiltViewModel
class ServiceDetailViewModel @Inject constructor(
    private val serviceRepository: IServiceRepository,
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
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                serviceRepository.getServiceById(serviceId).collect { service ->
                    if (service != null) {
                        _uiState.value = _uiState.value.copy(
                            service = service,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Service not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load service"
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
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                serviceRepository.deleteService(service)
                _uiState.value = _uiState.value.copy(isLoading = false, isDeleted = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to delete service"
                )
            }
        }
    }
}
