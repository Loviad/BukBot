package com.example.bukbot.controller.vodds

import com.example.bukbot.utils.Settings.PATH_TO_SETTING_API
import com.fasterxml.jackson.databind.ObjectMapper
import jayeson.lib.recordfetcher.*
import com.google.common.eventbus.Subscribe
import java.util.ArrayList
import jayeson.lib.datafetcher.core.events.ConnectedEvent
import jayeson.lib.datafetcher.core.events.StoppedEvent
import jayeson.lib.datafetcher.core.events.SubscriberEvent
import jayeson.lib.datastructure.SoccerEvent
import jayeson.lib.record.Record
import jayeson.lib.recordfetcher.DeltaConverterConfig.FS3
import jayeson.utility.JacksonConfig
import jayeson.utility.JacksonConfigFormat
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

open class DelCrawlerSession : CrawlerSession() {
    private var recordConverter: DeltaFeedConverter? = null

    open val allEvents: Collection<SoccerEvent>
        get() = (if (this.recordConverter == null) ArrayList() else this.recordConverter!!.allEvents) as Collection<SoccerEvent>

    var fS3UserName: String
        get() = this.getConfig().deltaRecordConverter.fs3Username
        set(fs3Username) {
            this.getConfig().deltaRecordConverter.fs3Username = fs3Username
        }

    var fS3Password: String
        get() = this.getConfig().deltaRecordConverter.fs3Password
        set(fs3Password) {
            this.getConfig().deltaRecordConverter.fs3Password = fs3Password
        }

    override fun getAllRecords(): Collection<Record>? {
        return null
    }

    override fun initializeConfiguration() {
        val mapper = ObjectMapper()
//        val k = System.getProperties()
        val user: DeltaCrawlerSessionConfig = mapper.readValue(File(PATH_TO_SETTING_API), DeltaCrawlerSessionConfig::class.java)
//        this.config = JacksonConfig.readConfig("deltaCrawlerSession.json", null, DeltaCrawlerSessionConfig::class.java, JacksonConfigFormat.JSON) as CrawlerSessionConfig
        this.config = user
        if (this.config == null) {
            log.error("Configuration file not found! Cannot find: \ndeltaCrawlerSession.json or \nconfiguration file defined at -DdeltaCrawlerSessionConfigurationFile\n in the class path or at the defined location or working directory!")
            log.info("using default config")
            this.config = DeltaCrawlerSessionConfig()
        }

    }

    override fun getConfig(): DeltaCrawlerSessionConfig {
        return super.getConfig() as DeltaCrawlerSessionConfig
    }

    fun addDeltaEventHandler(deh: DeltaEventHandler) {
        this.recordConverter!!.addDeltaEventHandler(deh)
    }

    @Subscribe
    override fun handleSubscriberEvent(event: SubscriberEvent?) {
        if (event is ConnectedEvent) {
            log.trace("Creating record converter!")
            if (this.recordConverter == null) {
                this.getSubscriber().removeChannel("delta")
                this.recordConverter = DeltaFeedConverter(this.getConfig().deltaRecordConverter)
                this.getSubscriber().addChannel("delta", this.recordConverter, true)
                this.recordConverter!!.start()
            }
        } else if (event is StoppedEvent) {
            this.getSubscriber().removeChannel("delta")
        }

    }

    fun getEvents(eventIdList: List<String>): Collection<SoccerEvent> {
        return (if (this.recordConverter == null) ArrayList() else this.recordConverter!!.getEvents(eventIdList)) as Collection<SoccerEvent>
    }

    override fun disconnect() {
        log.info("Crawler Session disconnecting")
        this.subscriber.disconnect()
        if (this.recordConverter != null) {
            println("Clear record")
            this.recordConverter!!.clearAllRecords()
            this.recordConverter!!.terminate()
            this.recordConverter = null
        } else {
            log.error("Record Concerter is null. It was not created before when the crawler is connected.")
        }

        this.refreshTimer.cancel()
        this.refreshTimer = null
    }

    override fun validateConnectionParams(username: String?, password: String?, connectionString: String?): Boolean {
        log.info("Validating MQ and FS3 params before connecting")
        if (username != null && connectionString != null) {
            if (this.getConfig().deltaRecordConverter.fs3List.isEmpty()) {
                log.error("No FS3s defined. Please add atleast one FS3")
                return false
            } else {
                return true
            }
        } else {
            log.error("No username or connection string is not defined to connect to MQ")
            return false
        }
    }

    fun enableDeltaEventHandling() {
        this.getConfig().deltaRecordConverter.isCallBackEnabled = true
    }

    fun disableDeltaEventHandling() {
        this.getConfig().deltaRecordConverter.isCallBackEnabled = false
    }

    fun addFS3(host: String, port: Int) {
        val fs3 = FS3()
        fs3.host = host
        fs3.port = port
        this.getConfig().deltaRecordConverter.fs3List.add(fs3)
    }

    companion object {
        private val log = LoggerFactory.getLogger(DeltaCrawlerSession::class.java)
        protected val defaultFileName = "deltaCrawlerSession.json"
        protected val systemVar = "deltaCrawlerSessionConfigurationFile"
    }
}
