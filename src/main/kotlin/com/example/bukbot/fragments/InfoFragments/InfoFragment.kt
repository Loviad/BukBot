package com.example.bukbot.fragments.InfoFragments

import com.example.bukbot.controller.browser.WebBrowser.State
import com.example.bukbot.controller.browser.events.ParsingEventListener
import com.example.bukbot.controller.page.PageController
import com.example.bukbot.data.database.Dao.ValueBetsItem
import com.example.bukbot.domain.interactors.browser.IBrowserInterractor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class InfoFragment: ParsingEventListener {
    override fun showInfoMessage(s: String) {

    }

    override fun onValueBetsItemsUpdate(list: List<ValueBetsItem>) {
        pageController.sendValuebetsItemsUpdate(list)
    }

    override fun onChangeStateBrowser(state: State) {
        val className:String = when(state){
            State.LOAD_PAGE -> "btn-warning"
            State.PARSED -> "btn-primary"
            State.PAGE_LOADED -> "btn-success"
            State.AWAIT -> "btn-info"
            State.NOT_INIT -> "btn-danger"
            State.INITING -> "btn-info"
            State.ERROR_LOAD_PAGE -> "btn-danger"
            State.ERROR_DRIVER -> "btn-danger"
            State.PARSING -> "btn-warning"
            State.NEED_AUTH -> "btn-danger"
        }
        pageController.sendStatus(className)
    }


    @Autowired
    private lateinit var browser: IBrowserInterractor
    @Autowired
    private lateinit var pageController: PageController

    @PostConstruct
    fun init(){
        browser.addEventListener(this)
    }
}