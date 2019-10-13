package com.example.bukbot.controller.vodds

import com.example.bukbot.data.database.Dao.EventItem
import com.example.bukbot.data.repositories.EventItemRepository
import jayeson.lib.datastructure.SoccerEvent
import jayeson.lib.recordfetcher.DeltaCrawlerSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class VoddsController {

    @Autowired
    private lateinit var mongo: EventItemRepository

    //val systemProps = System.getProperties()
    init {
        System.setProperty("deltaCrawlerSessionConfigurationFile", "conf/deltaCrawlerSession.json");
        //systemProps["deltaCrawlerSessionConfigurationFile"] = "deltaCrawlerSession.json"
    }
    lateinit var cs: DelCrawlerSession


    fun start(){
        //systemProps.put("deltaCrawlerSessionConfigurationFile", "/home/sergey/projects/BukBot/conf/deltaCrawlerSession.json")

        cs = DelCrawlerSession()
        cs.subscriberRestartInterval = 0

        cs.connect()
        cs.waitConnection()
        while (true) {
            cs.waitConnection()
            val events = cs.allEvents

            println("-------------------" + events.size + " events------------------------------------------------------------------")
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
                                r.rateOver.toDouble(),
                                r.rateUnder.toDouble(),
                                r.rateEqual.toDouble()
                        )
                        mongo.saveItem(item)
                        println("(id:" + r.oddId + " Source:" + r.source + " TypeOdd:" + r.oddType + " TimeType:" + r.timeType + " TypePivot:" + r.pivotType + " PivotBias:" + r.pivotBias + " PivotValue:" + r.pivotValue + " RateOver:" +
                                r.rateOver + " RateUnder:" +
                                r.rateUnder + " RateEqual:" +
                                r.rateEqual + ")")
                    }
                    println("\n")
            }


            try {


            } catch (ex: Exception) {
                println("Exception")
                ex.printStackTrace()
            }

            try {
                Thread.sleep(10000)
            } catch (ex: Exception) {

            }

        }
//        val dft = DeltaFeedTracker()
//        cs.addDeltaEventHandler(dft)
    }

    @PreDestroy
    fun exit(){
        cs.disconnect()
    }

    @PostConstruct
    fun testMongo(){
        val listPin = mongo.findByPin()
        listPin.forEach{ pinItem ->
            val value = mongo.findForCheckValue(pinItem.idEvent, pinItem.idOdd)
            value.forEach{ valueItem ->
                if (pinItem.timeType == valueItem.timeType &&
                    pinItem.typePivot == valueItem.typePivot &&
                    pinItem.pivotBias == valueItem.pivotBias &&
                    pinItem.pivotValue == valueItem.pivotValue) {

                    try {
                        if (((valueItem.rateOver / pinItem.rateOver) * 100) - 100 > 1.5) {
                            println("rateOver event" + "id -" + valueItem.idOdd + " timeType -" + valueItem.timeType + " pivotBias -" + valueItem.pivotBias + " typePivot -" + valueItem.typePivot)
                            println("PIN88 \t-\t " + valueItem.source)
                            println("" + pinItem.rateOver + "\t-\t" + valueItem.rateOver)
                            print("\n")
                        }
                    } catch (e: Exception){}
                    try {
                        if (((valueItem.rateUnder/pinItem.rateUnder)*100)-100 > 1.5 ){
                            println("rateUnder id -" + valueItem.idOdd + " timeType -" + valueItem.timeType + " pivotBias -" + valueItem.pivotBias + " typePivot -" + valueItem.typePivot)
                            println("PIN88 \t-\t " + valueItem.source)
                            println("" + pinItem.rateUnder + "\t-\t" + valueItem.rateUnder)
                            print("\n")
                        }
                    } catch (e: Exception){}
                    try {
                        if (((valueItem.rateEqual/pinItem.rateEqual)*100)-100 > 1.5 ){
                            println("rateEqual id -" + valueItem.idOdd + " timeType -" + valueItem.timeType + " pivotBias -" + valueItem.pivotBias + " typePivot -" + valueItem.typePivot)
                            println("PIN88 \t-\t " + valueItem.source)
                            println("" + pinItem.rateEqual + "\t-\t" + valueItem.rateEqual)
                            print("\n")
                        }
                    } catch (e: Exception){}
                }
            }
        }

        try {
            Thread.sleep(600000)
        } catch (ex: Exception) {

        }
    }

}