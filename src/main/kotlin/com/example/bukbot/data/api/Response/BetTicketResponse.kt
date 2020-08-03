package com.example.bukbot.data.api.Response

data class BetTicketResponse(
        val actionStatus: Int?,
        val actionMessage: String?,
        val reqId: String?,
        val currentOdd: Float?,
        val minStake: Float?,
        val maxStake: Float?,
        val pivotValue: Float?,
        val homeScore: Int?,
        val awayScore: Int?,
        var sportBook: String? = null
)