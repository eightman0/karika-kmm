package karika.distribucija.ba.ui.view.salesrep.messages.admin

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.MessagesRepository
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.ui.view.salesrep.dashboard.SalesRepConfig
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesAdminMessagesComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {

    private val repository = MessagesRepository()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations = _conversations.asStateFlow()

    /** "all" | "sent" | "received" */
    private val _filter = MutableStateFlow("all")
    val filter = _filter.asStateFlow()

    init {
        load()
    }

    fun setFilter(f: String) {
        _filter.value = f
    }

    fun openConversation(conversation: Conversation) {
        salesRepPush(SalesRepConfig.AdminConversation(conversation.copy(admin = true)))
    }

    fun openNewMessage() {
        // TODO: navigate to new message compose
    }

    fun goBack() = salesRepBack()

    fun refresh() = load()

    private fun load() {
        scope.launch {
            repository.messages(admin = true).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _conversations.value = result.data.map { it.copy(admin = true) }
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
