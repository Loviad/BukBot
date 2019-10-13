package com.example.bukbot.data.database.Dao

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class EventItem(
        @Id
        val id: String,
        val idEvent: String,
        val idOdd: Int,
        val source: String,
        val timeType: String,
        val typePivot: String,
        val pivotBias: String,
        val pivotValue: Double,
        val rateOver: Double,
        val rateUnder: Double,
        val rateEqual: Double
) {


    constructor(
             idEvent: String,
             idOdd: Int,
             source: String,
             timeType: String,
             typePivot: String,
             pivotBias: String,
             pivotValue: Double,
             rateOver: Double,
             rateUnder: Double,
             rateEqual: Double
    ) : this(
            "${idEvent}_${idOdd}_${source}_${timeType}_${typePivot}_${pivotBias}_${pivotValue}",
            idEvent,
            idOdd,
            source,
            timeType,
            typePivot,
            pivotBias,
            pivotValue,
            rateOver,
            rateUnder,
            rateEqual
    )
}