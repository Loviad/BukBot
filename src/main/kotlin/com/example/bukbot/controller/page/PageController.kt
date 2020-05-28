package com.example.bukbot.controller.page

import com.example.bukbot.BukBotApplication.Companion.backgroundTaskDispatcher
import com.example.bukbot.data.SSEModel.MatchCrop
import com.example.bukbot.data.SSEModel.PlacingBet
import com.example.bukbot.data.SSEModel.SystemStateMessage
import com.example.bukbot.data.database.Dao.ValueBetsItem
import com.example.bukbot.data.oddsList.PinOdd
import com.example.bukbot.data.telegram.models.IMessageData
import com.example.bukbot.data.telegram.models.loginInfo.LoginInfo
import com.example.bukbot.domain.interactors.auth.AuthInterractor
import com.example.bukbot.domain.interactors.page.PageInterractor
import com.example.bukbot.domain.interactors.vodds.VoddsInterractor
import com.example.bukbot.service.events.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Controller
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs


@Controller
class PageController : CoroutineScope,
        VoddsSnapshotListener,
        VoddsPlacingBetListener,
        VoddsFailureBetListener,
        VoddsMatchListUpdate,
        VoddsInsertOddEvent{

    override val coroutineContext = backgroundTaskDispatcher

    private val emittersAuth = HashMap<String, SseEmitter>()
    private val emittersData = HashMap<String, SseEmitter>()
    private val nonBlockingService = Executors
            .newCachedThreadPool()
    private var currentUserLoginned: String = ""

    @Autowired
    private lateinit var authInterractor: AuthInterractor
    @Autowired
    private lateinit var pageInterractor: PageInterractor
    @Autowired
    private lateinit var voddsInterractor: VoddsInterractor


    private var uid = abs(UUID.randomUUID().hashCode()).toString()

    @PostConstruct
    fun init() {
        voddsInterractor.addEventListener(this)
        pageInterractor.setControl(this)
    }


    @GetMapping("/")
    fun index(model: Model, authentication: Authentication): String {
        val state = pageInterractor.getSystemState()
        val balance = voddsInterractor.getBalance()
        model.addAttribute("id", authentication.name)
        model.addAttribute("stateParse", state.first)
        model.addAttribute("stateBetting", state.second)
        model.addAttribute("balance", balance)
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
        launch { authInterractor.sendAuthRequest() }
        return emitter
    }

    @GetMapping("/data_listen/{id}")
    fun dataListener(@PathVariable("id") id: String): SseEmitter {
        val emitter = SseEmitter(180_000L)
        emitter.onTimeout { emitter.complete() }
        emitter.onCompletion { emittersData.remove(id) }
        emittersData[id] = emitter
        return emitter
    }

    @GetMapping("/valuebets")
    fun valuePage(model: Model, authentication: Authentication): String {
        model.addAttribute("id", authentication.name)
        return "valuebets"
    }

    @GetMapping("/apitest")
    fun apiTestPage(model: Model, authentication: Authentication): String {
        val state = pageInterractor.getSystemState()
        val balance = voddsInterractor.getBalance()
        model.addAttribute("id", authentication.name)
        model.addAttribute("stateParse", state.first)
        model.addAttribute("stateBetting", state.second)
        model.addAttribute("balance", balance)
        return "apitest"
    }

    @GetMapping("/lte")
    fun lteTheme(model: Model, authentication: Authentication): String {
        val state = pageInterractor.getSystemState()
        val balance = voddsInterractor.getBalance()
        model.addAttribute("id", authentication.name)
        model.addAttribute("stateParse", state.first)
        model.addAttribute("stateBetting", state.second)
        model.addAttribute("balance", balance)
        return "lte"
    }

    @GetMapping("/valuebets/{id}")
    fun valuePage(@PathVariable("id") id: String): SseEmitter {
        val emitter = SseEmitter(180_000L)
        emitter.onTimeout { emitter.complete() }
        emitter.onCompletion { emittersData.remove(id) }
        emittersData[id] = emitter
        return emitter
    }

    @PostMapping(path = ["/command"])
    @ResponseStatus(HttpStatus.OK)
    fun commandParse(@RequestBody note: String, @RequestParam(required = false) name: String) {
        when (name) {
            "switchParse" -> switchParse()
            "switchBetting" -> switchBetting()
            else -> Unit
        }
    }

    private fun switchParse() {
        pageInterractor.switchParse()
    }

    private fun switchBetting() {
        pageInterractor.switchBetting()
    }

    override fun onStartSnapshot() {
        val state = pageInterractor.getSystemState()
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("systemState")
                            .reconnectTime(20_000L)
                            .data(SystemStateMessage(state.first, state.second),
                                    MediaType.APPLICATION_JSON)
                    emitter.value.send(k)

                } catch (ioe: IOException) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    fun onStartBetting() {
        val state = pageInterractor.getSystemState()
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("systemState")
                            .reconnectTime(20_000L)
                            .data(SystemStateMessage(state.first, state.second),
                                    MediaType.APPLICATION_JSON)
                    emitter.value.send(k)

                } catch (ioe: IOException) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    override fun onFailureBet(txt: String) {
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("placeBet")
                            .reconnectTime(20_000L)
                            .data(txt,
                                    MediaType.APPLICATION_JSON)
                    emitter.value.send(k)

                } catch (ioe: IOException) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    override fun onPlacingBet(item: PlacingBet) {
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("placeBet")
                            .reconnectTime(20_000L)
                            .data(item,
                                    MediaType.APPLICATION_JSON)
                    emitter.value.send(k)

                } catch (ioe: IOException) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    override fun onStopSnapshot() {
        val state = pageInterractor.getSystemState()
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("systemState")
                            .reconnectTime(20_000L)
                            .data(SystemStateMessage(state.first, state.second),
                                    MediaType.APPLICATION_JSON)
                    emitter.value.send(k)

                } catch (ioe: IOException) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    fun sendLogin(messageData: IMessageData) = launch {
        if (messageData is LoginInfo) {
            currentUserLoginned = messageData.user
            emittersAuth[messageData.getpageId()]?.let {
                nonBlockingService.execute {
                    try {
                        val k = SseEmitter.event()
                                .name("login")
                                .data(messageData,
                                        MediaType.APPLICATION_JSON)
                        it.send(k)

                    } catch (ioe: IOException) {
                        emittersAuth.remove(messageData.getpageId())
                    } finally {
                        emittersAuth.forEach {
                            it.value.complete()
                        }
                        emittersAuth.clear()
                    }
                }
            }
        }
    }

    override suspend fun onMatchesUpdate(matches: List<MatchCrop>) {
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("matchUpdate")
                            .reconnectTime(20_000L)
                            .data(matches,
                                    MediaType.APPLICATION_JSON)
                    emitter.value.send(k)

                } catch (ioe: IOException) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    override suspend fun onInsertOdds(odds: List<PinOdd>) {
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("oddsInsert")
                            .reconnectTime(20_000L)
                            .data(odds,
                                    MediaType.APPLICATION_JSON)
                    emitter.value.send(k)

                } catch (ioe: IOException) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    override suspend fun onPinDownEvent(events: List<String>) {
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("pinDownEvent")
                            .reconnectTime(20_000L)
                            .data(events,
                                    MediaType.APPLICATION_JSON)
                    emitter.value.send(k)

                } catch (ioe: IOException) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    fun sendValuebetsItemsUpdate(list: List<ValueBetsItem>) {
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("valuebetsUpdate")
                            .reconnectTime(20_000L)
                            .data(list,
                                    MediaType.APPLICATION_JSON)
                    emitter.value.send(k)

                } catch (ioe: IOException) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    suspend fun sendSystemState(state: Pair<Boolean, Boolean>) {
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("systemState")
                            .reconnectTime(20_000L)
                            .data(SystemStateMessage(state.first, state.second),
                                    MediaType.APPLICATION_JSON)
                    emitter.value.send(k)

                } catch (ioe: IOException) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }



}