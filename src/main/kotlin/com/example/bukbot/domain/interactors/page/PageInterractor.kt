package com.example.bukbot.domain.interactors.page

import com.example.bukbot.BukBotApplication
import com.example.bukbot.controller.page.PageController
import com.example.bukbot.service.events.IBetPlacingListener
import com.example.bukbot.utils.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct


@Component
class PageInterractor: IBetPlacingListener {

    @Autowired
    private lateinit var settings: Settings

    private lateinit var controller: PageController


    @PostConstruct
    fun start(){
        settings.addSettingsEventListener(this)
    }

    override fun onChangeBetPlace(newValue: Boolean) {
        controller.onStartBetting()
    }

    fun switchParse() {
        if (settings.getSnapState()){
            settings.setGettingSnapshot(false)
        } else {
            settings.setGettingSnapshot(true)
        }
    }

    fun getSystemState(): Pair<Boolean, Boolean>{
        return settings.getGettingSnapshot() to settings.getBetPlacing()
    }

    fun switchBetting(){
        if(settings.getBettingState()){
            settings.setBetPlacing(false)
        } else {
            settings.setBetPlacing(true)
        }
    }

    fun setControl(control: PageController){
        controller = control
    }




}