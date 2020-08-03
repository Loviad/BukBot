package com.example.bukbot.domain.interactors.telegram

import com.example.bukbot.data.database.Dao.ApprovedUsers
import com.example.bukbot.data.repositories.ApprovedUserRepository
import com.example.bukbot.service.rest.ApiClient
import com.example.bukbot.utils.CurrentState
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
    @Autowired
    private lateinit var currentState: CurrentState

    fun findAllApprovedUsers(): List<ApprovedUsers> {
        return  approvedUserRepository.findAll()
    }

    fun findByChatId(chatId: String): ApprovedUsers {
        return approvedUserRepository.findByChatId(chatId)
    }

    fun getCredit(): Double{
        return currentState.state.credit
    }

    fun test(){
        api.test()
    }

    fun getBalance(){
        currentState.state.balance
    }

    fun getSystemState(): Pair<Boolean, Boolean>{
        return settings.getGettingSnapshot() to settings.getBetPlacing()
    }

}