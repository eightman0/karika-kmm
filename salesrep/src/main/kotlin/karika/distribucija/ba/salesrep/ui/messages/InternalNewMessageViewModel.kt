package karika.distribucija.ba.salesrep.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.model.StaffRecipient
import karika.distribucija.ba.salesrep.model.StaffThreadMessage
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesInternalNewMessageComponent.kt. Note: [subject] is captured but,
 * matching the Compose source exactly, never actually sent anywhere - startConversation() only
 * takes a counterpartEmployeeId, no subject field exists on that endpoint. */
class InternalNewMessageViewModel : ViewModel() {

    private val repository = SalesRepository()

    private val _subject = MutableLiveData("")
    val subject: LiveData<String> = _subject

    private val _recipientSearch = MutableLiveData("")
    val recipientSearch: LiveData<String> = _recipientSearch

    private var allRecipients: List<StaffRecipient> = emptyList()

    private val _filteredRecipients = MutableLiveData<List<StaffRecipient>>(emptyList())
    val filteredRecipients: LiveData<List<StaffRecipient>> = _filteredRecipients

    private val _selectedRecipient = MutableLiveData<StaffRecipient?>(null)
    val selectedRecipient: LiveData<StaffRecipient?> = _selectedRecipient

    private val _threadId = MutableLiveData<Long?>(null)
    val threadId: LiveData<Long?> = _threadId

    private val _messages = MutableLiveData<List<StaffThreadMessage>>(emptyList())
    val messages: LiveData<List<StaffThreadMessage>> = _messages

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadRecipients()
    }

    fun setSubject(value: String) {
        _subject.value = value
    }

    fun setRecipientSearch(text: String) {
        _recipientSearch.value = text
        _selectedRecipient.value = null
        val query = text.lowercase()
        _filteredRecipients.value = if (query.isBlank()) {
            allRecipients
        } else {
            allRecipients.filter { it.name.lowercase().contains(query) }
        }
    }

    fun selectRecipient(recipient: StaffRecipient) {
        _selectedRecipient.value = recipient
        _recipientSearch.value = recipient.name
        _filteredRecipients.value = emptyList()
    }

    fun clearRecipient() {
        _selectedRecipient.value = null
        _recipientSearch.value = ""
        _filteredRecipients.value = allRecipients
    }

    private fun loadRecipients() {
        viewModelScope.launch {
            repository.getConversationRecipients().collect { result ->
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
        val recipient = _selectedRecipient.value ?: return

        val currentThread = _threadId.value
        if (currentThread != null) {
            doSend(currentThread, message)
            return
        }

        viewModelScope.launch {
            repository.startConversation(recipient.employeeId).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> {
                        val newThreadId = result.data.threadId
                        _threadId.value = newThreadId
                        doSend(newThreadId, message)
                    }
                    is ResultState.Error -> _errorMessage.value = result.message
                }
            }
        }
    }

    private fun doSend(threadId: Long, message: String) {
        viewModelScope.launch {
            repository.sendConversationMessage(threadId, message).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> loadMessages(threadId)
                    is ResultState.Error -> _errorMessage.value = result.message
                }
            }
        }
    }

    private fun loadMessages(threadId: Long) {
        viewModelScope.launch {
            repository.getConversationMessages(threadId).collect { result ->
                if (result is ResultState.Success) {
                    _messages.value = result.data.items
                }
            }
        }
    }
}
