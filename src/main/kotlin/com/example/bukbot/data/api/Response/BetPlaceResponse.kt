package com.example.bukbot.data.api.Response

data class BetPlaceResponse(
        val actionMessage: String?,
        val actionStatus: Int?,
        val username: String?,
        val id: String?,
        val betStatus: String?,
        val betAmount: Float?,
        val betOdd: Float?,
        val reqId: String?
)