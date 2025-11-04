package karika.distribucija.ba.ui.view.main.profile.messages.admin

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

open class AdminMessagesComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) :
    CommonComponent(componentContext, stateHolder) {
    val _messages = MutableStateFlow<List<Conversation>>(emptyList())
    val messages = _messages.asStateFlow()

    init {
        init()
    }

    open fun init() {
        iOScope.launch {
            stateHolder.messageHandler.adminMessagesReloadState.collect {
                loadNextPage()
            }
        }
    }

    override fun loadNextPage(reset: Boolean) {
        iOScope.launch {
            messagesRepository.messages()
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _messages.update { result.data }
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }

    override fun navigateToMessagesOverview(item: Conversation) {
        super.navigateToMessagesOverview(item)
        if (item.isRead()) {
            return
        }
        iOScope.launch {
            messagesRepository.markAsRead(item.id)
                .collect {
                    if (item.admin) {
                        stateHolder.messageHandler.reloadAdminMessages()
                    } else {
                        stateHolder.messageHandler.reloadVendorMessages()
                    }
                }
        }
        stateHolder.customerNotificationHandler.notificationReceived()
    }
}