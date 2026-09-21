package karika.distribucija.ba.ui.view.salesrep.messages.customer

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.chatFileFrom
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatMessage
import karika.distribucija.ba.domain.model.ChatRecipient
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesCustomerNewMessageComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    initialCustomer: OperationalCustomer? = null
) : CommonComponent(componentContext, stateHolder) {

    val customerLocked = initialCustomer != null

    // ── Form state ─────────────────────────────────────────────────────────────
    private val _customerSearch = MutableStateFlow("")
    val customerSearch = _customerSearch.asStateFlow()

    private var allRecipients: List<ChatRecipient> = emptyList()

    private val _customers = MutableStateFlow<List<ChatRecipient>>(emptyList())
    val customers = _customers.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<ChatRecipient?>(null)
    val selectedCustomer = _selectedCustomer.asStateFlow()

    // ── Conversation state (after first send) ──────────────────────────────────
    private val _conversationId = MutableStateFlow<Long?>(null)
    val conversationId = _conversationId.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    val attachment = MutableStateFlow<Pair<String, ByteArray>?>(null)

    init {
        loadRecipients()
        if (initialCustomer != null) {
            val name = initialCustomer.company?.takeIf { it.isNotBlank() } ?: initialCustomer.fullName
            _selectedCustomer.value = ChatRecipient(
                axis = ChatAxis.VENDOR_CUSTOMER,
                counterpartId = initialCustomer.customerId,
                counterpartType = "customer",
                name = name
            )
            _customerSearch.value = name
        }
        scope.launch {
            stateHolder.customerThreadPush.collect { pushedId ->
                if (pushedId == _conversationId.value?.toString()) loadMessages(pushedId.toLong())
            }
        }
    }

    fun setCustomerSearch(text: String) {
        _customerSearch.value = text
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
        _customerSearch.value = customer.name ?: ""
    }

    fun clearCustomer() {
        _selectedCustomer.value = null
        _customerSearch.value = ""
        _customers.value = allRecipients
    }

    private fun loadRecipients() {
        scope.launch {
            chatRepository.getRecipients(ChatAxis.VENDOR_CUSTOMER).collect { result ->
                if (result is ResultState.Success) {
                    allRecipients = result.data
                    _customers.value = result.data
                }
            }
        }
    }

    fun send(text: String) {
        val msg = text.trim()
        if (msg.isBlank() && attachment.value == null) return
        val currentId = _conversationId.value

        if (currentId == null) {
            val counterpartId = _selectedCustomer.value?.counterpartId ?: return
            startAndSend(counterpartId, msg)
            return
        }

        doSend(currentId, msg)
    }

    private fun startAndSend(counterpartId: Long, msg: String) {
        scope.launch {
            chatRepository.startConversation(ChatAxis.VENDOR_CUSTOMER, counterpartId)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            val newId = result.data.conversationId
                            if (newId != null) {
                                _conversationId.value = newId
                                stateHolder.refreshCustomerMessages()
                                doSend(newId, msg)
                            } else {
                                hideLoader()
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

    private fun doSend(conversationId: Long, msg: String) {
        val files = attachment.value?.let { (name, bytes) -> listOf(chatFileFrom(name, bytes)) }
            ?: emptyList()

        scope.launch {
            chatRepository.sendMessage(conversationId, msg, files).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        attachment.value = null
                        loadMessages(conversationId)
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    private fun loadMessages(conversationId: Long) {
        scope.launch {
            chatRepository.getMessages(conversationId).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> {
                        _messages.value = result.data.items
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
