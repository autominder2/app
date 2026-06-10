package com.autominder.app.ui.screens.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.repository.IVehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ServiceWithVehicle(
    val service: Service,
    val vehicleName: String
)

data class ServiceGroup(
    val monthYear: String,
    val services: List<ServiceWithVehicle>
)

sealed class ServiceHistoryUiState {
    object Loading : ServiceHistoryUiState()
    object Empty : ServiceHistoryUiState()
    data class Error(val message: String) : ServiceHistoryUiState()
    data class Success(val groups: List<ServiceGroup>) : ServiceHistoryUiState()
}

@HiltViewModel
class ServiceHistoryViewModel @Inject constructor(
    private val serviceRepository: IServiceRepository,
    vehicleRepository: IVehicleRepository
) : ViewModel() {

    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val refreshTrigger = MutableStateFlow(0)

    fun retry() {
        refreshTrigger.value++
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ServiceHistoryUiState> = refreshTrigger.flatMapLatest {
        combine(
            serviceRepository.getAllServices(),
            vehicleRepository.getAllVehiclesIncludingArchived()
        ) { services, vehicles ->
        val vehicleNameMap = vehicles.associate { it.id to "${it.make} ${it.model}" }
        services.map { service ->
            ServiceWithVehicle(
                service = service,
                vehicleName = vehicleNameMap[service.vehicleId] ?: "Unknown Vehicle"
            )
        }
    }
        .map { servicesWithVehicle ->
            if (servicesWithVehicle.isEmpty()) {
                ServiceHistoryUiState.Empty
            } else {
                val grouped = servicesWithVehicle
                    .sortedByDescending { it.service.serviceDate }
                    .groupBy { monthYearFormat.format(Date(it.service.serviceDate)) }
                    .map { (monthYear, services) ->
                        ServiceGroup(monthYear = monthYear, services = services)
                    }
                ServiceHistoryUiState.Success(groups = grouped)
            }
        }
        .catch { e -> emit(ServiceHistoryUiState.Error(e.message ?: "Failed to load service history")) }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ServiceHistoryUiState.Loading
        )

    fun deleteService(service: Service) {
        viewModelScope.launch {
            serviceRepository.deleteService(service)
        }
    }

    fun undoDelete(service: Service) {
        viewModelScope.launch {
            // DAO insert uses REPLACE, so re-inserting with the original id restores it
            serviceRepository.insertService(service)
        }
    }
}
