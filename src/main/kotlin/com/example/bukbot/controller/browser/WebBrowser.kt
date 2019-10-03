package com.example.bukbot.controller.browser

import com.example.bukbot.BukBotApplication
import com.example.bukbot.data.database.Dao.ValueBetsItem
import com.example.bukbot.domain.interactors.browser.IBrowserInterractor
import com.example.bukbot.utils.threadfabrick.BrowserParserThreadFactory
import com.example.bukbot.utils.threadfabrick.BrowserThreadFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.CapabilityType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class WebBrowser : IBrowserController, CoroutineScope {

    @Autowired
    private lateinit var browserInterractor: IBrowserInterractor

    override val coroutineContext = BukBotApplication.backgroundTaskDispatcher
    var driver: ChromeDriver? = null

    private var authorization: Boolean = false
    private var valuebetsLoaded: Boolean = false

    private var driverState: State = State.NOT_INIT
        set(value) {
            field = value
            browserInterractor.onChangeState(value)
        }

    private val browserDispatcher = Executors.newSingleThreadExecutor(
            BrowserThreadFactory()
    ).asCoroutineDispatcher()

    private val parserDispatcher = Executors.newSingleThreadExecutor(
            BrowserParserThreadFactory()
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
        checkAuth()
        valueBets()
        parsingStart()
    }


    fun loadPage(page: String, classNameForWait: String, countReload: Int = 10) = launch(browserDispatcher) {
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

        driverState = if (!fined) {
            println("Страница загружена")
            State.PAGE_LOADED
        } else {
            println("Не могу загрузить страницу вилок")
            State.ERROR_LOAD_PAGE
        }
    }

    fun loadParsingPage() = launch(browserDispatcher) {
        driver?.get("https://www.allbestbets.com/arbs")
        TimeUnit.SECONDS.sleep(3L)
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

        driverState = if (!fined) {
            println("Страница загружена")
            State.PAGE_LOADED
        } else {
            println("Не могу загрузить страницу вилок")
            State.ERROR_LOAD_PAGE
        }
    }

    private fun checkAuth() = launch(browserDispatcher) {
        driverState = State.PARSING
        try {
            val k = driver?.findElementByXPath(
                    "//nav[contains(@class,'navbar-default')]//ul[contains(@class,'navbar-right')]/li/a[contains(@href,'/sign_in')]"
            )
            driverState = State.NEED_AUTH
            authorization = false
        } catch (e: NoSuchElementException) {
            authorization = true
        }
    }

    private fun valueBets() = launch(browserDispatcher) {
        if (!authorization) {
            browserInterractor.pushTelegramMessage("Autorization not approved")
            return@launch
        }
        driverState = State.LOAD_PAGE
        try {
            driver?.findElementByXPath(
                    "//nav[contains(@class,'navbar-default')]//ul[contains(@class,'navbar-right')]/li/a[contains(@href,'/valuebets')]"
            )?.click()
            TimeUnit.SECONDS.sleep(6L)
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

            driverState = if (!fined) {
                println("Страница загружена")
                valuebetsLoaded = true
                State.PAGE_LOADED
            } else {
                browserInterractor.pushTelegramMessage("Не могу загрузить страницу valuebets")
                valuebetsLoaded = false
                println("Не могу загрузить страницу valuebets")
                State.ERROR_LOAD_PAGE
            }
        } catch (e: NoSuchElementException) {
            browserInterractor.pushTelegramMessage("Не могу загрузить страницу valuebets")
            valuebetsLoaded = false
            driverState = State.ERROR_LOAD_PAGE
        }
    }

    private fun parsingStart() = launch(browserDispatcher) {
        var hashList: Int = 0
        while (valuebetsLoaded && authorization && driverState == State.PAGE_LOADED) {
            driverState = State.PARSING
            val container = driver?.findElementsByXPath(
                    "//div[@id='arbsScroll']/div[@class='scroller']/ul/li"
            )
            val listItems = ArrayList<ValueBetsItem>()
            container?.let {
                it.forEach { element ->
                    try {
                        Regex("[a-z0-9]{32}").find(element.getAttribute("class"))?.value?.let { id ->
                            val list = element.text.split("\n")
                            val comands = list[6].split(" - ")
                            listItems.add(
                                    ValueBetsItem(
                                            id,
                                            list[0].removeRange(list[0].length - 1, list[0].length).toDouble(),
                                            list[3],
                                            list[1],
                                            "${list[4]} ${list[5]}",
                                            comands[0], comands[1],
                                            list[7],
                                            list[8],
                                            list[9].toDouble(),
                                            DateTime.now(DateTimeZone.UTC)
                                    )
                            )
                        }
                    } catch (e: Exception){

                    }
                }
            }

            if(listItems.isNotEmpty() && hashList != listItems.hashCode()){
               browserInterractor.saveValuebetsItem(listItems)
            }
            driverState = State.AWAIT
            TimeUnit.SECONDS.sleep(60L)
            driverState = State.PAGE_LOADED
        }
    }


    override fun getCurrentState(): State {
        return driver?.let { driverState } ?: State.NOT_INIT
    }

    @PreDestroy
    fun close() {
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
        ERROR_DRIVER,
        PARSING,
        NEED_AUTH
    }
}