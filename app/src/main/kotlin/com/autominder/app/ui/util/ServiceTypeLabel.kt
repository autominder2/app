package com.autominder.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.autominder.app.R
import com.autominder.app.domain.model.ServiceType

/**
 * UI-layer localization for [ServiceType]. The enum's own `label` is
 * English-only legacy/storage text — never render it directly in UI.
 */
@Composable
fun ServiceType.localizedLabel(): String = stringResource(
    when (this) {
        ServiceType.OIL_CHANGE -> R.string.service_type_oil_change
        ServiceType.TIRE_ROTATION -> R.string.service_type_tire_rotation
        ServiceType.BRAKE_SERVICE -> R.string.service_type_brake_service
        ServiceType.BATTERY -> R.string.service_type_battery
        ServiceType.AIR_FILTER -> R.string.service_type_air_filter
        ServiceType.CABIN_FILTER -> R.string.service_type_cabin_filter
        ServiceType.TRANSMISSION -> R.string.service_type_transmission
        ServiceType.COOLANT -> R.string.service_type_coolant
        ServiceType.SPARK_PLUGS -> R.string.service_type_spark_plugs
        ServiceType.TIMING_BELT -> R.string.service_type_timing_belt
        ServiceType.WIPER_BLADES -> R.string.service_type_wiper_blades
        ServiceType.INSURANCE -> R.string.service_type_insurance
        ServiceType.REGISTRATION -> R.string.service_type_registration
        ServiceType.INSPECTION -> R.string.service_type_inspection
        ServiceType.EMISSIONS_TEST -> R.string.service_type_emissions_test
        ServiceType.CUSTOM -> R.string.service_type_custom
    }
)
