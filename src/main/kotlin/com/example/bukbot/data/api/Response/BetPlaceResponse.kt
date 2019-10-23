package com.example.bukbot.data.api.Response

data class BetPlaceResponse(
        val actionMessage: String?,
        val actionStatus: Int?,
        val username: String?,
        val id: String?,
        val betStatus: Int?,
        val betAmount: Double?,
        val betOdd: Double?,
        val reqId: String?
)