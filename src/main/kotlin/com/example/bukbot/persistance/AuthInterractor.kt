package com.example.bukbot.persistance

import com.example.bukbot.domain.ApprovedUsers
import com.example.bukbot.model.webmessages.LoginInfo
import com.example.bukbot.service.events.auth.AuthEventListener
import com.example.bukbot.service.events.auth.AuthRequestListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

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