package com.example.bukbot.domain.interactors.page

import com.example.bukbot.BukBotApplication
import com.example.bukbot.utils.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class PageInterractor: CoroutineScope {

    override val coroutineContext = BukBotApplication.backgroundTaskDispatcher
    @Autowired
    private lateinit var settings: Settings

    fun switchParse() {
        if (settings.getGettingSnapshot()){
            settings.setGettingSnapshot(false)
        } else {
            settings.setGettingSnapshot(true)
        }
    }

    fun getSystemState(): Pair<Boolean, Boolean>{
        return settings.getGettingSnapshot() to settings.getBetPlacing()
    }


}