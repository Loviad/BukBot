package com.example.bukbot.service.rest

import com.example.bukbot.controller.vodds.VoddsController
import com.example.bukbot.data.api.Balance
import com.example.bukbot.data.api.Response.BetTicketResponse
import com.example.bukbot.data.api.Response.BetPlaceResponse
import com.example.bukbot.data.database.Dao.Bet
import com.example.bukbot.data.database.Dao.EventItem
import com.example.bukbot.utils.Constans.GOLD
import com.example.bukbot.utils.Settings.BET_PLACING
import com.example.bukbot.utils.getAccessToken
import com.example.bukbot.utils.threadfabrick.ApiThreadFactory
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Component
import java.io.IOException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executors


@Component
class ApiClient: CoroutineScope {
    val JSON = MediaType.get("application/json; charset=utf-8")

    var client = OkHttpClient()

    var mapper = jacksonObjectMapper()

    private var balance: Double = 0.0

    override val coroutineContext = Executors.newSingleThreadExecutor(
            ApiThreadFactory()
    ).asCoroutineDispatcher()

    fun trest() {
        val body = FormBody.Builder()
                .add("username", "unity_group153")
                .add("accessToken", getAccessToken()!!)
                .add("reqId", UUID.randomUUID().toString())
                .build()
        val request = Request.Builder()
                .url("http://biweb-unity-test.olesportsresearch.com/getopenedbets")
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
    fun getBalance() {
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
            if(response.code() == 200) {
                val staff2: Balance = mapper.readValue(response.body()!!.string())
                balance = staff2.credit ?: 0.0
            }
        } catch (e: Exception) {
            Unit
        }
    }

//    @PostConstruct
    @Throws(IOException::class)
    fun getBetStatus(id: String) = launch {
        val body = FormBody.Builder()
                .add("username", "unity_group153")
                .add("accessToken", getAccessToken()!!)
                .add("id", "unity_fake_group57_ibc1|unity_fake_group57_ibc1|1571831471177")
                .build()
        val request = Request.Builder()
                .url("http://biweb-unity-test.olesportsresearch.com/getbetstatus")
                .post(body)
                .build()
        try {
            val response = client.newCall(request).execute()
            val s = response.body()!!.string()
            val staff2: Bet = mapper.readValue(s)
        } catch (e: Exception) {
            Unit
        }
    }

    @Throws(IOException::class)
    fun checkAndPlaceBetTicket(item: EventItem, target: VoddsController.TargetPivot) = launch {
        println("----StartPlace----")
        if(!BET_PLACING || (balance - GOLD.toDouble() < 0.001)) return@launch
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
                return@launch
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
            val objResponse: BetTicketResponse = mapper.readValue(response1.body()!!.string())
            if ((Math.round(objResponse.currentOdd!!.toDouble() * 1000.0) / 1000.0) == rate) {
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
                        if(response2.code() == 200) {
                            val staff2: BetPlaceResponse = mapper.readValue(response2.body()!!.string())
                            if(staff2.betStatus!! < 2){ balance -= GOLD }
                            println("BetStatus: " + staff2.betStatus + "\tBalance:" + balance + "\tId:" + staff2.id)
                            val s = response2.body()!!.string()
                        }
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