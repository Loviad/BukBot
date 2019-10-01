package com.example.bukbot.data.database.persistance.approvedusers

import com.example.bukbot.data.repositories.ApprovedUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component

@Component
class UserService: UserDetailsService {

    @Autowired
    private lateinit var approvedUserRepository: ApprovedUserRepository


    override fun loadUserByUsername(username: String?): UserDetails {
        return approvedUserRepository.findByChatId(username!!)
    }

}