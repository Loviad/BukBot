package com.example.bukbot.service.events

import com.example.bukbot.data.SSEModel.CurrentStateSSEModel
import com.example.bukbot.data.api.Response.openedbets.BetInfo
import com.example.bukbot.data.models.CurrentBalance

interface CurStateEvent:VoddsEvents {
    fun updateState(bal: CurrentStateSSEModel)
    fun openedBets(openedBets: Array<BetInfo>?)
}