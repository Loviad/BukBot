package com.example.bukbot.utils

import com.example.bukbot.BukBotApplication
import com.example.bukbot.data.SSEModel.CurrentStateSSEModel
import com.example.bukbot.data.api.Response.openedbets.BetInfo
import com.example.bukbot.data.api.Response.openedbets.OpenedBet
import com.example.bukbot.service.events.CurStateEvent
import com.example.bukbot.service.events.VoddsEvents
import com.example.bukbot.service.rest.ApiClient
import com.example.bukbot.service.telegrambot.TelegramBot
import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    fun startChecking() = launch {
        var i = 0
        while (true) {
            api.getBalance(state).join()
            api.getOpenBets(openedBets).join()
            state.OB = openedBets.get()?.totalResults ?: 0
            hal?.let {
                state.memory = (((it.memory.available / 1024.0) / 1024.0) / 1024.0).round(2)
            }
            sendEvents<CurStateEvent> {
                it.updateState(state)
            }
            i++
            if ( i == 6) {
                sendStateToTelegram()
                i = 0
            }
            delay( 5 * 60 * 1000)
        }
    }

    fun sendStateToTelegram() {
        telegBot?.sendStateMessage(
                "Баланс: ${state.balance}\n"+
                        "Кредит: ${state.credit}\n"+
                        "Profit/Loss: ${state.pl}\n"+
                        "OP: ${state.OB}\n"+
                        "FreeMemory: ${state.memory}"
        )
    }

    fun getOpenedBets(): Array<BetInfo>? {
        return openedBets.value?.betInfos
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
}