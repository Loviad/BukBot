package com.example.bukbot.data.api

data class Balance(
        val reqId: String?,
        val actionStatus: Int?,
        val actionMessage: String?,
        val username: String?,
        val credit: Double?,
        val pl:Double?,
        val outstanding: Double?,
        val currency: String?
)
