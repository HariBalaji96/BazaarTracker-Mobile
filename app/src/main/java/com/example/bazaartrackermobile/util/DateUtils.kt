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

    fun formatDateTime(dateString: String): String {
        // Assuming ISO format from API, convert to readable
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val date = parser.parse(dateString)
            if (date != null) formatter.format(date) else dateString
        } catch (e: Exception) {
            dateString
        }
    }
}
