package com.example.bukbot.controller.vodds

import com.example.bukbot.data.SSEModel.Match
import com.example.bukbot.data.database.Dao.PlacedBetDao
import com.example.bukbot.data.oddsList.PinOdd
import com.example.bukbot.data.repositories.PlacedBetRepository
import com.example.bukbot.domain.interactors.page.PageInterractor
import com.example.bukbot.domain.interactors.vodds.VoddsInterractor
import com.example.bukbot.service.events.IGettingSnapshotListener
import com.example.bukbot.service.rest.ApiClient
import com.example.bukbot.utils.*
import com.example.bukbot.utils.threadfabrick.UpdateVoddsThreadFactory
import com.example.bukbot.utils.threadfabrick.VoddsThreadFactory
import jayeson.lib.feed.api.LBType
import jayeson.lib.feed.api.twoside.PivotType
import jayeson.lib.feed.soccer.SoccerMatch
import jayeson.lib.feed.soccer.SoccerRecord
import jayeson.lib.sports.client.SportsFeedFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.stream.Stream
import javax.annotation.PostConstruct
import kotlin.random.Random
import kotlin.streams.toList


@Component
class VoddsController : CoroutineScope, IGettingSnapshotListener {

    @Autowired
    private lateinit var api: ApiClient

    @Autowired
    private lateinit var settings: Settings

    @Autowired
    private lateinit var currentState: CurrentState

    @Autowired
    private lateinit var voddsInterractor: VoddsInterractor

    @Autowired
    private lateinit var pageInterractor: PageInterractor

//    val matchList = HashMap<String, Match>()
    val pinOddList = HashMap<String, PinOdd>() // MatchId | PinOdd


    override val coroutineContext = //backgroundTaskDispatcher
            Executors.newSingleThreadExecutor(
                    VoddsThreadFactory()
            ).asCoroutineDispatcher()

    private val updateDispatcher = //backgroundTaskDispatcher
            Executors.newSingleThreadExecutor(
                    UpdateVoddsThreadFactory()
            ).asCoroutineDispatcher()


    @PostConstruct
    fun init() {
        settings.addSettingsEventListener(this)
//        start()
    }

    override fun onGettingSnapshotChange(newValue: Boolean) {
        if (newValue)
            start()
    }


    fun start() = launch {
        val factory = SportsFeedFactory()
        val client = factory.createFromConfigFile("/home/admin/libSportConfig.json")
        val noFilterIBetMatchFeedView = client.view(SoccerMatch::class.java)
        val myHandler = VoddsEventHandler(this@VoddsController)
        noFilterIBetMatchFeedView.register(myHandler)
        client.start()
        voddsInterractor.startParsing()
        pageInterractor.sendMessageConsole("Start Parsing", pageInterractor.ACCEPT)
    }

    fun insertMatches(stream: Stream<SoccerMatch>?) = launch(updateDispatcher) {
        if (stream == null) return@launch
        sendingMessageToConsole("Insert Matches")
//        stream.forEach {
//            matchList[it.id()] = Match(it.id(), it.host(), it.guest(), it.league(), it.startTime())
//        }
//        val now = DateTime.now().millis / 1000L
//        val match = matchList.filter {
//            it.value.startTime < now
//        }.map { it.key }
//        match.forEach {
//            matchList.remove(it)
//        }
//        voddsInterractor.changeMatchList(matchList)
    }

    fun deleteMatches(stream: Stream<SoccerMatch>?) = launch(updateDispatcher) {
        if (stream == null) return@launch
        sendingMessageToConsole("Delete Matches")
//        stream.forEach { match ->
//            matchList.remove(match.id())
//        }

//        voddsInterractor.changeMatchList(matchList)
    }


    fun updateOdd(stream: Stream<SoccerRecord>?) = launch(updateDispatcher) {
        if (stream == null || !currentState.canBetting || !settings.getBetPlacing()) return@launch
        var changeRate: Boolean
        val o = Random.nextInt( 0, 100)
        sendingMessageToConsole("Update ODDs Start : $o")
        stream.toList().forEach { record ->
//            if (api.bettingEvents["${record.matchId()}_${record.pivotType().name}_${record.timeType().name()}"] == null) {
                if (record.source() == "PIN88" && record.lbType() == LBType.BACK && (record.pivotType() == PivotType.HDP || record.pivotType() == PivotType.TOTAL)) {
                    pinOddList[stringHash(record).toString()]?.let {

                        changeRate = false                                               //0.05                                                         //0.05
                        if ((it.startRateOver - record.rateOver().toDouble()).round(3) >= settings.deltaPIN88) {
                            changeRate = true
                            it.targetPivot = TargetPivot.OVER
                            it.endRateOver = record.rateOver().toDouble().round(3)
                        }

                        if (
                                (it.startRateUnder - record.rateUnder().toDouble()).round(3) >= settings.deltaPIN88 &&
                                ((it.startRateOver - record.rateOver().toDouble()).round(3) < (it.startRateUnder - record.rateUnder().toDouble()).round(3) || !changeRate)
                        ) {
                            changeRate = true
                            it.targetPivot = TargetPivot.UNDER
                            it.endRateOver = record.rateUnder().toDouble().round(3)
                        }
                        if (
                                changeRate &&
                                settings.getBetPlacing()
                        ) {
                            api.placeBetTicket(it, o)
//                            { source, currentOdd, idBet ->
//                                bettingEvents["${record.matchId()}_${record.pivotType().name}_${record.timeType().name()}"] =
//                                placedBetRepository.savePlacedBet(
//                                        PlacedBetDao(
//                                                "${it.matchId}_${it.pivotType.name}_${it.tymeType}",
//                                                it.endRateOver,
//                                                source,
//                                                currentOdd.toDouble().round(3),
//                                                matchList[it.matchId]?.startTime ?: -1,
//                                                idBet
//                                        )
//                                )
//                            }
                        }

                    }
                }
//            }
        }
        sendingMessageToConsole("Update ODDs End : $o")
    }

    fun insertPinOdd(stream: Stream<SoccerRecord>?) = launch(updateDispatcher) {
        if (stream == null) return@launch
        stream.forEach {
            if (it.source() == "PIN88" && (it.pivotType() == PivotType.HDP || it.pivotType() == PivotType.TOTAL)) {
                val hash = stringHash(it).toString()
                pinOddList[hash]?.let {

                } ?: run {
                    pinOddList[hash] = PinOdd(it)
                }
            }
        }
//        if (change) voddsInterractor.changePinList(pinOddList)
//        println("${pinOddList.size}")
    }


    fun deleteOdd(stream: Stream<SoccerRecord>?) = launch(updateDispatcher) {
        if (stream == null) return@launch
        stream.forEach { record ->
            if (record.source() == "PIN88" && (record.pivotType() == PivotType.HDP || record.pivotType() == PivotType.TOTAL)) {
                pinOddList.remove(stringHash(record).toString())
            }
        }
//        voddsInterractor.changePinList(pinOddList)
//        println("${pinOddList.size}")
    }

    private fun sendingMessageToConsole(message: String)  {
        pageInterractor.sendMessageConsole(message)
    }


    private fun stringHash(item: SoccerRecord): Int {
        var result = item.matchId().hashCode()
        result = 31 * result + item.eventId().hashCode()
        result = 31 * result + item.pivotType().hashCode()
        result = 31 * result + item.oddType().hashCode()
        result = 31 * result + item.pivotBias().hashCode()
        result = 31 * result + item.oddFormat().hashCode()
        result = 31 * result + item.timeType().hashCode()
        result = 31 * result + item.pivotValue().hashCode()
        result = 31 * result + item.lbType().hashCode()
        return result
    }

    enum class TargetPivot {
        OVER,
        UNDER,
        EQUAL
    }

}