package karika.distribucija.ba.salesrep.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.ChatRepository
import karika.distribucija.ba.salesrep.api.toChatFiles
import karika.distribucija.ba.salesrep.model.ChatMessage
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.notifications.MessagePushBus
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesCustomerConversationComponent.kt (and, via
 * [AdminConversationViewModel], SalesAdminConversationComponent.kt): every load marks the
 * conversation read first, then fetches its messages; a send carries the pending attachment as
 * a base64 Chat V2 file and reloads on success. */
open class CustomerConversationViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = ChatRepository()

    val conversationId: Long = savedStateHandle.get<Long>("conversationId") ?: 0L
    val counterpartName: String? = savedStateHandle.get<String>("counterpartName")

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _attachment = MutableLiveData<Pair<String, ByteArray>?>(null)
    val attachment: LiveData<Pair<String, ByteArray>?> = _attachment

    init {
        load()
        viewModelScope.launch {
            MessagePushBus.events.collect { event ->
                if (event.conversationId == conversationId) load()
            }
        }
    }

    fun setAttachment(filename: String, bytes: ByteArray) {
        _attachment.value = filename to bytes
    }

    fun clearAttachment() {
        _attachment.value = null
    }

    private fun load() {
        if (conversationId == 0L) return
        viewModelScope.launch {
            repository.markRead(conversationId).collect {}
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
        if (text.isBlank() && _attachment.value == null) return
        viewModelScope.launch {
            repository.sendMessage(conversationId, text, _attachment.value.toChatFiles()).collect { result ->
                if (result is ResultState.Success) {
                    _attachment.value = null
                    load()
                } else if (result is ResultState.Error) {
                    _errorMessage.value = result.message
                }
            }
        }
    }
}
