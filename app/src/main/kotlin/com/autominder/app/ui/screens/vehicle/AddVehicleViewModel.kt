package com.autominder.app.ui.screens.vehicle

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autominder.app.R
import com.autominder.app.core.di.DefaultDispatcher
import com.autominder.app.core.util.AnalyticsEvents
import com.autominder.app.core.util.AnalyticsHelper
import com.autominder.app.core.util.AnalyticsParams
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.DrivingAmount
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.usecase.CreateDefaultRemindersUseCase
import com.autominder.app.domain.usecase.PlannedReminder
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.domain.validation.ValidationErrorCode
import com.autominder.app.domain.validation.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

enum class AddVehicleStep {
    DISCOVERY,
    IDENTITY,
    SCHEDULE
}

@Immutable
data class VehicleSuggestion(
    val make: String,
    val model: String,
    val badge: String? = null
)

@Immutable
data class AddVehicleUiState(
    val currentStep: AddVehicleStep = AddVehicleStep.DISCOVERY,
    val searchQuery: String = "",
    val brand: String = "",
    val model: String = "",
    val year: String = "",
    val role: String = "Daily Driver",
    val currentOdometer: String = "",
    val plateNumber: String = "",
    val vin: String = "",
    val photoUri: String? = null,
    val isCustomEntry: Boolean = false,
    val suggestions: List<VehicleSuggestion> = emptyList(),
    val previewReminders: List<PlannedReminder> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val errorArgs: List<Any> = emptyList()
)

sealed class AddVehicleUiEvent {
    data class StepChanged(val step: AddVehicleStep) : AddVehicleUiEvent()
    data class SearchQueryChanged(val query: String) : AddVehicleUiEvent()
    data class SuggestionSelected(val suggestion: VehicleSuggestion) : AddVehicleUiEvent()
    data class CustomEntryToggled(val isCustom: Boolean) : AddVehicleUiEvent()
    data class BrandChanged(val brand: String) : AddVehicleUiEvent()
    data class ModelChanged(val model: String) : AddVehicleUiEvent()
    data class YearChanged(val year: String) : AddVehicleUiEvent()
    data class RoleChanged(val role: String) : AddVehicleUiEvent()
    data class OdometerChanged(val odometer: String) : AddVehicleUiEvent()
    data class OdometerAdjusted(val delta: Int) : AddVehicleUiEvent()
    data class PlateNumberChanged(val plateNumber: String) : AddVehicleUiEvent()
    data class VinChanged(val vin: String) : AddVehicleUiEvent()
    data class PhotoUriChanged(val uri: String?) : AddVehicleUiEvent()
    data object NextStepClicked : AddVehicleUiEvent()
    data object PreviousStepClicked : AddVehicleUiEvent()
    data object SaveClicked : AddVehicleUiEvent()
}

@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    private val vehicleRepository: IVehicleRepository,
    private val createDefaultReminders: CreateDefaultRemindersUseCase,
    private val userPreferences: UserPreferences,
    private val analyticsHelper: AnalyticsHelper,
    private val savedStateHandle: SavedStateHandle,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    companion object {
        private const val KEY_BRAND = "brand"
        private const val KEY_MODEL = "model"
        private const val KEY_YEAR = "year"
        private const val KEY_ODOMETER = "odometer"

        val POPULAR_SUGGESTIONS = listOf(
            VehicleSuggestion("Toyota", "RAV4", "SUV"),
            VehicleSuggestion("Honda", "Civic", "Sedan"),
            VehicleSuggestion("Ford", "F-150", "Truck"),
            VehicleSuggestion("Tesla", "Model Y", "EV"),
            VehicleSuggestion("Toyota", "Camry", "Sedan"),
            VehicleSuggestion("Honda", "CR-V", "SUV"),
            VehicleSuggestion("Tesla", "Model 3", "EV"),
            VehicleSuggestion("BMW", "3 Series", "Sedan"),
            VehicleSuggestion("Hyundai", "Tucson", "SUV"),
            VehicleSuggestion("Chevrolet", "Silverado", "Truck"),
            VehicleSuggestion("Mazda", "CX-5", "SUV"),
            VehicleSuggestion("Toyota", "Corolla", "Sedan"),
            VehicleSuggestion("Subaru", "Outback", "Wagon"),
            VehicleSuggestion("Jeep", "Wrangler", "4x4"),
            VehicleSuggestion("Mercedes-Benz", "C-Class", "Sedan"),
            VehicleSuggestion("Volkswagen", "Golf", "Hatchback"),
            VehicleSuggestion("Porsche", "911", "Coupe")
        )

        val ALL_CATALOG_VEHICLES = listOf(
            VehicleSuggestion("Toyota", "RAV4", "SUV"),
            VehicleSuggestion("Toyota", "Camry", "Sedan"),
            VehicleSuggestion("Toyota", "Corolla", "Sedan"),
            VehicleSuggestion("Toyota", "Highlander", "SUV"),
            VehicleSuggestion("Toyota", "Tacoma", "Truck"),
            VehicleSuggestion("Toyota", "Tundra", "Truck"),
            VehicleSuggestion("Toyota", "Prius", "Hybrid"),
            VehicleSuggestion("Toyota", "4Runner", "SUV"),
            VehicleSuggestion("Honda", "Civic", "Sedan"),
            VehicleSuggestion("Honda", "Accord", "Sedan"),
            VehicleSuggestion("Honda", "CR-V", "SUV"),
            VehicleSuggestion("Honda", "Pilot", "SUV"),
            VehicleSuggestion("Honda", "HR-V", "SUV"),
            VehicleSuggestion("Ford", "F-150", "Truck"),
            VehicleSuggestion("Ford", "Mustang", "Coupe"),
            VehicleSuggestion("Ford", "Explorer", "SUV"),
            VehicleSuggestion("Ford", "Escape", "SUV"),
            VehicleSuggestion("Ford", "Bronco", "4x4"),
            VehicleSuggestion("Tesla", "Model 3", "EV"),
            VehicleSuggestion("Tesla", "Model Y", "EV"),
            VehicleSuggestion("Tesla", "Model S", "EV"),
            VehicleSuggestion("Tesla", "Model X", "EV"),
            VehicleSuggestion("BMW", "3 Series", "Sedan"),
            VehicleSuggestion("BMW", "5 Series", "Sedan"),
            VehicleSuggestion("BMW", "X3", "SUV"),
            VehicleSuggestion("BMW", "X5", "SUV"),
            VehicleSuggestion("BMW", "M3", "Performance"),
            VehicleSuggestion("Mercedes-Benz", "C-Class", "Sedan"),
            VehicleSuggestion("Mercedes-Benz", "E-Class", "Sedan"),
            VehicleSuggestion("Mercedes-Benz", "GLC", "SUV"),
            VehicleSuggestion("Mercedes-Benz", "GLE", "SUV"),
            VehicleSuggestion("Hyundai", "Tucson", "SUV"),
            VehicleSuggestion("Hyundai", "Elantra", "Sedan"),
            VehicleSuggestion("Hyundai", "Santa Fe", "SUV"),
            VehicleSuggestion("Hyundai", "Ioniq 5", "EV"),
            VehicleSuggestion("Nissan", "Altima", "Sedan"),
            VehicleSuggestion("Nissan", "Rogue", "SUV"),
            VehicleSuggestion("Nissan", "Sentra", "Sedan"),
            VehicleSuggestion("Chevrolet", "Silverado", "Truck"),
            VehicleSuggestion("Chevrolet", "Equinox", "SUV"),
            VehicleSuggestion("Chevrolet", "Malibu", "Sedan"),
            VehicleSuggestion("Chevrolet", "Corvette", "Coupe"),
            VehicleSuggestion("Kia", "Sportage", "SUV"),
            VehicleSuggestion("Kia", "Telluride", "SUV"),
            VehicleSuggestion("Kia", "EV6", "EV"),
            VehicleSuggestion("Volkswagen", "Golf", "Hatchback"),
            VehicleSuggestion("Volkswagen", "Jetta", "Sedan"),
            VehicleSuggestion("Volkswagen", "Tiguan", "SUV"),
            VehicleSuggestion("Mazda", "CX-5", "SUV"),
            VehicleSuggestion("Mazda", "Mazda3", "Sedan"),
            VehicleSuggestion("Mazda", "CX-30", "SUV"),
            VehicleSuggestion("Mazda", "MX-5 Miata", "Roadster"),
            VehicleSuggestion("Subaru", "Outback", "Wagon"),
            VehicleSuggestion("Subaru", "Forester", "SUV"),
            VehicleSuggestion("Subaru", "Crosstrek", "Crossover"),
            VehicleSuggestion("Subaru", "WRX", "Sedan"),
            VehicleSuggestion("Audi", "A4", "Sedan"),
            VehicleSuggestion("Audi", "Q5", "SUV"),
            VehicleSuggestion("Audi", "A3", "Sedan"),
            VehicleSuggestion("Lexus", "RX", "SUV"),
            VehicleSuggestion("Lexus", "NX", "SUV"),
            VehicleSuggestion("Lexus", "ES", "Sedan"),
            VehicleSuggestion("Volvo", "XC60", "SUV"),
            VehicleSuggestion("Volvo", "XC90", "SUV"),
            VehicleSuggestion("Jeep", "Grand Cherokee", "SUV"),
            VehicleSuggestion("Jeep", "Wrangler", "4x4"),
            VehicleSuggestion("Porsche", "911", "Coupe"),
            VehicleSuggestion("Porsche", "Cayenne", "SUV"),
            VehicleSuggestion("Porsche", "Macan", "SUV")
        )
    }

    private val _uiState = MutableStateFlow(
        AddVehicleUiState(
            brand = savedStateHandle[KEY_BRAND] ?: "",
            model = savedStateHandle[KEY_MODEL] ?: "",
            year = savedStateHandle[KEY_YEAR] ?: "",
            currentOdometer = savedStateHandle[KEY_ODOMETER] ?: "",
            suggestions = POPULAR_SUGGESTIONS
        )
    )
    val uiState: StateFlow<AddVehicleUiState> = _uiState.asStateFlow()

    init {
        updatePreviewReminders(_uiState.value.currentOdometer)
    }

    fun onEvent(event: AddVehicleUiEvent) {
        when (event) {
            is AddVehicleUiEvent.StepChanged -> {
                _uiState.value = _uiState.value.copy(currentStep = event.step, errorRes = null)
            }
            is AddVehicleUiEvent.SearchQueryChanged -> {
                handleUniversalSearchQuery(event.query)
            }
            is AddVehicleUiEvent.SuggestionSelected -> {
                _uiState.value = _uiState.value.copy(
                    brand = event.suggestion.make,
                    model = event.suggestion.model,
                    searchQuery = "${event.suggestion.make} ${event.suggestion.model}",
                    currentStep = AddVehicleStep.IDENTITY,
                    isCustomEntry = false,
                    errorRes = null
                )
                savedStateHandle[KEY_BRAND] = event.suggestion.make
                savedStateHandle[KEY_MODEL] = event.suggestion.model
            }
            is AddVehicleUiEvent.CustomEntryToggled -> {
                _uiState.value = _uiState.value.copy(
                    isCustomEntry = event.isCustom,
                    brand = if (event.isCustom) _uiState.value.brand else "",
                    model = if (event.isCustom) _uiState.value.model else "",
                    errorRes = null
                )
            }
            is AddVehicleUiEvent.BrandChanged -> {
                _uiState.value = _uiState.value.copy(brand = event.brand, errorRes = null)
                savedStateHandle[KEY_BRAND] = event.brand
            }
            is AddVehicleUiEvent.ModelChanged -> {
                _uiState.value = _uiState.value.copy(model = event.model, errorRes = null)
                savedStateHandle[KEY_MODEL] = event.model
            }
            is AddVehicleUiEvent.YearChanged -> {
                _uiState.value = _uiState.value.copy(year = event.year, errorRes = null)
                savedStateHandle[KEY_YEAR] = event.year
            }
            is AddVehicleUiEvent.RoleChanged -> {
                _uiState.value = _uiState.value.copy(role = event.role)
            }
            is AddVehicleUiEvent.OdometerChanged -> {
                _uiState.value = _uiState.value.copy(currentOdometer = event.odometer, errorRes = null)
                savedStateHandle[KEY_ODOMETER] = event.odometer
                updatePreviewReminders(event.odometer)
            }
            is AddVehicleUiEvent.OdometerAdjusted -> {
                val current = _uiState.value.currentOdometer.toIntOrNull() ?: 0
                val next = (current + event.delta).coerceAtLeast(0)
                val str = next.toString()
                _uiState.value = _uiState.value.copy(currentOdometer = str, errorRes = null)
                savedStateHandle[KEY_ODOMETER] = str
                updatePreviewReminders(str)
            }
            is AddVehicleUiEvent.PlateNumberChanged -> {
                _uiState.value = _uiState.value.copy(plateNumber = event.plateNumber)
            }
            is AddVehicleUiEvent.VinChanged -> {
                _uiState.value = _uiState.value.copy(vin = event.vin, errorRes = null)
            }
            is AddVehicleUiEvent.PhotoUriChanged -> {
                _uiState.value = _uiState.value.copy(photoUri = event.uri)
            }
            is AddVehicleUiEvent.NextStepClicked -> {
                handleNextStep()
            }
            is AddVehicleUiEvent.PreviousStepClicked -> {
                handlePreviousStep()
            }
            is AddVehicleUiEvent.SaveClicked -> {
                saveVehicle()
            }
        }
    }

    private fun handleUniversalSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch(defaultDispatcher) {
            val normalized = query.trim().lowercase().replace("-", " ").replace("  ", " ")
            if (normalized.isEmpty()) {
                _uiState.value = _uiState.value.copy(suggestions = POPULAR_SUGGESTIONS)
                return@launch
            }

            // Universal Fuzzy & Prefix Ranking
            val scoredResults = ALL_CATALOG_VEHICLES.mapNotNull { item ->
                val normMake = item.make.lowercase()
                val normModel = item.model.lowercase().replace("-", " ")
                val normFull = "$normMake $normModel"

                val score = when {
                    normModel == normalized || normFull == normalized -> 100
                    normModel.startsWith(normalized) -> 85
                    normMake.startsWith(normalized) -> 80
                    normFull.startsWith(normalized) -> 75
                    normModel.contains(normalized) -> 60
                    normFull.contains(normalized) -> 50
                    else -> null
                }
                score?.let { s -> Pair(item, s) }
            }.sortedByDescending { it.second }
                .map { it.first }
                .distinctBy { "${it.make}_${it.model}" }
                .take(15)

            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(suggestions = scoredResults)
            }
        }
    }

    private fun handleNextStep() {
        val state = _uiState.value
        when (state.currentStep) {
            AddVehicleStep.DISCOVERY -> {
                if (state.brand.isBlank() || state.model.isBlank()) {
                    _uiState.value = state.copy(errorRes = R.string.error_make_model_required)
                    return
                }
                _uiState.value = state.copy(currentStep = AddVehicleStep.IDENTITY, errorRes = null)
            }
            AddVehicleStep.IDENTITY -> {
                val yearInt = state.year.toIntOrNull() ?: 0
                if (state.year.isNotBlank()) {
                    Validators.validateYear(yearInt)?.let { validationError ->
                        _uiState.value = state.copy(
                            errorRes = validationError.code.toStringRes(),
                            errorArgs = validationError.args
                        )
                        return
                    }
                }
                _uiState.value = state.copy(currentStep = AddVehicleStep.SCHEDULE, errorRes = null)
            }
            AddVehicleStep.SCHEDULE -> {
                saveVehicle()
            }
        }
    }

    private fun handlePreviousStep() {
        val state = _uiState.value
        when (state.currentStep) {
            AddVehicleStep.DISCOVERY -> Unit
            AddVehicleStep.IDENTITY -> _uiState.value = state.copy(currentStep = AddVehicleStep.DISCOVERY, errorRes = null)
            AddVehicleStep.SCHEDULE -> _uiState.value = state.copy(currentStep = AddVehicleStep.IDENTITY, errorRes = null)
        }
    }

    private fun updatePreviewReminders(odometerStr: String) {
        viewModelScope.launch(defaultDispatcher) {
            val odometerDisplay = odometerStr.toIntOrNull() ?: 0
            val unit = userPreferences.distanceUnit.first()
            val odometerKm = DistanceUtil.displayToKm(odometerDisplay, unit)
            val now = System.currentTimeMillis()
            val plan = CreateDefaultRemindersUseCase.buildPlan(odometerKm, DrivingAmount.TYPICAL, now)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(previewReminders = plan)
            }
        }
    }

    private fun saveVehicle() {
        val state = _uiState.value
        if (state.brand.isBlank() || state.model.isBlank()) {
            _uiState.value = state.copy(
                currentStep = AddVehicleStep.DISCOVERY,
                errorRes = R.string.error_make_model_required,
                errorArgs = emptyList()
            )
            return
        }

        val yearInt = state.year.toIntOrNull() ?: 0
        if (state.year.isNotBlank()) {
            Validators.validateYear(yearInt)?.let { validationError ->
                _uiState.value = state.copy(
                    currentStep = AddVehicleStep.IDENTITY,
                    errorRes = validationError.code.toStringRes(),
                    errorArgs = validationError.args
                )
                return
            }
        }

        Validators.validateVin(state.vin)?.let { validationError ->
            _uiState.value = state.copy(
                errorRes = validationError.code.toStringRes(),
                errorArgs = validationError.args
            )
            return
        }

        val odometerDisplay = state.currentOdometer.toIntOrNull() ?: 0

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorRes = null, errorArgs = emptyList())
            try {
                val unit = userPreferences.distanceUnit.first()
                val odometerKm = DistanceUtil.displayToKm(odometerDisplay, unit)
                val now = System.currentTimeMillis()
                val roleNote = if (state.role.isNotBlank()) "[ ${state.role} ]" else ""

                val newVehicle = Vehicle(
                    id = 0,
                    make = state.brand,
                    model = state.model,
                    year = yearInt,
                    currentOdometer = odometerKm,
                    plateNumber = state.plateNumber,
                    vin = state.vin.ifBlank { null },
                    photoUri = state.photoUri,
                    isArchived = false,
                    notes = roleNote,
                    createdAt = now,
                    updatedAt = now
                )

                // Atomic transactional creation of vehicle + default reminders
                vehicleRepository.insertVehicleWithInitialState(newVehicle) { vehicleId ->
                    createDefaultReminders(vehicleId, odometerKm)
                }

                analyticsHelper.logEvent(
                    AnalyticsEvents.VEHICLE_ADDED,
                    mapOf(
                        AnalyticsParams.VEHICLE_MAKE to state.brand,
                        AnalyticsParams.VEHICLE_MODEL to state.model
                    )
                )

                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                Timber.e(e, "Failed to save vehicle")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorRes = R.string.error_save_vehicle_failed,
                    errorArgs = emptyList()
                )
            }
        }
    }
}

/**
 * Maps a pure-domain [ValidationErrorCode] to its display string resource.
 */
@StringRes
private fun ValidationErrorCode.toStringRes(): Int = when (this) {
    ValidationErrorCode.YEAR_TOO_EARLY -> R.string.validation_year_too_early
    ValidationErrorCode.YEAR_TOO_LATE -> R.string.validation_year_too_late
    ValidationErrorCode.ODOMETER_NEGATIVE -> R.string.validation_odometer_negative
    ValidationErrorCode.FIELD_REQUIRED -> R.string.validation_field_required
    ValidationErrorCode.VIN_INVALID_FORMAT -> R.string.validation_vin_invalid_format
    ValidationErrorCode.COST_NEGATIVE -> R.string.error_cost_negative
}
