package com.example.bukbot.domain.interactors.browser

import com.example.bukbot.controller.browser.WebBrowser.State
import com.example.bukbot.controller.browser.events.BrowserEventListener
import com.example.bukbot.data.database.Dao.ValueBetsItem

interface IBrowserInterractor {
    fun addEventListener(listener: BrowserEventListener)
    fun removeEventListener(listener: BrowserEventListener)
    fun onChangeState(state: State)
    fun getState(): State
    fun pushTelegramMessage(s: String)
    fun saveValuebetsItem(list: List<ValueBetsItem>)
}