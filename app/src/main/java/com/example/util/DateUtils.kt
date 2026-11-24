package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    private val displayDateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US)
    private val monthTitleFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    fun newId(): String = UUID.randomUUID().toString()

    fun todayIso(): String = synchronized(isoFormat) {
        isoFormat.format(Date())
    }

    fun currentMonthIso(): String = synchronized(monthFormat) {
        monthFormat.format(Date())
    }

    fun formatDisplayDate(date: Date = Date()): String = synchronized(displayDateFormat) {
        displayDateFormat.format(date)
    }

    fun formatMonthTitle(yearMonth: String): String {
        return try {
            val parsed = synchronized(isoFormat) { isoFormat.parse("$yearMonth-01") }
            if (parsed != null) {
                synchronized(monthTitleFormat) { monthTitleFormat.format(parsed) }
            } else yearMonth
        } catch (_: Exception) {
            yearMonth
        }
    }

    fun startOfCurrentMonthIso(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return synchronized(isoFormat) { isoFormat.format(cal.time) }
    }

    fun millisToIsoDate(millis: Long): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return fmt.format(Date(millis))
    }

    fun isoDateToMillis(iso: String): Long? {
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            fmt.parse(iso)?.time
        } catch (_: Exception) {
            null
        }
    }

    fun formatNumber(value: Int): String = NumberFormat.getNumberInstance(Locale.US).format(value)
    fun formatNumber(value: Long): String = NumberFormat.getNumberInstance(Locale.US).format(value)
}
