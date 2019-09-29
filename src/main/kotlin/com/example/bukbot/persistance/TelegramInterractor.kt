package com.example.bukbot.persistance

import com.example.bukbot.domain.ApprovedUsers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import java.util.*

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