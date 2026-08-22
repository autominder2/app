package com.autominder.app.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Hvac
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.autominder.app.domain.model.ServiceType

/** UI-layer icon for each service type (domain stays icon-free). */
fun ServiceType.icon(): ImageVector = when (this) {
    ServiceType.OIL_CHANGE -> Icons.Default.OilBarrel
    ServiceType.TIRE_ROTATION -> Icons.Default.TireRepair
    ServiceType.BRAKE_SERVICE -> Icons.Default.CarRepair
    ServiceType.BATTERY -> Icons.Default.BatteryChargingFull
    ServiceType.AIR_FILTER -> Icons.Default.Air
    ServiceType.CABIN_FILTER -> Icons.Default.Hvac
    ServiceType.TRANSMISSION -> Icons.Default.Settings
    ServiceType.COOLANT -> Icons.Default.AcUnit
    ServiceType.SPARK_PLUGS -> Icons.Default.Bolt
    ServiceType.TIMING_BELT -> Icons.Default.Loop
    ServiceType.WIPER_BLADES -> Icons.Default.WaterDrop
    ServiceType.INSURANCE -> Icons.Default.Shield
    ServiceType.REGISTRATION -> Icons.Default.Description
    ServiceType.INSPECTION -> Icons.AutoMirrored.Filled.FactCheck
    ServiceType.EMISSIONS_TEST -> Icons.Default.Co2
    ServiceType.CUSTOM -> Icons.Default.Build
}
