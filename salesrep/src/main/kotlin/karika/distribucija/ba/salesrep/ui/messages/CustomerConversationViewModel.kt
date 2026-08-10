package karika.distribucija.ba.salesrep.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.Message
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.model.SendMessageRequest
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesCustomerConversationComponent.kt. Every message sent on an existing
 * thread re-sends the conversation's original subject (matching Compose's
 * `subject = conversation.subject ?: ""` on every send, not just the first). */
open class CustomerConversationViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    protected open val admin: Boolean = false
    private val repository = SalesRepository()

    val threadId: String = savedStateHandle.get<String>("threadId").orEmpty()
    val customerName: String = savedStateHandle.get<String>("customerName").orEmpty()
    val subject: String? = savedStateHandle.get<String>("subject")
    private val receiverId: Int? = savedStateHandle.get<Int>("receiverId")?.takeIf { it >= 0 }

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _attachment = MutableLiveData<Pair<String, ByteArray>?>(null)
    val attachment: LiveData<Pair<String, ByteArray>?> = _attachment

    init {
        markRead()
        load()
    }

    fun setAttachment(filename: String, bytes: ByteArray) {
        _attachment.value = filename to bytes
    }

    fun clearAttachment() {
        _attachment.value = null
    }

    private fun load() {
        viewModelScope.launch {
            repository.getMessageThread(threadId, admin).collect { result ->
                if (result is ResultState.Success) {
                    _messages.value = result.data
                } else if (result is ResultState.Error) {
                    _errorMessage.value = result.message
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val message = text.trim()
        if (message.isBlank() && _attachment.value == null) return
        viewModelScope.launch {
            repository.sendMessage(
                SendMessageRequest(
                    sendToAdmin = admin,
                    message = message,
                    subject = subject ?: "",
                    receiverId = receiverId,
                    threadId = threadId.toIntOrNull(),
                    file = _attachment.value
                )
            ).collect { result ->
                if (result is ResultState.Success) {
                    _attachment.value = null
                    load()
                } else if (result is ResultState.Error) {
                    _errorMessage.value = result.message
                }
            }
        }
    }

    private fun markRead() {
        viewModelScope.launch {
            repository.markMessageRead(threadId).collect {}
        }
    }
}
