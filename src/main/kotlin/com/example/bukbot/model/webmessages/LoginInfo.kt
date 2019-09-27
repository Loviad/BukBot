package com.example.bukbot.model.webmessages

class LoginInfo(val user: String, val pass: String, private val pageId: String): IMessageData{
    override val type: String
        get() = "login"

    fun getpageId(): String {
        return pageId
    }
}