package com.example.bukbot.utils.threadfabrick

import java.util.concurrent.ThreadFactory

class VoddsThreadFactory : ThreadFactory {
    override fun newThread(r: Runnable?): Thread {
        return Thread(r, "BrowserThreadFactory")
    }
}