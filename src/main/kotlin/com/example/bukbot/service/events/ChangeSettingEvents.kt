package com.example.bukbot.service.events

import com.example.bukbot.data.database.Dao.SettingsDao

interface ChangeSettingEvents: SettingEvents {
    fun onChangeSettings(set: SettingsDao)
}