package com.example.bukbot.domain.interactors.telegram

import com.example.bukbot.data.database.Dao.ApprovedUsers
import com.example.bukbot.data.repositories.ApprovedUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TelegramInterractor {

    @Autowired
    private lateinit var approvedUserRepository: ApprovedUserRepository


    fun findAllApprovedUsers(): List<ApprovedUsers> {
        return  approvedUserRepository.findAll()
    }

    fun findByChatId(chatId: String): ApprovedUsers {
        return approvedUserRepository.findByChatId(chatId)
    }
}