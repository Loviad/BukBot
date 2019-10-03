package com.example.bukbot.controller.browser.events

import com.example.bukbot.controller.browser.WebBrowser.State
import com.example.bukbot.data.database.Dao.ValueBetsItem

interface ParsingEventListener: BrowserEventListener {
    fun onChangeStateBrowser(state: State)
    fun showInfoMessage(s: String)
    fun onValueBetsItemsUpdate(list:List<ValueBetsItem>)
}