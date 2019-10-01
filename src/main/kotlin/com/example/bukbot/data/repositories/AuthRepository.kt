package com.example.bukbot.data.repositories

import com.example.bukbot.controller.page.PageController
import com.example.bukbot.data.telegram.models.loginInfo.LoginInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AuthRepository {
    @Autowired
    private lateinit var authController: PageController

    fun sendLogin(loginItem: LoginInfo){
        authController.sendLogin(loginItem)
    }

    fun getCurrentUser(){

    }
}