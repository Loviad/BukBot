package com.example.bukbot.service.telegrambot

import com.example.bukbot.persistance.AuthInterractor
import com.example.bukbot.persistance.TelegramInterractor
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.BasicCredentialsProvider
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

        //telegramInterractor.saveUser()
        ApiContextInitializer.init()

        val botsApi = TelegramBotsApi()

        var botOptions = ApiContext.getInstance(DefaultBotOptions::class.java)
//
//        val telegramBotsApi = TelegramBotsApi()
//        val botOptions = ApiContext.getInstance(DefaultBotOptions::class.java)
//        val credentialsProvider = BasicCredentialsProvider()
//        credentialsProvider.setCredentials(
//                AuthScope(Proxy, Port),
//                UsernamePasswordCredentials(Proxy_user, Proxy_Password))
//        val httpHost = HttpHost(Proxy, Port)
//        val requestConfig = RequestConfig.custom().setProxy(httpHost).setAuthenticationEnabled(true).build()
//        botOptions.setRequestConfig(requestConfig)
//        botOptions.setCredentialsProvider(credentialsProvider)
//        botOptions.setHttpProxy(httpHost)

        botOptions.proxyHost = "127.0.0.1"
        botOptions.proxyPort = 9050
        botOptions.proxyType = DefaultBotOptions.ProxyType.SOCKS5

        try {
            botsApi.registerBot(TelegramBot(authInterractor, telegramInterractor, botOptions))
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}