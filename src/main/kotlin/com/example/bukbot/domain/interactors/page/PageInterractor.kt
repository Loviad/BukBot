package com.example.bukbot.domain.interactors.page

import com.example.bukbot.BukBotApplication
import com.example.bukbot.controller.page.PageController
import com.example.bukbot.data.SSEModel.AnalizeSSEModel
import com.example.bukbot.data.SSEModel.ConsoleMessage
import com.example.bukbot.data.SSEModel.ProgressAnalizeValue
import com.example.bukbot.data.SSEModel.ProgressTextValue
import com.example.bukbot.data.models.WLDmodel
import com.example.bukbot.service.events.IBetPlacingListener
import com.example.bukbot.utils.DatePatterns
import com.example.bukbot.utils.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct


@Component
class PageInterractor : CoroutineScope, IBetPlacingListener {

    @Autowired
    private lateinit var settings: Settings

    private var controller: PageController? = null
    override val coroutineContext = BukBotApplication.backgroundTaskDispatcher

    val MESSAGE = TypeMessage.MESSAGE
    val IMPORTANT = TypeMessage.IMPORTANT
    val ERROR = TypeMessage.ERROR
    val ACCEPT = TypeMessage.ACCEPT

    val messages: ArrayList<ConsoleMessage> = arrayListOf()

    @PostConstruct
    fun start() {
        settings.addSettingsEventListener(this)
    }

    override fun onChangeBetPlace(newValue: Boolean) {
        controller?.onStartBetting()
    }

    fun switchParse() {
        if (settings.getSnapState()) {
            settings.setGettingSnapshot(false)
        } else {
            settings.setGettingSnapshot(true)
        }
    }

    fun getSystemState(): Pair<Boolean, Boolean> {
        return settings.getGettingSnapshot() to settings.getBetPlacing()
    }

    fun switchBetting() {
        if (settings.getBettingState()) {
            settings.setBetPlacing(false)
        } else {
            settings.setBetPlacing(true)
        }
    }

    fun setControl(control: PageController) {
        controller = control
    }

    fun sendMessageConsole(message: String, type: TypeMessage = TypeMessage.MESSAGE) = launch {
        val mes = ConsoleMessage(
                DateTime.now().toString(DatePatterns.DAY_MONTH_YEAR_TIME),
                message,
                type.name
        )
        controller?.sendMessageConsole(
                mes
        )
        if (messages.count() > 19) {
            messages.removeAt(0)
        }
        messages.add(mes)
    }

    fun sendProgressValue(value: Int) {
        controller?.sendProgresValue(
                ProgressAnalizeValue(value)
        )
    }

    fun sendProgressMax(value: Int) {
        controller?.sendProgresValue(
                ProgressAnalizeValue(value)
        )
    }

    fun sendProgressText(value: String) {
        controller?.sendTextProgress(
                ProgressTextValue(value)
        )
    }


    fun sendAnalizeResult(bookList: Array<String>, bookWLD: Array<WLDmodel>, oddsList: Array<String>, oddsWLD: Array<WLDmodel>) {
        controller?.sendAnalizeResult(
                AnalizeSSEModel(
                        bookList,
                        bookWLD,
                        oddsList,
                        oddsWLD
                )
        )
    }

    enum class TypeMessage{
        MESSAGE,
        ERROR,
        IMPORTANT,
        ACCEPT
    }


}