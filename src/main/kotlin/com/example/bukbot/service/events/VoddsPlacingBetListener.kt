package com.example.bukbot.service.events

import com.example.bukbot.data.SSEModel.PlacingBet

interface VoddsPlacingBetListener: VoddsEvents {
    fun onPlacingBet(item: PlacingBet)
}