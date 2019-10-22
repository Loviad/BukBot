package com.example.bukbot.data.database.Dao

import org.springframework.data.annotation.Id

class Bet(
        @Id
        val betId: String,
        val idEvent: String,
        val idOdd: Int,
        val source: String,
        val timeType: String,
        val typePivot: String,
        val pivotBias: String,
        val pivotValue: Double,
        val rateOver: Double,
        val rateUnder: Double,
        val rateEqual: Double,
        val createdTime: Long,
        val oddType: String,
        val status: String
)