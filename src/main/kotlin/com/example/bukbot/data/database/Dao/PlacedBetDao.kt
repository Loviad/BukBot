package com.example.bukbot.data.database.Dao

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "placedBets")
class PlacedBetDao {
    @Id
    var id: Int
    var matchIdent: String
    var pinOdd: Double
    var targetSource: String
    var targetOdd: Double
    var startTime: Long
    var idBet: String

    constructor(){
        this.matchIdent = ""
        this.pinOdd = 0.0
        this.targetSource = ""
        this.targetOdd = 0.0
        this.startTime = 0
        this.idBet = ""
        this.id = 0
    }

    constructor(matchIdent: String,
                pinOdd: Double,
                targetSource: String,
                targetOdd: Double,
                startTime: Long,
                idBet: String) {
        this.matchIdent = matchIdent
        this.pinOdd = pinOdd
        this.targetSource = targetSource
        this.targetOdd = targetOdd
        this.startTime = startTime
        this.idBet = idBet
        this.id = hashCode()
    }

    constructor(
            id: Int,
            matchIdent: String,
            pinOdd: Double,
            targetSource: String,
            targetOdd: Double,
            startTime: Long,
            idBet: String) {
        this.id = id
        this.matchIdent = matchIdent
        this.pinOdd = pinOdd
        this.targetSource = targetSource
        this.targetOdd = targetOdd
        this.startTime = startTime
        this.idBet = idBet
    }


    override fun hashCode(): Int {
        var result = matchIdent.hashCode()
        result = 31 * result + pinOdd.hashCode()
        result = 31 * result + targetSource.hashCode()
        result = 31 * result + targetOdd.hashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + idBet.hashCode()
        return result
    }
}
