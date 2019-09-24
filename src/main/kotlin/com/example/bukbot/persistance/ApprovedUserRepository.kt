package com.example.bukbot.persistance

import com.example.bukbot.model.dbmodels.ApprovedUsers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ApprovedUserRepository: MongoRepository<ApprovedUsers, String> {

    fun findByChatId(chatId: String): ApprovedUsers

//    fun findAll(): List<ApprovedUsers>{
//        return mongoTemplate.findAll(ApprovedUsers::class.java)
//    }
}