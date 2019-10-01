package com.example.bukbot.controller.page

import com.example.bukbot.BukBotApplication.Companion.backgroundTaskDispatcher
import com.example.bukbot.data.telegram.models.IMessageData
import com.example.bukbot.data.telegram.models.loginInfo.LoginInfo
import com.example.bukbot.domain.interactors.auth.AuthInterractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.ui.Model
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashMap
import kotlin.math.abs


@Controller
class PageController: CoroutineScope {

    override val coroutineContext = backgroundTaskDispatcher

    private val emittersAuth = HashMap<String, SseEmitter>()
    private val emittersData = HashMap<String, SseEmitter>()
    private val nonBlockingService = Executors
            .newCachedThreadPool()
    private var currentUserLoginned: String =""

    @Autowired
    private lateinit var authInterractor: AuthInterractor

    private var uid = abs(UUID.randomUUID().hashCode()).toString()


    @GetMapping("/")
    fun index(model: Model, authentication: Authentication): String {
        model.addAttribute("id", authentication.name)
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
        emitter.onCompletion { emittersAuth.remove(id) }
        emittersAuth[id] = emitter
        uid = abs(UUID.randomUUID().hashCode()).toString()
        launch{authInterractor.sendAuthRequest()}
        return emitter
    }
//
//    @EventListener
//    fun handleEvents(messageData: IMessageData) {
//        emitters.forEach { emitter ->
//            nonBlockingService.execute {
//                try {
//                    emitter.value.send(emitter.key to abs(UUID.randomUUID().hashCode()).toString(),
//                            MediaType.APPLICATION_JSON)
//
//                } catch (ioe: IOException) {
//                    emitters.remove(emitter.key)
//                }
//            }
//        }
//    }

    fun sendLogin(messageData: IMessageData) = launch {
        if (messageData is LoginInfo){
            currentUserLoginned = messageData.user
            emittersAuth[messageData.getpageId()]?.let{
                nonBlockingService.execute {
                    try {
                        val k = SseEmitter.event()
                                .name("login")
                                .data(messageData,
                                        MediaType.APPLICATION_JSON)
                        it.send(k)

                    } catch (ioe: IOException) {
                        emittersAuth.remove(messageData.getpageId())
                    }
                    finally {
                        emittersAuth.forEach {
                            it.value.complete()
                        }
                        emittersAuth.clear()
                    }
                }
            }
        }
    }
}