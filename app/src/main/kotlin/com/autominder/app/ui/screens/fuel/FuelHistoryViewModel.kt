package com.autominder.app.ui.screens.fuel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.autominder.app.R
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.FuelEntry
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IFuelRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.usecase.CalculateEfficiencyUseCase
import com.autominder.app.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * Aggregated monthly spending for the bar chart.
 * [label] is a short month abbreviation (e.g. "Jan", "Feb").
 */
data class FuelMonthlySpend(
    val label: String,
    val cents: Long,
    val volumeMilliliters: Long
)

/**
 * The "best" or "worst" single fill-up by efficiency.
 */
data class EfficiencyExtreme(
    val efficiency: Double,
    val dateLabel: String
)

data class FuelHistoryUiState(
    val vehicleId: Long,
    val vehicle: Vehicle? = null,
    val distanceUnit: String = "km",
    val entries: List<FuelEntryDetailed> = emptyList(),
    val averageEfficiency: Double = 0.0,
    val totalFuelCostCents: Long = 0L,
    val totalVolumeMilliliters: Long = 0L,
    val averagePricePerLiterCents: Double = 0.0,

    // --- P0 Upgrade: New intelligence surfaces ---

    /** Cost per km (or mile) in cents, computed from total spend / total distance driven. */
    val costPerDistanceCents: Double = 0.0,

    /** Efficiency data points (oldest → newest) for the FuelEfficiencyChart trend line. */
    val efficiencyTrendSeries: List<Double> = emptyList(),

    /** Last 6 months of spending for the SpendingTrendChart bar chart. */
    val monthlySpending: List<FuelMonthlySpend> = emptyList(),

    /** Month-over-month fuel cost change as a percentage (e.g. -8.2 means 8.2% less). */
    val monthOverMonthDeltaPct: Double? = null,

    /** Best single-tank efficiency ever recorded. */
    val bestTank: EfficiencyExtreme? = null,

    /** Worst single-tank efficiency ever recorded. */
    val worstTank: EfficiencyExtreme? = null,

    /** Whether efficiency is trending up, down, or flat over the last 5 fill-ups. */
    val efficiencyTrend: EfficiencyTrend = EfficiencyTrend.FLAT,

    val isLoading: Boolean = false,
    @StringRes val errorRes: Int? = null
)

enum class EfficiencyTrend { IMPROVING, DECLINING, FLAT }

data class FuelEntryDetailed(
    val entry: FuelEntry,
    val efficiency: Double? = null,
    val pricePerLiterCents: Double? = null,
    val deltaKm: Int? = null
)

sealed class FuelHistoryUiEvent {
    data object Retry : FuelHistoryUiEvent()
    data class DeleteEntry(val entry: FuelEntry) : FuelHistoryUiEvent()
    data class UndoDelete(val entry: FuelEntry) : FuelHistoryUiEvent()
}

@HiltViewModel
class FuelHistoryViewModel @Inject constructor(
    private val fuelRepository: IFuelRepository,
    private val vehicleRepository: IVehicleRepository,
    private val userPreferences: UserPreferences,
    private val calculateEfficiency: CalculateEfficiencyUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.get<Long>("vehicleId")
        ?: runCatching { savedStateHandle.toRoute<NavRoutes.FuelHistory>().vehicleId }.getOrDefault(0L)

    private val _uiState = MutableStateFlow(FuelHistoryUiState(vehicleId = vehicleId, isLoading = true))
    val uiState: StateFlow<FuelHistoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(event: FuelHistoryUiEvent) {
        when (event) {
            is FuelHistoryUiEvent.Retry -> loadData()
            is FuelHistoryUiEvent.DeleteEntry -> deleteEntry(event.entry)
            is FuelHistoryUiEvent.UndoDelete -> undoDelete(event.entry)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorRes = null)

            combine(
                vehicleRepository.getVehicleById(vehicleId),
                fuelRepository.getFuelEntriesForVehicle(vehicleId),
                userPreferences.distanceUnit
            ) { vehicle, rawEntries, unit ->
                Triple(vehicle, rawEntries, unit)
            }.catch { e ->
                Timber.e(e, "Failed to load fuel history")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorRes = R.string.error_load_fuel_failed
                )
            }.collect { (vehicle, rawEntries, unit) ->
                if (rawEntries.isEmpty()) {
                    _uiState.value = FuelHistoryUiState(
                        vehicleId = vehicleId,
                        vehicle = vehicle,
                        distanceUnit = unit,
                        isLoading = false
                    )
                } else {
                    val sorted = rawEntries.sortedByDescending { it.odometer }
                    val sortedAsc = rawEntries.sortedBy { it.odometer }

                    // Build detailed entries with per-fill efficiency
                    val withDetails = sorted.mapIndexed { index, fuelEntry ->
                        val previous = if (index < sorted.size - 1) sorted[index + 1] else null
                        val eff = calculateEfficiency.calculate(fuelEntry, previous)

                        val liters = fuelEntry.volumeMilliliters / 1000.0
                        val pricePerLiter = if (liters > 0.0) fuelEntry.costCents / liters else null
                        val delta = if (previous != null && fuelEntry.odometer > previous.odometer) {
                            fuelEntry.odometer - previous.odometer
                        } else null

                        FuelEntryDetailed(
                            entry = fuelEntry,
                            efficiency = if (eff > 0) eff else null,
                            pricePerLiterCents = pricePerLiter,
                            deltaKm = delta
                        )
                    }

                    // Aggregate totals
                    val totalCost = rawEntries.sumOf { it.costCents }
                    val totalVolume = rawEntries.sumOf { it.volumeMilliliters.toLong() }
                    val totalLiters = totalVolume / 1000.0
                    val avgPricePerLiter = if (totalLiters > 0.0) totalCost / totalLiters else 0.0
                    val avgEff = calculateEfficiency.calculateAverage(rawEntries)

                    // P0 Upgrade: Cost per km (or mile)
                    val totalDistance = if (sortedAsc.size >= 2) {
                        sortedAsc.last().odometer - sortedAsc.first().odometer
                    } else 0
                    val costPerDistCents = if (totalDistance > 0) {
                        val displayDistance = if (unit == "mi") totalDistance * 0.621371 else totalDistance.toDouble()
                        totalCost.toDouble() / displayDistance
                    } else 0.0

                    // P0 Upgrade: Efficiency trend series (oldest → newest, skip first entry)
                    val effSeries = buildEfficiencyTrendSeries(sortedAsc)

                    // P0 Upgrade: Efficiency trend direction (last 5 data points)
                    val trend = computeEfficiencyTrend(effSeries)

                    // P0 Upgrade: Best & worst tank
                    val (best, worst) = findEfficiencyExtremes(withDetails)

                    // P0 Upgrade: Monthly spending aggregation (last 6 months)
                    val monthly = aggregateMonthlySpending(rawEntries)

                    // P0 Upgrade: Month-over-month delta
                    val momDelta = computeMonthOverMonthDelta(monthly)

                    _uiState.value = FuelHistoryUiState(
                        vehicleId = vehicleId,
                        vehicle = vehicle,
                        distanceUnit = unit,
                        entries = withDetails,
                        averageEfficiency = avgEff,
                        totalFuelCostCents = totalCost,
                        totalVolumeMilliliters = totalVolume,
                        averagePricePerLiterCents = avgPricePerLiter,
                        costPerDistanceCents = costPerDistCents,
                        efficiencyTrendSeries = effSeries,
                        monthlySpending = monthly,
                        monthOverMonthDeltaPct = momDelta,
                        bestTank = best,
                        worstTank = worst,
                        efficiencyTrend = trend,
                        isLoading = false
                    )
                }
            }
        }
    }

    // ─── P0 Upgrade: Intelligence helpers ─────────────────────────────────

    /**
     * Build an ordered list of efficiency values (oldest fill → newest fill)
     * for the trend line chart. Skips the very first fill-up since it has
     * no predecessor for distance calculation.
     */
    private fun buildEfficiencyTrendSeries(sortedAsc: List<FuelEntry>): List<Double> {
        if (sortedAsc.size < 2) return emptyList()
        val series = mutableListOf<Double>()
        for (i in 1 until sortedAsc.size) {
            val eff = calculateEfficiency.calculate(sortedAsc[i], sortedAsc[i - 1])
            if (eff > 0) series.add(eff)
        }
        return series
    }

    /**
     * Determines whether efficiency is improving, declining, or flat
     * based on a simple linear regression over the last N data points.
     */
    private fun computeEfficiencyTrend(series: List<Double>): EfficiencyTrend {
        val window = series.takeLast(5)
        if (window.size < 3) return EfficiencyTrend.FLAT

        // Simple slope: average of first half vs average of second half
        val mid = window.size / 2
        val firstHalfAvg = window.take(mid).average()
        val secondHalfAvg = window.drop(mid).average()
        val changePct = ((secondHalfAvg - firstHalfAvg) / firstHalfAvg) * 100

        return when {
            changePct > 2.0 -> EfficiencyTrend.IMPROVING
            changePct < -2.0 -> EfficiencyTrend.DECLINING
            else -> EfficiencyTrend.FLAT
        }
    }

    /**
     * Find the best and worst single-tank efficiency readings.
     */
    private fun findEfficiencyExtremes(
        entries: List<FuelEntryDetailed>
    ): Pair<EfficiencyExtreme?, EfficiencyExtreme?> {
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val withEff = entries.filter { it.efficiency != null && it.efficiency > 0 }
        if (withEff.isEmpty()) return null to null

        val bestEntry = withEff.maxByOrNull { it.efficiency!! }
        val worstEntry = withEff.minByOrNull { it.efficiency!! }

        val best = bestEntry?.let {
            EfficiencyExtreme(
                efficiency = it.efficiency!!,
                dateLabel = dateFormat.format(it.entry.date)
            )
        }
        val worst = worstEntry?.let {
            EfficiencyExtreme(
                efficiency = it.efficiency!!,
                dateLabel = dateFormat.format(it.entry.date)
            )
        }
        return best to worst
    }

    /**
     * Aggregate fuel spending into monthly buckets for the last 6 months.
     * Returns a list of [FuelMonthlySpend] from oldest to newest month.
     */
    private fun aggregateMonthlySpending(entries: List<FuelEntry>): List<FuelMonthlySpend> {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val buckets = mutableMapOf<String, Pair<Long, Long>>() // key → (cents, ml)
        val orderedKeys = mutableListOf<String>()

        // Create 6 monthly buckets from 5 months ago to current month
        for (i in 5 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -i)
            val key = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
            buckets[key] = 0L to 0L
            orderedKeys.add(key)
        }

        // Distribute entries into buckets
        for (entry in entries) {
            calendar.time = entry.date
            val key = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}"
            if (buckets.containsKey(key)) {
                val (currentCents, currentMl) = buckets[key]!!
                buckets[key] = (currentCents + entry.costCents) to (currentMl + entry.volumeMilliliters)
            }
        }

        return orderedKeys.map { key ->
            val parts = key.split("-")
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, parts[0].toInt())
            cal.set(Calendar.MONTH, parts[1].toInt())
            val (cents, ml) = buckets[key]!!
            FuelMonthlySpend(
                label = monthFormat.format(cal.time),
                cents = cents,
                volumeMilliliters = ml
            )
        }
    }

    /**
     * Compute month-over-month spending change percentage.
     * Compares the most recent completed month to the one before it.
     * Returns null if insufficient data.
     */
    private fun computeMonthOverMonthDelta(monthly: List<FuelMonthlySpend>): Double? {
        if (monthly.size < 2) return null
        // Look at the two most recent months with spending data
        val recent = monthly.filter { it.cents > 0 }.takeLast(2)
        if (recent.size < 2) return null
        val prev = recent[0].cents
        val curr = recent[1].cents
        if (prev <= 0L) return null
        return ((curr - prev).toDouble() / prev) * 100.0
    }

    private fun deleteEntry(entry: FuelEntry) {
        viewModelScope.launch {
            try {
                fuelRepository.deleteFuelEntry(entry)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete fuel entry")
            }
        }
    }

    private fun undoDelete(entry: FuelEntry) {
        viewModelScope.launch {
            try {
                fuelRepository.insertFuelEntry(entry)
            } catch (e: Exception) {
                Timber.e(e, "Failed to restore fuel entry")
            }
        }
    }
}
