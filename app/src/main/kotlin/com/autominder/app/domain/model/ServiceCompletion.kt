package com.autominder.app.domain.model

/**
 * A completed service as one logical ownership operation.
 *
 * The ViewModel validates raw input and builds this command; the data layer owns
 * the single transaction that persists the service record, advances the vehicle
 * odometer when the reading is newer, and rebases the matching reminder.
 *
 * [service].createdAt is the operation's "now" — reminder timestamps derive from
 * it so the whole write shares one instant instead of sampling the clock per row.
 */
data class ServiceCompletion(
    val service: Service,
    /** "Remind me for the next one" — the user's explicit choice on this save. */
    val remindNext: Boolean,
    /** Interval in km, already converted from the user's display unit. Null = no distance rule. */
    val reminderIntervalKm: Int?,
    /** Interval in days, already converted from the user's chosen months. Null = no date rule. */
    val reminderIntervalDays: Int?
)

/**
 * Outcome of a service completion. The whole operation either commits or leaves
 * the database untouched — there is no partially-applied state to report.
 */
sealed interface ServiceCompletionResult {

    /** Committed. [serviceId] is the row id of the newly stored service record. */
    data class Success(val serviceId: Long) : ServiceCompletionResult

    /**
     * The target vehicle no longer exists (deleted between screen load and Save).
     * Nothing was written: no service, no odometer change, no reminder change.
     */
    data object VehicleNotFound : ServiceCompletionResult

    /** A persistence failure rolled the entire transaction back. */
    data class Failed(val cause: Throwable) : ServiceCompletionResult
}
