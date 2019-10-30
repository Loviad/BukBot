package com.example.bukbot.domain.interactors.vodds

import com.example.bukbot.data.SSEModel.PlacingBet
import com.example.bukbot.data.database.Dao.PlacedBet
import com.example.bukbot.service.events.VoddsEvents
import com.example.bukbot.service.events.VoddsPlacingBetListener
import com.example.bukbot.service.events.VoddsSnapshotListener
import com.example.bukbot.service.rest.ApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class VoddsInterractor {

    @Autowired
    private lateinit var api: ApiClient

    private val eventListener = ArrayList<VoddsEvents>()

    fun addEventListener(listener: VoddsEvents){
        eventListener.add(listener)
    }

    fun removeEventListener(listener: VoddsEvents){
        eventListener.remove(listener)
    }

    private inline fun <reified TEvent : VoddsEvents> sendEvents(noinline sender: (TEvent) -> Unit) {
        eventListener.filterIsInstance<TEvent>().forEach { sender(it) }
    }

    fun startParsing(){
        sendEvents<VoddsSnapshotListener> {
            it.onStartSnapshot()
        }
    }

    fun stopParsing(){
        sendEvents<VoddsSnapshotListener> {
            it.onStopSnapshot()
        }
    }

    fun getBalance(){
        api.getCreditBalance()
    }

    fun onPlaceBet(item: PlacingBet) {
        sendEvents<VoddsPlacingBetListener> {
            it.onPlacingBet(item)
        }
    }
}