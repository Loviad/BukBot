package com.example.bukbot.utils

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

object DateTimeUtils {
    fun calcDelta(gmtServerDate: String?, currentTime: Long): Long {
        if (gmtServerDate.isNullOrEmpty()) {
            return 0
        }

        val serverDateTime =
                DateTimeFormat.forPattern(DatePatterns.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND).withZone(
                        DateTimeZone.forID("GMT")
                ).parseDateTime(gmtServerDate)
        return currentTime - serverDateTime.millis
    }

    fun correctTimeFromXmppTimestamp(timestamp: Long): DateTime {
        return DateTime(timestamp * 1_000)
                .withZone(DateTimeZone.UTC)
                .plusMillis(
                        (System.currentTimeMillis() % 10_000 / 10).toInt() /*оставляем секунды и ее десятую и сотую долю*/
                )
    }

    fun isTheSameDay(dayOne: DateTime, dayTwo: DateTime): Boolean {
        return dayOne.dayOfMonth == dayTwo.dayOfMonth &&
                dayOne.monthOfYear == dayTwo.monthOfYear &&
                dayOne.year == dayTwo.year
    }
}

fun String?.toDateTime(pattern: String, trimMillis: Boolean = false): DateTime? {
    val patternD = DateTimeFormat.forPattern(pattern).withZoneUTC()
    return try {
        if (this == null || this.isEmpty()) {
            null
        } else if (this.contains(".") && trimMillis) {
            patternD.parseDateTime(this.substringBefore("."))
        } else {
            patternD.parseDateTime(this)
        }
    } catch (e: Exception) {
        null
    }
}