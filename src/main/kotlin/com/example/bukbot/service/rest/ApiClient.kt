package com.example.bukbot.service.rest

import com.example.bukbot.BukBotApplication.Companion.backgroundTaskDispatcher
import com.example.bukbot.BukBotApplication.Companion.orderedApiTaskDispatcher
import com.example.bukbot.BukBotApplication.Companion.orderedBetsTaskDispatcher
import com.example.bukbot.controller.vodds.VoddsController
import com.example.bukbot.data.SSEModel.ConsoleMessage
import com.example.bukbot.data.SSEModel.CurrentStateSSEModel
import com.example.bukbot.data.api.Balance
import com.example.bukbot.data.api.Response.BetPlaceResponse
import com.example.bukbot.data.api.Response.BetTicketResponse
import com.example.bukbot.data.api.Response.openedbets.BetInfo
import com.example.bukbot.data.api.Response.openedbets.OpenedBet
import com.example.bukbot.data.oddsList.PinOdd
import com.example.bukbot.data.repositories.PlacedBetRepository
import com.example.bukbot.domain.interactors.page.PageInterractor
import com.example.bukbot.utils.*
import com.example.bukbot.utils.threadfabrick.BetsThreadFactory
import com.example.bukbot.utils.threadfabrick.PlaceBedThreadFactory
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
import java.lang.Math.floor
import java.util.*
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import javax.xml.stream.events.EndDocument
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet


@Component
class ApiClient : CoroutineScope {
    val JSON = MediaType.parse("application/json; charset=utf-8")

    var client = OkHttpClient()

    var mapper = jacksonObjectMapper()

    @Autowired
    private lateinit var settings: Settings

    @Autowired
    private lateinit var currentState: CurrentState

    @Autowired
    private lateinit var pageInterractor: PageInterractor

    @Autowired
    private lateinit var placedBetRepository: PlacedBetRepository

    //    var bettingEvents: ArrayList<Int> = arrayListOf()
    var bettingEvents: HashSet<String> = HashSet()

    private val biasType = HashMap<String, String>()

    @PostConstruct
    fun firstStart() {
        biasType["HOME FT"] = "HDP FT"
        biasType["AWAY FT"] = "HDP FT"
        biasType["HOME HT"] = "HDP HT"
        biasType["AWAY HT"] = "HDP HT"
        biasType["OVER HT"] = "TOTAL HT"
        biasType["UNDER HT"] = "TOTAL HT"
        biasType["OVER FT"] = "TOTAL FT"
        biasType["UNDER FT"] = "TOTAL FT"
    }

    override val coroutineContext = orderedApiTaskDispatcher
    val placedDispatcher = backgroundTaskDispatcher
    val betsDispatcher = orderedBetsTaskDispatcher
    val placeDispatcher = Executors.newSingleThreadExecutor(
            PlaceBedThreadFactory()
    ).asCoroutineDispatcher()

    fun containsBets(value: String): Boolean {
        return bettingEvents.contains(value)
    }

    fun addBets(value: String): Boolean {
        bettingEvents.add(value)
        return true
    }

    fun replaceBets(map: HashSet<String>): Boolean {
        bettingEvents = map
        return true
    }

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
            val map: Balance = mapper.readValue(k)

            if (map.actionStatus == 0) {
                blnc.credit = map.credit!!.round(2)
                blnc.balance = map.outstanding!!.round(2)
                blnc.pl = map.pl!!.round(2)
            }
            response1.close()
        } catch (e: Exception) {
            pageInterractor.sendMessageConsole("Ошибка при получении баланса: " + e.message, pageInterractor.ERROR)
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
            val map: OpenedBet = mapper.readValue(k)
            if (map.actionStatus == 0) {
                openBets.set(map)
                if (map.betInfos != null && map.betInfos.count() > 0) {
                    val tempArrayBet = HashSet(map.betInfos.map {
                        "${it.eventId}_${biasType[it.targetType]}"
                    })
                    val result = withContext(betsDispatcher) {
                        replaceBets(tempArrayBet)
                    }
                } else if (map.totalResults != null && map.totalResults == 0) {
                    val result = withContext(betsDispatcher) {
                        replaceBets(HashSet<String>())
                    }
                }
            }
        } catch (e: Exception) {
            pageInterractor.sendMessageConsole("Ошибка при парсинге открытых ставок: " + e.message, pageInterractor.ERROR)
        }
    }

    fun test() {
        val date = DateTime.parse("22/10/2011", DateTimeFormat.forPattern("dd/MM/yyyy"))
        println(DateTime.now(DateTimeZone.UTC).millis)
    }


    @Throws(IOException::class)
    fun placeBetTicket(item: PinOdd, o: Int) = launch(placedDispatcher) {

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
        settings.sportbookList.forEach { sportbook ->
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
                val map: BetTicketResponse = mapper.readValue(k)
                response1.close()
                if (map.actionStatus == 0 &&
                        map.minStake!!.toDouble() <= settings.getGold() &&
                        map.currentOdd!!.toDouble() >= settings.minKef &&
                        map.currentOdd!!.toDouble() <= 1.3) {
                    map.sportBook = sportbook
                    listMap.add(map)
                }
            } catch (e: Exception) {
                pageInterractor.sendMessageConsole("Ошибка при парсинге данных о ставке конторы $sportbook:$o: " + e.message, pageInterractor.ERROR)
            }
        }

        if (listMap.isNotEmpty()) {
            listMap.sortBy {
                it.currentOdd
            }
            var i = listMap.count() - 1
            try {
                    if (currentState.canBetting &&
                            ((listMap[i].currentOdd!! / item.endRateOver) * 100) - 100 >= settings.minValue &&
                            ((listMap[i].currentOdd!! / item.endRateOver) * 100) - 100 <= settings.maxValue) {
                        placeBet(
                                item.tymeType,
                                listMap[i].sportBook!!,
                                targetType,
                                item.matchId,
                                item.eventId,
                                item.recordId.toString(),
                                listMap[i].currentOdd!!.toString(),
                                o
                        )
                    }
            } catch (e: Exception) {
                pageInterractor.sendMessageConsole("Ошибка при поиске валуя:$o: " + e.message, pageInterractor.ERROR)
                return@launch
            }
        }
    }

    fun placeBet(
        timeType: String,
        company: String,
        targetType: String,
        matchId: String,
        eventId: String,
        recordId: String,
        targetOdd: String,
        threadNumber: Int
    ) = launch(placeDispatcher) {
        val result = withContext(betsDispatcher) {
            containsBets("${matchId}_${biasType["${targetType.toUpperCase()} ${timeType}"]}")
        }

        if (result) {
            pageInterractor.sendMessageConsole("Пропуск дублирующей ставки:$threadNumber", pageInterractor.ACCEPT)
            return@launch
        }

        val zUn = UUID.randomUUID().toString()
        val body = FormBody.Builder()
                .add("username", "unity_group170")
                .add("accessToken", getAccessToken()!!)
                .add("reqId", zUn)
                .add("company", company)
                .add("targetType", targetType)
                .add("sportType", "soccer")
                .add("matchId", matchId)
                .add("eventId", eventId)
                .add("recordId", recordId)
                .add("targetOdd", targetOdd)
                .add("gold", settings.getGold().toString())
                .add("acceptBetterOdd", true.toString())
                .add("autoStakeAdjustment", false.toString())
//              .add("maxAutoStakeAdjustment", false.toString())
                .build()
        val request2 = Request.Builder().addHeader("ContentType", "application/x-www-form-urlencoded")
                .url("${settings.urlApi}/placebet")
                .post(body)
                .build()
        try {
            val response2 = client.newCall(request2).execute()
            val k2 = response2.body()!!.string()
            val map: BetPlaceResponse = mapper.readValue(k2)
            response2.close()
            if (map.actionStatus == 0) {
                pageInterractor.sendMessageConsole(k2 + " : ${company} :$threadNumber", pageInterractor.ACCEPT)
            } else if (map.actionStatus == 14) {
                pageInterractor.sendMessageConsole("Баланс кончился:$threadNumber $k2", pageInterractor.IMPORTANT)
                currentState.state.balance = 0.0
                currentState.state.credit = 0.0
            } else {
                pageInterractor.sendMessageConsole("$k2:$threadNumber", pageInterractor.IMPORTANT)
            }

            val result = withContext(betsDispatcher) {
                addBets("${matchId}_${biasType["${targetType.toUpperCase()} ${timeType}"]}")
            }
//          bettingEvents.add("${item.matchId}_${item.oddType.name}_${targetType.toUpperCase()} ${item.tymeType}".hashCode())
        } catch (e: Exception) {
            pageInterractor.sendMessageConsole("Ошибка при парсинге установки ставки ${company}:$threadNumber: " + e.message, pageInterractor.ERROR)
        }
    }

    fun getSetledBets(start: Long, end: Long): ArrayList<BetInfo>? {
        val zUn = UUID.randomUUID().toString()
        val body = FormBody.Builder()
                .add("username", "unity_group170")
                .add("accessToken", getAccessToken()!!)
                .add("reqId", zUn)
                .add("minBetTime", start.toString())
                .add("maxBetTime", end.toString())
                .add("page", 0.toString())
                .build()
        val request = Request.Builder().addHeader("ContentType", "application/x-www-form-urlencoded")
                .url("${settings.urlApi}/getsettledbets")
                .post(body)
                .build()
        try {
            val response = client.newCall(request).execute()
            val k = response.body()!!.string()
            response.close()
            var map: OpenedBet = mapper.readValue(k)
            if (map.actionStatus == 0) {
                val betsList: ArrayList<BetInfo> = arrayListOf()
                betsList.addAll(map.betInfos!!)
                val maxPages = kotlin.math.floor(map.totalResults!! / 50.0).toInt()
                if (maxPages > 0 ) {
                    var i = 1
                    while(i <= maxPages) {
                        val zUn2 = UUID.randomUUID().toString()
                        val body2 = FormBody.Builder()
                                .add("username", "unity_group170")
                                .add("accessToken", getAccessToken()!!)
                                .add("reqId", zUn2)
                                .add("minBetTime", start.toString())
                                .add("maxBetTime", end.toString())
                                .add("page", i.toString())
                                .build()
                        val request2 = Request.Builder().addHeader("ContentType", "application/x-www-form-urlencoded")
                                .url("${settings.urlApi}/getsettledbets")
                                .post(body2)
                                .build()
                        try {
                            val response2 = client.newCall(request2).execute()
                            val k2 = response2.body()!!.string()
                            response.close()
                            map = mapper.readValue(k2)
                            if (map.actionStatus == 0) {
                                betsList.addAll(map.betInfos!!)
                                i++
                            } else {
                                val z = 1
                            }
                        } catch (e:Exception){
                            pageInterractor.sendMessageConsole("Ошибка при запросе истории ставок, страница $i:" + e.message, pageInterractor.ERROR)
                        }
                    }
                }
                return betsList
            }
        } catch (e:Exception) {
            pageInterractor.sendMessageConsole("Ошибка при парсинге истории ставок:" + e.message, pageInterractor.ERROR)
        }
        return null
    }
}
