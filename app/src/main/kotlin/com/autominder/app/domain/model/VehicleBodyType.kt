package com.autominder.app.domain.model

/**
 * Semantic vehicle body type — resolved from Make + Model text by
 * [com.autominder.app.domain.util.VehicleBodyTypeResolver].
 *
 * Intentionally carries NO Android / R.drawable references so this type
 * lives in the pure-Kotlin domain module and is fully unit-testable
 * without an Android runtime.
 *
 * Resolution priority (in [VehicleBodyTypeResolver]):
 * 1. Motorcycle keywords  (most distinctive, checked first)
 * 2. Truck / Pickup keywords
 * 3. Minivan keywords
 * 4. Convertible keywords
 * 5. Coupe keywords
 * 6. Hatchback keywords
 * 7. SUV / Crossover keywords
 * 8. [SEDAN] — default fallback
 */
enum class VehicleBodyType {
    SEDAN,
    SUV,
    TRUCK,
    COUPE,
    HATCHBACK,
    CONVERTIBLE,
    MINIVAN,
    MOTORCYCLE;

    companion object {
        /** Applied when make + model text matches no keyword family. */
        val DEFAULT: VehicleBodyType = SEDAN
    }
}
