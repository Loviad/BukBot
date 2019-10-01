package com.example.bukbot.controller.browser.events

interface ParsingEventListener: BrowserEventListener {
    fun onChangeStateBrowser(state: String)
    fun showInfoMessage(s: String)
}