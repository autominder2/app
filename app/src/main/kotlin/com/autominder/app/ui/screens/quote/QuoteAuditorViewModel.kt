package com.autominder.app.ui.screens.quote

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autominder.app.R
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.QuoteAuditResult
import com.autominder.app.domain.model.QuoteItem
import com.autominder.app.domain.model.QuoteVerdictStatus
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.usecase.AuditQuoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

data class QuoteAuditorUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicle: Vehicle? = null,
    val items: List<QuoteItem> = emptyList(),
    val auditResult: QuoteAuditResult? = null,
    val distanceUnit: String = "km",
    val isLoading: Boolean = false,
    val isAuditing: Boolean = false,
    val savedSuccessMessage: String? = null,
    @StringRes val errorRes: Int? = null
)

sealed class QuoteAuditorUiEvent {
    data class SelectVehicle(val vehicleId: Long) : QuoteAuditorUiEvent()
    data class AddItem(val serviceType: ServiceType, val defaultPriceCents: Int = 0) : QuoteAuditorUiEvent()
    data class RemoveItem(val itemId: String) : QuoteAuditorUiEvent()
    data class UpdateItemPrice(val itemId: String, val priceCents: Int) : QuoteAuditorUiEvent()
    data object AnalyzeQuote : QuoteAuditorUiEvent()
    data object ResetQuote : QuoteAuditorUiEvent()
    data object SaveApprovedServices : QuoteAuditorUiEvent()
    data object ClearSavedMessage : QuoteAuditorUiEvent()
}

@HiltViewModel
class QuoteAuditorViewModel @Inject constructor(
    private val vehicleRepository: IVehicleRepository,
    private val serviceRepository: IServiceRepository,
    private val auditQuoteUseCase: AuditQuoteUseCase,
    private val userPreferences: UserPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialVehicleId: Long? = savedStateHandle.get<Long>("vehicleId")?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(QuoteAuditorUiState(isLoading = true))
    val uiState: StateFlow<QuoteAuditorUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun onEvent(event: QuoteAuditorUiEvent) {
        when (event) {
            is QuoteAuditorUiEvent.SelectVehicle -> {
                val vehicle = _uiState.value.vehicles.find { it.id == event.vehicleId }
                _uiState.value = _uiState.value.copy(
                    selectedVehicle = vehicle,
                    auditResult = null
                )
                if (_uiState.value.items.isNotEmpty()) {
                    runAudit()
                }
            }
            is QuoteAuditorUiEvent.AddItem -> {
                val newItem = QuoteItem(
                    id = UUID.randomUUID().toString(),
                    serviceType = event.serviceType,
                    priceCents = event.defaultPriceCents
                )
                val updatedItems = _uiState.value.items + newItem
                _uiState.value = _uiState.value.copy(items = updatedItems)
                runAudit()
            }
            is QuoteAuditorUiEvent.RemoveItem -> {
                val updatedItems = _uiState.value.items.filterNot { it.id == event.itemId }
                _uiState.value = _uiState.value.copy(items = updatedItems)
                if (updatedItems.isEmpty()) {
                    _uiState.value = _uiState.value.copy(auditResult = null)
                } else {
                    runAudit()
                }
            }
            is QuoteAuditorUiEvent.UpdateItemPrice -> {
                val updatedItems = _uiState.value.items.map {
                    if (it.id == event.itemId) it.copy(priceCents = event.priceCents) else it
                }
                _uiState.value = _uiState.value.copy(items = updatedItems)
                runAudit()
            }
            is QuoteAuditorUiEvent.AnalyzeQuote -> runAudit()
            is QuoteAuditorUiEvent.ResetQuote -> {
                _uiState.value = _uiState.value.copy(
                    items = emptyList(),
                    auditResult = null
                )
            }
            is QuoteAuditorUiEvent.SaveApprovedServices -> saveApprovedServices()
            is QuoteAuditorUiEvent.ClearSavedMessage -> {
                _uiState.value = _uiState.value.copy(savedSuccessMessage = null)
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val vehicles = vehicleRepository.getAllVehicles().firstOrNull() ?: emptyList()
                val unit = userPreferences.distanceUnit.firstOrNull() ?: "km"
                val selected = if (initialVehicleId != null) {
                    vehicles.find { it.id == initialVehicleId } ?: vehicles.firstOrNull()
                } else {
                    vehicles.firstOrNull()
                }

                _uiState.value = QuoteAuditorUiState(
                    vehicles = vehicles,
                    selectedVehicle = selected,
                    distanceUnit = unit,
                    isLoading = false
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to load vehicles for Quote Auditor")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorRes = R.string.error_load_records_failed
                )
            }
        }
    }

    private fun runAudit() {
        val vehicle = _uiState.value.selectedVehicle ?: return
        val items = _uiState.value.items
        if (items.isEmpty()) {
            _uiState.value = _uiState.value.copy(auditResult = null)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAuditing = true)
            try {
                val result = auditQuoteUseCase(vehicle.id, items)
                _uiState.value = _uiState.value.copy(
                    auditResult = result,
                    isAuditing = false
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to run quote audit")
                _uiState.value = _uiState.value.copy(isAuditing = false)
            }
        }
    }

    private fun saveApprovedServices() {
        val vehicle = _uiState.value.selectedVehicle ?: return
        val result = _uiState.value.auditResult ?: return

        viewModelScope.launch {
            try {
                val approvedVerdicts = result.lineVerdicts.filter {
                    it.status == QuoteVerdictStatus.LEGITIMATE_DUE || it.status == QuoteVerdictStatus.VERIFY_FIRST
                }

                val now = System.currentTimeMillis()
                for (verdict in approvedVerdicts) {
                    val service = Service(
                        id = 0L,
                        vehicleId = vehicle.id,
                        serviceType = verdict.item.serviceType,
                        customLabel = verdict.item.customLabel,
                        odometerAtService = vehicle.currentOdometer,
                        serviceDate = now,
                        costCents = verdict.item.priceCents.takeIf { it > 0 },
                        shopName = "Audited Service",
                        notes = "Logged via AutoMinder Quote Auditor",
                        receiptPhotoUri = null,
                        createdAt = now
                    )
                    serviceRepository.insertService(service)
                }

                _uiState.value = _uiState.value.copy(
                    savedSuccessMessage = "${approvedVerdicts.size} approved services logged to vehicle history"
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to save approved services")
            }
        }
    }
}
