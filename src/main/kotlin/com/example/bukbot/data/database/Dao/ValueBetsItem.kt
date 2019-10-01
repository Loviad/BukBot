package com.example.bukbot.data.database.Dao

import org.joda.time.DateTime
import org.springframework.data.annotation.Id

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
        val dateTime: DateTime
) {

    val hash: Int

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
}