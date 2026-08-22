package com.autominder.app.domain.model

/**
 * A sensible default maintenance interval for a service type, used to pre-fill
 * the "remind me for the next one" prompt when a user logs a service. Distance
 * is stored in km (converted to the user's unit for display); [months] drives
 * the date interval. Either field may be null when only one axis makes sense
 * (e.g. Registration is time-based, not distance-based).
 */
data class SuggestedInterval(
    val km: Int?,
    val months: Int?
)

/**
 * Maps each [ServiceType] to a reasonable default interval. These are starting
 * points the user can adjust — the value is in never making them guess.
 */
fun ServiceType.suggestedInterval(): SuggestedInterval = when (this) {
    ServiceType.OIL_CHANGE -> SuggestedInterval(km = 8_000, months = 6)
    ServiceType.TIRE_ROTATION -> SuggestedInterval(km = 12_000, months = 12)
    ServiceType.BRAKE_SERVICE -> SuggestedInterval(km = 40_000, months = 24)
    ServiceType.BATTERY -> SuggestedInterval(km = null, months = 48)
    ServiceType.AIR_FILTER -> SuggestedInterval(km = 20_000, months = 12)
    ServiceType.CABIN_FILTER -> SuggestedInterval(km = 20_000, months = 12)
    ServiceType.TRANSMISSION -> SuggestedInterval(km = 60_000, months = 24)
    ServiceType.COOLANT -> SuggestedInterval(km = 60_000, months = 24)
    ServiceType.SPARK_PLUGS -> SuggestedInterval(km = 50_000, months = 36)
    ServiceType.TIMING_BELT -> SuggestedInterval(km = 100_000, months = 60)
    ServiceType.WIPER_BLADES -> SuggestedInterval(km = 15_000, months = 12)
    ServiceType.INSURANCE -> SuggestedInterval(km = null, months = 12)
    ServiceType.REGISTRATION -> SuggestedInterval(km = null, months = 12)
    ServiceType.INSPECTION -> SuggestedInterval(km = null, months = 12)
    ServiceType.EMISSIONS_TEST -> SuggestedInterval(km = null, months = 12)
    ServiceType.CUSTOM -> SuggestedInterval(km = 10_000, months = 12)
}
