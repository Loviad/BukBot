package com.example.bukbot.data.oddsList

import com.example.bukbot.controller.vodds.VoddsController
import com.example.bukbot.utils.round
import jayeson.lib.feed.api.LBType
import jayeson.lib.feed.api.OddFormat
import jayeson.lib.feed.api.OddType
import jayeson.lib.feed.api.TimeType
import jayeson.lib.feed.api.twoside.PivotBias
import jayeson.lib.feed.api.twoside.PivotType
import jayeson.lib.feed.soccer.SoccerRecord

class PinOdd {
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
    val tymeType: String
        get() {
            return tymeTypeProp.name()
        }

    private val pivotValueProp: Double
    val pivotValue: Double
        get() {
            return pivotValueProp
        }
    private var startRateOverProp: Double = 0.0
    val startRateOver: Double
        get() {
            return startRateOverProp.round(3)
        }
    private var startRateUnderProp: Double = 0.0
    val startRateUnder: Double
        get() {
            return startRateUnderProp.round(3)
        }

    private var recordIdProp: Long = 0
    val recordId: Long
        get() {
            return recordIdProp
        }

    //    private var endRateProp: Double = 0.0
//    var endRateOver: Double
//        get() {
//            return endRateProp.round(3)
//        }
//        set(value) {
//            endRateProp = value
//        }
    var endRateOver: Double = 0.0
        get() {
            return field.round(3)
        }
        set(value) {
            field = value
        }

    private var targetPivotProp: VoddsController.TargetPivot = VoddsController.TargetPivot.OVER
    var targetPivot: VoddsController.TargetPivot
        get() {
            return targetPivotProp
        }
        set(value) {
            targetPivotProp = value
        }

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
        this.startRateOverProp = item.rateOver().toDouble()
        this.startRateUnderProp = item.rateUnder().toDouble()
        this.pivotValueProp = item.pivotValue().toDouble()
        this.lblType = item.lbType()
        this.recordIdProp = item.id()
    }

    override fun hashCode(): Int {
        var result = matchId.hashCode()
        result = 31 * result + eventId.hashCode()
        result = 31 * result + pivotType.hashCode()
        result = 31 * result + oddType.hashCode()
        result = 31 * result + pivotBias.hashCode()
        result = 31 * result + oddFormat.hashCode()
        result = 31 * result + tymeType.hashCode()
        result = 31 * result + pivotValueProp.hashCode()
        result = 31 * result + lblType.hashCode()
        return result
    }

}