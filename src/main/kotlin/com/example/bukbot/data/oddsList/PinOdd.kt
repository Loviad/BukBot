package com.example.bukbot.data.oddsList

import jayeson.lib.feed.api.LBType
import jayeson.lib.feed.api.OddFormat
import jayeson.lib.feed.api.OddType
import jayeson.lib.feed.api.TimeType
import jayeson.lib.feed.api.twoside.PivotBias
import jayeson.lib.feed.api.twoside.PivotType
import jayeson.lib.feed.soccer.SoccerRecord

class  PinOdd {
    private var matchIdProp: String
    val matchId: String
        get() {
            return matchIdProp
        }

    private val eventIdProp: String
    val eventId: String
        get() {
            return eventIdProp
        }

    private var pivotTypeProp: PivotType
    val pivotType: PivotType
        get() {
            return pivotTypeProp
        }

    private var oddTypeProp: OddType
    val oddType: OddType
        get() {
            return oddTypeProp
        }

    private var pivotBiasProp: PivotBias
    val pivotBias: PivotBias
        get() {
            return pivotBiasProp
        }

    private var oddFormatProp: OddFormat
    val oddFormat: OddFormat
        get() {
            return oddFormatProp
        }

    private var tymeTypeProp: TimeType
    val tymeType: TimeType
        get() {
            return tymeTypeProp
        }

    val pivotValue: Double
    var startRateOver: Double = 0.0
    var startRateUnder: Double = 0.0

    val lblType: LBType

    constructor(
            item: SoccerRecord
    ) {
        this.matchIdProp = item.matchId()
        this.eventIdProp = item.eventId()
        this.pivotTypeProp = item.pivotType()
        this.oddTypeProp = item.oddType()
        this.pivotBiasProp = item.pivotBias()
        this.oddFormatProp = item.oddFormat()
        this.tymeTypeProp = item.timeType()
        this.startRateOver = item.rateOver().toDouble()
        this.startRateUnder = item.rateUnder().toDouble()
        this.pivotValue = item.pivotValue().toDouble()
        this.lblType = item.lbType()
    }

    override fun hashCode(): Int {
        var result = matchId.hashCode()
        result = 31 * result + eventId.hashCode()
        result = 31 * result + pivotType.hashCode()
        result = 31 * result + oddType.hashCode()
        result = 31 * result + pivotBias.hashCode()
        result = 31 * result + oddFormat.hashCode()
        result = 31 * result + tymeType.hashCode()
        result = 31 * result + pivotValue.hashCode()
        result = 31 * result + lblType.hashCode()
        return result
    }

}