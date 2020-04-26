package com.example.bukbot.controller.vodds

import jayeson.lib.feed.api.IBetEvent
import jayeson.lib.feed.soccer.SoccerMatch
import jayeson.lib.feed.soccer.SoccerRecord
import jayeson.lib.sports.client.*
import org.slf4j.LoggerFactory

class VoddsEventHandler(val controller: VoddsController): DeltaEventHandler<SoccerMatch, IBetEvent, SoccerRecord> {

    private val log = LoggerFactory.getLogger("VoddsEventHandler")
    override fun onUpdateOdd(updateOdd: UpdateOdd<SoccerRecord>?) {
        controller.updateOdd(updateOdd?.get()?.stream())
    }

//    override fun onInsertMatch(insertMatch: InsertMatch<SoccerMatch>?) {
////        controller.insertMatches(insertMatch?.get()?.stream())
//    }
//
//    override fun onDeleteMatch(deleteMatch: DeleteMatch<SoccerMatch>?) {
////        controller.deleteMatches(deleteMatch?.get()?.stream())
//    }

    override fun onInsertOdd(insertOdd: InsertOdd<SoccerRecord>?) {
        controller.insertPinOdd(insertOdd?.get()?.stream())
    }

//    override fun onInsertMatch(insertMatch: InsertMatch<IBetMatch>?) {
//        controller.insertMatches(insertMatch?.get()?.stream())
//    }
}