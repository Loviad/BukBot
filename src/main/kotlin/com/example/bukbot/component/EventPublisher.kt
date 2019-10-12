package com.example.bukbot.component

import com.example.bukbot.data.telegram.models.loginInfo.LoginInfo
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.stereotype.Component

@Component
class EventPublisher : ApplicationEventPublisherAware {

    private var eventPublisher: ApplicationEventPublisher? = null

    fun sendAuthAccept() {
        LOGGER.info("Publish")

//        val info = LoginInfo("heap", "pass", "123123")
//        eventPublisher!!.publishEvent(info)
    }

    override fun setApplicationEventPublisher(eventPublisher: ApplicationEventPublisher) {
        this.eventPublisher = eventPublisher
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(EventPublisher::class.java)
    }
}