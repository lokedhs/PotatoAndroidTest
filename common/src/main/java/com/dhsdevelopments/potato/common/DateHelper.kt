package com.dhsdevelopments.potato.common

import android.content.Context
import java.text.DateFormat
import java.text.MessageFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateHelper {
    private val inputFormat: DateFormat by lazy {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        format.timeZone = TimeZone.getTimeZone("UTC")
        format
    }

    private val dateTimeOutputFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    }

    @Synchronized
    fun parseDate(text: String): Date {
        return try {
            inputFormat.parse(text)
        }
        catch (e: ParseException) {
            throw IllegalArgumentException("illegal date format: $text", e)
        }
    }

    @Synchronized
    fun formatDateTimeOutputFormat(date: Date): String = dateTimeOutputFormat.format(date)

    companion object {
        const val SECOND_MILLIS: Long = 1000
        const val MINUTE_MILLIS = SECOND_MILLIS * 60
        const val HOUR_MILLIS = MINUTE_MILLIS * 60
        const val DAY_MILLIS = HOUR_MILLIS * 24

        fun makeDateDiffString(context: Context, date: Long): String {
            val resources = context.resources

            val diffInMillis = System.currentTimeMillis() - date

            return when {
                diffInMillis <= MINUTE_MILLIS -> {
                    resources.getString(R.string.dates_less_than_one_minute_ago)
                }
                diffInMillis <= HOUR_MILLIS -> {
                    val mins = diffInMillis / MINUTE_MILLIS
                    val fmt = resources.getString(R.string.date_number_minutes_ago)
                    MessageFormat.format(fmt, mins)
                }
                diffInMillis <= DAY_MILLIS -> {
                    val hours = diffInMillis / HOUR_MILLIS
                    val fmt = resources.getString(R.string.date_number_hours_ago)
                    MessageFormat.format(fmt, hours)
                }
                else -> {
                    val days = diffInMillis / DAY_MILLIS
                    val fmt = resources.getString(R.string.date_number_days_ago)
                    MessageFormat.format(fmt, days)
                }
            }
        }
    }
}
