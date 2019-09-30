package com.example.bukbot.service.browser

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class BrowserRepository {
    @Autowired
    private lateinit var browserController: IBrowserController

    fun getCurrentState(): WebBrowser.State {
        return browserController.getCurrentState()
    }
}