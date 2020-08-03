package com.example.bukbot.service.events

import com.example.bukbot.data.SSEModel.Match

interface VoddsMatchListUpdate: VoddsEvents {
    suspend fun onMatchesUpdate(matches: List<Match>)
}