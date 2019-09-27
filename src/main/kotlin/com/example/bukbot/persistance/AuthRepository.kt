package com.example.bukbot.persistance

import com.example.bukbot.controller.PageController
import com.example.bukbot.model.webmessages.LoginInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AuthRepository {
    @Autowired
    private lateinit var authController: PageController

    fun sendLogin(loginItem: LoginInfo){
        authController.sendLogin(loginItem)
    }
}