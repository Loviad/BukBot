package com.example.bukbot.utils

import com.example.bukbot.BukBotApplication
import com.example.bukbot.data.SSEModel.CurrentStateSSEModel
import com.example.bukbot.data.api.Response.openedbets.BetInfo
import com.example.bukbot.data.api.Response.openedbets.OpenedBet
import com.example.bukbot.data.database.Dao.OpenBets
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import oshi.SystemInfo
import oshi.hardware.HardwareAbstractionLayer
import javax.annotation.PostConstruct


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
    private val eventListener = ArrayList<VoddsEvents>()
    private val openedBets: SimpleObjectProperty<OpenedBet?> = SimpleObjectProperty(null)
    private var telegBot: TelegramBot? = null

    var si: SystemInfo? = null
    var hal: HardwareAbstractionLayer? = null

    val state = CurrentStateSSEModel()


    val canBetting: Boolean
        get() {
            return  if (settings.balanceBetting) {
                        state.balance
                    } else {0.0} +
                    if (settings.creditBetting) {
                        state.credit
                    } else {0.0} - settings.saveBalance - settings.getGold() >= 0.0
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
//                hal?.let {
//                    state.memory = (((it.memory.available / 1024.0) / 1024.0) / 1024.0).round(2)
//                }
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
            } catch (e:Exception) {
                pageInterractor.sendMessageConsole("EXEPT in STATE_CHECKER", pageInterractor.ERROR)
            }
            i++
            delay( 5 * 60 * 1000)
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
                "Баланс: ${state.balance}\n"+
                        "Кредит: ${state.credit}\n"+
                        "Profit/Loss: ${state.pl}\n"+
                        "OB: ${state.OB}\n"+
                        "FreeMemory: ${state.memory}"
        )
    }



    fun addEventListener(listener: VoddsEvents){
        eventListener.add(listener)
    }

    fun removeEventListener(listener: VoddsEvents){
        eventListener.remove(listener)
    }

    private suspend inline fun <reified TEvent : VoddsEvents> sendEvents(noinline sender: suspend (TEvent) -> Unit) {
        eventListener.filterIsInstance<TEvent>().forEach { sender(it) }
    }

    fun getStatistic(startDate: String, endDate: String) {
        val k = 1
    }
}