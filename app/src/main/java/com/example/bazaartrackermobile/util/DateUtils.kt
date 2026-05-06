package com.example.bazaartrackermobile.util

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun getRelativeTimeSpan(time: Long): CharSequence {

        return DateUtils.getRelativeTimeSpanString(
            time,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
    }

    fun formatDateTime(dateString: String?): String {

        if (dateString.isNullOrEmpty()) {
            return ""
        }

        val timestamp = parseDate(dateString)

        if (timestamp == 0L) {
            return dateString
        }

        val formatter = SimpleDateFormat(
            "MMM dd, yyyy",
            Locale.getDefault()
        )

        return formatter.format(Date(timestamp))
    }

    fun formatToApiDate(calendar: Calendar): String {

        val formatter = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )

        return formatter.format(calendar.time)
    }

    fun parseDate(dateString: String?): Long {

        if (dateString.isNullOrEmpty()) {
            return 0L
        }

        return try {

            // HANDLE:
            // Mon May 04 14:15:28 IST 2026

            val cleanedDate = dateString
                .replace("IST", "+0530")

            val parser = SimpleDateFormat(
                "EEE MMM dd HH:mm:ss Z yyyy",
                Locale.ENGLISH
            )

            parser.parse(cleanedDate)?.time ?: 0L

        } catch (e: Exception) {

            try {

                val parser = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.ENGLISH
                )

                parser.parse(dateString)?.time ?: 0L

            } catch (e: Exception) {

                0L
            }
        }
    }
    fun getStartOfToday(): Calendar {

        return Calendar.getInstance().apply {

            set(Calendar.HOUR_OF_DAY, 0)

            set(Calendar.MINUTE, 0)

            set(Calendar.SECOND, 0)

            set(Calendar.MILLISECOND, 0)
        }
    }

    fun getStartOfWeek(): Calendar {

        val cal = getStartOfToday()

        while (
            cal.get(Calendar.DAY_OF_WEEK)
            != cal.firstDayOfWeek
        ) {

            cal.add(Calendar.DAY_OF_MONTH, -1)
        }

        return cal
    }

    fun getStartOfMonth(): Calendar {

        return getStartOfToday().apply {

            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
}