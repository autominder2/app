package com.autominder.app.domain.model

/**
 * 16 canonical service types + CUSTOM catch-all.
 * CUSTOM must stay last for UI ordering.
 * Stored as String name via TypeConverter — new entries are forward-compatible.
 */
enum class ServiceType(val label: String) {
    OIL_CHANGE("Oil Change"),
    TIRE_ROTATION("Tire Rotation"),
    BRAKE_SERVICE("Brake Service"),
    BATTERY("Battery"),
    AIR_FILTER("Air Filter"),
    CABIN_FILTER("Cabin Filter"),
    TRANSMISSION("Transmission"),
    COOLANT("Coolant"),
    SPARK_PLUGS("Spark Plugs"),
    TIMING_BELT("Timing Belt"),
    WIPER_BLADES("Wiper Blades"),
    // Document & Compliance
    INSURANCE("Insurance"),
    REGISTRATION("Registration"),
    INSPECTION("Inspection"),
    EMISSIONS_TEST("Emissions Test"),
    CUSTOM("Custom")
}
