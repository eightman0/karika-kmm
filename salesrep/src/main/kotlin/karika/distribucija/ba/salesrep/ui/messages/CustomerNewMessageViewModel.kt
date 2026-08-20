package karika.distribucija.ba.salesrep.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.Message
import karika.distribucija.ba.salesrep.model.OperationalCustomer
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.model.SendMessageRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesCustomerNewMessageComponent.kt. Unlike the internal-messaging new
 * message screen, subject IS actually sent here, but only with the first message (once a
 * threadId exists, later sends omit it - matching `if (currentThread == null) subject else null`).
 * Also matching Compose exactly: send is only gated on non-blank text, not on a customer being
 * selected. */
class CustomerNewMessageViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = SalesRepository()

    /** Reached from the customer list's "Pošalji poruku" quick action pre-selects the customer,
     * matching composeApp's SalesCustomersComponent.openMessageCustomer(initialCustomer). Reached
     * from the inbox's own "new message" button, these args are absent and the field starts empty. */
    private val initialCustomerId = savedStateHandle.get<Long>("initialCustomerId") ?: 0L

    private val _subject = MutableLiveData("")
    val subject: LiveData<String> = _subject

    private val _customerSearch = MutableLiveData("")
    val customerSearch: LiveData<String> = _customerSearch

    private val _customers = MutableLiveData<List<OperationalCustomer>>(emptyList())
    val customers: LiveData<List<OperationalCustomer>> = _customers

    private val _selectedCustomer = MutableLiveData<OperationalCustomer?>(null)
    val selectedCustomer: LiveData<OperationalCustomer?> = _selectedCustomer

    private val _threadId = MutableLiveData<String?>(null)
    val threadId: LiveData<String?> = _threadId

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _attachment = MutableLiveData<Pair<String, ByteArray>?>(null)
    val attachment: LiveData<Pair<String, ByteArray>?> = _attachment

    private var searchJob: Job? = null

    init {
        if (initialCustomerId != 0L) {
            selectCustomer(
                OperationalCustomer(
                    customerId = initialCustomerId,
                    firstname = savedStateHandle.get<String>("initialCustomerFirstname"),
                    lastname = savedStateHandle.get<String>("initialCustomerLastname"),
                    company = savedStateHandle.get<String>("initialCustomerCompany"),
                    partnershipId = 0L,
                    partnershipStatus = "active"
                )
            )
        }
        loadCustomers(null)
    }

    fun setAttachment(filename: String, bytes: ByteArray) {
        _attachment.value = filename to bytes
    }

    fun clearAttachment() {
        _attachment.value = null
    }

    fun setSubject(value: String) {
        _subject.value = value
    }

    fun setCustomerSearch(text: String) {
        _customerSearch.value = text
        _selectedCustomer.value = null
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            loadCustomers(text.takeIf { it.isNotBlank() })
        }
    }

    fun selectCustomer(customer: OperationalCustomer) {
        _selectedCustomer.value = customer
        _customerSearch.value = customer.company ?: customer.fullName
    }

    fun clearCustomer() {
        _selectedCustomer.value = null
        _customerSearch.value = ""
        loadCustomers(null)
    }

    private fun loadCustomers(search: String?) {
        viewModelScope.launch {
            repository.getCustomers(page = 1, pageSize = 20, search = search).collect { result ->
                if (result is ResultState.Success) _customers.value = result.data.items
            }
        }
    }

    fun send(text: String) {
        val currentThread = _threadId.value
        val message = text.trim()
        if (message.isBlank() && _attachment.value == null) return

        viewModelScope.launch {
            repository.sendMessage(
                SendMessageRequest(
                    sendToAdmin = false,
                    message = message,
                    subject = if (currentThread == null) _subject.value.orEmpty().trim() else null,
                    receiverId = _selectedCustomer.value?.customerId?.toInt(),
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
            repository.getMessageThread(threadId, admin = false).collect { result ->
                if (result is ResultState.Success) _messages.value = result.data
            }
        }
    }
}
