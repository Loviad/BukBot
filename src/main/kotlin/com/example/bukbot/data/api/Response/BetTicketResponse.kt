package com.example.bukbot.data.api.Response

data class BetTicketResponse(
        val actionStatus: String?,
        val actionMessage: String?,
        val reqId: String?,
        val currentOdd: Float?,
        val minStake: Float?,
        val maxStake: Float?,
        val pivotValue: String?,
        val homeScore: Int?,
        val awayScore: Int?
)