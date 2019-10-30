package com.example.bukbot.utils

import com.example.bukbot.service.events.IBetPlacingListener
import com.example.bukbot.service.events.IGettingSnapshotListener
import com.example.bukbot.service.events.SettingEvents
import org.springframework.stereotype.Component


@Component
class Settings {
    private var BET_PLACING: Boolean = false                                                                            //делать ставки
    private var GETTING_SNAPSHOT: Boolean = false                                                                       //запрашивать события с VODDS

    private val eventListener = ArrayList<SettingEvents>()

    fun setBetPlacing(value: Boolean){
        BET_PLACING = value
        sendEvents<IBetPlacingListener> {
            it.onChangeBetPlace(value)
        }
    }

    fun getBetPlacing(): Boolean = BET_PLACING

    fun setGettingSnapshot(value: Boolean){
        GETTING_SNAPSHOT = value
        sendEvents<IGettingSnapshotListener> {
            it.onGettingSnapshotChange(value)
        }
        if (!value){
            setBetPlacing(value)
        }
    }

    fun getGettingSnapshot(): Boolean = GETTING_SNAPSHOT

    fun addSettingsEventListener(listener: SettingEvents){
        eventListener.add(listener)
    }

    fun removeAuthEventListener(listener: SettingEvents){
        eventListener.remove(listener)
    }

    private inline fun <reified TEvent : SettingEvents> sendEvents(noinline sender: (TEvent) -> Unit) {
        eventListener.filterIsInstance<TEvent>().forEach { sender(it) }
    }
}