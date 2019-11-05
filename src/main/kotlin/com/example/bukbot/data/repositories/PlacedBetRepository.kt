package com.example.bukbot.data.repositories

import com.example.bukbot.data.database.Dao.Bet
import com.example.bukbot.data.database.Dao.PlacedBet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component


@Component
class PlacedBetRepository {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    fun findById(id: String): PlacedBet? {
        return mongoTemplate.findById(
                id,
                PlacedBet::class.java
        )
    }

    fun saveBet(item: PlacedBet){
        mongoTemplate.save(item)
    }
    fun replaceBet(item: PlacedBet){
        mongoTemplate.findAndReplace(
                Query.query(
                        Criteria.where("_id").`is`(item.id)
                ),
                Bet::class.java
        )
    }
}