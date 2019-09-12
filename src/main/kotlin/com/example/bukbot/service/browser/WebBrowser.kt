package com.example.bukbot.service.browser

import com.example.bukbot.BukBotApplication
import com.example.bukbot.utils.threadfabrick.BrowserThreadFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.CapabilityType
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class WebBrowser : CoroutineScope {

    override val coroutineContext = BukBotApplication.backgroundTaskDispatcher
    var driver: ChromeDriver? = null

    private var driverState: State = State.NOT_INIT
        set(value) {
            field = value
            println("State change to: ${value.name}")
            //TODO: отправка по рест смены состояния
        }

    private val browserDispatcher = Executors.newSingleThreadExecutor(
            BrowserThreadFactory()
    ).asCoroutineDispatcher()

    @PostConstruct
    fun start() {
        System.setProperty(
                "webdriver.chrome.driver",
                this::class.java.getResource("/webdriver/chromedriver").path
        )
        val options = ChromeOptions()
        options.addArguments("user-data-dir=/home/sergey/chromeProfile")
        options.setCapability(CapabilityType.PAGE_LOAD_STRATEGY, "eager")
        driver = ChromeDriver(options)
        loadParsingPage()
    }


    fun loadPage(page: String, classNameForWait: String, countReload: Int = 10) = launch {
        if(driverState == State.LOAD_PAGE) {
            println("Браузер занят")
        }
        driver?.get(page)
        var count = 0
        var fined = true
        while (count < countReload && fined) {
            driverState = State.LOAD_PAGE
            try {
                driver?.findElementByClassName(classNameForWait)
                fined = false
            } catch (e: Exception) {
                driverState = State.ERROR_LOAD_PAGE
                driver?.navigate()?.refresh()
                TimeUnit.SECONDS.sleep(10L)
            }
            count++
        }

        driverState = if(!fined){
            println("Страница загружена")
            State.PAGE_LOADED
        } else {
            println("Не могу загрузить страницу вилок")
            State.ERROR_LOAD_PAGE
        }
    }

    fun loadParsingPage() = launch(browserDispatcher) {
        driver?.get("file:///home/sergey/musor/testSite/index.html")
        var count = 0
        var fined = true
        while (count < 10 && fined) {
            driverState = State.LOAD_PAGE
            try {
                driver?.findElementByClassName("scroller")
                fined = false
            } catch (e: Exception) {
                driverState = State.ERROR_LOAD_PAGE
                driver?.navigate()?.refresh()
                TimeUnit.SECONDS.sleep(10L)
            }
            count++
        }

        driverState = if(!fined){
            println("Страница загружена")
            State.PAGE_LOADED
        } else {
            println("Не могу загрузить страницу вилок")
            State.ERROR_LOAD_PAGE
        }
    }

    @PreDestroy
    fun close(){
        driver?.close()
        driver?.quit()
    }

    enum class State {
        LOAD_PAGE,
        PARSED,
        PAGE_LOADED,
        AWAIT,
        NOT_INIT,
        INITING,
        ERROR_LOAD_PAGE,
        ERROR_DRIVER
    }
}