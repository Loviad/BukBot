package com.example.bukbot.service.telegrambot

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import java.lang.Exception


class TelegrammBot: TelegramLongPollingBot() {

    override fun onUpdateReceived(update: Update?) {
        val message = update?.message
        message?.let{
            if(message.hasText()){
                when(message.text){
                    "/help" -> sendMsg(message, "Привет")
                }
            }
        }
    }

    private fun sendMsg(message: Message, str: String){
        val sendMessage = SendMessage()
        sendMessage.enableMarkdown(true)
        sendMessage.chatId = message.chatId.toString()
        sendMessage.replyToMessageId = message.messageId
        sendMessage.text = str
        try {
            execute(sendMessage)
        } catch (e: Exception){

        }
    }

    override fun getBotUsername(): String {
        return "first_buk_bot"
    }

    override fun getBotToken(): String {
        return "972427891:AAGVtH_slSf4_T7FPFc1T7O3BnVWPrtUmMs"
    }
}