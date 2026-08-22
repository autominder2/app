package com.autominder.app.ui.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Locale-aware thousands grouping for distance/odometer display values.
 * [com.autominder.app.domain.util.DistanceUtil] stays unit-conversion only —
 * grouping is a presentation concern and lives here instead.
 */
object DistanceFormat {
    fun grouped(value: Int, locale: Locale = Locale.getDefault()): String =
        NumberFormat.getIntegerInstance(locale).format(value)
}
