package com.example.bukbot.service.telegrambot

import com.example.bukbot.persistance.AuthInterractor
import com.example.bukbot.persistance.TelegramInterractor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.ApiContextInitializer

@Component
class TelegrammBotInitializer{

    @Autowired
    private lateinit var authInterractor: AuthInterractor
    @Autowired
    private lateinit var telegramInterractor: TelegramInterractor
//
//    private val items: List<ApprovedUsers> = ArrayList<ApprovedUsers>().apply {
//        add(ApprovedUsers(
//                UUID.randomUUID().toString(),
//                "984717325",
//                "Sergey",
//                "Golushkov"
//        ))
//    }

    @PostConstruct
    fun start(){
        ApiContextInitializer.init()

        val botsApi = TelegramBotsApi()

        try {
            botsApi.registerBot(TelegramBot(authInterractor, telegramInterractor))
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}