package com.example.bukbot.service.browser.events

interface ParsingEventListener: BrowserEventListener {
    fun onChangeStateBrowser(state: String)
    fun showInfoMessage(s: String)
}