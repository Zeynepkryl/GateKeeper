package com.zeynep.gatekeeper.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {

    private const val TIME_PATTERN = "HH:mm:ss.SSS"

    private val timeFormat: SimpleDateFormat
        get() = SimpleDateFormat(TIME_PATTERN, Locale.getDefault())

    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))
}
