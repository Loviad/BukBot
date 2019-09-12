package com.loviad.bukbot.utils

import java.util.concurrent.ThreadFactory

class BackgroundTaskThreadFactory : ThreadFactory {
    private var counter = 0

    override fun newThread(r: Runnable?): Thread {
        return Thread(r, "BackgroundTaskThread-${counter++}")
    }
}