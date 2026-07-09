package karika.distribucija.ba.ui.view.salesrep.messages.customer

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.Message
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.SendMessageRequest
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesCustomerNewMessageComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {

    private val salesRepository = SalesRepository()

    // ── Form state ─────────────────────────────────────────────────────────────
    private val _subject = MutableStateFlow("")
    val subject = _subject.asStateFlow()

    private val _customerSearch = MutableStateFlow("")
    val customerSearch = _customerSearch.asStateFlow()

    private val _customers = MutableStateFlow<List<OperationalCustomer>>(emptyList())
    val customers = _customers.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<OperationalCustomer?>(null)
    val selectedCustomer = _selectedCustomer.asStateFlow()

    // ── Conversation state (after first send) ──────────────────────────────────
    private val _threadId = MutableStateFlow<String?>(null)
    val threadId = _threadId.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    val attachment = MutableStateFlow<Pair<String, ByteArray>?>(null)

    private var searchJob: Job? = null

    init {
        loadCustomers("")
    }

    fun setSubject(v: String) { _subject.value = v }

    fun setCustomerSearch(text: String) {
        _customerSearch.value = text
        _selectedCustomer.value = null
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(400)
            loadCustomers(text)
        }
    }

    fun selectCustomer(customer: OperationalCustomer) {
        _selectedCustomer.value = customer
        _customerSearch.value = customer.company ?: customer.fullName
    }

    fun clearCustomer() {
        _selectedCustomer.value = null
        _customerSearch.value = ""
        loadCustomers("")
    }

    private fun loadCustomers(search: String) {
        scope.launch {
            salesRepository.getCustomers(
                page = 1,
                pageSize = 20,
                search = search.takeIf { it.isNotBlank() }
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> _customers.value = result.data.items
                    is ResultState.Error -> Unit
                }
            }
        }
    }

    fun send(text: String) {
        val currentThread = _threadId.value
        val msg = text.trim()
        if (msg.isBlank() && attachment.value == null) return

        scope.launch {
            messagesRepository.send(
                SendMessageRequest(
                    sendToAdmin = false,
                    message = msg,
                    subject = if (currentThread == null) _subject.value.trim() else null,
                    receiverId = _selectedCustomer.value?.customerId?.toInt(),
                    threadId = currentThread?.toIntOrNull(),
                    file = attachment.value
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        attachment.value = null
                        if (currentThread == null) {
                            val newThreadId = result.data.threadId
                            if (newThreadId != null) {
                                _threadId.value = newThreadId
                                loadMessages(newThreadId)
                                stateHolder.refreshCustomerMessages()
                            }
                        } else {
                            loadMessages(currentThread)
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

    private fun loadMessages(threadId: String) {
        scope.launch {
            messagesRepository.get(threadId = threadId, admin = false).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> {
                        _messages.value = result.data.firstOrNull()?.messages?.firstOrNull()
                            ?: emptyList()
                    }
                    is ResultState.Error -> showErrorMessage(result.message)
                }
            }
        }
    }

    fun pickFile() {
        stateHolder.handler.pickFile { name, data -> attachment.value = Pair(name, data) }
    }

    fun pickPhoto() {
        stateHolder.handler.pickPhoto { name, data -> attachment.value = Pair(name, data) }
    }

    fun goBack() = salesRepBack()
}
