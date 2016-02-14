package com.dhsdevelopments.potato

import android.content.Context
import java.text.DateFormat
import java.text.MessageFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateHelper {
    private val inputFormat: DateFormat by lazy {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        inputFormat
    }

    private val dateTimeOutputFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    }

    @Synchronized fun parseDate(text: String): Date {
        try {
            return inputFormat.parse(text)
        }
        catch (e: ParseException) {
            throw IllegalArgumentException("illegal date format: " + text, e)
        }

    }

    @Synchronized fun formatDateTimeOutputFormat(date: Date): String {
        return dateTimeOutputFormat.format(date)
    }

    companion object {
        val SECOND_MILLIS: Long = 1000
        val MINUTE_MILLIS = SECOND_MILLIS * 60
        val HOUR_MILLIS = MINUTE_MILLIS * 60
        val DAY_MILLIS = HOUR_MILLIS * 24

        fun makeDateDiffString(context: Context, date: Long): String {
            val resources = context.resources

            val diffInMillis = System.currentTimeMillis() - date

            if (diffInMillis <= MINUTE_MILLIS) {
                return resources.getString(R.string.dates_less_than_one_minute_ago)
            }
            else if (diffInMillis <= HOUR_MILLIS) {
                val mins = diffInMillis / MINUTE_MILLIS
                val fmt = resources.getString(R.string.date_number_minutes_ago)
                return MessageFormat.format(fmt, mins)
            }
            else if (diffInMillis <= DAY_MILLIS) {
                val hours = diffInMillis / HOUR_MILLIS
                val fmt = resources.getString(R.string.date_number_hours_ago)
                return MessageFormat.format(fmt, hours)
            }
            else {
                val days = diffInMillis / DAY_MILLIS
                val fmt = resources.getString(R.string.date_number_days_ago)
                return MessageFormat.format(fmt, days)
            }
        }
    }
}
