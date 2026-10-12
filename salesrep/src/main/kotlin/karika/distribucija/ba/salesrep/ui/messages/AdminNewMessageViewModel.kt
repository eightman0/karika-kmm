package karika.distribucija.ba.salesrep.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.ChatRepository
import karika.distribucija.ba.salesrep.api.toChatFiles
import karika.distribucija.ba.salesrep.model.ChatAxis
import karika.distribucija.ba.salesrep.model.ChatMessage
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.notifications.MessagePushBus
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesAdminNewMessageComponent.kt. There is only one recipient, so the
 * first send starts the vendor_admin conversation (no counterpart id) and sends into it; the
 * screen then continues as that conversation. */
class AdminNewMessageViewModel : ViewModel() {

    private val repository = ChatRepository()

    /** null = new message mode; non-null = conversation mode. */
    private val _conversationId = MutableLiveData<Long?>(null)
    val conversationId: LiveData<Long?> = _conversationId

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _attachment = MutableLiveData<Pair<String, ByteArray>?>(null)
    val attachment: LiveData<Pair<String, ByteArray>?> = _attachment

    init {
        viewModelScope.launch {
            MessagePushBus.events.collect { event ->
                if (event.conversationId == _conversationId.value) loadMessages(event.conversationId)
            }
        }
    }

    fun setAttachment(filename: String, bytes: ByteArray) {
        _attachment.value = filename to bytes
    }

    fun clearAttachment() {
        _attachment.value = null
    }

    fun send(text: String) {
        val message = text.trim()
        if (message.isBlank() && _attachment.value == null) return
        val currentId = _conversationId.value
        if (currentId != null) {
            doSend(currentId, message)
            return
        }

        viewModelScope.launch {
            repository.startConversation(ChatAxis.VENDOR_ADMIN).collect { result ->
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
            repository.sendMessage(conversationId, message, _attachment.value.toChatFiles()).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> {
                        _attachment.value = null
                        loadMessages(conversationId)
                    }
                    is ResultState.Error -> _errorMessage.value = result.message
                }
            }
        }
    }

    private fun loadMessages(conversationId: Long) {
        viewModelScope.launch {
            repository.getMessages(conversationId).collect { result ->
                when (result) {
                    is ResultState.Success -> _messages.value = result.data.items
                    is ResultState.Error -> _errorMessage.value = result.message
                    is ResultState.Loading -> Unit
                }
            }
        }
    }
}
