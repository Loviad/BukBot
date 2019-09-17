package com.example.bukbot.component

import com.example.bukbot.model.MemoryInfo
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.lang.management.ManagementFactory

@Component
class MemoryUsagePublisher : ApplicationEventPublisherAware {

    private var eventPublisher: ApplicationEventPublisher? = null

    @Scheduled(fixedDelay = 5000)
    internal fun memoryUsage() {
        LOGGER.info("Publish")

        val info = MemoryInfo("heap", "nonHeap")
        eventPublisher!!.publishEvent(info)
    }

    override fun setApplicationEventPublisher(eventPublisher: ApplicationEventPublisher) {
        this.eventPublisher = eventPublisher
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MemoryUsagePublisher::class.java)
    }
}