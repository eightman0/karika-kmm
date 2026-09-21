package karika.distribucija.ba.ui.view.salesrep.messages.internal

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.domain.model.ChatMessage
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorOperationsMe
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesInternalConversationComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    val conversation: ChatConversation
) : CommonComponent(componentContext, stateHolder) {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    init {
        scope.launch {
            stateHolder.customerThreadPush.collect {
                load()
            }
        }

        load()
    }

    private fun load() {
        markRead()
        val conversationId = conversation.conversationId ?: return
        scope.launch {
            chatRepository.getMessages(conversationId).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _messages.value = result.data.items
                    }
                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val conversationId = conversation.conversationId ?: return
        val msg = text.trim()
        if (msg.isBlank()) return
        scope.launch {
            chatRepository.sendMessage(conversationId, msg).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        load()
                    }
                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    private fun markRead() {
        val conversationId = conversation.conversationId ?: return
        scope.launch {
            chatRepository.markRead(conversationId).collect {
                stateHolder.refreshInternalMessages()
                stateHolder.vendorNotificationHandler.reloadChatMessageCount()
            }
        }
    }

    fun isMine(message: ChatMessage, me: VendorOperationsMe): Boolean {
        val myRefId = if (me.isVendorOwner) me.vendorId ?: 0L else me.employeeId ?: 0L
        return message.senderRefId == myRefId
    }

    fun goBack() = salesRepBack()
}
