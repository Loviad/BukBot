package com.example.bukbot.data.browsermodels

import com.example.bukbot.data.telegram.models.IMessageData

class StatusBrowser(val value: String): IMessageData {
    override val type: String = "StatusBrowser"
}