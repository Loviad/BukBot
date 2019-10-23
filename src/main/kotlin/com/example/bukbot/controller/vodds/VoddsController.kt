package com.example.bukbot.controller.vodds

import com.example.bukbot.data.database.Dao.EventItem
import com.example.bukbot.data.repositories.EventItemRepository
import com.example.bukbot.service.rest.ApiClient
import com.example.bukbot.utils.Settings.GETTING_SNAPSHOT
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
class VoddsController: CoroutineScope {

    @Autowired
    private lateinit var mongo: EventItemRepository

    @Autowired
    private lateinit var api: ApiClient

    override val coroutineContext = Executors.newSingleThreadExecutor(
            VoddsThreadFactory()
    ).asCoroutineDispatcher()

    //val systemProps = System.getProperties()
    init {
        System.setProperty("deltaCrawlerSessionConfigurationFile", "conf/deltaCrawlerSession.json");
        //systemProps["deltaCrawlerSessionConfigurationFile"] = "deltaCrawlerSession.json"
    }
    lateinit var cs: DelCrawlerSession

    @PostConstruct
    fun start() {
        api.trest()
        api.getBalance()
        if (!GETTING_SNAPSHOT) return
        //systemProps.put("deltaCrawlerSessionConfigurationFile", "/home/sergey/projects/BukBot/conf/deltaCrawlerSession.json")

        cs = DelCrawlerSession()
        cs.subscriberRestartInterval = 0

        cs.connect()
        cs.waitConnection()
        launch{
            while (true) {
                mongo.clearEvents()
                cs.waitConnection()
                val events = cs.allEvents
                var emptyListFlag: Boolean = events.size > 0

//                println("-------------------" + events.size + " events------------------------------------------------------------------")
                for (e in events) {

                    val rs = e.getRecords()



                    if (rs.isEmpty()) continue
//                    val state = e.getLiveState()


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
                        mongo.saveItem(item)
//                        println("(id:" + r.oddId + " Source:" + r.source + " TypeOdd:" + r.oddType + " TimeType:" + r.timeType + " TypePivot:" + r.pivotType + " PivotBias:" + r.pivotBias + " PivotValue:" + r.pivotValue + " RateOver:" +
//                                r.rateOver + " RateUnder:" +
//                                r.rateUnder + " RateEqual:" +
//                                r.rateEqual + ")")
                    }
                }
                if(emptyListFlag) {
                    var fork: Int = 0
                    val listPin = mongo.findByPin()
                    listPin.forEach { pinItem ->
                        val value = mongo.findForCheckValue(pinItem.idEvent, pinItem.idOdd)
                        value.forEach { valueItem ->
                            if (pinItem.timeType == valueItem.timeType &&
                                    pinItem.typePivot == valueItem.typePivot &&
                                    pinItem.pivotBias == valueItem.pivotBias &&
                                    pinItem.pivotValue == valueItem.pivotValue) {

                                try {
                                    if (((valueItem.rateOver / pinItem.rateOver) * 100) - 100 > 1.5) {
//                                        println("rateOver " + "id -" + valueItem.idOdd + " timeType -" + valueItem.timeType + " pivotBias -" + valueItem.pivotBias + " typePivot -" + valueItem.typePivot + " typeValue -" + valueItem.pivotValue)
//                                        println("PIN88 \t-\t " + valueItem.source)
//                                        println("" + pinItem.rateOver + "\t-\t" + valueItem.rateOver)
//                                        print("\n")
                                        api.checkAndPlaceBetTicket(valueItem, TargetPivot.OVER)
                                        fork++
                                    }
                                } catch (e: Exception) {
                                }
                                try {
                                    if (((valueItem.rateUnder / pinItem.rateUnder) * 100) - 100 > 1.5) {
//                                        println("rateUnder id -" + valueItem.idOdd + " timeType -" + valueItem.timeType + " pivotBias -" + valueItem.pivotBias + " typePivot -" + valueItem.typePivot + " typeValue -" + valueItem.pivotValue)
//                                        println("PIN88 \t-\t " + valueItem.source)
//                                        println("" + pinItem.rateUnder + "\t-\t" + valueItem.rateUnder)
//                                        print("\n")
                                        api.checkAndPlaceBetTicket(valueItem, TargetPivot.UNDER)
                                        fork++
                                    }
                                } catch (e: Exception) {
                                }
                                try {
                                    if (((valueItem.rateEqual / pinItem.rateEqual) * 100) - 100 > 1.5) {
//                                        println("rateEqual id -" + valueItem.idOdd + " timeType -" + valueItem.timeType + " pivotBias -" + valueItem.pivotBias + " typePivot -" + valueItem.typePivot + " typeValue -" + valueItem.pivotValue)
//                                        println("PIN88 \t-\t " + valueItem.source)
//                                        println("" + pinItem.rateEqual + "\t-\t" + valueItem.rateEqual)
//                                        print("\n")
                                        api.checkAndPlaceBetTicket(valueItem, TargetPivot.EQUAL)
                                        fork++
                                    }
                                } catch (e: Exception) {
                                }
                            }
                        }
                    }
                    println("-------------------" + fork + " FORK------------------------------------------------------------------")
                    TimeUnit.SECONDS.sleep(10L)
                } else {
                    TimeUnit.SECONDS.sleep(10L)
                }
            }
        }
//        val dft = DeltaFeedTracker()
//        cs.addDeltaEventHandler(dft)
    }

    @PreDestroy
    fun exit(){
        cs.disconnect()
    }

    enum class TargetPivot{
        OVER,
        UNDER,
        EQUAL
    }

}