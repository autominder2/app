package com.autominder.app.domain.model

/**
 * Pure Kotlin domain model for a completed service record.
 * Does not depend on Android or Room.
 */
data class Service(
    val id: Long = 0,
    val vehicleId: Long,
    val serviceType: ServiceType,
    val customLabel: String? = null,
    val odometerAtService: Int,
    val serviceDate: Long,
    val costCents: Int? = null,
    val shopName: String? = null,
    val notes: String = "",
    val receiptPhotoUri: String? = null,
    val createdAt: Long = 0
)
