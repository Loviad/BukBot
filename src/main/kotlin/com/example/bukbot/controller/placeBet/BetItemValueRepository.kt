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
        mongoTemplate.findAll(BetItemValue::class.java).map { item ->
            println("${item.source}:${item.pivotType}:${item.pivotValue}:${item.pivotBias}:${item.type.toString()}:${item.value}:${item.pinValue}:${item.home}:${item.guest}:${item.timeType.toString()}")
            listBettedMatch[item.id] = item
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
            mongoTemplate.save(item,"betItemValue")
            true
        }
    }

    fun getPlacedItem(id: String): BetItemValue?{
        return listBettedMatch[id]
    }
}