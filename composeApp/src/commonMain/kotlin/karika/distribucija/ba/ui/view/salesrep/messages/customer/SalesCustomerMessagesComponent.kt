package karika.distribucija.ba.ui.view.salesrep.messages.customer

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.salesrep.dashboard.SalesRepConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesCustomerMessagesComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {

    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations = _conversations.asStateFlow()

    init {
        load()
        scope.launch {
            stateHolder.refreshCustomerMessages.collect { load() }
        }
    }

    fun openConversation(conversation: ChatConversation) {
        salesRepPush(SalesRepConfig.CustomerConversation(conversation))
    }

    fun openNewMessage() {
        salesRepPush(SalesRepConfig.CustomerNewMessage())
    }

    fun goBack() = salesRepBack()

    fun refresh() = load()

    private fun load() {
        scope.launch {
            chatRepository.getConversations(axis = ChatAxis.VENDOR_CUSTOMER, pageSize = 100)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _conversations.value = result.data.items
                        }
                        is ResultState.Error -> {
                            hideLoader()
                            showErrorMessage(result.message)
                        }
                    }
                }
        }
    }
}
