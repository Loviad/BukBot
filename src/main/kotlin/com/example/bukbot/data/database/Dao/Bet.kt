package com.example.bukbot.data.database.Dao

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Bet(
        @Id
        val id: String,
        val eventId: String,
        val homeTeam: String,
        val awayTeam: String,
        val betStake: Double,
        val returnAmount: Double,
        val pivotBias: String,
        val serverPivot: Double,
        val league: String,
        val serverOdd: Double,
        val targetType: String,
        val betStatus: Int,
        val betTime: String,
        val settledTime: String
)