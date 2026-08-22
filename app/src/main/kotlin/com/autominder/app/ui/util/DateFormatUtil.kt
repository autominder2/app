package com.autominder.app.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Thread-safe date formatting utilities.
 *
 * SimpleDateFormat is NOT thread-safe, so we use ThreadLocal to give each
 * thread its own instance. Locale.getDefault() is intentionally resolved at
 * call time (not at class-load time) so locale changes while the app is
 * running are respected — fixes the ConstantLocale lint warning.
 */
object DateFormatUtil {

    private val shortFormatLocal = ThreadLocal.withInitial<SimpleDateFormat> {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    }

    private val fullFormatLocal = ThreadLocal.withInitial<SimpleDateFormat> {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    }

    fun formatDate(millis: Long): String =
        shortFormatLocal.get()!!.format(Date(millis))

    fun formatDateTime(millis: Long): String =
        fullFormatLocal.get()!!.format(Date(millis))
}
