package com.autominder.app.domain.model

/**
 * Pure Kotlin domain model for a manual odometer reading.
 * Named MileageLogEntry to avoid collision with NavRoutes.MileageLog.
 * Does not depend on Android or Room.
 */
data class MileageLogEntry(
    val id: Long = 0,
    val vehicleId: Long,
    val odometer: Int,
    val loggedAt: Long = 0,
    val notes: String? = null
)
