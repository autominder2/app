package com.autominder.app.ui.util

import androidx.annotation.DrawableRes
import com.autominder.app.R
import com.autominder.app.domain.model.VehicleBodyType

/**
 * Maps a [VehicleBodyType] to its corresponding side-profile vector drawable.
 *
 * This lives in the UI layer intentionally — the domain [VehicleBodyType]
 * carries no Android resource references, keeping it pure-Kotlin and testable.
 */
@DrawableRes
fun VehicleBodyType.toDrawableRes(): Int = when (this) {
    VehicleBodyType.SEDAN       -> R.drawable.ic_body_sedan
    VehicleBodyType.SUV         -> R.drawable.ic_body_suv
    VehicleBodyType.TRUCK       -> R.drawable.ic_body_truck
    VehicleBodyType.COUPE       -> R.drawable.ic_body_coupe
    VehicleBodyType.HATCHBACK   -> R.drawable.ic_body_hatchback
    VehicleBodyType.CONVERTIBLE -> R.drawable.ic_body_convertible
    VehicleBodyType.MINIVAN     -> R.drawable.ic_body_minivan
    VehicleBodyType.MOTORCYCLE  -> R.drawable.ic_body_motorcycle
}
