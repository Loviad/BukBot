package com.example.bukbot.domain.interactors.browser

import com.example.bukbot.data.repositories.BrowserRepository
import com.example.bukbot.controller.browser.WebBrowser.State
import com.example.bukbot.controller.browser.events.BrowserEventListener
import com.example.bukbot.controller.browser.events.ParsingEventListener
import com.example.bukbot.controller.browser.events.TelegramEventListener
import com.example.bukbot.data.database.Dao.ValueBetsItem
import com.example.bukbot.data.repositories.ValueBetsItemRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class BrowserInterractor: IBrowserInterractor {
    @Autowired
    private lateinit var browser: BrowserRepository
    @Autowired
    private lateinit var valuebetsRepository: ValueBetsItemRepository

    private val eventListener = ArrayList<BrowserEventListener>()
    override fun addEventListener(listener: BrowserEventListener){
        eventListener.add(listener)
    }
    override fun removeEventListener(listener: BrowserEventListener){
        eventListener.remove(listener)
    }
    private inline fun <reified TEvent : BrowserEventListener> sendEvents(noinline sender: (TEvent) -> Unit) {
        eventListener.filterIsInstance<TEvent>().forEach { sender(it) }
    }
    override fun onChangeState(state: State) {
        sendEvents<ParsingEventListener> {
            it.onChangeStateBrowser(state)
        }
    }
    override fun getState(): State {
       return browser.getCurrentState()
    }

    override fun pushTelegramMessage(s: String) {
        sendEvents<TelegramEventListener> {
            it.pushMessage(s)
        }
    }

    override fun saveValuebetsItem(list: List<ValueBetsItem>) {
        valuebetsRepository.saveAll(list)
        sendEvents<ParsingEventListener> {
            it.onValueBetsItemsUpdate(list)
        }
    }
}