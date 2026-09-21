package karika.distribucija.ba.ui.view.salesrep.messages.customer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackCallback
import karika.distribucija.ba.domain.api.chatFileFrom
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.domain.model.ChatMessage
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesCustomerConversationComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    val conversation: ChatConversation
) : CommonComponent(componentContext, stateHolder) {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    val attachment = MutableStateFlow<Pair<String, ByteArray>?>(null)

    init {
        load()
        scope.launch {
            stateHolder.customerThreadPush.collect { conversationId ->
                if (conversationId == conversation.conversationId?.toString()) load()
            }
        }
        backHandler.register(BackCallback {
            if (stateHolder.imagePreview.value != null) {
                stateHolder.imagePreview.value = null
                return@BackCallback
            }

            super.salesRepBack()
        })
    }

    private fun load() {
        val conversationId = conversation.conversationId ?: return
        scope.launch {
            chatRepository.markRead(conversationId)
                .collect {
                    if (it is ResultState.Success) {
                        stateHolder.refreshCustomerMessages()
                        stateHolder.vendorNotificationHandler.reloadChatMessageCount()
                    }
                }

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
        if (text.isBlank() && attachment.value == null) return
        val files = attachment.value?.let { (name, bytes) -> listOf(chatFileFrom(name, bytes)) }
            ?: emptyList()

        scope.launch {
            chatRepository.sendMessage(conversationId, text, files).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        attachment.value = null
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

    fun pickFile() {
        stateHolder.handler.pickFile { name, data ->
            attachment.value = Pair(name, data)
        }
    }

    fun pickPhoto() {
        stateHolder.handler.pickPhoto { name, data ->
            attachment.value = Pair(name, data)
        }
    }

    fun goBack() = salesRepBack()
}
