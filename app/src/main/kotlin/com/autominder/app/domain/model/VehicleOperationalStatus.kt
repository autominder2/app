package com.autominder.app.domain.model

/**
 * High-level operational status of a vehicle.
 *
 * Core invariant:
 * A vehicle with currentOdometer <= 0 is ALWAYS [SETUP_INCOMPLETE].
 * The system MUST NEVER claim "All clear" or "Healthy" when required data is missing.
 */
enum class VehicleOperationalStatus(val priority: Int) {
    OVERDUE(5),
    DUE_SOON(4),
    UPCOMING(3),
    HEALTHY(2),
    SETUP_INCOMPLETE(1);

    val requiresUserAttention: Boolean
        get() = this == OVERDUE || this == DUE_SOON || this == SETUP_INCOMPLETE
}
