package com.autominder.app.domain.usecase.cockpit

import androidx.compose.runtime.Immutable
import com.autominder.app.domain.model.FuelEntry
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
data class MonthlySpend(val label: String, val cents: Int)

@Immutable
data class TypeSpend(
    val serviceType: ServiceType? = null,
    val customLabel: String? = null,
    val isOther: Boolean = false,
    val cents: Int
)

@Immutable
data class OwnershipCostSummary(
    val totalCostCents: Int = 0,
    val yearCostCents: Int = 0,
    val costPerKmCents: Double? = null,
    val monthlySpending: List<MonthlySpend> = emptyList(),
    val costByType: List<TypeSpend> = emptyList()
)

/**
 * Computes comprehensive vehicle financial intelligence:
 * 1. Total and year-to-date maintenance costs.
 * 2. Running cost per distance unit.
 * 3. 6-month spending trajectory.
 * 4. Cost-by-type breakdown.
 */
@Singleton
class CalculateOwnershipCostUseCase @Inject constructor() {

    fun execute(
        totalCostCents: Int,
        yearCostCents: Int,
        services: List<Service>,
        fuelEntries: List<FuelEntry>,
        currentOdometer: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): OwnershipCostSummary {
        val monthlySpending = computeMonthlySpending(services, nowMillis)
        val costByType = computeCostByType(services)
        val costPerKm = computeCostPerKm(totalCostCents, services, fuelEntries, currentOdometer)

        return OwnershipCostSummary(
            totalCostCents = totalCostCents,
            yearCostCents = yearCostCents,
            costPerKmCents = costPerKm,
            monthlySpending = monthlySpending,
            costByType = costByType
        )
    }

    private fun computeMonthlySpending(
        services: List<Service>,
        now: Long
    ): List<MonthlySpend> {
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        return (5 downTo 0).map { monthsBack ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.MONTH, -monthsBack)
            }
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)
            val cents = services.filter { s ->
                val sCal = Calendar.getInstance().apply { timeInMillis = s.serviceDate }
                sCal.get(Calendar.MONTH) == month && sCal.get(Calendar.YEAR) == year
            }.sumOf { it.costCents ?: 0 }
            MonthlySpend(label = monthFormat.format(cal.time), cents = cents)
        }
    }

    private fun computeCostByType(
        services: List<Service>
    ): List<TypeSpend> {
        val byType = services
            .filter { (it.costCents ?: 0) > 0 }
            .groupBy { it.customLabel ?: it.serviceType.name }
            .map { (_, group) ->
                val first = group.first()
                TypeSpend(
                    serviceType = if (first.customLabel == null) first.serviceType else null,
                    customLabel = first.customLabel,
                    cents = group.sumOf { it.costCents ?: 0 }
                )
            }
            .sortedByDescending { it.cents }
        if (byType.size <= 5) return byType
        val top = byType.take(5)
        val otherCents = byType.drop(5).sumOf { it.cents }
        return top + TypeSpend(isOther = true, cents = otherCents)
    }

    private fun computeCostPerKm(
        totalCostCents: Int,
        services: List<Service>,
        fuelEntries: List<FuelEntry>,
        currentOdometer: Int
    ): Double? {
        if (totalCostCents <= 0) return null
        val earliest = (services.map { it.odometerAtService } + fuelEntries.map { it.odometer })
            .filter { it > 0 }
            .minOrNull() ?: return null
        val distance = currentOdometer - earliest
        if (distance < 100) return null
        return totalCostCents.toDouble() / distance
    }
}
