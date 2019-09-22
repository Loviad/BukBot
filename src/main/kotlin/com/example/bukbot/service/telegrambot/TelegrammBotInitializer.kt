package com.example.bukbot.service.telegrambot

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.ApiContextInitializer




@Component
class TelegrammBotInitializer{
    @PostConstruct
    fun start(){
        ApiContextInitializer.init()

        val botsApi = TelegramBotsApi()

        try {
            botsApi.registerBot(TelegrammBot())
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }

    }
}