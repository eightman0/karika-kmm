package karika.distribucija.ba.salesrep.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.Message
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.model.SendMessageRequest
import karika.distribucija.ba.salesrep.notifications.MessagePushBus
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesAdminNewMessageComponent.kt. Unlike Customer's new-message screen,
 * there's no recipient picker - `receiverId=0`/`sendToAdmin=true` are fixed constants, since
 * this screen only ever targets the admin. Subject is sent only with the first message, matching
 * `if (currentThread == null) subj else null`. */
class AdminNewMessageViewModel : ViewModel() {

    private val repository = SalesRepository()

    private val _subject = MutableLiveData("")
    val subject: LiveData<String> = _subject

    private val _threadId = MutableLiveData<String?>(null)
    val threadId: LiveData<String?> = _threadId

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _attachment = MutableLiveData<Pair<String, ByteArray>?>(null)
    val attachment: LiveData<Pair<String, ByteArray>?> = _attachment

    init {
        viewModelScope.launch {
            MessagePushBus.events.collect { event ->
                if (event.admin && event.threadId == _threadId.value) loadMessages(event.threadId)
            }
        }
    }

    fun setSubject(value: String) {
        _subject.value = value
    }

    fun setAttachment(filename: String, bytes: ByteArray) {
        _attachment.value = filename to bytes
    }

    fun clearAttachment() {
        _attachment.value = null
    }

    fun send(text: String) {
        val currentThread = _threadId.value
        val message = text.trim()
        if (message.isBlank() && _attachment.value == null) return

        viewModelScope.launch {
            repository.sendMessage(
                SendMessageRequest(
                    sendToAdmin = true,
                    message = message,
                    subject = if (currentThread == null) _subject.value.orEmpty().trim() else null,
                    receiverId = 0,
                    threadId = currentThread?.toIntOrNull(),
                    file = _attachment.value
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> {
                        _attachment.value = null
                        if (currentThread == null) {
                            val newThreadId = result.data.threadId
                            if (newThreadId != null) {
                                _threadId.value = newThreadId
                                loadMessages(newThreadId)
                            }
                        } else {
                            loadMessages(currentThread)
                        }
                    }
                    is ResultState.Error -> _errorMessage.value = result.message
                }
            }
        }
    }

    private fun loadMessages(threadId: String) {
        viewModelScope.launch {
            repository.getMessageThread(threadId, admin = true).collect { result ->
                if (result is ResultState.Success) _messages.value = result.data
            }
        }
    }
}
