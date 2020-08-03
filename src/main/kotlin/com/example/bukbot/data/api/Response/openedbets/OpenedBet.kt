package com.example.bukbot.data.api.Response.openedbets

class OpenedBet(
        val reqId: String?,
        val actionStatus: Int?,
        val actionMessage: String?,
        val totalResults: Int?,
        val betInfos: Array<BetInfo>?
)