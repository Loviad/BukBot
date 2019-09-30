package com.example.bukbot.service.browser

interface IBrowserInterractor {
    fun onChangeState(state: String)
    fun getState(): WebBrowser.State
    fun pushTelegramMessage(s: String)
}