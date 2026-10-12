package karika.distribucija.ba.salesrep.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.ChatRepository
import karika.distribucija.ba.salesrep.api.toChatFiles
import karika.distribucija.ba.salesrep.model.ChatAxis
import karika.distribucija.ba.salesrep.model.ChatMessage
import karika.distribucija.ba.salesrep.model.ChatRecipient
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.notifications.MessagePushBus
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesCustomerNewMessageComponent.kt. The customers come from Chat V2's
 * vendor_customer recipients, filtered locally by name; the first send starts the conversation
 * with the picked customer (POST /V1/chat/conversations) and then sends into it, after which the
 * screen continues as that conversation. */
class CustomerNewMessageViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = ChatRepository()

    /** Reached from the customer list's "Pošalji poruku" quick action pre-selects (and locks) the
     * customer, matching composeApp's SalesCustomersComponent.openMessageCustomer(initialCustomer).
     * Reached from the inbox's own "new message" button, these args are absent and the field starts empty. */
    private val initialCustomerId = savedStateHandle.get<Long>("initialCustomerId") ?: 0L
    val customerLocked: Boolean = initialCustomerId != 0L

    private var allRecipients: List<ChatRecipient> = emptyList()

    private val _customers = MutableLiveData<List<ChatRecipient>>(emptyList())
    val customers: LiveData<List<ChatRecipient>> = _customers

    private val _selectedCustomer = MutableLiveData<ChatRecipient?>(null)
    val selectedCustomer: LiveData<ChatRecipient?> = _selectedCustomer

    private val _conversationId = MutableLiveData<Long?>(null)
    val conversationId: LiveData<Long?> = _conversationId

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _attachment = MutableLiveData<Pair<String, ByteArray>?>(null)
    val attachment: LiveData<Pair<String, ByteArray>?> = _attachment

    init {
        loadRecipients()
        if (customerLocked) {
            val company = savedStateHandle.get<String>("initialCustomerCompany")
            val fullName = listOfNotNull(
                savedStateHandle.get<String>("initialCustomerFirstname"),
                savedStateHandle.get<String>("initialCustomerLastname")
            ).joinToString(" ")
            _selectedCustomer.value = ChatRecipient(
                axis = ChatAxis.VENDOR_CUSTOMER,
                counterpartId = initialCustomerId,
                counterpartType = "customer",
                name = company?.takeIf { it.isNotBlank() } ?: fullName
            )
        }
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

    fun setCustomerSearch(text: String) {
        _selectedCustomer.value = null
        val query = text.trim().lowercase()
        _customers.value = if (query.isBlank()) {
            allRecipients
        } else {
            allRecipients.filter { it.name?.lowercase()?.contains(query) == true }
        }
    }

    fun selectCustomer(customer: ChatRecipient) {
        _selectedCustomer.value = customer
    }

    fun clearCustomer() {
        _selectedCustomer.value = null
        _customers.value = allRecipients
    }

    private fun loadRecipients() {
        viewModelScope.launch {
            repository.getRecipients(ChatAxis.VENDOR_CUSTOMER).collect { result ->
                if (result is ResultState.Success) {
                    allRecipients = result.data
                    _customers.value = result.data
                }
            }
        }
    }

    fun send(text: String) {
        val message = text.trim()
        if (message.isBlank() && _attachment.value == null) return
        val currentId = _conversationId.value
        if (currentId != null) {
            doSend(currentId, message)
            return
        }

        val counterpartId = _selectedCustomer.value?.counterpartId ?: return
        viewModelScope.launch {
            repository.startConversation(ChatAxis.VENDOR_CUSTOMER, counterpartId).collect { result ->
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
