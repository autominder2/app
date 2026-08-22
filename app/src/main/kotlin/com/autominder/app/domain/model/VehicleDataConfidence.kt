package com.autominder.app.domain.model

/**
 * Data confidence representation for vehicle telemetry and maintenance calculations.
 *
 * Deterministic model — NEVER claims "High confidence" when odometer is stale,
 * zero, or missing.
 */
enum class VehicleDataConfidence {
    HIGH,
    MEDIUM,
    ESTIMATED,
    MISSING_MILEAGE,
    INCOMPLETE;

    val isTrustworthy: Boolean
        get() = this == HIGH || this == MEDIUM

    companion object {
        private const val FOURTEEN_DAYS_MS = 14L * 24 * 60 * 60 * 1000
        private const val SIXTY_DAYS_MS = 60L * 24 * 60 * 60 * 1000

        /**
         * Evaluates data confidence from deterministic vehicle attributes.
         */
        fun evaluate(
            currentOdometer: Int,
            lastOdometerUpdateMillis: Long?,
            hasServiceHistory: Boolean,
            nowMillis: Long = System.currentTimeMillis()
        ): VehicleDataConfidence {
            if (currentOdometer <= 0) {
                return MISSING_MILEAGE
            }

            if (lastOdometerUpdateMillis == null) {
                return if (hasServiceHistory) MEDIUM else ESTIMATED
            }

            val ageMillis = nowMillis - lastOdometerUpdateMillis
            return when {
                ageMillis <= FOURTEEN_DAYS_MS && hasServiceHistory -> HIGH
                ageMillis <= SIXTY_DAYS_MS -> MEDIUM
                else -> ESTIMATED
            }
        }
    }
}
