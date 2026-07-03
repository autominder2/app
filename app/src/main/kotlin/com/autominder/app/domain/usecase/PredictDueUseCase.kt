package com.autominder.app.domain.usecase

import com.autominder.app.domain.model.Reminder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A dated odometer observation — mileage logs and fuel entries both qualify.
 */
data class OdometerPoint(val odometerKm: Int, val timestamp: Long)

/**
 * Personalized due forecast for one reminder.
 *
 * @param kmRemaining distance until the km trigger (null when the reminder has
 *   no km trigger or it has already passed)
 * @param predictedAt epoch millis when the reminder is expected to come due —
 *   the earlier of the date trigger and the projected km-trigger crossing
 */
data class DuePrediction(
    val kmRemaining: Int?,
    val predictedAt: Long?
)

/**
 * Turns raw "due at 56,000 km / due on Sep 1" data into "expect this around
 * Aug 12" by learning the vehicle's real driving rate from its odometer
 * history. Degrades gracefully: with fewer than [MIN_POINTS] observations or
 * a span shorter than [MIN_SPAN_DAYS], no rate is inferred and only the
 * explicit date trigger is used.
 */
@Singleton
class PredictDueUseCase @Inject constructor() {

    /**
     * Average km/day from odometer history, or null when there isn't enough
     * signal to trust. Points may arrive unordered; non-monotonic readings
     * (corrections, typos) are tolerated because only the overall span is used.
     */
    fun dailyKmRate(points: List<OdometerPoint>): Double? {
        if (points.size < MIN_POINTS) return null
        val sorted = points.sortedBy { it.timestamp }
        val first = sorted.first()
        val last = sorted.last()
        val spanDays = (last.timestamp - first.timestamp) / MILLIS_PER_DAY.toDouble()
        val kmDriven = last.odometerKm - first.odometerKm
        if (spanDays < MIN_SPAN_DAYS || kmDriven <= 0) return null
        return kmDriven / spanDays
    }

    /**
     * Forecast for a single reminder given the vehicle's current odometer and
     * an optional learned [dailyKmRate].
     */
    fun predict(
        reminder: Reminder,
        currentOdometerKm: Int,
        dailyKmRate: Double?,
        nowMillis: Long = System.currentTimeMillis()
    ): DuePrediction {
        val kmRemaining = reminder.nextDueOdometer
            ?.let { it - currentOdometerKm }
            ?.takeIf { it > 0 }

        val dateFromKm: Long? = if (kmRemaining != null && dailyKmRate != null && dailyKmRate > 0) {
            nowMillis + (kmRemaining / dailyKmRate * MILLIS_PER_DAY).toLong()
        } else null

        val dateFromTrigger = reminder.nextDueDate?.takeIf { it > nowMillis }

        val predictedAt = listOfNotNull(dateFromKm, dateFromTrigger).minOrNull()

        return DuePrediction(kmRemaining = kmRemaining, predictedAt = predictedAt)
    }

    companion object {
        private const val MIN_POINTS = 2
        private const val MIN_SPAN_DAYS = 3.0
        private const val MILLIS_PER_DAY = 86_400_000L
    }
}
