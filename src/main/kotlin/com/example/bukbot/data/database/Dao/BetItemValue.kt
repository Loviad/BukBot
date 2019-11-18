package com.example.bukbot.data.database.Dao

import com.example.bukbot.controller.vodds.VoddsController
import jayeson.lib.feed.api.twoside.IB2Record
import jayeson.lib.feed.api.twoside.PivotBias
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class BetItemValue(
        var source: String,
        val pivotType: String,
        val pivotValue: Double,
        val pivotBias: String,
        val type: VoddsController.TargetPivot, //Over or Under or Equail
        var value: Double,
        var pinValue: Double = 0.0,
        val matchId: String,
        val eventId: String,
        val recordId: Long,
        var record: IB2Record? = null
) {
}