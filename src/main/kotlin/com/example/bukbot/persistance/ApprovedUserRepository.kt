package com.example.bukbot.persistance

import com.example.bukbot.domain.ApprovedUsers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.*

@Component
class ApprovedUserRepository {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    fun findByChatId(chatId: String): ApprovedUsers{
        val k = mongoTemplate.findOne<ApprovedUsers>(Query.query(where("_id").`is`(chatId)))
        return k!!
    }

    fun findAll(): List<ApprovedUsers> {
        return mongoTemplate.findAll()
    }
}