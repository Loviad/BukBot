package com.example.bukbot.data.repositories

import com.example.bukbot.controller.browser.IBrowserController
import com.example.bukbot.controller.browser.WebBrowser
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