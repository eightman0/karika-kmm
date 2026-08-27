package karika.distribucija.ba.ui.view.salesrep.messages.admin

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.Message
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.SendMessageRequest
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesAdminNewMessageComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {

    // ── Compose form state ─────────────────────────────────────────────────────
    private val _subject = MutableStateFlow("")
    val subject = _subject.asStateFlow()

    // ── Conversation state (after first send) ──────────────────────────────────
    /** null = new message mode; non-null = conversation mode */
    private val _threadId = MutableStateFlow<String?>(null)
    val threadId = _threadId.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    val attachment = MutableStateFlow<Pair<String, ByteArray>?>(null)

    init {
        scope.launch {
            stateHolder.adminThreadPush.collect { pushedThreadId ->
                if (pushedThreadId == _threadId.value) loadMessages(pushedThreadId)
            }
        }
    }

    fun setSubject(v: String) { _subject.value = v }

    fun send(text: String) {
        val currentThread = _threadId.value
        val subj = _subject.value.trim()
        val msg = text.trim()
        if (msg.isBlank() && attachment.value == null) return

        scope.launch {
            messagesRepository.send(
                SendMessageRequest(
                    sendToAdmin = true,
                    message = msg,
                    subject = if (currentThread == null) subj else null,
                    receiverId = 0,
                    threadId = currentThread?.toIntOrNull(),
                    file = attachment.value
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        attachment.value = null
                        // On first send, grab threadId from response and switch to conversation mode
                        if (currentThread == null) {
                            val newThreadId = result.data.threadId
                            if (newThreadId != null) {
                                _threadId.value = newThreadId
                                loadMessages(newThreadId)
                                stateHolder.refreshAdminMessages()
                            }
                        } else {
                            loadMessages(currentThread)
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

    private fun loadMessages(threadId: String) {
        scope.launch {
            messagesRepository.get(threadId = threadId, admin = true).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit // don't show full-screen loader during reload
                    is ResultState.Success -> {
                        _messages.value = result.data.firstOrNull()?.messages?.firstOrNull()
                            ?: emptyList()
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
