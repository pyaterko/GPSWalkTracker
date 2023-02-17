package com.example.gpswalktracker.utils

import android.annotation.SuppressLint
import android.icu.util.Calendar
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    @SuppressLint("SimpleDateFormat")
    private val timeFormatter = SimpleDateFormat("HH:mm:ss")

    fun getTime(time: Long): String {
        val calendar = Calendar.getInstance()
        timeFormatter.timeZone= TimeZone.getTimeZone("UTC")
        calendar.timeInMillis = time
        return timeFormatter.format(calendar.time)
    }
}