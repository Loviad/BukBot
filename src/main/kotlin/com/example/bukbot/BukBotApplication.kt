package com.example.bukbot

import ch.rasc.sse.eventbus.config.EnableSseEventBus
import com.example.bukbot.service.telegrambot.TelegramBot
import com.example.bukbot.utils.threadfabrick.ApiThreadFactory
import com.example.bukbot.utils.threadfabrick.StateThreadFactory
import com.loviad.bukbot.utils.BackgroundTaskThreadFactory
import com.loviad.bukbot.utils.OrderedTaskThreadFactory
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import org.springframework.boot.SpringApplication
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import javax.annotation.PostConstruct


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

		var orderedApiTaskDispatcher = Executors.newSingleThreadExecutor(
				ApiThreadFactory()
		).asCoroutineDispatcher()

		var orderedStateTaskDispatcher = Executors.newSingleThreadExecutor(
				StateThreadFactory()
		).asCoroutineDispatcher()
	}
}

fun main(args: Array<String>) {
	runApplication<BukBotApplication>(*args)
}
