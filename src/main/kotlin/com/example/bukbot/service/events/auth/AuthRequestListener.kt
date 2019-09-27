package com.example.bukbot.service.events.auth

interface AuthRequestListener: AuthEventListener {
    fun requestAuth()
}