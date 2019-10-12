package com.example.bukbot.data.database.Dao

import com.example.bukbot.utils.DatePatterns.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND
import org.joda.time.DateTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class ValueBetsItem(
        @Id
        val id: String,
        var percent: Double,
        val bookmaker: String,
        val sport: String,
        val time: String,
        val home: String,
        val guest: String,
        val league: String,
        val event: String,
        val koef: Double,
        dateTime: DateTime
) {
    var work: Boolean = false
    private val hash: Int

    val dateTime: String = dateTime.toString(YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)

    init {
        hash = this.hashCode()
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + percent.hashCode()
        result = 31 * result + bookmaker.hashCode()
        result = 31 * result + sport.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + home.hashCode()
        result = 31 * result + guest.hashCode()
        result = 31 * result + league.hashCode()
        result = 31 * result + event.hashCode()
        result = 31 * result + koef.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ValueBetsItem

        if (id != other.id) return false
        if (percent != other.percent) return false
        if (bookmaker != other.bookmaker) return false
        if (sport != other.sport) return false
        if (time != other.time) return false
        if (home != other.home) return false
        if (guest != other.guest) return false
        if (league != other.league) return false
        if (event != other.event) return false
        if (koef != other.koef) return false
        if (dateTime != other.dateTime) return false

        return true
    }
}