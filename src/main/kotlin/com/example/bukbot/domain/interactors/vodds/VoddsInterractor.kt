package com.example.bukbot.domain.interactors.vodds

import com.example.bukbot.controller.placeBet.BetItemValueRepository
import com.example.bukbot.data.SSEModel.PlacingBet
import com.example.bukbot.service.events.VoddsEvents
import com.example.bukbot.service.events.VoddsFailureBetListener
import com.example.bukbot.service.events.VoddsPlacingBetListener
import com.example.bukbot.service.events.VoddsSnapshotListener
import com.example.bukbot.service.rest.ApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class VoddsInterractor {

    @Autowired
    private lateinit var api: ApiClient

    @Autowired
    private lateinit var plBet: BetItemValueRepository

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

    fun containsId(id: String): Boolean{
        return plBet.containsId(id)
    }

    fun onPlaceBet(item: PlacingBet) {
        sendEvents<VoddsPlacingBetListener> {
            it.onPlacingBet(item)
        }
    }

    fun onFailureBet(txt: String) {
        sendEvents<VoddsFailureBetListener> {
            it.onFailureBet(txt)
        }
    }
}