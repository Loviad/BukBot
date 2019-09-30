package com.example.bukbot.service.browser.events

interface TelegramEventListener: BrowserEventListener {
    fun pushMessage(test:String)
}