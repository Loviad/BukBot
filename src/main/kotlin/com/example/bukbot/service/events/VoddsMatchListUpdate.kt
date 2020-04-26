package com.example.bukbot.service.events

import com.example.bukbot.data.SSEModel.MatchCrop

interface VoddsMatchListUpdate: VoddsEvents {
    suspend fun onMatchesUpdate(matches: List<MatchCrop>)
}