package com.example.bukbot.domain.interactors.browser

import com.example.bukbot.controller.browser.WebBrowser
import com.example.bukbot.data.database.Dao.ValueBetsItem

interface IBrowserInterractor {
    fun onChangeState(state: String)
    fun getState(): WebBrowser.State
    fun pushTelegramMessage(s: String)
    fun saveValuebetsItem(list: List<ValueBetsItem>)
}