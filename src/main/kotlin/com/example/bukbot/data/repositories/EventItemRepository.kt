package com.example.bukbot.data.repositories

import com.example.bukbot.data.database.Dao.EventItem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Component


@Component
class EventItemRepository {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    fun findByPin(): List<EventItem> {
        return mongoTemplate.find(
                query(
                        where("source").`is`("PIN88")
                ),
                EventItem::class.java
        )
    }

    fun findForCheckValue(eventId: String, oddId: Int): List<EventItem>{
        return mongoTemplate.find(
                query(
                        where("idEvent").`is`(eventId)
                                .and("idOdd").`is`(oddId)
                                .and("source").ne("PIN88")
                )
                ,EventItem::class.java
        )
    }


    fun saveItem(item: EventItem){
        mongoTemplate.save(item)
    }


}