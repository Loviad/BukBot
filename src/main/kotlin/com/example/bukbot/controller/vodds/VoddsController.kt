package com.example.bukbot.controller.vodds

import com.example.bukbot.data.ItemModel
import com.example.bukbot.data.SSEModel.PlacingBet
import com.example.bukbot.data.database.Dao.EventItem
import com.example.bukbot.data.repositories.EventItemRepository
import com.example.bukbot.domain.interactors.vodds.VoddsInterractor
import com.example.bukbot.service.events.IGettingSnapshotListener
import com.example.bukbot.service.rest.ApiClient
import com.example.bukbot.utils.Settings
import com.example.bukbot.utils.threadfabrick.ApiThreadFactory
import com.example.bukbot.utils.threadfabrick.VoddsThreadFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class VoddsController : CoroutineScope, IGettingSnapshotListener {

    @Autowired
    private lateinit var mongo: EventItemRepository

    @Autowired
    private lateinit var api: ApiClient
    @Autowired
    private lateinit var settings: Settings
    @Autowired
    private lateinit var voddsInterractor: VoddsInterractor

    override val coroutineContext = //backgroundTaskDispatcher
            Executors.newSingleThreadExecutor(
                    VoddsThreadFactory()
            ).asCoroutineDispatcher()

    private val test = Executors.newSingleThreadExecutor(
            ApiThreadFactory()
    ).asCoroutineDispatcher()

    //val systemProps = System.getProperties()
    init {
        System.setProperty("deltaCrawlerSessionConfigurationFile", "conf/deltaCrawlerSession.json");
        //systemProps["deltaCrawlerSessionConfigurationFile"] = "deltaCrawlerSession.json"
    }

    lateinit var cs: DelCrawlerSession

    private var PARSING_STATE: Boolean = false

    @PostConstruct
    fun init(){
        settings.addSettingsEventListener(this)
    }

    override fun onGettingSnapshotChange(newValue: Boolean) {
        if(newValue)
            start()
    }


    fun start() = launch {
        if (!settings.getGettingSnapshot()) return@launch
        //systemProps.put("deltaCrawlerSessionConfigurationFile", "/home/sergey/projects/BukBot/conf/deltaCrawlerSession.json")

        cs = DelCrawlerSession()
        cs.subscriberRestartInterval = 0

        cs.connect()
        cs.waitConnection()

            api.getCredit()
            voddsInterractor.startParsing()
            while (settings.getGettingSnapshot()) {
                cs.waitConnection()
                val events = cs.allEvents
                var emptyListFlag: Boolean = events.size > 0

                for (e in events) {
                    mongo.clearEvents()
                    val rs = e.getRecords()



                    if (rs.isEmpty()) continue
//                    val state = e.getLiveState()

//                    println("id:" + e.eventId + "\tHost:" + e.host + "\tGuest:" + e.guest + "\tLeague:" + e.league)
                    for (r in rs) {
                        val item: EventItem = EventItem(
                                e.eventId,
                                r.oddId,
                                r.source,
                                r.timeType.name,
                                r.pivotType.name,
                                r.pivotBias.name,
                                r.pivotValue.toDouble(),
                                Math.round(r.rateOver.toDouble() * 1000.0) / 1000.0,
                                Math.round(r.rateUnder.toDouble() * 1000.0) / 1000.0,
                                Math.round(r.rateEqual.toDouble() * 1000.0) / 1000.0,
                                r.createdTime,
                                r.oddType.name
                        )
                        if (item.typePivot != "ONE_TWO") {
                            mongo.saveItem(item)
//                            println("(id:" + r.oddId + " Source:" + r.source + " TypeOdd:" + r.oddType + " TimeType:" + r.timeType + " TypePivot:" + r.pivotType + " PivotBias:" + r.pivotBias + " PivotValue:" + r.pivotValue + " RateOver:" +
//                                    r.rateOver + " RateUnder:" +
//                                    r.rateUnder + " RateEqual:" +
//                                    r.rateEqual + ")")
                        }
                    }
//                    println("\n")

                    if (emptyListFlag && settings.getGettingSnapshot()) {
                        val listPin = mongo.findByPin()
                        var lastItemHDP: ItemModel? = null
                        var lastItemTOTAL: ItemModel? = null
                        listPin.forEach { pinItem ->
                            val value = mongo.findForCheckValue(pinItem.idEvent, pinItem.idOdd)

                            value.forEach { valueItem ->

                                if (pinItem.timeType == valueItem.timeType &&
                                        pinItem.typePivot == valueItem.typePivot &&
                                        pinItem.pivotBias == valueItem.pivotBias &&
                                        pinItem.pivotValue == valueItem.pivotValue) {

                                    when (pinItem.typePivot) {
                                        "HDP" -> {
                                            val kefOver: Double = try {
                                                ((valueItem.rateOver / pinItem.rateOver) * 100) - 100
                                            } catch (e: Exception) {
                                                -1000.0
                                            }
                                            val kefUnder: Double = try {
                                                ((valueItem.rateUnder / pinItem.rateUnder) * 100) - 100
                                            } catch (e: Exception) {
                                                -1000.0
                                            }

                                            if(kefOver > 2 && valueItem.rateOver > 0.45){
                                                if(lastItemHDP != null) {
                                                    if (lastItemHDP!!.kef < kefOver) lastItemHDP = ItemModel(pinItem.rateOver, valueItem.rateOver, kefOver, TargetPivot.OVER, valueItem)
                                                } else {
                                                    lastItemHDP = ItemModel(pinItem.rateOver, valueItem.rateOver, kefOver, TargetPivot.OVER, valueItem)
                                                }
                                            }
                                            if(kefUnder > 2 && valueItem.rateUnder > 0.45){
                                                if(lastItemHDP != null) {
                                                    if (lastItemHDP!!.kef < kefUnder) lastItemHDP = ItemModel(pinItem.rateUnder, valueItem.rateUnder, kefUnder, TargetPivot.UNDER, valueItem)
                                                } else {
                                                    lastItemHDP = ItemModel(pinItem.rateUnder, valueItem.rateUnder, kefUnder, TargetPivot.UNDER, valueItem)
                                                }
                                            }
                                        }
                                        "TOTAL" ->{
                                            val kefOver: Double = try {
                                                ((valueItem.rateOver / pinItem.rateOver) * 100) - 100
                                            } catch (e: Exception) {
                                                -1000.0
                                            }
                                            val kefUnder: Double = try {
                                                ((valueItem.rateUnder / pinItem.rateUnder) * 100) - 100
                                            } catch (e: Exception) {
                                                -1000.0
                                            }

                                            if(kefOver > 2 && valueItem.rateOver > 0.45){
                                                if(lastItemTOTAL != null) {
                                                    if (lastItemTOTAL!!.kef < kefOver) lastItemTOTAL = ItemModel(pinItem.rateOver, valueItem.rateOver, kefOver, TargetPivot.OVER, valueItem)
                                                } else {
                                                    lastItemTOTAL = ItemModel(pinItem.rateOver, valueItem.rateOver, kefOver, TargetPivot.OVER, valueItem)
                                                }
                                            }
                                            if(kefUnder > 2 && valueItem.rateUnder > 0.45){
                                                if(lastItemTOTAL != null) {
                                                    if (lastItemTOTAL!!.kef < kefUnder) lastItemTOTAL = ItemModel(pinItem.rateUnder, valueItem.rateUnder, kefUnder, TargetPivot.UNDER, valueItem)
                                                } else {
                                                    lastItemTOTAL = ItemModel(pinItem.rateUnder, valueItem.rateUnder, kefUnder, TargetPivot.UNDER, valueItem)
                                                }
                                            }
                                        }
                                    }

                                }
                            }

                        }
                        lastItemHDP?.let {
                            api.checkAndPlaceBetTicket(it.item, it.type) { result ->
                                if(result) voddsInterractor.onPlaceBet(PlacingBet(
                                        e.host, e.guest, it.item.source, it.ratePin, it.rateVal, it.item.typePivot, it.item.pivotValue
                                ))
                            }

                            println("rateUnder id -" + it.item.idOdd + " timeType -" + it.item.timeType + " pivotBias -" + it.item.pivotBias + " typePivot -" + it.item.typePivot + " typeValue -" + it.item.pivotValue)
                            println("PIN88 \t-\t " + it.item.source)
                            println("" + it.ratePin + "\t-\t" + it.rateVal)
                            print("\n")

                        }
                        lastItemTOTAL?.let {
                            api.checkAndPlaceBetTicket(it.item, it.type){ result ->
                                if(result) voddsInterractor.onPlaceBet(PlacingBet(
                                        e.host, e.guest, it.item.source, it.ratePin, it.rateVal, it.item.typePivot, it.item.pivotValue
                                ))
                            }
                            println("rateUnder id -" + it.item.idOdd + " timeType -" + it.item.timeType + " pivotBias -" + it.item.pivotBias + " typePivot -" + it.item.typePivot + " typeValue -" + it.item.pivotValue)
                            println("PIN88 \t-\t " + it.item.source)
                            println("" + it.ratePin + "\t-\t" + it.rateVal)
                            print("\n")
                        }


                    }

                }
                TimeUnit.SECONDS.sleep(10L)
            }
        PARSING_STATE = false
        cs.disconnect()
        voddsInterractor.stopParsing()
       /** val dft = DeltaFeedTracker()
        cs.addDeltaEventHandler(dft) **/
    }

    @PreDestroy
    fun exit() {
        cs.disconnect()
    }

    enum class TargetPivot {
        OVER,
        UNDER,
        EQUAL
    }

}