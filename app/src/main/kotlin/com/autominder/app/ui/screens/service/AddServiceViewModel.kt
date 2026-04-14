package com.autominder.app.ui.screens.service

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.repository.IReminderRepository
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddServiceUiState(
    val serviceType: ServiceType = ServiceType.OIL_CHANGE,
    val customLabel: String = "",
    val odometer: String = "",
    val serviceDate: Long? = null,
    val cost: String = "",
    val shopName: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

sealed class AddServiceUiEvent {
    data class ServiceTypeChanged(val type: ServiceType) : AddServiceUiEvent()
    data class CustomLabelChanged(val label: String) : AddServiceUiEvent()
    data class OdometerChanged(val odometer: String) : AddServiceUiEvent()
    data class ServiceDateChanged(val date: Long?) : AddServiceUiEvent()
    data class CostChanged(val cost: String) : AddServiceUiEvent()
    data class ShopNameChanged(val shopName: String) : AddServiceUiEvent()
    data class NotesChanged(val notes: String) : AddServiceUiEvent()
    object SaveClicked : AddServiceUiEvent()
}

@HiltViewModel
class AddServiceViewModel @Inject constructor(
    private val serviceRepository: IServiceRepository,
    private val vehicleRepository: IVehicleRepository,
    private val reminderRepository: IReminderRepository,
    private val userPreferences: UserPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.toRoute<NavRoutes.AddService>().vehicleId

    private val _uiState = MutableStateFlow(AddServiceUiState())
    val uiState: StateFlow<AddServiceUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddServiceUiEvent) {
        when (event) {
            is AddServiceUiEvent.ServiceTypeChanged -> _uiState.value = _uiState.value.copy(serviceType = event.type)
            is AddServiceUiEvent.CustomLabelChanged -> _uiState.value = _uiState.value.copy(customLabel = event.label)
            is AddServiceUiEvent.OdometerChanged -> _uiState.value = _uiState.value.copy(odometer = event.odometer)
            is AddServiceUiEvent.ServiceDateChanged -> _uiState.value = _uiState.value.copy(serviceDate = event.date)
            is AddServiceUiEvent.CostChanged -> _uiState.value = _uiState.value.copy(cost = event.cost)
            is AddServiceUiEvent.ShopNameChanged -> _uiState.value = _uiState.value.copy(shopName = event.shopName)
            is AddServiceUiEvent.NotesChanged -> _uiState.value = _uiState.value.copy(notes = event.notes)
            is AddServiceUiEvent.SaveClicked -> saveService()
        }
    }

    private fun saveService() {
        val state = _uiState.value

        val odometerInt = state.odometer.toIntOrNull()
        if (odometerInt == null || odometerInt < 0) {
            _uiState.value = state.copy(error = "Please enter a valid odometer reading")
            return
        }

        if (state.serviceDate == null) {
            _uiState.value = state.copy(error = "Please select a service date")
            return
        }

        if (state.serviceType == ServiceType.CUSTOM && state.customLabel.isBlank()) {
            _uiState.value = state.copy(error = "Please specify a name for the custom service")
            return
        }

        // Parse cost from dollars string to cents
        val costCents = if (state.cost.isBlank()) {
            null
        } else {
            val costDouble = state.cost.toDoubleOrNull()
            if (costDouble == null || costDouble < 0) {
                _uiState.value = state.copy(error = "Please enter a valid cost")
                return
            }
            (costDouble * 100).toInt()
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val unit = userPreferences.distanceUnit.first()
                val odometerKm = DistanceUtil.displayToKm(odometerInt, unit)
                val vehicle = vehicleRepository.getVehicleById(vehicleId).firstOrNull()
                
                if (vehicle != null && odometerKm < vehicle.currentOdometer) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Odometer reading cannot be lower than the current vehicle odometer (${DistanceUtil.kmToDisplay(vehicle.currentOdometer, unit)})"
                    )
                    return@launch
                }

                val now = System.currentTimeMillis()
                val service = Service(
                    id = 0,
                    vehicleId = vehicleId,
                    serviceType = state.serviceType,
                    customLabel = if (state.serviceType == ServiceType.CUSTOM) state.customLabel else null,
                    odometerAtService = odometerKm,
                    serviceDate = state.serviceDate,
                    costCents = costCents,
                    shopName = state.shopName.ifBlank { null },
                    notes = state.notes,
                    createdAt = now
                )
                serviceRepository.insertService(service)
                vehicleRepository.updateOdometer(vehicleId, odometerKm)

                // Auto-reset matching reminder using interval fields
                val matchingReminder = reminderRepository.findActiveReminderByType(
                    vehicleId = vehicleId,
                    serviceType = state.serviceType
                )
                if (matchingReminder != null) {
                    val resetReminder = matchingReminder.copy(
                        nextDueOdometer = matchingReminder.intervalKm?.let { odometerKm + it },
                        nextDueDate = matchingReminder.intervalDays?.let {
                            state.serviceDate + (it.toLong() * 86_400_000L)
                        },
                        isCompleted = false,
                        completedAt = null,
                        snoozeUntil = null,
                        lastNotifiedAt = null,
                        updatedAt = System.currentTimeMillis()
                    )
                    reminderRepository.updateReminder(resetReminder)
                }

                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save service"
                )
            }
        }
    }
}
