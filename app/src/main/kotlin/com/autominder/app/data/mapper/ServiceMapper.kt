package com.autominder.app.data.mapper

import com.autominder.app.data.local.entity.ServiceEntity
import com.autominder.app.domain.model.Service

/**
 * Maps between [ServiceEntity] and [Service].
 */
fun ServiceEntity.toDomain(): Service {
    return Service(
        id = id,
        vehicleId = vehicleId,
        serviceType = serviceType,
        customLabel = customLabel,
        odometerAtService = odometerAtService,
        serviceDate = serviceDate,
        costCents = costCents,
        shopName = shopName,
        notes = notes,
        receiptPhotoUri = receiptPhotoUri,
        createdAt = createdAt
    )
}

fun Service.toEntity(): ServiceEntity {
    return ServiceEntity(
        id = id,
        vehicleId = vehicleId,
        serviceType = serviceType,
        customLabel = customLabel,
        odometerAtService = odometerAtService,
        serviceDate = serviceDate,
        costCents = costCents,
        shopName = shopName,
        notes = notes,
        receiptPhotoUri = receiptPhotoUri,
        createdAt = createdAt
    )
}
