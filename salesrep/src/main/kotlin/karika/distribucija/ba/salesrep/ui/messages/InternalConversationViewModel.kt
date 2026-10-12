package karika.distribucija.ba.salesrep.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.ChatRepository
import karika.distribucija.ba.salesrep.model.ChatMessage
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.notifications.MessagePushBus
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesInternalConversationComponent.kt: every load marks the conversation
 * read and fetches its messages; any message push reloads it. Text only, no attachments. */
class InternalConversationViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = ChatRepository()
    val conversationId: Long = savedStateHandle.get<Long>("conversationId") ?: 0L
    val counterpartName: String? = savedStateHandle.get<String>("counterpartName")

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        viewModelScope.launch {
            MessagePushBus.events.collect { load() }
        }
        load()
    }

    private fun load() {
        if (conversationId == 0L) return
        markRead()
        viewModelScope.launch {
            repository.getMessages(conversationId).collect { result ->
                if (result is ResultState.Success) {
                    _messages.value = result.data.items
                } else if (result is ResultState.Error) {
                    _errorMessage.value = result.message
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (conversationId == 0L) return
        val message = text.trim()
        if (message.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(conversationId, message).collect { result ->
                if (result is ResultState.Success) {
                    load()
                } else if (result is ResultState.Error) {
                    _errorMessage.value = result.message
                }
            }
        }
    }

    private fun markRead() {
        viewModelScope.launch {
            repository.markRead(conversationId).collect {}
        }
    }
}
