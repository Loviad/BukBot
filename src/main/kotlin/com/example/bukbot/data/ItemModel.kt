package com.example.bukbot.data

import com.example.bukbot.controller.vodds.VoddsController
import com.example.bukbot.data.database.Dao.EventItem

class ItemModel(val ratePin: Double, val rateVal: Double, val kef: Double = -1000.0, val type: VoddsController.TargetPivot, val item: EventItem)