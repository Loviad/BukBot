package com.example.bukbot.service.telegrambot

import org.glassfish.grizzly.ProcessorExecutor.execute
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.generics.WebhookBot

@Component
class TelegrammBot: TelegramLongPollingBot() {

    override fun onUpdateReceived(update: Update?) {
        if (update != null) {
            if (update.hasMessage() && update.message.hasText()) {
                val message = SendMessage() // Create a SendMessage object with mandatory fields
                        .setChatId(update.message.chatId)
                        .setText(update.message.text)
                try {
                    execute(message) // Call method to send the message
                } catch (e: TelegramApiException) {
                    e.printStackTrace()
                }

            }
        }
    }

    override fun getBotUsername(): String {
        return "first_buk_bot"
    }

    override fun getBotToken(): String {
        return "972427891:AAGVtH_slSf4_T7FPFc1T7O3BnVWPrtUmMs"
    }
}