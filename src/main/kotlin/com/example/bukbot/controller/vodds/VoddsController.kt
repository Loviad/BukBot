package com.example.bukbot.controller.vodds

import com.example.bukbot.data.oddsList.PinOdd
import com.example.bukbot.domain.interactors.vodds.VoddsInterractor
import com.example.bukbot.service.events.IGettingSnapshotListener
import com.example.bukbot.service.rest.ApiClient
import com.example.bukbot.utils.Settings
import com.example.bukbot.utils.round
import com.example.bukbot.utils.threadfabrick.VoddsThreadFactory
import com.loviad.bukbot.utils.BackgroundTaskThreadFactory
import jayeson.lib.feed.api.IBetRecord
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
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import javax.annotation.PostConstruct


@Component
class VoddsController : CoroutineScope, IGettingSnapshotListener {

    @Autowired
    private lateinit var api: ApiClient
    @Autowired
    private lateinit var settings: Settings
    @Autowired
    private lateinit var voddsInterractor: VoddsInterractor

//    val matchList = HashMap<String, MatchItem>()
    val pinOddList = HashMap<String, PinOdd>() // MatchId | PinOdd
    val pinDownEvent = ArrayList<String>()


    override val coroutineContext = //backgroundTaskDispatcher
            Executors.newSingleThreadExecutor(
                    VoddsThreadFactory()
            ).asCoroutineDispatcher()

    private val updateDispatcher = //backgroundTaskDispatcher
            Executors.newSingleThreadExecutor(
                    VoddsThreadFactory()
            ).asCoroutineDispatcher()

    private val backgroundTaskDispatcher = ThreadPoolExecutor(
            2, 12, 60L,
            TimeUnit.SECONDS, SynchronousQueue<Runnable>(), BackgroundTaskThreadFactory()
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
        val client = factory.createFromConfigFile("/home/sergey/projects/BukBot/build/resources/main/conf/libSportConfig.json")
        val noFilterIBetMatchFeedView = client.view(SoccerMatch::class.java)
        val myHandler = VoddsEventHandler(this@VoddsController)
        noFilterIBetMatchFeedView.register(myHandler)
        client.start()
        voddsInterractor.startParsing()
    }

//    fun insertMatches(stream: Stream<SoccerMatch>?) = launch {
//        if(stream == null) return@launch
//        stream.forEach {
//            matchList["${it.id()}_"+it.meta()["AGGREGATE_KEY"]] = MatchItem(it)
//        }
//        voddsInterractor.changeMatchList(matchList)
//    }

//    fun deleteMatches(stream: Stream<SoccerMatch>?) = launch(updateDispatcher) {
//        if(stream == null) return@launch
//        stream.forEach {match ->
//            pinOddList.forEach{ map ->
//                if (map.value.matchId == match.id()){
//                    pinOddList.remove(map.key)
//                    pinDownEvent.removeIf {
//                        it.contains(match.id())
//                    }
//                }
//            }
//        }
//        voddsInterractor.changePinList(pinOddList)
//        voddsInterractor.findPinDownEvent(pinDownEvent)
//    }


    fun updateOdd(stream: Stream<SoccerRecord>?) = launch(updateDispatcher){
        if(stream == null) return@launch
        var change = false
        stream.forEach { record ->
            if (record.source() == "CROWN" && record.pivotType() == PivotType.HDP){
                pinOddList[stringHash(record).toString()]?.let {
                                                                         //0.05                                                         //0.05
                    if ((it.startRateOver - record.rateOver().toDouble() > 0.01) || (it.startRateUnder - record.rateUnder().toDouble() > 0.01)){
                        change = true
                        if (pinDownEvent.size > 9) {
                            pinDownEvent.removeAt(0)
                        }
                        pinDownEvent.add(
                                "${it.matchId}_${it.eventId}_${it.pivotValue} : original value = ${record.rateOver().toDouble().round(3)}"
                        )
                    }
                }
            }
        }
        if (change) {
            voddsInterractor.findPinDownEvent(pinDownEvent)
        }
    }

    fun insertPinOdd(stream: Stream<SoccerRecord>?) = launch(updateDispatcher) {
        if(stream == null) return@launch
        var change = false
        stream.forEach {
            if (it.source() == "CROWN" && it.pivotType() == PivotType.HDP){
                change = true
                val hash = stringHash(it).toString()
                pinOddList[hash]?.let {

                } ?: run{
                    pinOddList[hash] = PinOdd(it)
                }
            }
        }
        if (change) voddsInterractor.changePinList(pinOddList)
        println("${pinOddList.size}")
    }


    fun deleteOdd(stream: Stream<SoccerRecord>?)= launch(updateDispatcher) {
        if(stream == null) return@launch
        stream.forEach { record ->
            if (record.source() == "CROWN" && record.pivotType() == PivotType.HDP){
                pinOddList.remove(stringHash(record).toString())
            }
        }
        voddsInterractor.changePinList(pinOddList)
        println("${pinOddList.size}")
    }

    private fun stringHash(item: SoccerRecord): Int{
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