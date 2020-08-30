package com.example.bukbot.data.SSEModel

import com.example.bukbot.data.models.WLDmodel

class AnalizeSSEModel(
        val bookList: Array<String>,
        val bookWLD: Array<WLDmodel>,
        val oddsList: Array<String>,
        val oddsWLD: Array<WLDmodel>
)