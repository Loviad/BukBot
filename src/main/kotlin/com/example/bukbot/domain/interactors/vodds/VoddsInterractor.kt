package com.example.bukbot.domain.interactors.vodds

import com.example.bukbot.data.SSEModel.ConsoleMessage
import com.example.bukbot.data.SSEModel.PlacingBet
import com.example.bukbot.data.SSEModel.Match
import com.example.bukbot.data.oddsList.PinOdd
import com.example.bukbot.service.events.*
import org.springframework.stereotype.Component
import java.util.HashMap

@Component
class VoddsInterractor {

    private val eventListener = ArrayList<VoddsEvents>()

    fun addEventListener(listener: VoddsEvents){
        eventListener.add(listener)
    }

    fun removeEventListener(listener: VoddsEvents){
        eventListener.remove(listener)
    }

    private suspend inline fun <reified TEvent : VoddsEvents> sendEvents(noinline sender: suspend (TEvent) -> Unit) {
        eventListener.filterIsInstance<TEvent>().forEach { sender(it) }
    }

    suspend fun startParsing(){
        sendEvents<VoddsSnapshotListener> {
            it.onStartSnapshot()
        }
    }

    suspend fun stopParsing(){
        sendEvents<VoddsSnapshotListener> {
            it.onStopSnapshot()
        }
    }

    suspend fun onPlaceBet(item: PlacingBet) {
        sendEvents<VoddsPlacingBetListener> {
            it.onPlacingBet(item)
        }
    }

    suspend fun onFailureBet(txt: String) {
        sendEvents<VoddsFailureBetListener> {
            it.onFailureBet(txt)
        }
    }

    suspend fun changePinList(pinOddList: HashMap<String, PinOdd>) {
        sendEvents<VoddsInsertOddEvent> {
            it.onInsertOdds(
                    pinOddList.map {pin ->
                        pin.value
                    }
            )
        }
    }

    suspend fun placeEvent(bet: PlacingBet) {
        sendEvents<VoddsInsertOddEvent> {
            it.onPlaceEvent(bet)
        }
    }

    suspend fun changeMatchList(matchList: HashMap<String, Match>) {
        sendEvents<VoddsMatchListUpdate> {
            it.onMatchesUpdate(
                    matchList.map { match ->
                        match.value
                    }
            )
        }
    }
}