package com.example.bukbot.data.repositories

import com.example.bukbot.data.database.Dao.ApprovedUsers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Component
class ApprovedUserRepository {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    fun findByChatId(chatId: String): ApprovedUsers {
        return mongoTemplate.findOne<ApprovedUsers>(Query.query(where("_id").`is`(chatId)))!!
    }

    fun findAll(): List<ApprovedUsers> {
        return mongoTemplate.findAll()
    }
}