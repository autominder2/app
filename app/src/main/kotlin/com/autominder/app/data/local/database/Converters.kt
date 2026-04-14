package com.autominder.app.data.local.database

import androidx.room.TypeConverter
import com.autominder.app.domain.model.ServiceType

/**
 * Room TypeConverter for [ServiceType].
 * Stored as String name (NOT ordinal — ordinal is fragile if enum order changes).
 */
class Converters {

    @TypeConverter
    fun fromServiceType(value: ServiceType): String = value.name

    @TypeConverter
    fun toServiceType(value: String): ServiceType =
        ServiceType.entries.firstOrNull { it.name == value }
            ?: ServiceType.CUSTOM  // graceful fallback for forward-compat
}
