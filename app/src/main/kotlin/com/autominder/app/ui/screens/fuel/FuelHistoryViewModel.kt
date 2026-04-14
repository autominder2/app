package com.autominder.app.ui.screens.fuel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.autominder.app.domain.model.FuelEntry
import com.autominder.app.domain.repository.IFuelRepository
import com.autominder.app.domain.usecase.CalculateEfficiencyUseCase
import com.autominder.app.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FuelHistoryUiState(
    val vehicleId: Long,
    val entries: List<FuelEntryWithEfficiency> = emptyList(),
    val averageEfficiency: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class FuelEntryWithEfficiency(
    val entry: FuelEntry,
    val efficiency: Double? = null // null if no previous entry
)

@HiltViewModel
class FuelHistoryViewModel @Inject constructor(
    private val fuelRepository: IFuelRepository,
    private val calculateEfficiency: CalculateEfficiencyUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.toRoute<NavRoutes.FuelHistory>().vehicleId
    private val refreshTrigger = MutableStateFlow(0)

    fun retry() {
        refreshTrigger.value++
    }

    val uiState: StateFlow<FuelHistoryUiState> = refreshTrigger.flatMapLatest {
        fuelRepository.getFuelEntriesForVehicle(vehicleId)
            .map { entries ->
                if (entries.isEmpty()) {
                    FuelHistoryUiState(vehicleId = vehicleId)
                } else {
                    val sorted = entries.sortedByDescending { it.odometer }
                    val withEfficiency = sorted.mapIndexed { index, fuelEntry ->
                        val previous = if (index < sorted.size - 1) sorted[index + 1] else null
                        val eff = calculateEfficiency.calculate(fuelEntry, previous)
                        FuelEntryWithEfficiency(fuelEntry, if (eff > 0) eff else null)
                    }
                    FuelHistoryUiState(
                        vehicleId = vehicleId,
                        entries = withEfficiency,
                        averageEfficiency = calculateEfficiency.calculateAverage(entries)
                    )
                }
            }
            .catch { e -> emit(FuelHistoryUiState(vehicleId = vehicleId, error = e.message)) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FuelHistoryUiState(vehicleId = vehicleId, isLoading = true)
    )

    fun deleteEntry(entry: FuelEntry) {
        viewModelScope.launch {
            fuelRepository.deleteFuelEntry(entry)
        }
    }
}
