package com.example.bukbot.controller.placeBet

import com.example.bukbot.data.database.Dao.BetItemValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class BetItemValueRepository {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    private var listBettedMatch = HashMap<String, BetItemValue>()

    @PostConstruct
    fun start() {
        mongoTemplate.findAll(BetItemValue::class.java).map {
            listBettedMatch[it.id] = it
        }
    }

    fun containsId(id: String): Boolean {
        return listBettedMatch.containsKey(id)
    }

    fun addPlaceBet(item: BetItemValue): Boolean {
        return if (listBettedMatch.containsKey(item.id)){
            false
        } else {
            listBettedMatch[item.id] = item
            mongoTemplate.save(item)
            true
        }
    }

    fun getPlacedItem(id: String): BetItemValue?{
        return listBettedMatch[id]
    }
}