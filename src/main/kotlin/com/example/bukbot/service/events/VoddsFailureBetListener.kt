package com.example.bukbot.service.events

interface VoddsFailureBetListener: VoddsEvents {
    fun onFailureBet(txt: String)
}