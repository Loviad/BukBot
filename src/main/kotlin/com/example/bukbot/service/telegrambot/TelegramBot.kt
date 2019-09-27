package com.example.bukbot.service.telegrambot

import com.example.bukbot.domain.ApprovedUsers
import com.example.bukbot.model.webmessages.LoginInfo
import com.example.bukbot.persistance.AuthInterractor
import com.example.bukbot.persistance.TelegramInterractor
import com.example.bukbot.service.events.auth.AuthRequestListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.lang.Exception
import javax.annotation.PostConstruct
import kotlin.collections.HashMap


//val sendMessage = SendMessage()
//sendMessage.enableMarkdown(true)
//sendMessage.chatId = message.chatId.toString()
//sendMessage.text = if(user.chatId == message.chatId.toString() &&
//user.firstName == message.chat.firstName &&
//user.lastName == message.chat.lastName) {
//    "Привет"
//} else {"Я тебя не знаю"}
//try {
//    execute(sendMessage)
//} catch (e: Exception) {
//
//}

class TelegramBot(val authInterractor: AuthInterractor, val telegramInterractor: TelegramInterractor): TelegramLongPollingBot(), AuthRequestListener {
    private var approvedUsersList = HashMap<String, ApprovedUsers>()
    fun setApprovedList(list: List<ApprovedUsers>){

    }

    init {
        authInterractor.addAuthEventListener(this)
        telegramInterractor.findAllApprovedUsers().map {
            approvedUsersList[it.chatId] = it
        }

//        repository.save(ApprovedUsers(
//                "984717325",
//                true,
//                UUID.randomUUID().toString(),
//                "Golushkov",
//                true,
//                true,
//                true,
//                "Sergey"
//
//        ))
    }

    override fun onUpdateReceived(update: Update?) {
        val message = update?.message
        message?.let{
            if (!checkApproved(message)) return
            message.entities?.firstOrNull()?.let {
                if (it.type == "bot_command"){
                    when(val command = message.text.substring(it.offset, it.offset + it.length)){
                        "/approved" -> sendTxtMessage(message, "Привет")
                        "/login" -> approvedLogin(message, IntRange(it.offset, it.offset + it.length))
                        else -> sendTxtMessage(message, " Я не понимаю: ${command}")
                    }
                }
            } ?: sendTxtMessage(message, "Введите команду")
        }
    }

    fun approvedLogin(message: Message, range: IntRange) {
        val user = telegramInterractor.findByChatId(message.chatId.toString())
        authInterractor.sendLogin(LoginInfo(user.chatId, user.password, message.text.removeRange(range)))
    }

    private fun checkApproved(message: Message):Boolean{
        val key = message.chatId.toString()
        if (!approvedUsersList.containsKey(key)){
            return false
        }
        return approvedUsersList[key]?.chatId == message.chatId.toString() &&
                approvedUsersList[key]?.userLastName== message.chat.firstName &&
                approvedUsersList[key]?.username == message.chat.lastName
    }

    override fun getBotUsername(): String {
        return "first_buk_bot"
    }

    override fun getBotToken(): String {
        return "972427891:AAGVtH_slSf4_T7FPFc1T7O3BnVWPrtUmMs"
    }

    private fun sendTxtMessage(message: Message, str: String){
        val sendMessage = SendMessage()
        sendMessage.enableMarkdown(true)
        sendMessage.chatId = message.chatId.toString()
//        sendMessage.replyToMessageId = message.messageId
        sendMessage.text = str
        try {
            execute(sendMessage)
        } catch (e: Exception){

        }
    }
    override fun requestAuth() {
        telegramInterractor.findAllApprovedUsers().forEach {
            val sendMessage = SendMessage()
            sendMessage.enableMarkdown(true)
            sendMessage.chatId = it.chatId
//        sendMessage.replyToMessageId = message.messageId
            sendMessage.text = "Попытка входа в панель управления\n если это не Вы, введите\n /logoutAll\n Для входа введите\n /login \"цифры со страницы входа\"\n(через пробел, безкавычек)"
            try {
                execute(sendMessage)
            } catch (e: Exception){

            }
        }
    }
}