package com.example.bukbot.data.repositories

import com.example.bukbot.data.database.Dao.PlacedBetDao
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findAllAndRemove
import org.springframework.data.mongodb.core.findAndRemove
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class PlacedBetRepository {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

//    fun getSettings(): SettingsDao {
//        return mongoTemplate.findOne(Query.query(Criteria.where("_id").`is`("1")))!!
//    }

    fun createTable() {
        mongoTemplate.db.createCollection("placedBets")
    }

    fun findCollections() {
        val k = mongoTemplate.db.listCollectionNames()
        val collections = k.filter {
            it == "placedBets"
        }
        if (collections.isEmpty()) {
            createTable()
        } else {
            deleteOldMatches(DateTime.now().millis / 1000L)
        }
    }

    fun findAllBets(): List<PlacedBetDao> {
        return mongoTemplate.findAll<PlacedBetDao>()
    }

    fun savePlacedBet(bet: PlacedBetDao) {
        mongoTemplate.save(bet)
    }

    fun deleteOldMatches(time: Long){
        mongoTemplate.findAllAndRemove(Query.query(Criteria.where("startTime").lt(time)), PlacedBetDao::class.java,"placedBets")

    }
}
