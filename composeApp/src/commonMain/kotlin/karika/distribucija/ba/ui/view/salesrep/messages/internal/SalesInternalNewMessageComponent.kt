package karika.distribucija.ba.ui.view.salesrep.messages.internal

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.StaffRecipient
import karika.distribucija.ba.domain.model.StaffThreadMessage
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesInternalNewMessageComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {

    private val salesRepository = SalesRepository()

    private val _subject = MutableStateFlow("")
    val subject = _subject.asStateFlow()

    private val _recipientSearch = MutableStateFlow("")
    val recipientSearch = _recipientSearch.asStateFlow()

    private val _allRecipients = MutableStateFlow<List<StaffRecipient>>(emptyList())

    private val _filteredRecipients = MutableStateFlow<List<StaffRecipient>>(emptyList())
    val filteredRecipients = _filteredRecipients.asStateFlow()

    private val _selectedRecipient = MutableStateFlow<StaffRecipient?>(null)
    val selectedRecipient = _selectedRecipient.asStateFlow()

    private val _threadId = MutableStateFlow<Long?>(null)
    val threadId = _threadId.asStateFlow()

    private val _messages = MutableStateFlow<List<StaffThreadMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    init {
        loadRecipients()
    }

    fun setSubject(v: String) { _subject.value = v }

    fun setRecipientSearch(text: String) {
        _recipientSearch.value = text
        _selectedRecipient.value = null
        val query = text.lowercase()
        _filteredRecipients.value = if (query.isBlank()) _allRecipients.value
        else _allRecipients.value.filter { it.name.lowercase().contains(query) }
    }

    fun selectRecipient(recipient: StaffRecipient) {
        _selectedRecipient.value = recipient
        _recipientSearch.value = recipient.name
        _filteredRecipients.value = emptyList()
    }

    fun clearRecipient() {
        _selectedRecipient.value = null
        _recipientSearch.value = ""
        _filteredRecipients.value = _allRecipients.value
    }

    private fun loadRecipients() {
        scope.launch {
            salesRepository.getConversationRecipients().collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> {
                        _allRecipients.value = result.data
                        _filteredRecipients.value = result.data
                    }
                    is ResultState.Error -> showErrorMessage(result.message)
                }
            }
        }
    }

    fun send(text: String) {
        val msg = text.trim()
        if (msg.isBlank()) return
        val recipient = _selectedRecipient.value ?: return

        val currentThread = _threadId.value
        if (currentThread != null) {
            doSend(currentThread, msg)
            return
        }

        scope.launch {
            salesRepository.startConversation(recipient.employeeId).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        val newThreadId = result.data.threadId
                        _threadId.value = newThreadId
                        stateHolder.refreshInternalMessages()
                        doSend(newThreadId, msg)
                    }
                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    private fun doSend(tid: Long, msg: String) {
        scope.launch {
            salesRepository.sendConversationMessage(tid, msg).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        loadMessages(tid)
                    }
                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    private fun loadMessages(tid: Long) {
        scope.launch {
            salesRepository.getConversationMessages(tid).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> _messages.value = result.data.items
                    is ResultState.Error -> Unit
                }
            }
        }
    }

    fun goBack() = salesRepBack()
}
