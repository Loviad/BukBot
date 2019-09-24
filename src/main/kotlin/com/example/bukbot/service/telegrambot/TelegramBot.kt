package com.example.bukbot.service.telegrambot

import com.example.bukbot.model.dbmodels.ApprovedUsers
import com.example.bukbot.persistance.ApprovedUserRepository
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import java.lang.Exception


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
class TelegramBot(val repository: ApprovedUserRepository): TelegramLongPollingBot() {
    private var approvedUsersList = HashMap<String, ApprovedUsers>()

    init {
        repository.findAll().map {
            approvedUsersList[it.chatId] = it
        }
    }

    override fun onUpdateReceived(update: Update?) {
        val message = update?.message
        message?.let{
            if (!checkApproved(message)) return
            if(message.hasText()){
                when(message.text){
                    "/approved" -> sendTxtMessage(message, "Привет")
                }
            }
        }
    }

    private fun checkApproved(message: Message):Boolean{
        val key = message.chatId.toString()
        if (!approvedUsersList.containsKey(key)){
            return false
        }
        return approvedUsersList[key]?.chatId == message.chatId.toString() &&
                approvedUsersList[key]?.firstName == message.chat.firstName &&
                approvedUsersList[key]?.lastName == message.chat.lastName
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
}