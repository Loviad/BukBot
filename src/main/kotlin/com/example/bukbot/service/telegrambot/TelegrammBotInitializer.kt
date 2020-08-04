package com.example.bukbot.service.telegrambot

import com.example.bukbot.domain.interactors.auth.AuthInterractor
import com.example.bukbot.domain.interactors.telegram.TelegramInterractor
import com.example.bukbot.utils.CurrentState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.ApiContext



@Component
class TelegrammBotInitializer{

    @Autowired
    private lateinit var authInterractor: AuthInterractor
    @Autowired
    private lateinit var telegramInterractor: TelegramInterractor
    @Autowired
    private lateinit var currentState: CurrentState

    @PostConstruct
    fun start(){

        ApiContextInitializer.init()

        val botsApi = TelegramBotsApi()

        val botOptions = ApiContext.getInstance(DefaultBotOptions::class.java)


//        botOptions.proxyHost = "127.0.0.1"
//        botOptions.proxyPort = 9050
//        botOptions.proxyType = DefaultBotOptions.ProxyType.SOCKS5

        try {
            botsApi.registerBot(TelegramBot(authInterractor, telegramInterractor, currentState, botOptions))
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }

        //vController.start()
    }
}