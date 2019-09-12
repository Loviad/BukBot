package com.loviad.bukbot.utils

import java.util.concurrent.ThreadFactory

class OrderedTaskThreadFactory(val name: String) : ThreadFactory {

    override fun newThread(r: Runnable?): Thread {
        return Thread(r, name)
    }
}