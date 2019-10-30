package com.example.bukbot.service.events

interface IGettingSnapshotListener: SettingEvents {
    fun onGettingSnapshotChange(newValue: Boolean)
}