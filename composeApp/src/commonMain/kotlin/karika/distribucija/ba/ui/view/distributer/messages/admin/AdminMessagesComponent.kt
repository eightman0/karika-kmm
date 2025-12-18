package karika.distribucija.ba.ui.view.distributer.messages.admin

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.MessagesRepository
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.distributer.dashboard.DashConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminMessagesComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    val searchText = mutableStateOf("")

    private val _messages = MutableStateFlow<List<Conversation>>(emptyList())
    val messages = _messages.asStateFlow()

    init {
        init()
    }

    fun init() {
        scope.launch {
            stateHolder.messageHandler.adminMessagesReloadState.collect {
                loadNextPage()
            }
        }
    }

    override fun loadNextPage(reset: Boolean) {
        if (loader.value) {
            return
        }

        scope.launch {
            MessagesRepository()
                .messages(true)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _messages.update { result.data.map { it.copy(admin = true) } }
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
        dashNavigate(DashConfig.MessageOverview(item))

        if (item.isRead()) {
            return
        }
        scope.launch {
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