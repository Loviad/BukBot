package com.example.bukbot.service.auth.events

interface AuthRequestListener: AuthEventListener {
    fun requestAuth()
}