package com.example.bukbot.data.repositories

import com.example.bukbot.data.database.Dao.Bet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Component

@Component
class BetRepository {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    fun findByPin(id: String): Bet? {
        return mongoTemplate.findById(
                id,
                Bet::class.java
        )
    }

    fun saveBet(item: Bet){
        mongoTemplate.save(item)
    }
    fun replaceBet(item: Bet){
        mongoTemplate.findAndReplace(
             query(
                     where("_id").`is`(item.id)
             ),
             Bet::class.java
        )
    }
}