package com.example.bukbot.service.events

import com.example.bukbot.data.SSEModel.PlacingBet
import com.example.bukbot.data.oddsList.PinOdd

interface VoddsInsertOddEvent:VoddsEvents {
    suspend fun onInsertOdds(odds: List<PinOdd>)
    suspend fun onPlaceEvent(event: PlacingBet)
}