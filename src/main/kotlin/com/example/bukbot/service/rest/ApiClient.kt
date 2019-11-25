package com.example.bukbot.service.rest

import com.example.bukbot.BukBotApplication.Companion.backgroundTaskDispatcher
import com.example.bukbot.controller.vodds.VoddsController
import com.example.bukbot.data.api.Balance
import com.example.bukbot.data.api.Response.BetTicketResponse
import com.example.bukbot.data.api.Response.BetPlaceResponse
import com.example.bukbot.data.database.Dao.Bet
import com.example.bukbot.data.database.Dao.BetItemValue
import com.example.bukbot.data.database.Dao.EventItem
import com.example.bukbot.utils.Settings
import com.example.bukbot.utils.getAccessToken
import com.example.bukbot.utils.threadfabrick.ApiThreadFactory
import org.springframework.stereotype.Component
import java.io.IOException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import javafx.scene.image.Image
import kotlinx.coroutines.*
import okhttp3.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import java.util.concurrent.Executors


@Component
class ApiClient: CoroutineScope {
    val JSON = MediaType.parse("application/json; charset=utf-8")

    var client = OkHttpClient()

    var mapper = jacksonObjectMapper()

    private var balance: Double = -2222.0

    @Autowired
    private lateinit var settings: Settings

    override val coroutineContext = Executors.newSingleThreadExecutor(
            ApiThreadFactory()
    ).asCoroutineDispatcher()

    private val backDispatcher = backgroundTaskDispatcher

    fun getOpenedBets() {
        launch(backDispatcher) {
            val body = FormBody.Builder()
                    .add("username", "unity_group153")
                    .add("accessToken", getAccessToken()!!)
                    .add("reqId", UUID.randomUUID().toString())
                    .build()
            val request = Request.Builder()
                    .url("https://biweb-unity.stagingunity.com/getopenedbets")
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
    }

    fun getBalance() {
        launch(backDispatcher) {
            val body = FormBody.Builder()
                    .add("username", "unity_group153")
                    .add("accessToken", getAccessToken()!!)
                    .add("reqId", UUID.randomUUID().toString())
                    .add("getPl", "true")
                    .add("getCredit", "true")
                    .add("getOutstanding", "true")
                    .build()
            val request = Request.Builder()
                    .addHeader("ContentType", "application/x-www-form-urlencoded")
                    .url("https://biweb-unity.stagingunity.com/getuserbalance")
                    .post(body)
                    .build()
            try {
                val response = client.newCall(request).execute()
                println(response.body()!!.string())
//                val staff2: Balance = mapper.readValue(s)
            } catch (e: Exception) {
                Unit
            }
        }
    }

    fun test(){
        val date = DateTime.parse("22/10/2011",
                DateTimeFormat.forPattern("dd/MM/yyyy"))
       println(DateTime.now(DateTimeZone.UTC).millis)
    }


    fun getSettBets() {
        launch(backDispatcher) {
            val body = FormBody.Builder()
                    .add("username", "unity_group153")
                    .add("accessToken", getAccessToken()!!)
                    .add("reqId", UUID.randomUUID().toString())
                    .add("minBetTime", DateTime.parse("20/10/2019",
                            DateTimeFormat.forPattern("dd/MM/yyyy")).millis.toString())
                    .add("maxBetTime", DateTime.now().millis.toString())
                    .add("page", "0")
                    .build()
            val request = Request.Builder()
                    .url("https://biweb-unity.stagingunity.com/getsettledbets")
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
    }

    fun getCreditBalance(): Double {
//        launch(backDispatcher){
            getCredit()
//        }
        return balance
    }


    @Throws(IOException::class)
    fun getCredit() {
        val body = FormBody.Builder()
                .add("username", "unity_group153")
                .add("accessToken", getAccessToken()!!)
                .add("reqId", UUID.randomUUID().toString())
                .build()
        val request = Request.Builder()
                .url("https://biweb-unity.stagingunity.com/getusercredit")
                .post(body)
                .build()
        try {
            val response = client.newCall(request).execute()
            if(response.code() == 200) {
                val staff2: Balance = mapper.readValue(response.body()!!.string())
                balance = staff2.credit ?: -2222.0
            }
        } catch (e: Exception) {
            balance = -2222.0
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
                .url("https://biweb-unity.stagingunity.com/getbetstatus")
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

    fun checkAndPlaceBetTicket(item: BetItemValue, code: (result: Boolean, txt: String) -> Unit){
            placeBetTicket(item, code)
    }

    @Throws(IOException::class)
    private fun placeBetTicket(item: BetItemValue, code: (result: Boolean, txt: String) -> Unit) = launch(coroutineContext) {

//        if((balance - settings.getGold() < 0.001)) return@launch
        var rate: Double = item.value
        var targetType: String

        when {
            item.pivotType == "HDP" && item.type == VoddsController.TargetPivot.OVER -> {
                targetType = "home"
            }
            item.pivotType == "HDP" && item.type == VoddsController.TargetPivot.UNDER -> {
                 targetType = "away"
            }
            item.pivotType == "TOTAL" && item.type == VoddsController.TargetPivot.OVER -> {
                 targetType = "over"
            }
            item.pivotType == "TOTAL" && item.type == VoddsController.TargetPivot.UNDER -> {
                targetType = "under"
            }
            else -> {
                return@launch
            }
        }
//        val body = FormBody.Builder()
//                                .add("username", "unity_group153")
//                                .add("accessToken", getAccessToken()!!)
//                                .add("reqId", UUID.randomUUID().toString())
//                                .add("company", item.source)
//                                .add("targettype", targetType)
//                                .add("market", item.oddType.toLowerCase())
//                                .add("eventid", item.idEvent)
//                                .add("oddid", item.idOdd.toString())
//                                .add("targetodd", rate.toString())
//                                .add("gold", settings.getGold().toString())
//                                .add("acceptbetterodd", "false")
//                                .add("autoStakeAdjustment", "false")
//                                .add("createdTime", item.createdTime.toString())
//                                .build()
//        val body = RequestBody.create(JSON, o.toString())
//        val body = RequestBody.create(JSON,
//                "\"username\":\"unity_group153\", \"accessToken\":${getAccessToken()!!}, \"reqId\":\"$zUn\", \"company\":\"${item.source.toLowerCase()}\", \"targetType\":\"$targetType\", \"matchId\":\"${item.matchId}\", \"eventId\":\"${item.eventId}\", \"recordId\":${item.recordId}"
//        )
        val zUn = UUID.randomUUID().toString()
        val body = FormBody.Builder()
                .add("username", "unity_group153")
                .add("accessToken", getAccessToken()!!)
                .add("reqId", zUn)
                .add("company", item.source.toLowerCase())
                .add("targetType", targetType)
                .add("sportType", "soccer")
                .add("matchId", item.matchId)
                .add("eventId", item.eventId)
                .add("recordId", item.recordId.toString())
                .build()
        val request1 = Request.Builder()
                .addHeader("ContentType", "application/x-www-form-urlencoded")
                .url("https://biweb-unity.stagingunity.com/getbetticket")
                .post(body)
                .build()
        try {
            val response1 = client.newCall(request1).execute()
            val k = response1.body()!!.string()
            if (response1.code() == 200) {
                code(true, response1.body()!!.string())
                val objResponse: BetTicketResponse = mapper.readValue(k, BetTicketResponse::class.java)
//                println("min:\t${staff2.minStake} -!- ${staff2.actionStatus}")
                if (zUn == objResponse.reqId && (Math.round(objResponse.currentOdd!!.toDouble() * 1000.0) / 1000.0) >= rate) {
                    if(objResponse.minStake!! <= settings.getGold() && objResponse.maxStake!!.toDouble() >= settings.getGold()) {
                        val body = FormBody.Builder()
                                .add("username", "unity_group153")
                                .add("accessToken", getAccessToken()!!)
                                .add("reqId", UUID.randomUUID().toString())
                                .add("company", item.source)
                                .add("sportType", "soccer")
                                .add("targetType", targetType)
                                .add("matchId", item.matchId)
                                .add("eventId", item.eventId)
                                .add("recordId", item.recordId.toString())
                                .add("targetOdd", objResponse.currentOdd.toDouble().toString())
                                .add("gold", /*settings.getGold().toString()*/"1.0")
                                .add("acceptBetterOdd", "false")
                                .add("autoStakeAdjustment", "false")
                                .build()
                        val request = Request.Builder()
                                .url("https://biweb-unity.stagingunity.com/placebet")
                                .post(body)
                                .build()
                        try {
                            val response2 = client.newCall(request).execute()
                            if (response2.code() == 200) {
                                val s = response2.body()!!.string()
                                val staff2: BetPlaceResponse = mapper.readValue(response2.body()!!.string())
                                println("BetStatus: " + staff2.betStatus + "\tId:" + staff2.id + "\tTxt:" + staff2.actionMessage + "\tMatchId:" + item.matchId)
//                                if (staff2.betStatus!! < 2) {
//                                    balance -= settings.getGold()
//                                }


                            }
                        } catch (e: Exception) {
                            Unit
                        }
                    }
                }

            }
            response1.close()
//            val objResponse: BetTicketResponse = mapper.readValue(response1.body()!!.string())
//            if (zUn == objResponse.reqId && (Math.round(objResponse.currentOdd!!.toDouble() * 1000.0) / 1000.0) >= rate) {
//                if(objResponse.minStake!! <= settings.getGold() && objResponse.maxStake!!.toDouble() >= settings.getGold()) {
//                    println("----StartPlace----")
//                    code(true, "")
//                    if (settings.getBetPlacing() && settings.getGettingSnapshot()) {
//                        val body = FormBody.Builder()
//                                .add("username", "unity_group153")
//                                .add("accessToken", getAccessToken()!!)
//                                .add("reqId", UUID.randomUUID().toString())
//                                .add("company", item.source)
//                                .add("targettype", targetType)
////                                .add("market", item.oddType.toLowerCase())
////                                .add("eventid", item.idEvent)
////                                .add("oddid", item.idOdd.toString())
//                                .add("targetodd", rate.toString())
//                                .add("gold", settings.getGold().toString())
//                                .add("acceptbetterodd", "false")
//                                .add("autoStakeAdjustment", "false")
////                                .add("createdTime", item.createdTime.toString())
//                                .build()
//                        val request = Request.Builder()
//                                .url("https://biweb-unity.stagingunity.com/placebet")
//                                .post(body)
//                                .build()
//                        try {
//                            val response2 = client.newCall(request).execute()
//                            if (response2.code() == 200) {
//                                val staff2: BetPlaceResponse = mapper.readValue(response2.body()!!.string())
//                                if (staff2.betStatus!! < 2) {
//                                    //code(true)
//                                    balance -= settings.getGold()
//                                }
////                                println("BetStatus: " + staff2.betStatus + "\tBalance:" + balance + "\tId:" + staff2.id + "\tTxt:" + staff2.actionMessage)
////                                val s = response2.body()!!.string()
//                            }
//                        } catch (e: Exception) {
//                            Unit
//                        }
//                    }
//
//                }
//
//            } else {
//                code(false, response1.body()!!.string())
//            }
        } catch (e: Exception) {
            Unit
        }
    }
}