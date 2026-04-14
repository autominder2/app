package com.autominder.app.domain.model

/**
 * Pure Kotlin domain model for a Vehicle.
 * Does not depend on Android or Room.
 */
data class Vehicle(
    val id: Long = 0,
    val make: String,
    val model: String,
    val year: Int,
    val plateNumber: String = "",
    val vin: String? = null,
    val currentOdometer: Int,
    val photoUri: String? = null,
    val isArchived: Boolean = false,
    val notes: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
