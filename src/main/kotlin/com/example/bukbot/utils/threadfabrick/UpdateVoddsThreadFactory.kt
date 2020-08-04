package com.example.bukbot.utils.threadfabrick

import java.util.concurrent.ThreadFactory

class UpdateVoddsThreadFactory : ThreadFactory {
    override fun newThread(r: Runnable?): Thread {
        return Thread(r, "UpdateVoddsThreadFactory")
    }
}