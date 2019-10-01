package com.example.bukbot.utils.threadfabrick

import java.util.concurrent.ThreadFactory

class BrowserParserThreadFactory : ThreadFactory {
    override fun newThread(r: Runnable?): Thread {
        return Thread(r, "BrowserParserThreadFactory")
    }
}