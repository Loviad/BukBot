package com.example.bukbot.persistance

import com.example.bukbot.domain.ApprovedUsers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserDao {
    @Autowired
    private lateinit var repository: ApprovedUserRepository


    fun findByChatId(username: String?): Optional<ApprovedUsers>{
        return Optional.ofNullable(
                repository.findByChatId(username!!)
        )
    }

//    fun findByChatId(username: String?): Optional<ApprovedUsers>{
//        return Optional.ofNullable(
//        mongoTemplate.findOne(query(where("chatId").isEqualTo(username)), ApprovedUsers::class.java))
//    }

    fun save(approvedUsers: ApprovedUsers?) {
        approvedUsers?.let {
            repository.save(approvedUsers)
        }
    }
}