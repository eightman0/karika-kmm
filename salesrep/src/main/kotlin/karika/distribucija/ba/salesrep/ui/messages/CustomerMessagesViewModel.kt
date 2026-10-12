package karika.distribucija.ba.salesrep.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.ChatRepository
import karika.distribucija.ba.salesrep.model.ChatAxis
import karika.distribucija.ba.salesrep.model.ChatConversation
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.notifications.MessagePushBus
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesCustomerMessagesComponent.kt (Chat V2, vendor_customer axis, one
 * page of 100) plus SalesConversationList's Sve / Poslano / Primljeno split. */
open class CustomerMessagesViewModel : ViewModel() {

    enum class Filter { ALL, SENT, RECEIVED }

    protected open val axis: ChatAxis = ChatAxis.VENDOR_CUSTOMER
    private val repository = ChatRepository()

    private var allConversations: List<ChatConversation> = emptyList()

    private val _filter = MutableLiveData(Filter.ALL)
    val filter: LiveData<Filter> = _filter

    private val _filteredConversations = MutableLiveData<List<ChatConversation>>(emptyList())
    val filteredConversations: LiveData<List<ChatConversation>> = _filteredConversations

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    // No init-time load() here - the fragment's onResume() already calls refresh() on first
    // display (and on every return to this screen), so an init-time load would double the call.
    // The push subscription below only reacts to live events, it doesn't load anything itself.
    init {
        viewModelScope.launch {
            MessagePushBus.events.collect { refresh() }
        }
    }

    fun refresh() = load()

    fun setFilter(filter: Filter) {
        _filter.value = filter
        applyFilter()
    }

    private fun load() {
        viewModelScope.launch {
            repository.getConversations(axis = axis, pageSize = 100).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isLoading.value = true
                    is ResultState.Success -> {
                        _isLoading.value = false
                        allConversations = result.data.items
                        applyFilter()
                    }
                    is ResultState.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }

    /** Sent = the last message came from one of our employees (sender_employee_name set). */
    private fun applyFilter() {
        _filteredConversations.value = when (_filter.value) {
            Filter.SENT -> allConversations.filter { it.isLastSentByUs() }
            Filter.RECEIVED -> allConversations.filterNot { it.isLastSentByUs() }
            else -> allConversations
        }
    }
}
