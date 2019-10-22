package com.example.bukbot.data.api

data class Balance(
        val actionMessage: String?,
        val username: String?,
        val credit: Double?,
        val currency: String?,
        val actionStatus: Int?,
        val reqId: String?
)
