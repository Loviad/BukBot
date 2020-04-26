package com.example.bukbot.service.events

import com.example.bukbot.data.oddsList.PinOdd

interface VoddsInsertOddEvent:VoddsEvents {
    suspend fun onInsertOdds(odds: List<PinOdd>)
    suspend fun onPinDownEvent(events: List<String>)
}