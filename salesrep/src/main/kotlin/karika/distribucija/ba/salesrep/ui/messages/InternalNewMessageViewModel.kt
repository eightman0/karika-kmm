package karika.distribucija.ba.salesrep.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.ChatRepository
import karika.distribucija.ba.salesrep.model.ChatAxis
import karika.distribucija.ba.salesrep.model.ChatMessage
import karika.distribucija.ba.salesrep.model.ChatRecipient
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesInternalNewMessageComponent.kt: the colleagues come from Chat V2's
 * staff recipients, filtered locally by name; the first send starts the staff conversation with
 * the picked colleague and sends into it, after which the screen continues as that conversation. */
class InternalNewMessageViewModel : ViewModel() {

    private val repository = ChatRepository()

    private var allRecipients: List<ChatRecipient> = emptyList()

    private val _filteredRecipients = MutableLiveData<List<ChatRecipient>>(emptyList())
    val filteredRecipients: LiveData<List<ChatRecipient>> = _filteredRecipients

    private val _selectedRecipient = MutableLiveData<ChatRecipient?>(null)
    val selectedRecipient: LiveData<ChatRecipient?> = _selectedRecipient

    private val _conversationId = MutableLiveData<Long?>(null)
    val conversationId: LiveData<Long?> = _conversationId

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadRecipients()
    }

    fun setRecipientSearch(text: String) {
        _selectedRecipient.value = null
        val query = text.trim().lowercase()
        _filteredRecipients.value = if (query.isBlank()) {
            allRecipients
        } else {
            allRecipients.filter { it.name?.lowercase()?.contains(query) == true }
        }
    }

    fun selectRecipient(recipient: ChatRecipient) {
        _selectedRecipient.value = recipient
        _filteredRecipients.value = emptyList()
    }

    fun clearRecipient() {
        _selectedRecipient.value = null
        _filteredRecipients.value = allRecipients
    }

    private fun loadRecipients() {
        viewModelScope.launch {
            repository.getRecipients(ChatAxis.STAFF).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> {
                        allRecipients = result.data
                        _filteredRecipients.value = result.data
                    }
                    is ResultState.Error -> _errorMessage.value = result.message
                }
            }
        }
    }

    fun send(text: String) {
        val message = text.trim()
        if (message.isBlank()) return

        val currentId = _conversationId.value
        if (currentId != null) {
            doSend(currentId, message)
            return
        }

        val recipient = _selectedRecipient.value ?: return
        viewModelScope.launch {
            repository.startConversation(ChatAxis.STAFF, recipient.counterpartId).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> {
                        val newId = result.data.conversationId
                        if (newId != null) {
                            _conversationId.value = newId
                            doSend(newId, message)
                        }
                    }
                    is ResultState.Error -> _errorMessage.value = result.message
                }
            }
        }
    }

    private fun doSend(conversationId: Long, message: String) {
        viewModelScope.launch {
            repository.sendMessage(conversationId, message).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> loadMessages(conversationId)
                    is ResultState.Error -> _errorMessage.value = result.message
                }
            }
        }
    }

    private fun loadMessages(conversationId: Long) {
        viewModelScope.launch {
            repository.getMessages(conversationId).collect { result ->
                if (result is ResultState.Success) {
                    _messages.value = result.data.items
                }
            }
        }
    }
}
