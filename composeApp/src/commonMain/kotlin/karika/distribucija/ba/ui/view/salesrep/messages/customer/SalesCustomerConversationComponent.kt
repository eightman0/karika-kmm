package karika.distribucija.ba.ui.view.salesrep.messages.customer

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.domain.model.Message
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.SendMessageRequest
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesCustomerConversationComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    val conversation: Conversation
) : CommonComponent(componentContext, stateHolder) {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    val attachment = MutableStateFlow<Pair<String, ByteArray>?>(null)

    init {
        load()
    }

    private fun load() {
        markAsReadMessage(conversation.id, admin = false)
        scope.launch {
            messagesRepository.get(
                threadId = conversation.id,
                admin = false
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _messages.value = result.data.firstOrNull()?.messages?.firstOrNull()
                            ?: emptyList()
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
        if (text.isBlank() && attachment.value == null) return
        scope.launch {
            messagesRepository.send(
                SendMessageRequest(
                    sendToAdmin = false,
                    message = text,
                    subject = conversation.subject ?: "",
                    receiverId = conversation.receiverId(),
                    threadId = conversation.id?.toIntOrNull(),
                    file = attachment.value
                )
            ).collect { result ->
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
