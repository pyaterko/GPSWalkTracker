package com.example.gpswalktracker.utils

import android.annotation.SuppressLint
import android.icu.util.Calendar
import java.text.SimpleDateFormat
import java.util.*
@SuppressLint("SimpleDateFormat")
object DateUtils {

    private val timeFormatter = SimpleDateFormat("HH:mm:ss")
    private val dateFormatter = SimpleDateFormat("dd/MM/yyy HH:mm")

    fun getTime(time: Long): String {
        val calendar = Calendar.getInstance()
        timeFormatter.timeZone= TimeZone.getTimeZone("UTC")
        calendar.timeInMillis = time
        return timeFormatter.format(calendar.time)
    }

    fun getDate(): String {
        val calendar = Calendar.getInstance()
        timeFormatter.timeZone= TimeZone.getTimeZone("UTC")
        return dateFormatter.format(calendar.time)
    }
}