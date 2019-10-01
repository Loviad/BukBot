package com.example.bukbot.data.telegram.models.loginInfo

import com.example.bukbot.data.telegram.models.IMessageData

class LoginInfo(val user: String, val pass: String, private val pageId: String): IMessageData {
    override val type: String
        get() = "login"

    fun getpageId(): String {
        return pageId
    }
}