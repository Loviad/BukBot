package com.example.bukbot.controller.vodds

import com.example.bukbot.data.database.Dao.BetItemValue
import com.example.bukbot.domain.interactors.vodds.VoddsInterractor
import com.example.bukbot.service.events.IGettingSnapshotListener
import com.example.bukbot.service.rest.ApiClient
import com.example.bukbot.utils.Settings
import com.example.bukbot.utils.threadfabrick.ApiThreadFactory
import com.example.bukbot.utils.threadfabrick.VoddsThreadFactory
import jayeson.lib.feed.api.IBetEvent
import jayeson.lib.feed.api.IBetMatch
import jayeson.lib.feed.api.IBetRecord
import jayeson.lib.feed.api.twoside.IB2Match
import jayeson.lib.feed.api.twoside.IB2Record
import jayeson.lib.feed.api.twoside.PivotType
import jayeson.lib.sports.client.SportsFeedFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class VoddsController : CoroutineScope, IGettingSnapshotListener {

    @Autowired
    private lateinit var api: ApiClient
    @Autowired
    private lateinit var settings: Settings
    @Autowired
    private lateinit var voddsInterractor: VoddsInterractor

    val valueList = HashMap<String, BetItemValue>()

    var home: String = ""
    var guest: String = ""

    override val coroutineContext = //backgroundTaskDispatcher
            Executors.newSingleThreadExecutor(
                    VoddsThreadFactory()
            ).asCoroutineDispatcher()

    private val test = Executors.newSingleThreadExecutor(
            ApiThreadFactory()
    ).asCoroutineDispatcher()

    @PostConstruct
    fun init() {
        settings.addSettingsEventListener(this)

    }

    override fun onGettingSnapshotChange(newValue: Boolean) {
        if (newValue)
            start()
    }


    fun start() = launch {

        if (!settings.getGettingSnapshot()) return@launch
        val factory = SportsFeedFactory()

        /* Create SportsFeedClient using default config file (located in conf folder - libSportsConfig.json) */
        val path = System.getProperty("user.dir")
        val client = factory.createFromConfigFile("/home/sergey/projects/BukBot/conf/libSportConfig.json")
//        val client = factory.create()

        /* A single client supports multiple views.
       Views also determine the shape of records that will be retrieved.
       Below are IBetMatch view created with no filters applied. */
        val noFilterIBetMatchFeedView = client.view(IBetMatch::class.java)

        /* You can now retrieve data from the newly created view. You can choose to process this data by polling the view or by attaching an event handler. */
        /* Process events by attaching an event handler */
//        val myHandler = PushModeHandler("noFilter")
//        noFilterIBetMatchFeedView.register(myHandler)

        /* Start the client */
        client.start()

        /* Read data by polling the view from a feed view, basically it is to repeatedly poll for the latest snapshot */
        api.getBalance()
        voddsInterractor.startParsing()
        println("start")
        while (settings.getGettingSnapshot()) {
            /* You can update 'IB2Match' to 'SoccerMatch' or 'TennisMatch' or etc. according to the type of FeedView you initialized */
            val limit = 3
            val snapshot = noFilterIBetMatchFeedView.snapshot()
            val matches = snapshot.matches()
            printMatches<IBetMatch>(matches)
            TimeUnit.SECONDS.sleep(10L)
        }
        println("stop")
        voddsInterractor.stopParsing()

    }

    fun <M : IBetMatch> printMatches(matches: Collection<M>, limit: Int) {
        val it = matches.iterator()
        var i = 0

        var total: Pair<Double, BetItemValue>?
        var hdp: Pair<Double, BetItemValue>?
        while (settings.getGettingSnapshot() && i < limit && it.hasNext()) {
            val match = it.next()
            valueList.clear()
//            println("-------------------------")
//            println( match.sportType().toString() + ":" + match.league() + ":" + (match as IB2Match).participantOne() + ":" + (match as IB2Match).participantTwo())
            home = (match as IB2Match).participantOne()
            guest = (match as IB2Match).participantTwo()
            printEvents(match.events())
            total = null
            hdp = null
            valueList.forEach { item ->
                val kef = ((item.value.value/item.value.pinValue)*100)- 100
                if(item.value.source != "PIN88" && item.value.value > 0.45  && kef > 2 && item.value.pinValue > 0) {
                    when (item.value.pivotType) {
                        "TOTAL" -> {
                            total?.let {
                                if (it.first <= kef) {
                                    total = kef to item.value
                                }
                            } ?: run {
                                total = kef to item.value
                            }
                        }
                        "HDP" -> {
                            hdp?.let {
                                if (it.first <= kef) {
                                    hdp = kef to item.value
                                }
                            } ?: run {
                                hdp = kef to item.value
                            }
                        }
                    }
                }
            }
            total?.let {
//                    print("${it.first}\t${it.second.source}:${it.second.type}:${it.second.pivotBias}:${it.second.pivotType}:${it.second.pivotValue}:${it.second.value}:${it.second.pinValue}\n")
                    api.checkAndPlaceBetTicket(it.second)
            }
            hdp?.let {
//                    print("${it.first}\t${it.second.source}:${it.second.type}:${it.second.pivotBias}:${it.second.pivotType}:${it.second.pivotValue}:${it.second.value}:${it.second.pinValue}\n")
                    api.checkAndPlaceBetTicket(it.second)
            }
            i++
        }
    }

    fun <E : IBetEvent> printEvents(events: Collection<E>, limit: Int) {
        val it = events.iterator()
        var i = 0
        while (settings.getGettingSnapshot() && i < limit && it.hasNext()) {
            val event = it.next()
            printRecords(event.records())
            i++
        }
    }

    fun <R : IBetRecord> printRecords(records: Collection<R>, limit: Int) {
        val it = records.iterator()
        while (it.hasNext()) {
            val record = it.next() as IB2Record
//            println(record.source() + ":" + record.pivotType() + ":" + record.pivotValue() + ":" + record.pivotBias() + ":" + record.oddType() + ":" + record.rateOver() + ":" + record.rateUnder() + ":" + record.rateEqual())
            if(record.pivotType() != PivotType.ONE_TWO) {
//                print("\n${record.source()}_${record.matchId()}_${record.eventId()}_${record.id()}\t${record.pivotType()}_${record.pivotValue()}_${record.pivotBias()}_${TargetPivot.OVER.name}_${record.timeType()}")
                when (record.source()) {
                    "PIN88" -> {
                        valueList["${record.pivotType()}_${record.pivotValue()}_${record.pivotBias()}_${TargetPivot.OVER.name}_${record.timeType()}"]?.let {
                            it.pinValue = Math.round(record.rateOver().toDouble() * 1000.0) / 1000.0
                        } ?: run {
                            valueList["${record.pivotType()}_${record.pivotValue()}_${record.pivotBias()}_${TargetPivot.OVER.name}_${record.timeType()}"] =
                                    BetItemValue(
                                            "${record.matchId()}_${record.pivotType()}",
                                            record.source(),
                                            record.pivotType().name,
                                            record.pivotValue().toDouble(),
                                            record.pivotBias().name,
                                            TargetPivot.OVER,
                                            Math.round(record.rateOver().toDouble() * 1000.0) / 1000.0,
                                            Math.round(record.rateOver().toDouble() * 1000.0) / 1000.0,
                                            record.matchId(),
                                            record.eventId(),
                                            record.id(),
                                            home,
                                            guest,
                                            record.timeType().toString()
                                    )
                        }
                        valueList["${record.pivotType()}_${record.pivotValue()}_${record.pivotBias()}_${TargetPivot.UNDER.name}_${record.timeType()}"]?.let {
                            it.pinValue = Math.round(record.rateUnder().toDouble() * 1000.0) / 1000.0
                        } ?: run {
                            valueList["${record.pivotType()}_${record.pivotValue()}_${record.pivotBias()}_${TargetPivot.UNDER.name}_${record.timeType()}"] =
                                    BetItemValue(
                                            "${record.matchId()}_${record.pivotType()}",
                                            record.source(),
                                            record.pivotType().name,
                                            record.pivotValue().toDouble(),
                                            record.pivotBias().name,
                                            TargetPivot.UNDER,
                                            Math.round(record.rateUnder().toDouble() * 1000.0) / 1000.0,
                                            Math.round(record.rateUnder().toDouble() * 1000.0) / 1000.0,
                                            record.matchId(),
                                            record.eventId(),
                                            record.id(),
                                            home,
                                            guest,
                                            record.timeType().toString()
                                    )
                        }
                    }
                    else -> {
                        valueList["${record.pivotType()}_${record.pivotValue()}_${record.pivotBias()}_${TargetPivot.OVER.name}_${record.timeType()}"]?.let {
                            if (it.value < (Math.round(record.rateOver().toDouble() * 1000.0) / 1000.0)) {
                                it.value = Math.round(record.rateOver().toDouble() * 1000.0) / 1000.0
                                it.source = record.source()
                            }
                        } ?: run {
                            valueList["${record.pivotType()}_${record.pivotValue()}_${record.pivotBias()}_${TargetPivot.OVER.name}_${record.timeType()}"] =
                                    BetItemValue(
                                            "${record.matchId()}_${record.pivotType()}",
                                            record.source(),
                                            record.pivotType().name,
                                            record.pivotValue().toDouble(),
                                            record.pivotBias().name,
                                            TargetPivot.OVER,
                                            Math.round(record.rateOver().toDouble() * 1000.0) / 1000.0,
                                            matchId = record.matchId(),
                                            eventId = record.eventId(),
                                            recordId = record.id(),
                                            home = home,
                                            guest = guest,
                                            timeType = record.timeType().toString()
                                    )
                        }
                        valueList["${record.pivotType()}_${record.pivotValue()}_${record.pivotBias()}_${TargetPivot.UNDER.name}_${record.timeType()}"]?.let {
                            if (it.value < (Math.round(record.rateUnder().toDouble() * 1000.0) / 1000.0)) {
                                it.value = Math.round(record.rateUnder().toDouble() * 1000.0) / 1000.0
                                it.source = record.source()
                            }
                        } ?: run {
                            valueList["${record.pivotType()}_${record.pivotValue()}_${record.pivotBias()}_${TargetPivot.UNDER.name}_${record.timeType()}"] =
                                    BetItemValue(
                                            "${record.matchId()}_${record.pivotType()}",
                                            record.source(),
                                            record.pivotType().name,
                                            record.pivotValue().toDouble(),
                                            record.pivotBias().name,
                                            TargetPivot.UNDER,
                                            Math.round(record.rateUnder().toDouble() * 1000.0) / 1000.0,
                                            matchId = record.matchId(),
                                            eventId = record.eventId(),
                                            recordId = record.id(),
                                            home = home,
                                            guest = guest,
                                            timeType = record.timeType().toString()
                                    )
                        }
                    }

                }
            }
        }

    }


    fun <M : IBetMatch> printMatches(matches: Collection<M>) {
        printMatches(matches, matches.size)
    }

    fun <E : IBetEvent> printEvents(events: Collection<E>) {
        printEvents(events, events.size)
    }

    fun <R : IBetRecord> printRecords(records: Collection<R>) {
        printRecords(records, records.size)
    }

    enum class TargetPivot {
        OVER,
        UNDER,
        EQUAL
    }

}