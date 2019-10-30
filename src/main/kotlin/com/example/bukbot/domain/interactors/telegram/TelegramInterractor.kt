package com.example.bukbot.domain.interactors.telegram

import com.example.bukbot.data.database.Dao.ApprovedUsers
import com.example.bukbot.data.repositories.ApprovedUserRepository
import com.example.bukbot.service.rest.ApiClient
import com.example.bukbot.utils.Settings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TelegramInterractor {

    @Autowired
    private lateinit var approvedUserRepository: ApprovedUserRepository

    @Autowired
    private lateinit var api: ApiClient
    @Autowired
    private lateinit var settings: Settings

    fun findAllApprovedUsers(): List<ApprovedUsers> {
        return  approvedUserRepository.findAll()
    }

    fun findByChatId(chatId: String): ApprovedUsers {
        return approvedUserRepository.findByChatId(chatId)
    }

    fun getCredit(): Double{
        return api.getCreditBalance()
    }

    fun test(){
        api.test()
    }

    fun setedBets(){
        api.getSettBets()
    }

    fun openedBets(){
        api.getOpenedBets()
    }

    fun getBalance(){
        api.getBalance()
    }

    fun getSystemState(): Pair<Boolean, Boolean>{
        return settings.getGettingSnapshot() to settings.getBetPlacing()
    }

}