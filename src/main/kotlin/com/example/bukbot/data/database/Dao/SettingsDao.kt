package com.example.bukbot.data.database.Dao

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "settings")
class SettingsDao(
        @Id
        private val id: String = "1",
        val gold: Double = 2.5,
        val urlApi: String = "",
        val deltaPIN88: Double = 0.05,
        val minKef: Double = 2.0,
        val minValue: Double = 0.5,
        val maxValue: Double = 20.0,
        val saveBalance: Double = 0.0,
        val autoStartParsing: Boolean = false,
        val autoStartBetting: Boolean = false,
        val balanceBetting: Boolean = false,
        val creditBetting: Boolean = false
) {
    override fun toString(): String {
        return "Settings[gold:$gold; urlApi:$urlApi; deltaPIN88:$deltaPIN88; minKef:$minKef; minValue:$minValue; maxValue:$maxValue; autoStartParsing:$autoStartParsing; autoStartBetting:$autoStartBetting]"
    }
}