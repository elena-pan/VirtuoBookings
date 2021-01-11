package com.virtuobookings.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.ColorRes
import java.text.SimpleDateFormat
import java.time.*
import java.time.temporal.WeekFields
import java.util.*

// Set color
internal fun TextView.setTextColorRes(@ColorRes color: Int) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    setTextColor(context.getColor(color))
} else {
    setTextColor(resources.getColor(color))
}

/**
 * Take the Long milliseconds and convert it to a nicely formatted string for display.
 * Display in GMT time
 *
 * MM/dd/yyyy - Month, day, and full year numerically
 * HH:mm - Hours and minutes in 24hr format
 */
@SuppressLint("SimpleDateFormat")
fun convertLongToDateString(systemTime: Long): String {
    val gmtZoneId = ZoneId.of("Etc/GMT").id
    val pattern = SimpleDateFormat("MM/dd/yyyy HH:mm")
    pattern.timeZone = TimeZone.getTimeZone(gmtZoneId)
    return pattern.format(systemTime).toString()
}

/**
 * Take the Long milliseconds and convert it to a nicely formatted string for display.
 * Display in GMT time
 *
 * HH:mm - Hours and minutes in 24hr format
 */
@SuppressLint("SimpleDateFormat")
fun convertLongToTimeString(systemTime: Long): String {
    val gmtZoneId = ZoneId.of("Etc/GMT").id
    val pattern = SimpleDateFormat("HH:mm")
    pattern.timeZone = TimeZone.getTimeZone(gmtZoneId)
    return pattern.format(systemTime).toString()
}

/**
 * Take the Long milliseconds and convert it to a LocalDateTime
 * Everything is in UTC - don't convert timezones
 */
fun convertLongToLocalDateTime(systemTime: Long): LocalDateTime {
    return LocalDateTime.ofInstant(
        Instant.ofEpochMilli(systemTime), ZoneId.of("Etc/GMT"))
}

/**
 * Convert local date and local time to Long millis
 * Everything is in UTC - don't convert timezones
 */
fun convertDateTimeToLong(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int): Long {
    val zonedDateTime = ZonedDateTime.of(year, month, dayOfMonth, hour, minute, 0, 0, ZoneId.of("Etc/GMT"))
    return zonedDateTime.toInstant().toEpochMilli()
}

// Check if device is connected to network
fun isNetworkConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        }
        return false
    } else {
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}

// Hide keyboard if no items have focus
fun hideKeyboard(activity: Activity) {
    val inputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    // Check if no view has focus
    val currentFocusedView = activity.currentFocus
    currentFocusedView?.let {
        inputMethodManager.hideSoftInputFromWindow(
            currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}


fun daysOfWeekFromLocale(): Array<DayOfWeek> {
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    var daysOfWeek = DayOfWeek.values()
    // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
    // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
    if (firstDayOfWeek != DayOfWeek.MONDAY) {
        val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
        val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
        daysOfWeek = rhs + lhs
    }
    return daysOfWeek
}

