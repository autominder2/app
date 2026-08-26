package com.autominder.app.domain.intelligence

import androidx.compose.runtime.Immutable
import com.autominder.app.domain.model.MileageLogEntry
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.Vehicle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * What AutoMinder can say about a vehicle without ever having inspected it.
 *
 * This engine used to emit a 0-100 `score` and a four-way verdict
 * (EXCELLENT / GOOD / ATTENTION_NEEDED / CRITICAL) computed from weighted
 * severity penalties. Both were removed on 2026-08-26, for two reasons:
 *
 *  1. `.claude/rules/ui.md` bans health scores outright. A percentage implies
 *     a measurement of the machine. AutoMinder has never sensed the machine —
 *     it has only ever read rows the owner typed in.
 *  2. The scoring was not internally honest either: a vehicle with no
 *     reminders and no history defaulted to `score = 100`,
 *     `verdict = EXCELLENT`, so the app was most confident about the car it
 *     knew least about.
 *
 * What replaces it is a restatement, not an inference. [ConfidenceState] is
 * read straight off the reminder statuses [ServiceStatus] already computed,
 * and the counts are counts. Nothing here is weighted, and nothing is derived
 * from anything the owner did not enter.
 *
 * All copy lives in `strings.xml`. This layer returns typed signals plus their
 * numeric and label payloads so the UI can localize and pluralize them. The
 * previous version returned English sentences from the domain layer and leaked
 * raw enum names such as `OIL_CHANGE` directly to the screen.
 */
enum class ConfidenceState {
    /** No reminders and no history — there is nothing to report yet. */
    NEEDS_SETUP,

    /** Every reminder the owner set is inside its interval. */
    UP_TO_DATE,

    /** At least one reminder is approaching its interval; none are past it. */
    DUE_SOON,

    /** At least one reminder is past its interval. */
    OVERDUE
}

/**
 * One observation the app can defend. Each maps to a string resource; the
 * payloads let the UI pluralize a count and resolve a [ServiceType] through
 * `ServiceTypeLabels.labelRes()`.
 */
enum class ConfidenceSignal {
    SCHEDULE_ACTIVE,
    NO_SCHEDULE,
    RECORDS_ON_FILE,
    NO_RECORDS,
    ODOMETER_RECENT,
    ODOMETER_STALE,
    NOTHING_OVERDUE,
    ITEM_OVERDUE,
    ITEM_DUE_SOON
}

@Immutable
data class ConfidenceFactor(
    val signal: ConfidenceSignal,
    val isPositive: Boolean,
    /** Payload for pluralized signals: a count of items, or a number of days. */
    val count: Int = 0,
    /** Set on item-level signals when the reminder uses a canonical type. */
    val serviceType: ServiceType? = null,
    /** Set on item-level signals when the reminder has an owner-typed label. */
    val customLabel: String? = null
)

@Immutable
data class VehicleConfidence(
    val state: ConfidenceState = ConfidenceState.NEEDS_SETUP,
    val overdueCount: Int = 0,
    val dueSoonCount: Int = 0,
    val nextServiceType: ServiceType? = null,
    val nextCustomLabel: String? = null,
    val factors: List<ConfidenceFactor> = emptyList()
)

@Singleton
class VehicleConfidenceEngine @Inject constructor() {

    fun evaluate(
        vehicle: Vehicle,
        reminders: List<Reminder>,
        statuses: Map<Long, ServiceStatus>,
        mileageLogs: List<MileageLogEntry>,
        services: List<Service>,
        nowMillis: Long = System.currentTimeMillis()
    ): VehicleConfidence {
        val overdue = reminders.filter { statuses[it.id] == ServiceStatus.OVERDUE }
        val dueSoon = reminders.filter { statuses[it.id] == ServiceStatus.DUE_SOON }

        val state = when {
            reminders.isEmpty() && services.isEmpty() -> ConfidenceState.NEEDS_SETUP
            overdue.isNotEmpty() -> ConfidenceState.OVERDUE
            dueSoon.isNotEmpty() -> ConfidenceState.DUE_SOON
            else -> ConfidenceState.UP_TO_DATE
        }

        val next = overdue.firstOrNull() ?: dueSoon.firstOrNull() ?: reminders.firstOrNull()

        return VehicleConfidence(
            state = state,
            overdueCount = overdue.size,
            dueSoonCount = dueSoon.size,
            nextServiceType = next?.serviceType,
            nextCustomLabel = next?.customLabel,
            factors = buildFactors(
                vehicle = vehicle,
                reminders = reminders,
                services = services,
                mileageLogs = mileageLogs,
                overdue = overdue,
                dueSoon = dueSoon,
                nowMillis = nowMillis
            )
        )
    }

    private fun buildFactors(
        vehicle: Vehicle,
        reminders: List<Reminder>,
        services: List<Service>,
        mileageLogs: List<MileageLogEntry>,
        overdue: List<Reminder>,
        dueSoon: List<Reminder>,
        nowMillis: Long
    ): List<ConfidenceFactor> = buildList {
        // What is being watched.
        if (reminders.isEmpty()) {
            add(ConfidenceFactor(ConfidenceSignal.NO_SCHEDULE, isPositive = false))
        } else {
            add(
                ConfidenceFactor(
                    signal = ConfidenceSignal.SCHEDULE_ACTIVE,
                    isPositive = true,
                    count = reminders.size
                )
            )
        }

        // What history exists to compare against.
        if (services.isEmpty()) {
            add(ConfidenceFactor(ConfidenceSignal.NO_RECORDS, isPositive = false))
        } else {
            add(
                ConfidenceFactor(
                    signal = ConfidenceSignal.RECORDS_ON_FILE,
                    isPositive = true,
                    count = services.size
                )
            )
        }

        // How current the mileage is. Due dates drift when this goes stale, so
        // it is a statement about the data, not about the vehicle.
        daysSinceOdometer(vehicle, mileageLogs, nowMillis)?.let { days ->
            val fresh = days <= ODOMETER_FRESH_DAYS
            add(
                ConfidenceFactor(
                    signal = if (fresh) ConfidenceSignal.ODOMETER_RECENT else ConfidenceSignal.ODOMETER_STALE,
                    isPositive = fresh,
                    count = days
                )
            )
        }

        // Item-level state, read straight off ServiceStatus.
        if (overdue.isEmpty() && dueSoon.isEmpty() && reminders.isNotEmpty()) {
            add(ConfidenceFactor(ConfidenceSignal.NOTHING_OVERDUE, isPositive = true))
        }
        overdue.forEach { add(it.toFactor(ConfidenceSignal.ITEM_OVERDUE)) }
        dueSoon.forEach { add(it.toFactor(ConfidenceSignal.ITEM_DUE_SOON)) }
    }

    private fun Reminder.toFactor(signal: ConfidenceSignal) = ConfidenceFactor(
        signal = signal,
        isPositive = false,
        serviceType = serviceType,
        customLabel = customLabel
    )

    /**
     * Days since the odometer was last touched, preferring an explicit mileage
     * log over [Vehicle.updatedAt] — editing a vehicle's nickname is not an
     * odometer reading. Null when there is nothing to date.
     */
    private fun daysSinceOdometer(
        vehicle: Vehicle,
        mileageLogs: List<MileageLogEntry>,
        nowMillis: Long
    ): Int? {
        val lastTouched = mileageLogs.maxOfOrNull { it.loggedAt } ?: vehicle.updatedAt
        if (lastTouched <= 0L || lastTouched > nowMillis) return null
        return ((nowMillis - lastTouched) / MILLIS_PER_DAY).toInt()
    }

    private companion object {
        const val MILLIS_PER_DAY = 1000L * 60 * 60 * 24
        const val ODOMETER_FRESH_DAYS = 30
    }
}
