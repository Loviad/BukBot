package com.example.bukbot.controller.vodds

import jayeson.lib.feed.api.*
import jayeson.lib.feed.core.B2Record
import jayeson.lib.sports.client.*
import org.slf4j.LoggerFactory
import java.util.*
import java.util.stream.Collectors

class PushModeHandler(private val identifier: String) : DeltaEventHandler<IBetMatch, IBetEvent, IBetRecord> {

    fun convertEpochToReadable(epoch: Long): String {
        val new_epoch = epoch * 1000
        val date = Date(new_epoch)
        return date.toString()
    }

    override fun onInsertMatch(event: InsertMatch<IBetMatch>) {
        val identifier = "PUSH_INSERT_MATCH"
        printMatch(identifier, event.get())
    }

    override fun onUpdateMatch(event: UpdateMatch<IBetMatch>) {
        val identifier = "PUSH_UPDATE_MATCH"
        printMatch(identifier, event.get())
    }

    override fun onDeleteMatch(event: DeleteMatch<IBetMatch>) {
        val identifier = "PUSH_DELETE_MATCH"
        printMatch(identifier, event.get())
    }

    override fun onInsertEvent(event: InsertEvent<IBetEvent>) {
        val identifier = "PUSH_INSERT_EVENT"
        printEvent(identifier, event.get())
    }

    override fun onUpdateEvent(event: UpdateEvent<IBetEvent>) {
        val identifier = "PUSH_UPDATE_EVENT"
        printEvent(identifier, event.get())
    }

    override fun onDeleteEvent(event: DeleteEvent<IBetEvent>) {
        val identifier = "PUSH_DELETE_EVENT"
        printEvent(identifier, event.get())
    }

    override fun onInsertOdd(event: InsertOdd<IBetRecord>) {
        val identifier = "PUSH_INSERT_ODD"
        printRecord(identifier, event.get())
    }

    override fun onUpdateOdd(event: UpdateOdd<IBetRecord>) {
        val identifier = "PUSH_UPDATE_ODD"
        printRecord(identifier, event.get())
    }

    override fun onDeleteOdd(event: DeleteOdd<IBetRecord>) {
        val identifier = "PUSH_DELETE_ODD"
        printRecord(identifier, event.get())
    }

    override fun onReset(resetKeys: Reset) {
        resetKeys.get().stream().forEach { key -> log.info("PUSH_RESET {}", key) }
    }

    override fun onSwitchFilterStart() {
        log.info("SWITCH_FILTER_START")
    }

    override fun onSwitchFilterEnd() {
        log.info("SWITCH_FILTER_END")
    }

    override fun onFullSnapshotStart() {
        log.info("FULLSNAPSHOT_START")
    }

    override fun onFullSnapshotEnd() {
        log.info("FULLSNAPSHOT_END")
    }

    override fun onSwitchFilterFail() {
        log.info("SWITCH_FILTER_FAIL")
    }

    private fun printRecord(identifier: String, collection: Collection<IBetRecord>) {
        collection.stream()
                .forEach { r ->
                    log.info(this.identifier + " " + identifier + " " + r.matchId() + "_" + r.eventId() + "_"
                            + r.source() + "_" + r.oddType() + "_" + r.id() + "_" + (r as B2Record).pivotType())
                }
    }

    private fun printEvent(identifier: String, collection: Collection<IBetEvent>) {
//        collection.stream().forEach { e ->
//            val keys = e.eventStates().stream().map<PartitionKey> { s -> (s as IBetEventState).partitionKey() }
//                    .collect<List<PartitionKey>, Any>(Collectors.toList<PartitionKey>()).toString()
//            log.info(this.identifier + " " + identifier + " " + e.matchId() + "_" + e.id() + "_" + keys)
//
//        }
    }

    private fun printMatch(identifier: String, collection: Collection<IBetMatch>) {
        for (match in collection) {
            // System.out.println("push mode: " + match.host() + ":" + match.guest());
        }
        collection.stream().forEach { m -> log.info(this.identifier + " " + identifier + " " + (m as IBetMatch).id() + " " + convertEpochToReadable(m.startTime())) }
    }

    companion object {
        private val log = LoggerFactory.getLogger(PushModeHandler::class.java)
    }

}
