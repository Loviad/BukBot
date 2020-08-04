package com.example.bukbot.data.database.Dao

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "openBets")
class OpenBets(
        @Id
        val id: Long,
        val count: Int
)