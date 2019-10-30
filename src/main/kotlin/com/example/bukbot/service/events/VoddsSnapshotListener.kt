package com.example.bukbot.service.events

interface VoddsSnapshotListener: VoddsEvents {
    fun onStopSnapshot()
    fun onStartSnapshot()
}