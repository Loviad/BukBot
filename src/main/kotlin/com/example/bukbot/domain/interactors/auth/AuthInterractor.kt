package com.example.bukbot.domain.interactors.auth

import com.example.bukbot.data.database.Dao.ApprovedUsers
import com.example.bukbot.data.repositories.ApprovedUserRepository
import com.example.bukbot.data.telegram.models.loginInfo.LoginInfo
import com.example.bukbot.data.repositories.AuthRepository
import com.example.bukbot.service.auth.events.AuthEventListener
import com.example.bukbot.service.auth.events.AuthRequestListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AuthInterractor {
    @Autowired
    private lateinit var authRepository: AuthRepository
    @Autowired
    private lateinit var approvedUserRepository: ApprovedUserRepository

    private val eventListener = ArrayList<AuthEventListener>()

    fun addAuthEventListener(listener: AuthEventListener){
        eventListener.add(listener)
    }
    fun removeAuthEventListener(listener: AuthEventListener){
        eventListener.remove(listener)
    }

    fun findByChatId(chatId: String): ApprovedUsers {
        return approvedUserRepository.findByChatId(chatId)
    }

    fun sendLogin(loginInfo: LoginInfo) {
        authRepository.sendLogin(loginInfo)
    }

    fun sendAuthRequest() {
        sendEvents<AuthRequestListener> {
            it.requestAuth()
        }
    }
    private inline fun <reified TEvent : AuthEventListener> sendEvents(noinline sender: (TEvent) -> Unit) {
        eventListener.filterIsInstance<TEvent>().forEach { sender(it) }
    }

}
