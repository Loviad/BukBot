package com.example.bukbot.service.rest

import com.example.bukbot.controller.vodds.VoddsController
import com.example.bukbot.data.api.Balance
import com.example.bukbot.data.api.BetTicket
import com.example.bukbot.data.database.Dao.EventItem
import com.example.bukbot.utils.Constans.GOLD
import com.example.bukbot.utils.Settings.BET_PLACING
import com.example.bukbot.utils.getAccessToken
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Component
import java.io.IOException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.*


@Component
class ApiClient {
    val JSON = MediaType.get("application/json; charset=utf-8")

    var client = OkHttpClient()

    var mapper = jacksonObjectMapper()

    @Throws(IOException::class)
    fun post() {
        val body = FormBody.Builder()
                .add("username", "unity_group153")
                .add("accessToken", getAccessToken()!!)
                .build()
        val request = Request.Builder()
                .url("http://biweb-unity-test.olesportsresearch.com/getusercredit")
                .post(body)
                .build()
        try {
            val response = client.newCall(request).execute()
            val s = response.body()!!.string()
            val staff2: Balance = mapper.readValue(s)
        } catch (e: Exception) {
            Unit
        }
    }

    @Throws(IOException::class)
    fun getBetTicket(item: EventItem, target: VoddsController.TargetPivot) {
        if(!BET_PLACING) return
        var rate: Double
        var targetType: String

        when {
            item.typePivot == "HDP" && target == VoddsController.TargetPivot.OVER -> {
                rate = item.rateOver; targetType = "home"
            }
            item.typePivot == "HDP" && target == VoddsController.TargetPivot.UNDER -> {
                rate = item.rateUnder; targetType = "away"
            }
            item.typePivot == "TOTAL" && target == VoddsController.TargetPivot.OVER -> {
                rate = item.rateOver; targetType = "over"
            }
            item.typePivot == "TOTAL" && target == VoddsController.TargetPivot.UNDER -> {
                rate = item.rateUnder; targetType = "under"
            }
            item.typePivot == "ONE_TWO" && target == VoddsController.TargetPivot.OVER -> {
                rate = item.rateOver; targetType = "one"
            }
            item.typePivot == "ONE_TWO" && target == VoddsController.TargetPivot.UNDER -> {
                rate = item.rateUnder; targetType = "two"
            }
            item.typePivot == "ONE_TWO" && target == VoddsController.TargetPivot.EQUAL -> {
                rate = item.rateEqual; targetType = "draw"
            }
            else -> {
                return@getBetTicket
            }
        }

        val body = FormBody.Builder()
                .add("username", "unity_group153")
                .add("accessToken", getAccessToken()!!)
                .add("reqId", UUID.randomUUID().toString())
                .add("company", item.source)
                .add("targettype", targetType)
                .add("market", item.oddType.toLowerCase())
                .add("eventid", item.idEvent)
                .add("oddid", item.idOdd.toString())
                .add("createdTime", item.createdTime.toString())
                .build()
        val request1 = Request.Builder()
                .url("http://biweb-unity-test.olesportsresearch.com/getbetticket")
                .post(body)
                .build()
        try {
            val response1 = client.newCall(request1).execute()
            val objResponse: BetTicket = mapper.readValue(response1.body()!!.string())
            val u = Math.round(objResponse.currentOdd!!.toDouble() * 1000.0) / 1000.0
            if (u == rate) {
                if(objResponse.minStake!! <= GOLD && objResponse.maxStake!!.toDouble() >= GOLD)
                {
                    val body = FormBody.Builder()
                            .add("username", "unity_group153")
                            .add("accessToken", getAccessToken()!!)
                            .add("reqId", UUID.randomUUID().toString())
                            .add("company", item.source)
                            .add("targettype", targetType)
                            .add("market", item.oddType.toLowerCase())
                            .add("eventid", item.idEvent)
                            .add("oddid", item.idOdd.toString())
                            .add("targetodd", rate.toString())
                            .add("gold", GOLD.toString())
                            .add("acceptbetterodd", "false")
                            .add("autoStakeAdjustment", "false")
                            .add("createdTime", item.createdTime.toString())
                            .build()
                    val request = Request.Builder()
                            .url("http://biweb-unity-test.olesportsresearch.com/placebet")
                            .post(body)
                            .build()
                    try {
                        val response2 = client.newCall(request).execute()
                        val s = response2.body()!!.string()
                        val k = 1
                    } catch (e: Exception) {
                        Unit
                    }

                }

            }
        } catch (e: Exception) {
            Unit
        }
    }
}