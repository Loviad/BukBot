package com.example.bukbot.data.database.Dao

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document


@Document
class PlacedBet(
        @Id
        val id: String,
        val eventId: String,
        val idOdd: Int

)