package com.example.bukbot.data.repositories

import com.example.bukbot.data.database.Dao.SettingsDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Description
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct


@Component
class SettingsRepository {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    fun createTable() {
        mongoTemplate.db.createCollection("settings")
        mongoTemplate.save(SettingsDao(), "settings")
    }

    @PostConstruct
    fun findCollections() {
        val k = mongoTemplate.db.listCollectionNames()
        val collections = k.filter {
            it == "settings"
        }
        if(collections.isEmpty()) {
            createTable()
        } else {
            val document = mongoTemplate.findAll(SettingsDao::class.java, "settings")
            if (document.size == 0) {
                mongoTemplate.save(SettingsDao(), "settings")
            }
        }
    }

    fun getSettings(): SettingsDao {
        return mongoTemplate.findOne(Query.query(Criteria.where("_id").`is`("1")))!!
    }

    fun saveSettings(setting: SettingsDao) {
        mongoTemplate.save(setting, "settings")
    }

}