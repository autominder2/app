package com.autominder.app.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatUtil {
    private val shortFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val fullFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    fun formatDate(millis: Long): String = shortFormat.format(Date(millis))
    fun formatDateTime(millis: Long): String = fullFormat.format(Date(millis))
}
