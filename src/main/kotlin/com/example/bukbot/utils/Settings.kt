package com.example.bukbot.utils

import com.example.bukbot.data.database.Dao.SettingsDao
import com.example.bukbot.data.repositories.SettingsRepository
import com.example.bukbot.service.events.ChangeSettingEvents
import com.example.bukbot.service.events.IBetPlacingListener
import com.example.bukbot.service.events.IGettingSnapshotListener
import com.example.bukbot.service.events.SettingEvents
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestParam
import javax.annotation.PostConstruct


@Component
class Settings {
    @Autowired
    private lateinit var repository: SettingsRepository

    private var BET_PLACING: Boolean = false                                                                            //делать ставки
    private var GETTING_SNAPSHOT: Boolean = false                                                                       //запрашивать события с VODDS
    private var GOLD: Double = 25.0
    var urlApi: String = ""
    var deltaPIN88: Double = 0.05
    var minKef: Double = 2.0
    var minValue: Double = 0.5
    var maxValue: Double = 20.0
    var autoStartParsing: Boolean = false
    var autoStartBetting: Boolean = false
    var balanceBetting: Boolean = false
    var creditBetting: Boolean = false
    var saveBalance: Double = 0.0

    private var notPaused: Boolean = true

//    var urlApi: String = "https://biweb-unity.stagingunity.com/v2"

    private val eventListener = ArrayList<SettingEvents>()

    @PostConstruct
    fun initializer() {
        val setting = getSettings()
        setGold(setting.gold)
        urlApi = setting.urlApi
        deltaPIN88 = setting.deltaPIN88
        minKef = setting.minKef
        minValue = setting.minValue
        maxValue = setting.maxValue
        saveBalance = setting.saveBalance
        if (setting.autoStartParsing) setGettingSnapshot(setting.autoStartParsing)
        autoStartParsing = setting.autoStartParsing
        if (setting.autoStartBetting) setBetPlacing(setting.autoStartBetting)
        autoStartBetting = setting.autoStartBetting
        balanceBetting = setting.balanceBetting
        creditBetting = setting.creditBetting
    }

    fun setBetPlacing(value: Boolean){
        BET_PLACING = value
        sendEvents<IBetPlacingListener> {
            it.onChangeBetPlace(value)
        }
    }

    fun getGold(): Double = GOLD
    fun getGoldF(): Float = GOLD.toFloat()

    fun setGold(value: Double) {
        GOLD = value
    }

    fun getBetPlacing(): Boolean = BET_PLACING && notPaused

    fun setGettingSnapshot(value: Boolean){
        GETTING_SNAPSHOT = value
        sendEvents<IGettingSnapshotListener> {
            it.onGettingSnapshotChange(value)
        }
    }

    fun pause(){
        notPaused = false
    }

    fun start() {
        notPaused = true
        sendEvents<IGettingSnapshotListener> {
            if (GETTING_SNAPSHOT) it.onGettingSnapshotChange(true)
        }
    }

    fun getGettingSnapshot(): Boolean = GETTING_SNAPSHOT && notPaused

    fun getSnapState(): Boolean = GETTING_SNAPSHOT

    fun getBettingState(): Boolean = BET_PLACING

    val sportbookList = listOf("ISN", "SBC", "SBO", "CROWN")

    fun addSettingsEventListener(listener: SettingEvents){
        eventListener.add(listener)
    }

    fun removeAuthEventListener(listener: SettingEvents){
        eventListener.remove(listener)
    }

    private inline fun <reified TEvent : SettingEvents> sendEvents(noinline sender: (TEvent) -> Unit) {
        eventListener.filterIsInstance<TEvent>().forEach { sender(it) }
    }

    fun setSettings(
            urlApi: String,
            gold: Double,
            deltaPIN: Double,
            minKef: Double,
            minValue: Double,
            maxValue: Double,
            balanceBetting: Boolean,
            creditBetting: Boolean,
            saveBalance: Double
    ){
        repository.saveSettings(
                SettingsDao(
                        gold = gold,
                        urlApi = urlApi,
                        deltaPIN88 = deltaPIN88,
                        minKef = minKef,
                        minValue = minValue,
                        maxValue = maxValue,
                        balanceBetting = balanceBetting,
                        creditBetting = creditBetting,
                        saveBalance = saveBalance
                )
        )
        this.urlApi = urlApi
        this.GOLD = gold
        this.deltaPIN88 = deltaPIN
        this.minKef = minKef
        this.minValue = minValue
        this.maxValue = maxValue
        this.balanceBetting = balanceBetting
        this.creditBetting = creditBetting
        this.saveBalance = saveBalance

        sendEvents<ChangeSettingEvents> {
            it.onChangeSettings(repository.getSettings())
        }
    }


    fun getSettings(): SettingsDao {
        return repository.getSettings()
    }
}