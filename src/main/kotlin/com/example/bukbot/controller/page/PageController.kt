package com.example.bukbot.controller.page

import com.example.bukbot.BukBotApplication.Companion.backgroundTaskDispatcher
import com.example.bukbot.data.SSEModel.*
import com.example.bukbot.data.api.Response.openedbets.BetInfo
import com.example.bukbot.data.database.Dao.SettingsDao
import com.example.bukbot.data.database.Dao.ValueBetsItem
import com.example.bukbot.data.oddsList.PinOdd
import com.example.bukbot.data.telegram.models.IMessageData
import com.example.bukbot.data.telegram.models.loginInfo.LoginInfo
import com.example.bukbot.domain.interactors.auth.AuthInterractor
import com.example.bukbot.domain.interactors.page.PageInterractor
import com.example.bukbot.domain.interactors.vodds.VoddsInterractor
import com.example.bukbot.service.events.*
import com.example.bukbot.utils.CurrentState
import com.example.bukbot.utils.DatePatterns
import com.example.bukbot.utils.Settings
import com.example.bukbot.utils.getAccessToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import kotlin.collections.HashMap
import kotlin.math.abs


@Controller
class PageController : CoroutineScope,
        VoddsSnapshotListener,
        VoddsPlacingBetListener,
        VoddsFailureBetListener,
        VoddsMatchListUpdate,
        VoddsInsertOddEvent,
        ChangeSettingEvents,
        CurStateEvent{

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
    @Autowired
    private lateinit var settings: Settings
    @Autowired
    private lateinit var currentState: CurrentState


    private var uid = abs(UUID.randomUUID().hashCode()).toString()

    @PostConstruct
    fun init() {
        voddsInterractor.addEventListener(this)
        pageInterractor.setControl(this)
        currentState.addEventListener(this)
    }


    @GetMapping("/")
    fun index(model: Model, authentication: Authentication): String {
        val state = pageInterractor.getSystemState()
        model.addAttribute("id", authentication.name)
        model.addAttribute("stateParse", state.first)
        model.addAttribute("stateBetting", state.second)
        model.addAttribute("balance", currentState.state.balance)
        model.addAttribute("credit", currentState.state.credit)
        model.addAttribute("pl", currentState.state.pl)
        model.addAttribute("memory", currentState.state.memory)
        currentState.getOpenedBets()?.let {
            model.addAttribute("openedBets", it)
        }
        val o = currentState.getGraphicsFields()
        if (o.count() > 0) {
            model.addAttribute("labels", o.map { DateTime(it.id).toString(DatePatterns.DAY_MONTH_HOUR) })
            model.addAttribute("fields", o.map { it.count })
        }

        return "lte"
    }

    @GetMapping("/terminal")
    fun console(model: Model, authentication: Authentication): String {
        val state = pageInterractor.getSystemState()
        model.addAttribute("id", authentication.name)
        model.addAttribute("stateParse", state.first)
        model.addAttribute("stateBetting", state.second)
        model.addAttribute("balance", currentState.state.balance)
        model.addAttribute("credit", currentState.state.credit)
        model.addAttribute("pl", currentState.state.pl)
        model.addAttribute("memory", currentState.state.memory)
        model.addAttribute("messages", pageInterractor.messages)
        return "cons"
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
    @GetMapping("/analitics")
    fun analiticsPage(model: Model, authentication: Authentication): String {
        val state = pageInterractor.getSystemState()
        model.addAttribute("id", authentication.name)
        model.addAttribute("stateParse", state.first)
        model.addAttribute("stateBetting", state.second)
        model.addAttribute("balance", currentState.state.balance)
        model.addAttribute("credit", currentState.state.credit)
        model.addAttribute("pl", currentState.state.pl)
        model.addAttribute("memory", currentState.state.memory)
        return "analitics"
    }

    @GetMapping("/apitest")
    fun apiTestPage(model: Model, authentication: Authentication): String {
        val state = pageInterractor.getSystemState()
        model.addAttribute("id", authentication.name)
        model.addAttribute("stateParse", state.first)
        model.addAttribute("stateBetting", state.second)
        model.addAttribute("balance", currentState.state.balance)
        model.addAttribute("credit", currentState.state.credit)
        model.addAttribute("pl", currentState.state.pl)
        model.addAttribute("memory", currentState.state.memory)

        val tempSettings = settings.getSettings()

        model.addAttribute("url", tempSettings.urlApi)
        model.addAttribute("gold", tempSettings.gold)
        model.addAttribute("delta", tempSettings.deltaPIN88)
        model.addAttribute("minKef", tempSettings.minKef)
        model.addAttribute("minValue", tempSettings.minValue)
        model.addAttribute("maxValue", tempSettings.maxValue)
        model.addAttribute("saveBalance", tempSettings.saveBalance)
        model.addAttribute("balanceBetting", tempSettings.balanceBetting)
        model.addAttribute("creditBetting", tempSettings.creditBetting)
        model.addAttribute("autoStartParsing", tempSettings.autoStartParsing)
        model.addAttribute("autoStartBetting", tempSettings.autoStartBetting)
        return "apitest"
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
            "getToken" -> getToken()
            else -> Unit
        }
    }

    @PostMapping(path = ["/savesettings"])
    @ResponseStatus(HttpStatus.OK)
    fun savesettings(@RequestBody note: String,
                     @RequestParam(required = false) urlApi: String,
                     @RequestParam(required = false) gold: Double,
                     @RequestParam(required = false) deltaPIN: Double,
                     @RequestParam(required = false) minKef: Double,
                     @RequestParam(required = false) minValue: Double,
                     @RequestParam(required = false) maxValue: Double,
                     @RequestParam(required = false) saveBalance: Double,
                     @RequestParam(required = false) balanceBetting: Boolean,
                     @RequestParam(required = false) creditBetting: Boolean
    ) {
        settings.setSettings(urlApi, gold, deltaPIN, minKef, minValue, maxValue, balanceBetting, creditBetting, saveBalance)
    }

    @PostMapping(path = ["/initstatistic"])
    @ResponseStatus(HttpStatus.OK)
    fun initStatistic(@RequestBody note: String,
                     @RequestParam(required = false) range: String
    ) {
        currentState.getStatistic(range)
    }

    private fun getToken() {
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("currentToken")
                            .reconnectTime(20_000L)
                            .data(TokenMessage(getAccessToken()),
                                    MediaType.APPLICATION_JSON)
                    emitter.value.send(k)

                } catch (ioe: IOException) {
                    emittersData.remove(emitter.key)
                }
            }
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

    override suspend fun onMatchesUpdate(matches: List<Match>) {
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

    override suspend fun onPlaceEvent(bet: PlacingBet) {
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("placeBet")
                            .reconnectTime(20_000L)
                            .data(bet,
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

                } catch (ioe: Exception) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    override fun onChangeSettings(set: SettingsDao) {
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("changeSettings")
                            .reconnectTime(20_000L)
                            .data(set,
                                    MediaType.APPLICATION_JSON)
                    emitter.value.send(k)

                } catch (ioe: Exception) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    override fun updateState(bal: CurrentStateSSEModel) {
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("balance")
                            .reconnectTime(20_000L)
                            .data(bal, MediaType.APPLICATION_JSON)

                    emitter.value.send(k)

                } catch (ioe: Exception) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    override fun openedBets(openedBets: Array<BetInfo>?) {
        if (openedBets == null) return
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("openedBets")
                            .reconnectTime(20_000L)
                            .data(openedBets, MediaType.APPLICATION_JSON)

                    emitter.value.send(k)

                } catch (ioe: Exception) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    suspend fun sendMessageConsole(message: ConsoleMessage) {
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("consoleMessage")
                            .reconnectTime(20_000L)
                            .data(message, MediaType.APPLICATION_JSON)

                    emitter.value.send(k)

                } catch (ioe: Exception) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    fun sendAnalizeResult(analizeSSEModel: AnalizeSSEModel) {
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("analizeResult")
                            .reconnectTime(20_000L)
                            .data(analizeSSEModel, MediaType.APPLICATION_JSON)

                    emitter.value.send(k)

                } catch (ioe: Exception) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    fun sendProgresValue(progresVal: ProgressAnalizeValue) {
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("progressNow")
                            .reconnectTime(20_000L)
                            .data(progresVal, MediaType.APPLICATION_JSON)

                    emitter.value.send(k)

                } catch (ioe: Exception) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    fun sendProgresMax(progresVal: ProgressAnalizeValue) {
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("progressMax")
                            .reconnectTime(20_000L)
                            .data(progresVal, MediaType.APPLICATION_JSON)

                    emitter.value.send(k)

                } catch (ioe: Exception) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }

    fun sendTextProgress(progresText: ProgressTextValue) {
        emittersData.forEach { emitter ->
            nonBlockingService.execute {
                try {
                    val k = SseEmitter.event()
                            .name("progressText")
                            .reconnectTime(20_000L)
                            .data(progresText, MediaType.APPLICATION_JSON)

                    emitter.value.send(k)

                } catch (ioe: Exception) {
                    emittersData.remove(emitter.key)
                }
            }
        }
    }
}