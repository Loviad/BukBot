package com.example.bukbot.service.rest

import com.example.bukbot.BukBotApplication.Companion.backgroundTaskDispatcher
import com.example.bukbot.BukBotApplication.Companion.orderedApiTaskDispatcher
import com.example.bukbot.controller.vodds.VoddsController
import com.example.bukbot.data.SSEModel.CurrentStateSSEModel
import com.example.bukbot.data.api.Balance
import com.example.bukbot.data.api.Response.BetPlaceResponse
import com.example.bukbot.data.api.Response.BetTicketResponse
import com.example.bukbot.data.api.Response.openedbets.OpenedBet
import com.example.bukbot.data.oddsList.PinOdd
import com.example.bukbot.utils.CurrentState
import com.example.bukbot.utils.Settings
import com.example.bukbot.utils.getAccessToken
import org.springframework.stereotype.Component
import java.io.IOException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import javafx.beans.property.ObjectProperty
import jayeson.lib.feed.api.twoside.PivotType
import kotlinx.coroutines.*
import okhttp3.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import kotlin.collections.ArrayList


@Component
class ApiClient: CoroutineScope {
    val JSON = MediaType.parse("application/json; charset=utf-8")

    var client = OkHttpClient()

    var mapper = jacksonObjectMapper()

    @Autowired
    private lateinit var settings: Settings
    @Autowired
    private lateinit var currentState: CurrentState

    override val coroutineContext = orderedApiTaskDispatcher
    val placedDispatcher = backgroundTaskDispatcher

    @Throws(IOException::class)
    fun getBalance(blnc: CurrentStateSSEModel) = launch(coroutineContext) {
        val zUn = UUID.randomUUID().toString()
        val body = FormBody.Builder()
                .add("username", "unity_group170")
                .add("accessToken", getAccessToken()!!)
                .add("reqId", zUn)
                .add("getPl", true.toString())
                .add("getCredit", true.toString())
                .add("getOutstanding", true.toString())
                .build()
        val request = Request.Builder()
                .addHeader("ContentType", "application/x-www-form-urlencoded")
                .url("${settings.urlApi}/getuserbalance")
                .post(body)
                .build()
        try {
            val response1 = client.newCall(request).execute()
            val k = response1.body()!!.string()
            val map : Balance = mapper.readValue(k)

            if(map.actionStatus == 0) {
                blnc.credit = map.credit!!
                blnc.balance = map.outstanding!!
                blnc.pl = map.pl!!
            }
            response1.close()
        } catch (e: Exception) {
            Unit
        }
    }

    @Throws(IOException::class)
    fun getOpenBets(openBets: ObjectProperty<OpenedBet?>) = launch(coroutineContext) {
        val zUn = UUID.randomUUID().toString()
        val body = FormBody.Builder()
                .add("username", "unity_group170")
                .add("accessToken", getAccessToken()!!)
                .add("reqId", zUn)
                .build()
        val request = Request.Builder()
                .addHeader("ContentType", "application/x-www-form-urlencoded")
                .url("${settings.urlApi}/getopenedbets")
                .post(body)
                .build()
        try {
            val response1 = client.newCall(request).execute()
            val k = response1.body()!!.string()
            response1.close()
            val map : OpenedBet = mapper.readValue(k)
            if(map.actionStatus == 0) {
                openBets.set(map)
            }
        } catch (e: Exception) {
            openBets.set(null)
        }
    }

    fun test(){
        val date = DateTime.parse("22/10/2011",
                DateTimeFormat.forPattern("dd/MM/yyyy"))
       println(DateTime.now(DateTimeZone.UTC).millis)
    }


    @Throws(IOException::class)
    fun placeBetTicket(item: PinOdd, code: (String, Float, String) -> Unit) = launch(placedDispatcher) {

        var targetType: String

        when {
            item.pivotType == PivotType.HDP && item.targetPivot == VoddsController.TargetPivot.OVER -> {
                targetType = "home"
            }
            item.pivotType == PivotType.HDP && item.targetPivot == VoddsController.TargetPivot.UNDER -> {
                 targetType = "away"
            }
            item.pivotType == PivotType.TOTAL && item.targetPivot == VoddsController.TargetPivot.OVER -> {
                 targetType = "over"
            }
            item.pivotType == PivotType.TOTAL && item.targetPivot == VoddsController.TargetPivot.UNDER -> {
                targetType = "under"
            }
            else -> {
                return@launch
            }
        }

        val listMap = ArrayList<BetTicketResponse>()

        settings.sportbookList.forEach {sportbook ->
            val zUn = UUID.randomUUID().toString()
            val body = FormBody.Builder()
                    .add("username", "unity_group170")
                    .add("accessToken", getAccessToken()!!)
                    .add("reqId", zUn)
                    .add("company", sportbook)
                    .add("targetType", targetType)
                    .add("sportType", "soccer")
                    .add("matchId", item.matchId)
                    .add("eventId", item.eventId)
                    .add("recordId", item.recordId.toString())
                    .build()
            val request1 = Request.Builder()
                    .addHeader("ContentType", "application/x-www-form-urlencoded")
                    .url("${settings.urlApi}/getbetticket")
                    .post(body)
                    .build()
            try {
                val response1 = client.newCall(request1).execute()
                val k = response1.body()!!.string()
                val map : BetTicketResponse = mapper.readValue(k)

                if(map.actionStatus == 0) {
                    map.sportBook = sportbook
                    listMap.add(map)
                }

                response1.close()
            } catch (e: Exception) {
                Unit
            }
        }

        if(listMap.isNotEmpty()) {
            listMap.sortBy {
                it.currentOdd
            }
            var i = listMap.count() - 1
            try {
                while (i > -1) {
                    if (listMap[i].currentOdd!! >= settings.minKef &&
                            ((listMap[i].currentOdd!! / item.endRateOver) * 100) - 100 >= settings.minValue &&
                            ((listMap[i].currentOdd!! / item.endRateOver) * 100) - 100 <= settings.maxValue) {
                        if (listMap[i].minStake!!.toDouble() <= settings.getGold() && currentState.canBetting) {
                            val zUn = UUID.randomUUID().toString()
                            val body = FormBody.Builder()
                                    .add("username", "unity_group170")
                                    .add("accessToken", getAccessToken()!!)
                                    .add("reqId", zUn)
                                    .add("company", listMap[i].sportBook!!)
                                    .add("targetType", targetType)
                                    .add("sportType", "soccer")
                                    .add("matchId", item.matchId)
                                    .add("eventId", item.eventId)
                                    .add("recordId", item.recordId.toString())
                                    .add("targetOdd", listMap[i].currentOdd!!.toString())
                                    .add("gold", settings.getGold().toString())
                                    .add("acceptBetterOdd", true.toString())
                                    .add("autoStakeAdjustment", false.toString())
//                                    .add("maxAutoStakeAdjustment", false.toString())
                                    .build()
                            val request2 = Request.Builder()
                                    .addHeader("ContentType", "application/x-www-form-urlencoded")
                                    .url("${settings.urlApi}/placebet")
                                    .post(body)
                                    .build()
                            try {
                                val response2 = client.newCall(request2).execute()
                                val k2 = response2.body()!!.string()
                                println(k2)
                                val map : BetPlaceResponse = mapper.readValue(k2)
                                response2.close()
                                if(map.actionStatus == 0) {
                                    code(listMap[i].sportBook!!, listMap[i].currentOdd!!, map.id!!)
                                    i = -1
                                } else if(map.actionStatus == 14) {
                                    currentState.state.balance = 0.0
                                    currentState.state.credit = 0.0
                                }
                            } catch (e: Exception) {
                                Unit
                            }

                        }
                    }
                    i--
                }
            } catch (e:Exception) {

            }
        }
    }
}
