package com.example.bukbot.data.repositories

import com.example.bukbot.data.database.Dao.OpenBets
import com.mongodb.client.model.CreateCollectionOptions
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class OpenedBetsRepository{
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    fun createTable() {
        mongoTemplate.db.createCollection("openBets", CreateCollectionOptions().capped(true).sizeInBytes(5242880L).maxDocuments(960))
    }

    @PostConstruct
    fun findCollections() {
        val k = mongoTemplate.db.listCollectionNames()
        val collections = k.filter {
            it == "openBets"
        }
        if (collections.isEmpty()) {
            createTable()
        }
    }

    fun findAllBets(): List<OpenBets> {
        return mongoTemplate.findAll()
    }

    fun saveOpenBets(count: Int) {
        mongoTemplate.save(
                OpenBets(
                        DateTime.now().withZone(DateTimeZone.UTC).millis,
                        count
                )
        )
    }
}