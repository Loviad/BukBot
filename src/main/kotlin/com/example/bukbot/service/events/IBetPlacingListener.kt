package com.example.bukbot.service.events

interface IBetPlacingListener: SettingEvents {
    fun onChangeBetPlace(newValue: Boolean)
}