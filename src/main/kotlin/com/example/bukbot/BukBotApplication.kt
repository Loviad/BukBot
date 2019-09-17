package com.example.bukbot

import ch.rasc.sse.eventbus.config.EnableSseEventBus
import com.loviad.bukbot.utils.BackgroundTaskThreadFactory
import com.loviad.bukbot.utils.OrderedTaskThreadFactory
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

@EnableScheduling
@SpringBootApplication
class BukBotApplication {

	companion object {
		var backgroundTaskDispatcher = ThreadPoolExecutor(
				2, 12, 60L,
				TimeUnit.SECONDS, SynchronousQueue<Runnable>(), BackgroundTaskThreadFactory()
		).asCoroutineDispatcher()

		var orderedBackgroundTaskDispatcher = Executors.newSingleThreadExecutor(
				OrderedTaskThreadFactory("OrderedBackgroundTaskThread")
		).asCoroutineDispatcher()
	}
}

fun main(args: Array<String>) {
	runApplication<BukBotApplication>(*args)
}
