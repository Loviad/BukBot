package com.example.bukbot.service.browser

import com.example.bukbot.service.browser.events.BrowserEventListener
import com.example.bukbot.service.browser.events.ParsingEventListener
import com.example.bukbot.service.browser.events.TelegramEventListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class BrowserInterractor: IBrowserInterractor {
    @Autowired
    private lateinit var browser: BrowserRepository

    private val eventListener = ArrayList<BrowserEventListener>()
    fun addAuthEventListener(listener: BrowserEventListener){
        eventListener.add(listener)
    }
    fun removeAuthEventListener(listener: BrowserEventListener){
        eventListener.remove(listener)
    }
    private inline fun <reified TEvent : BrowserEventListener> sendEvents(noinline sender: (TEvent) -> Unit) {
        eventListener.filterIsInstance<TEvent>().forEach { sender(it) }
    }
    override fun onChangeState(state: String) {
        sendEvents<ParsingEventListener> {
            it.onChangeStateBrowser(state)
        }
    }
    override fun getState(): WebBrowser.State {
       return browser.getCurrentState()
    }

    override fun pushTelegramMessage(s: String) {
        sendEvents<TelegramEventListener> {
            it.pushMessage(s)
        }
    }
}