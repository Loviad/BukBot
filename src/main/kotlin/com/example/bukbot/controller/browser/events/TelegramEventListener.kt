package com.example.bukbot.controller.browser.events

interface TelegramEventListener: BrowserEventListener {
    fun pushMessage(test:String)
}