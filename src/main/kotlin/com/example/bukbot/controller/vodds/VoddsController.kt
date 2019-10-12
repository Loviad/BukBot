package com.example.bukbot.controller.vodds

import jayeson.lib.recordfetcher.DeltaCrawlerSession
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class VoddsController {

    //val systemProps = System.getProperties()
    init {
        System.setProperty("deltaCrawlerSessionConfigurationFile", "conf/deltaCrawlerSession.json");
        //systemProps["deltaCrawlerSessionConfigurationFile"] = "deltaCrawlerSession.json"
    }
    lateinit var cs: DelCrawlerSession

    @PostConstruct
    fun start(){
        //systemProps.put("deltaCrawlerSessionConfigurationFile", "/home/sergey/projects/BukBot/conf/deltaCrawlerSession.json")

        cs = DelCrawlerSession()
        cs.subscriberRestartInterval = 0

        cs.connect()
        cs.waitConnection()

        val dft = DeltaFeedTracker()
        cs.addDeltaEventHandler(dft)
    }

    @PreDestroy
    fun exit(){
        cs.disconnect()
    }

}