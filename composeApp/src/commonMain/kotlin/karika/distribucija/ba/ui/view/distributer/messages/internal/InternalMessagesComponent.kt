package karika.distribucija.ba.ui.view.distributer.messages.internal

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.distributer.dashboard.DashConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InternalMessagesComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) :
    CommonComponent(componentContext, stateHolder) {

    private val _messages = MutableStateFlow<List<ChatConversation>>(emptyList())
    val messages = _messages.asStateFlow()

    init {
        init()
    }

    fun init() {
        val job = scope.launch {
            stateHolder.messageHandler.adminMessagesReloadState.collect {
                loadNextPage()
            }
        }

        lifecycle.doOnDestroy {
            job.cancel()
        }
    }

    override fun loadNextPage(reset: Boolean) {
        scope.launch {
            chatRepository.getConversations(axis = ChatAxis.STAFF, pageSize = 100)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _messages.update { result.data.items }
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }

    override fun navigateToMessagesOverview(item: ChatConversation) {
        dashNavigate(DashConfig.MessageOverview(item))
    }
}
