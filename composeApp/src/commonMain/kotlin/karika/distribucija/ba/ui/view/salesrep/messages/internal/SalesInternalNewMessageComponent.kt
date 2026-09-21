package karika.distribucija.ba.ui.view.salesrep.messages.internal

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatMessage
import karika.distribucija.ba.domain.model.ChatRecipient
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorOperationsMe
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesInternalNewMessageComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {

    private val _recipientSearch = MutableStateFlow("")
    val recipientSearch = _recipientSearch.asStateFlow()

    private var allRecipients: List<ChatRecipient> = emptyList()

    private val _filteredRecipients = MutableStateFlow<List<ChatRecipient>>(emptyList())
    val filteredRecipients = _filteredRecipients.asStateFlow()

    private val _selectedRecipient = MutableStateFlow<ChatRecipient?>(null)
    val selectedRecipient = _selectedRecipient.asStateFlow()

    private val _conversationId = MutableStateFlow<Long?>(null)
    val conversationId = _conversationId.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    init {
        loadRecipients()
    }

    fun setRecipientSearch(text: String) {
        _recipientSearch.value = text
        _selectedRecipient.value = null
        val query = text.trim().lowercase()
        _filteredRecipients.value = if (query.isBlank()) allRecipients
        else allRecipients.filter { it.name?.lowercase()?.contains(query) == true }
    }

    fun selectRecipient(recipient: ChatRecipient) {
        _selectedRecipient.value = recipient
        _recipientSearch.value = recipient.name ?: ""
        _filteredRecipients.value = emptyList()
    }

    fun clearRecipient() {
        _selectedRecipient.value = null
        _recipientSearch.value = ""
        _filteredRecipients.value = allRecipients
    }

    private fun loadRecipients() {
        scope.launch {
            chatRepository.getRecipients(ChatAxis.STAFF).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> {
                        allRecipients = result.data
                        _filteredRecipients.value = result.data
                    }
                    is ResultState.Error -> showErrorMessage(result.message)
                }
            }
        }
    }

    fun send(text: String) {
        val msg = text.trim()
        if (msg.isBlank()) return

        val currentId = _conversationId.value
        if (currentId != null) {
            doSend(currentId, msg)
            return
        }

        val recipient = _selectedRecipient.value ?: return
        scope.launch {
            chatRepository.startConversation(ChatAxis.STAFF, recipient.counterpartId)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            val newId = result.data.conversationId
                            if (newId != null) {
                                _conversationId.value = newId
                                stateHolder.refreshInternalMessages()
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
        scope.launch {
            chatRepository.sendMessage(conversationId, msg).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
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
                    is ResultState.Success -> _messages.value = result.data.items
                    is ResultState.Error -> Unit
                }
            }
        }
    }

    fun isMine(message: ChatMessage, me: VendorOperationsMe): Boolean {
        val myRefId = if (me.isVendorOwner) me.vendorId ?: 0L else me.employeeId ?: 0L
        return message.senderRefId == myRefId
    }

    fun goBack() = salesRepBack()
}
