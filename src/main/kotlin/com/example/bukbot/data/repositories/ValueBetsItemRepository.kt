package com.example.bukbot.data.repositories

import com.example.bukbot.data.database.Dao.ValueBetsItem
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ValueBetsItemRepository: MongoRepository<ValueBetsItem, String> {

    fun findFirst10ByOrderByDateTime(): List<ValueBetsItem>
    //fun findAndModify(query: Query, update: Update, entityClass: ValueBetsItem)
}