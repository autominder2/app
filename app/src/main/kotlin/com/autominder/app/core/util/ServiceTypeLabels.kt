package com.autominder.app.core.util

import android.content.Context
import androidx.annotation.StringRes
import com.autominder.app.R
import com.autominder.app.domain.model.ServiceType

/**
 * The single mapping from [ServiceType] to its localized string resource.
 *
 * This lives in `core/util` rather than `ui/util` on purpose. The UI is not the
 * only place that shows a service name to a user — notifications and the
 * home-screen widget do too, and both run outside composition where
 * `stringResource` is unavailable. Before this existed, `ReminderCheckWorker`
 * had no localized path at all and fell back to
 * `serviceType.name.replace('_', ' ')`, which posts a raw English enum name to
 * every user in every locale.
 *
 * Never add a second copy of this `when`. One mapping, three call sites.
 */
@StringRes
fun ServiceType.labelRes(): Int = when (this) {
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

/**
 * Localized label for use outside composition — workers, receivers, the widget.
 * Inside a `@Composable`, use `ServiceType.localizedLabel()` from `ui/util`.
 */
fun ServiceType.localizedLabel(context: Context): String = context.getString(labelRes())
