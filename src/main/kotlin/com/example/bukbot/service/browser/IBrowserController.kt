package com.example.bukbot.service.browser

interface IBrowserController {
    fun getCurrentState() : WebBrowser.State
}