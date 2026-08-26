package com.autominder.app.ui.screens.service

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autominder.app.R
import com.autominder.app.core.di.DefaultDispatcher
import com.autominder.app.data.export.ExportServiceHistoryUseCase
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.repository.IVehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class ServiceSortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST,
    HIGHEST_COST
}

data class ServiceWithVehicle(
    val service: Service,
    val vehicleName: String?
)

data class ServiceGroup(
    val monthYear: String,
    val monthlySpendCents: Long,
    val services: List<ServiceWithVehicle>
)

data class ServiceHistoryUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicleId: Long? = null,
    val selectedCategory: ServiceType? = null,
    val sortOrder: ServiceSortOrder = ServiceSortOrder.NEWEST_FIRST,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val distanceUnit: String = "km",
    val totalSpendCents: Long = 0L,
    val serviceCount: Int = 0,
    val averageCostCents: Long = 0L,
    val topExpenseCategory: ServiceType? = null,
    val topExpenseSpendCents: Long = 0L,
    val costPerDistanceCents: Double? = null,
    val groups: List<ServiceGroup> = emptyList(),
    val isFilterEmpty: Boolean = false,
    val isLoading: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val exportUri: Uri? = null,
    val isExporting: Boolean = false
)

private data class ServiceFilterCriteria(
    val vehicleId: Long?,
    val category: ServiceType?,
    val sortOrder: ServiceSortOrder,
    val query: String,
    val isSearch: Boolean
)

sealed class ServiceHistoryUiEvent {
    data object Retry : ServiceHistoryUiEvent()
    data class SelectVehicle(val vehicleId: Long?) : ServiceHistoryUiEvent()
    data class SelectCategory(val category: ServiceType?) : ServiceHistoryUiEvent()
    data class SetSortOrder(val order: ServiceSortOrder) : ServiceHistoryUiEvent()
    data class UpdateSearchQuery(val query: String) : ServiceHistoryUiEvent()
    data class ToggleSearch(val active: Boolean) : ServiceHistoryUiEvent()
    data object ClearFilters : ServiceHistoryUiEvent()
    data class DeleteService(val service: Service) : ServiceHistoryUiEvent()
    data class UndoDelete(val service: Service) : ServiceHistoryUiEvent()
    data class ExportHistory(val vehicleId: Long? = null) : ServiceHistoryUiEvent()
    data class ExportPassport(val vehicleId: Long? = null) : ServiceHistoryUiEvent()
    data object ClearExportUri : ServiceHistoryUiEvent()
}

@HiltViewModel
class ServiceHistoryViewModel @Inject constructor(
    private val serviceRepository: IServiceRepository,
    private val vehicleRepository: IVehicleRepository,
    private val userPreferences: UserPreferences,
    private val exportServiceHistory: ExportServiceHistoryUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    private val _selectedVehicleId = MutableStateFlow<Long?>(null)
    private val _selectedCategory = MutableStateFlow<ServiceType?>(null)
    private val _sortOrder = MutableStateFlow(ServiceSortOrder.NEWEST_FIRST)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)
    private val _exportUri = MutableStateFlow<Uri?>(null)
    private val _isExporting = MutableStateFlow(false)
    private val _refreshTrigger = MutableStateFlow(0)

    private val _uiState = MutableStateFlow(ServiceHistoryUiState(isLoading = true))
    val uiState: StateFlow<ServiceHistoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(event: ServiceHistoryUiEvent) {
        when (event) {
            is ServiceHistoryUiEvent.Retry -> {
                _refreshTrigger.value++
                loadData()
            }
            is ServiceHistoryUiEvent.SelectVehicle -> _selectedVehicleId.value = event.vehicleId
            is ServiceHistoryUiEvent.SelectCategory -> {
                _selectedCategory.value = if (_selectedCategory.value == event.category) null else event.category
            }
            is ServiceHistoryUiEvent.SetSortOrder -> _sortOrder.value = event.order
            is ServiceHistoryUiEvent.UpdateSearchQuery -> _searchQuery.value = event.query
            is ServiceHistoryUiEvent.ToggleSearch -> {
                _isSearchActive.value = event.active
                if (!event.active) _searchQuery.value = ""
            }
            is ServiceHistoryUiEvent.ClearFilters -> {
                _selectedVehicleId.value = null
                _selectedCategory.value = null
                _searchQuery.value = ""
                _isSearchActive.value = false
                _sortOrder.value = ServiceSortOrder.NEWEST_FIRST
            }
            is ServiceHistoryUiEvent.DeleteService -> deleteService(event.service)
            is ServiceHistoryUiEvent.UndoDelete -> undoDelete(event.service)
            is ServiceHistoryUiEvent.ExportHistory -> exportHistory(event.vehicleId)
            is ServiceHistoryUiEvent.ExportPassport -> exportPassport(event.vehicleId)
            is ServiceHistoryUiEvent.ClearExportUri -> _exportUri.value = null
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val filterFlow = combine(
                _selectedVehicleId,
                _selectedCategory,
                _sortOrder,
                _searchQuery,
                _isSearchActive
            ) { vehId, cat, sort, query, isSearch ->
                ServiceFilterCriteria(vehId, cat, sort, query, isSearch)
            }

            val exportFlow = combine(_exportUri, _isExporting) { uri, exporting ->
                Pair(uri, exporting)
            }

            combine(
                serviceRepository.getAllServices(),
                vehicleRepository.getAllVehiclesIncludingArchived(),
                userPreferences.distanceUnit,
                filterFlow,
                exportFlow
            ) { allServices: List<Service>, vehicles: List<Vehicle>, unit: String, filter: ServiceFilterCriteria, exportInfo: Pair<Uri?, Boolean> ->
                val (exportUri, isExporting) = exportInfo
                val vehicleNameMap = vehicles.associate { it.id to com.autominder.app.domain.util.VehicleDisplayNameFormatter.format(it.make, it.model, it.year) }

                // 1. Filter logic
                val filtered = allServices.filter { service ->
                    val matchesVehicle = filter.vehicleId == null || service.vehicleId == filter.vehicleId
                    val matchesCategory = filter.category == null || service.serviceType == filter.category
                    val matchesQuery = filter.query.isBlank() ||
                            service.serviceType.label.contains(filter.query, ignoreCase = true) ||
                            (service.customLabel?.contains(filter.query, ignoreCase = true) == true) ||
                            (service.shopName?.contains(filter.query, ignoreCase = true) == true) ||
                            service.notes.contains(filter.query, ignoreCase = true)

                    matchesVehicle && matchesCategory && matchesQuery
                }

                // 2. Metrics & Intelligence
                val totalSpend = filtered.mapNotNull { it.costCents }.sumOf { it.toLong() }
                val count = filtered.size
                val avgCost = if (count > 0 && totalSpend > 0) totalSpend / count else 0L

                // Top Expense Category
                val categorySpends = filtered
                    .groupBy { it.serviceType }
                    .mapValues { (_, svcs) -> svcs.mapNotNull { it.costCents }.sumOf { it.toLong() } }
                val topCategoryEntry = categorySpends.maxByOrNull { it.value }
                val topCategory = if (topCategoryEntry != null && topCategoryEntry.value > 0) topCategoryEntry.key else null
                val topCategorySpend = topCategoryEntry?.value ?: 0L

                // Cost per Distance Calculation
                val relevantVehicles = if (filter.vehicleId != null) {
                    vehicles.filter { it.id == filter.vehicleId }
                } else {
                    vehicles
                }
                val totalOdo = relevantVehicles.sumOf { it.currentOdometer }
                val costPerDist = if (totalOdo > 0 && totalSpend > 0) {
                    (totalSpend.toDouble() / 100.0) / totalOdo.toDouble()
                } else null

                // 3. Sorting & Grouping
                val sorted = when (filter.sortOrder) {
                    ServiceSortOrder.NEWEST_FIRST -> filtered.sortedByDescending { it.serviceDate }
                    ServiceSortOrder.OLDEST_FIRST -> filtered.sortedBy { it.serviceDate }
                    ServiceSortOrder.HIGHEST_COST -> filtered.sortedByDescending { it.costCents ?: 0 }
                }

                val withVehicle = sorted.map { service ->
                    ServiceWithVehicle(
                        service = service,
                        vehicleName = vehicleNameMap[service.vehicleId]
                    )
                }

                val grouped = if (filter.sortOrder == ServiceSortOrder.HIGHEST_COST) {
                    if (withVehicle.isNotEmpty()) {
                        listOf(
                            ServiceGroup(
                                monthYear = "All Time (Highest Cost)",
                                monthlySpendCents = totalSpend,
                                services = withVehicle
                            )
                        )
                    } else emptyList()
                } else {
                    withVehicle
                        .groupBy { monthYearFormat.format(Date(it.service.serviceDate)) }
                        .map { (monthYear, services) ->
                            val groupSpend = services.mapNotNull { it.service.costCents }.sumOf { it.toLong() }
                            ServiceGroup(
                                monthYear = monthYear,
                                monthlySpendCents = groupSpend,
                                services = services
                            )
                        }
                }

                val isFilterEmpty = allServices.isNotEmpty() && filtered.isEmpty()

                ServiceHistoryUiState(
                    vehicles = vehicles,
                    selectedVehicleId = filter.vehicleId,
                    selectedCategory = filter.category,
                    sortOrder = filter.sortOrder,
                    searchQuery = filter.query,
                    isSearchActive = filter.isSearch,
                    distanceUnit = unit,
                    totalSpendCents = totalSpend,
                    serviceCount = count,
                    averageCostCents = avgCost,
                    topExpenseCategory = topCategory,
                    topExpenseSpendCents = topCategorySpend,
                    costPerDistanceCents = costPerDist,
                    groups = grouped,
                    isFilterEmpty = isFilterEmpty,
                    isLoading = false,
                    exportUri = exportUri,
                    isExporting = isExporting
                )
            }
                .flowOn(defaultDispatcher)
                .catch { e ->
                    Timber.e(e, "Failed to load service history")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorRes = R.string.error_load_records_failed
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun exportHistory(vehicleId: Long?) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val uri = exportServiceHistory(vehicleId)
                _exportUri.value = uri
            } catch (e: Exception) {
                Timber.e(e, "Failed to export service history")
            } finally {
                _isExporting.value = false
            }
        }
    }

    private fun exportPassport(vehicleId: Long?) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val uri = exportServiceHistory.exportPassport(vehicleId)
                _exportUri.value = uri
            } catch (e: Exception) {
                Timber.e(e, "Failed to export maintenance passport")
            } finally {
                _isExporting.value = false
            }
        }
    }

    private fun deleteService(service: Service) {
        viewModelScope.launch {
            try {
                serviceRepository.deleteService(service)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete service")
            }
        }
    }

    private fun undoDelete(service: Service) {
        viewModelScope.launch {
            try {
                serviceRepository.insertService(service)
            } catch (e: Exception) {
                Timber.e(e, "Failed to restore service")
            }
        }
    }
}
