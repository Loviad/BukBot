package com.example.bukbot.utils

import com.example.bukbot.BukBotApplication
import com.example.bukbot.BukBotApplication.Companion.backgroundTaskDispatcher
import com.example.bukbot.data.SSEModel.AnalizeSSEModel
import com.example.bukbot.data.SSEModel.CurrentStateSSEModel
import com.example.bukbot.data.api.Response.openedbets.BetInfo
import com.example.bukbot.data.api.Response.openedbets.OpenedBet
import com.example.bukbot.data.database.Dao.OpenBets
import com.example.bukbot.data.models.WLDmodel
import com.example.bukbot.data.repositories.OpenedBetsRepository
import com.example.bukbot.domain.interactors.page.PageInterractor
import com.example.bukbot.service.events.CurStateEvent
import com.example.bukbot.service.events.VoddsEvents
import com.example.bukbot.service.rest.ApiClient
import com.example.bukbot.service.telegrambot.TelegramBot
import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import oshi.SystemInfo
import oshi.hardware.HardwareAbstractionLayer
import javax.annotation.PostConstruct
import kotlin.math.floor


@Component
class CurrentState : CoroutineScope {
    @Autowired
    private lateinit var api: ApiClient

    @Autowired
    private lateinit var settings: Settings

    @Autowired
    private lateinit var openedBetsRepository: OpenedBetsRepository

    @Autowired
    private lateinit var pageInterractor: PageInterractor

    override val coroutineContext = BukBotApplication.orderedStateTaskDispatcher
    val orderedContext = backgroundTaskDispatcher
    private val eventListener = ArrayList<VoddsEvents>()
    private val openedBets: SimpleObjectProperty<OpenedBet?> = SimpleObjectProperty(null)
    private var telegBot: TelegramBot? = null

    var si: SystemInfo? = null
    var hal: HardwareAbstractionLayer? = null

    val state = CurrentStateSSEModel()


    val canBetting: Boolean
        get() {
            return if (settings.balanceBetting) {
                state.balance
            } else {
                0.0
            } +
                    if (settings.creditBetting) {
                        state.credit
                    } else {
                        0.0
                    } - settings.saveBalance - settings.getGold() >= 0.0
        }

    fun setTelegramBot(bot: TelegramBot) {
        telegBot = bot
    }

    @PostConstruct
    fun start() {
        si = SystemInfo()
        hal = si?.hardware
        startChecking()
    }

    fun startChecking() = launch(coroutineContext) {
        var i = 0
        api.getBalance(state).join()
        api.getOpenBets(openedBets).join()
        state.OB = openedBets.get()?.totalResults ?: 0
//        sendStateToTelegram()
        while (true) {
            try {
                state.OB = openedBets.get()?.totalResults ?: 0
                hal?.let {
                    state.memory = (((it.memory.available / 1024.0) / 1024.0) / 1024.0).round(2)
                }
                sendEvents<CurStateEvent> {
                    it.updateState(state)
                }

                if (i >= 12) {
                    i = 0
                    pageInterractor.sendMessageConsole("Send STATE to TELEGRAM", pageInterractor.IMPORTANT)
                    openedBetsRepository.saveOpenBets(state.OB)
                    sendStateToTelegram()
                }
                api.getBalance(state).join()
                api.getOpenBets(openedBets).join()
            } catch (e: Exception) {
                pageInterractor.sendMessageConsole("EXEPT in STATE_CHECKER", pageInterractor.ERROR)
            }
            i++
            delay(5 * 60 * 1000)
            pageInterractor.sendMessageConsole("TIMEOUT 5 MIN IS DELAY i=$i teleg is null= ${telegBot == null}", pageInterractor.IMPORTANT)
        }
    }

    fun getGraphicsFields(): Array<OpenBets> {
        return openedBetsRepository.findAllBets().sortedBy {
            it.id
        }.toTypedArray()
    }

    fun getOpenedBets(): Array<BetInfo>? {
        updateOpenBets()
        return openedBets.value?.betInfos
    }

    fun updateOpenBets() = runBlocking {
        api.getOpenBets(openedBets).join()
    }

    fun sendStateToTelegram() {
        telegBot?.sendStateMessage(
                "Баланс: ${state.balance}\n" +
                        "Кредит: ${state.credit}\n" +
                        "Profit/Loss: ${state.pl}\n" +
                        "OB: ${state.OB}\n" +
                        "FreeMemory: ${state.memory}"
        )
    }


    fun addEventListener(listener: VoddsEvents) {
        eventListener.add(listener)
    }

    fun removeEventListener(listener: VoddsEvents) {
        eventListener.remove(listener)
    }

    private suspend inline fun <reified TEvent : VoddsEvents> sendEvents(noinline sender: suspend (TEvent) -> Unit) {
        eventListener.filterIsInstance<TEvent>().forEach { sender(it) }
    }

    fun getStatistic(range: String) = launch(orderedContext) {
        val startAndEnd = range.split(" - ")
        val start = startAndEnd[0].split(".")  // mm.dd.yyyy
        val end = startAndEnd[1].split(".")  // mm.dd.yyyy
        val startDate = DateTime(
                start[2].toInt(),
                start[0].toInt(),
                start[1].toInt(),
                0, 0,
                DateTimeZone.UTC//now().zone
        )
        val endDate = DateTime(
                end[2].toInt(),
                end[0].toInt(),
                end[1].toInt(),
                0, 0,
                DateTimeZone.UTC//now().zone
        )
        val bets = api.getSetledBets(startDate.millis, endDate.millis)
        if (bets == null) {
            pageInterractor.sendMessageConsole("Запрос истории вернул нулевой результат", pageInterractor.ERROR)
            pageInterractor.sendProgressText("Запрос истории вернул нулевой результат")
            return@launch
        }
        bets.sortBy {
            it.serverOdd
        }
        // state 4 = WIN
        // state 5 = LOSS
        // state 7 = DRAW

        /** WIN LOSS DRAW */
        pageInterractor.sendProgressText("Разбираем результаты ставок")
        pageInterractor.sendProgressValue(0)
        pageInterractor.sendProgressMax(100)
        val winArray = bets.filter {
            it.betStatus == 4
        }
        pageInterractor.sendProgressValue(33)
        val lossArray = bets.filter {
            it.betStatus == 5
        }
        pageInterractor.sendProgressValue(66)
        val drawArray = bets.filter {
            it.betStatus == 7
        }
        pageInterractor.sendProgressValue(100)

        /** SPORTBOOK WIN LOSS DRAW */
        pageInterractor.sendProgressText("Разбираем результаты по конторам")
        pageInterractor.sendProgressValue(0)
        val sportBookListWLD: ArrayList<WLDmodel> = arrayListOf()
        val arraySportBook: ArrayList<String> = arrayListOf()
        var col = settings.sportbookList.count()
        pageInterractor.sendProgressMax(col)
        for (i in settings.sportbookList){
            arraySportBook.add(i)
            sportBookListWLD.add(
                    WLDmodel(
                            winArray.filter {
                                it.sportbook == i.toLowerCase()
                            }.count(),
                            lossArray.filter {
                                it.sportbook == i.toLowerCase()
                            }.count(),
                            drawArray.filter {
                                it.sportbook == i.toLowerCase()
                            }.count()
                    )
            )
            pageInterractor.sendProgressValue(settings.sportbookList.count() - col)
            col--
        }

        /** ODDS */
        pageInterractor.sendProgressText("Разбираем результаты по коэффициентам")
        pageInterractor.sendProgressValue(0)
        val min = (floor(bets.first().serverOdd!! * 10) / 10.0) + 1
        val max = (floor(bets.last().serverOdd!! * 10) / 10.0) + 1

        val arrayOddsList: ArrayList<String> = arrayListOf()
        val arrayOddsWLD: ArrayList<WLDmodel> = arrayListOf()
        var stepOdd = min
        col = ((max - min) / 0.1).toInt()
        val temp = col
        pageInterractor.sendProgressMax(col)
        while (stepOdd <= max){
            arrayOddsList.add(stepOdd.round(1).toString())
            arrayOddsWLD.add(
                    WLDmodel(
                            winArray.filter {
                                it.serverOdd!! >= stepOdd - 1.0 && it.serverOdd!! < stepOdd - 0.9
                            }.count(),
                            lossArray.filter {
                                it.serverOdd!! >= stepOdd - 1.0 && it.serverOdd!! < stepOdd - 0.9
                            }.count(),
                            drawArray.filter {
                                it.serverOdd!! >= stepOdd - 1.0 && it.serverOdd!! < stepOdd - 0.9
                            }.count()
                    )
            )
            stepOdd += 0.1
            pageInterractor.sendProgressValue(temp - col)
            col--
        }

        pageInterractor.sendAnalizeResult(
                arraySportBook.toTypedArray(),
                sportBookListWLD.toTypedArray(),
                arrayOddsList.toTypedArray(),
                arrayOddsWLD.toTypedArray()
        )
    }
}