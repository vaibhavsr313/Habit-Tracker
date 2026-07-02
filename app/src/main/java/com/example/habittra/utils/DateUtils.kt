package com.example.habittra.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val storageFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFmt  = SimpleDateFormat("d MMM",     Locale.getDefault())
    private val dayNumFmt   = SimpleDateFormat("d",         Locale.getDefault())

    fun todayString(): String = storageFmt.format(Date())

    fun todayDisplayString(): String = toDisplayString(todayString())

    // Returns list of Triple(dayLabel, dayNumber, dateString) for Mon–Sun
    fun currentWeekDates(): List<Triple<String, String, String>> {
        val cal = Calendar.getInstance()

        // Shift back to Monday of this week
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val shift = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_MONTH, -shift)

        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return dayNames.map { name ->
            Triple(name, dayNumFmt.format(cal.time), storageFmt.format(cal.time))
                .also { cal.add(Calendar.DAY_OF_MONTH, 1) }
        }
    }

    // Returns last 365 days as "yyyy-MM-dd" strings, oldest first
    fun last365Days(): List<String> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -364)
        return (0 until 365).map {
            storageFmt.format(cal.time).also { cal.add(Calendar.DAY_OF_MONTH, 1) }
        }
    }

    fun toDisplayString(dateStr: String): String {
        val date = storageFmt.parse(dateStr) ?: return dateStr
        val dayMonth = ordinalDate(date)  // "1st Jul"
        val isToday  = dateStr == todayString()
        return if (isToday) "Today, $dayMonth"
        else "${SimpleDateFormat("EEEE", Locale.getDefault()).format(date)}, $dayMonth"
    }

    // Converts date → "1st Jul", "22nd Dec" etc
    private fun ordinalDate(date: java.util.Date): String {
        val cal = java.util.Calendar.getInstance().apply { time = date }
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val suffix = when {
            day in 11..13        -> "th"
            day % 10 == 1        -> "st"
            day % 10 == 2        -> "nd"
            day % 10 == 3        -> "rd"
            else                 -> "th"
        }
        val month = SimpleDateFormat("MMM", Locale.getDefault()).format(date)
        return "$day$suffix $month"
    }
}