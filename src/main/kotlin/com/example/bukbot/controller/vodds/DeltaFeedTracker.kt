package com.example.bukbot.controller.vodds

import jayeson.lib.datastructure.SoccerEvent
import jayeson.lib.recordfetcher.DeltaEventHandler
import java.text.SimpleDateFormat
import java.util.*

class DeltaFeedTracker : DeltaEventHandler {
    override fun onNewEvents(newEvents: List<SoccerEvent>) {
//        println("Created Events:")
//        for (se in newEvents) {
//            println(String.format("Id %s \t-\t Host %s \t-\t Guest %s \t-\t League %s", se.eventId, se.host, se.guest, se.league))
//        }

    }

    override fun onChangedEvents(changedEvents: List<SoccerEvent>) {
//        println("Updated Events:")
//        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:SS z")
//        for (se in changedEvents) {
//            println(String.format("Id %s \t-\t Host %s \t-\t Guest %s \t-\t League %s", se.eventId, se.host, se.guest, se.league))
//
//            val state = se.liveState
//            val d = Date(state.startTime * 1000)
//            println(String.format("LiveState -- Starttime %d (%s) Source %s - Duration %d - Score %d-%d", state.startTime,
//                    dateFormat.format(d), state.source, state.duration, state.hostPoint, state.guestPoint))
//        }

    }

    override fun onDeletedEvents(deletedEvents: List<SoccerEvent>) {
//        println("Deleted Events:")
//        for (se in deletedEvents) {
//            println(String.format("Id %s \t-\t Host %s \t-\t Guest %s \t-\t League %s", se.eventId, se.host, se.guest, se.league))
//        }
//

    }

    override fun onChangedOdds(changedOdds: List<SoccerEvent>) {
//        println("Updated Odds")
//        for (se in changedOdds) {
//            println(String.format("Id %s \t-\t Host %s \t-\t Guest %s \t-\t League %s", se.eventId, se.host, se.guest, se.league))
//            val rs = se.records
//            for (r in rs) {
//                println("(" + r.oddId + " " + r.source + " " + r.oddType + " " + r.pivotType + " " + r.pivotValue + " " + r.pivotString + " " +
//                        " " + r.rateOverUid + " " + r.rateOver + " " +
//                        r.rateUnderUid + " " + r.rateUnder + " " +
//                        r.rateEqualUid + " " + r.rateEqual + ")")
//            }
//        }

    }

    override fun onDeletedOdds(deletedOdds: List<SoccerEvent>) {
//        println("Delteted Odds")
//        for (se in deletedOdds) {
//            println(String.format("Id %s \t-\t Host %s \t-\t Guest %s \t-\t League %s", se.eventId, se.host, se.guest, se.league))
//            val rs = se.records
//            for (r in rs) {
//                println("(" + r.oddId + " " + r.source + " " + r.oddType + " " + r.pivotType + " " + r.pivotValue + " " + r.pivotString + " " +
//                        " " + r.rateOverUid + " " + r.rateOver + " " +
//                        r.rateUnderUid + " " + r.rateUnder + " " +
//                        r.rateEqualUid + " " + r.rateEqual + ")")
//            }
//        }
    }

    override fun onRefreshedOdds(refreshedSources: List<String>) {
//        println("Refreshed Odds")
//        for (source in refreshedSources)
//            println(String.format(" Source %s Refreshed", source))


    }

    override fun onNewOdds(newOdds: List<SoccerEvent>) {
        //println("New Odds")
        for (se in newOdds) {
            if(se.host == "Sk Sokol Brozany") {
                println(String.format("Id %s \t-\t Host %s \t-\t Guest %s \t-\t League %s \t-\t ",
                        se.eventId,
                        se.host,
                        se.guest,
                        se.league
                ))
                val rs = se.records
                for (r in rs) {
                    println("(id:" + r.oddId + " Source:" + r.source + " Type:" + r.oddType + " TypePivot:" + r.pivotType + " PivotValue:" + r.pivotValue + " RateOver:" +
                            r.rateOver + " RateUnder:" +
                            r.rateUnder + " RateEqual:" +
                            r.rateEqual + ")")
                }
            }
        }

    }

    override fun onSecondaryLiveStateChange(changedEvents: List<SoccerEvent>) {
//        println("Secondary live state changes")
//        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:SS z")
//        for (se in changedEvents) {
//            println(String.format("Id %s \t-\t Host %s \t-\t Guest %s \t-\t League %s", se.eventId, se.host, se.guest, se.league))
//
//            val states = se.allLiveState
//            for (state in states) {
//                val d = Date(state.startTime * 1000)
//                println(String.format("LiveState -- Starttime %d (%s) Source %s - Duration %d - Score %d-%d", state.startTime,
//                        dateFormat.format(d), state.source, state.duration, state.hostPoint, state.guestPoint))
//
//            }
//        }

    }
}