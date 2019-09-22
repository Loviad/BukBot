package com.example.bukbot.controller

import com.example.bukbot.component.EventPublisher
import com.example.bukbot.model.IMessageData
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.http.MediaType
import org.springframework.ui.Model
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashMap
import kotlin.math.abs


@Controller
class AuthController {

    @Autowired
    private lateinit var publisher: EventPublisher

    private val emitters = HashMap<String, SseEmitter>()
    private val nonBlockingService = Executors
            .newCachedThreadPool()

    private var uid = abs(UUID.randomUUID().hashCode()).toString()


    @GetMapping("/")
    fun list(): String {
        return "index"
    }

    @GetMapping("/login")
    fun loginPage(model: Model): String {
        model.addAttribute("id", uid)
        return "login"
    }

    @GetMapping("/whait_auth/{id}")
    fun register(@PathVariable("id") id: String): SseEmitter {
        val emitter = SseEmitter(180_000L)
        emitter.onTimeout { emitter.complete() }
        emitter.onCompletion { emitters.remove(id) }
        emitters[id] = emitter
        uid = abs(UUID.randomUUID().hashCode()).toString()
        return emitter
    }

    @EventListener
    fun handleEvents(messageData: IMessageData) {

        emitters.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    emitter.value.send(emitter.key to abs(UUID.randomUUID().hashCode()).toString(),
                            MediaType.APPLICATION_JSON)

                } catch (ioe: IOException) {
                    emitters.remove(emitter.key)
                }
            }
        }
    }
}