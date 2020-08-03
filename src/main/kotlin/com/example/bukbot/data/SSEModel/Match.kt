package com.example.bukbot.data.SSEModel

data class Match(
        val id: String,
        val host: String,
        val guest: String,
        val league: String,
        val startTime: Long
)