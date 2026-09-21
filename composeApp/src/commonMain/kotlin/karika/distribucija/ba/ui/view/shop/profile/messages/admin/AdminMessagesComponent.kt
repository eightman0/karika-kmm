package karika.distribucija.ba.ui.view.shop.profile.messages.admin

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatConversation
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
    val _messages = MutableStateFlow<List<ChatConversation>>(emptyList())
    val messages = _messages.asStateFlow()

    init {
        init()
    }

    open fun init() {
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
            chatRepository.getConversations(axis = ChatAxis.CUSTOMER_ADMIN, pageSize = 100)
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
}
