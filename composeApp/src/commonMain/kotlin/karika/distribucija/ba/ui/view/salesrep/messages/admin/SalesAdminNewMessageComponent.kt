package karika.distribucija.ba.ui.view.salesrep.messages.admin

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.chatFileFrom
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatMessage
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesAdminNewMessageComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {

    // ── Conversation state (after first send) ──────────────────────────────────
    /** null = new message mode; non-null = conversation mode */
    private val _conversationId = MutableStateFlow<Long?>(null)
    val conversationId = _conversationId.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    val attachment = MutableStateFlow<Pair<String, ByteArray>?>(null)

    init {
        scope.launch {
            stateHolder.adminThreadPush.collect { pushedId ->
                if (pushedId == _conversationId.value?.toString()) loadMessages(pushedId.toLong())
            }
        }
    }

    fun send(text: String) {
        val msg = text.trim()
        if (msg.isBlank() && attachment.value == null) return
        val currentId = _conversationId.value

        if (currentId == null) {
            startAndSend(msg)
            return
        }

        doSend(currentId, msg)
    }

    private fun startAndSend(msg: String) {
        scope.launch {
            chatRepository.startConversation(ChatAxis.VENDOR_ADMIN).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        val newId = result.data.conversationId
                        if (newId != null) {
                            _conversationId.value = newId
                            stateHolder.refreshAdminMessages()
                            doSend(newId, msg)
                        } else {
                            hideLoader()
                        }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    private fun doSend(conversationId: Long, msg: String) {
        val files = attachment.value?.let { (name, bytes) -> listOf(chatFileFrom(name, bytes)) }
            ?: emptyList()

        scope.launch {
            chatRepository.sendMessage(conversationId, msg, files).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        attachment.value = null
                        loadMessages(conversationId)
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    private fun loadMessages(conversationId: Long) {
        scope.launch {
            chatRepository.getMessages(conversationId).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> {
                        _messages.value = result.data.items
                    }

                    is ResultState.Error -> showErrorMessage(result.message)
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
