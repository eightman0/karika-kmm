package karika.distribucija.ba.salesrep.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.Conversation
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.notifications.MessagePushBus
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesCustomerMessagesComponent.kt. */
open class CustomerMessagesViewModel : ViewModel() {

    enum class Filter { ALL, SENT, RECEIVED }

    protected open val admin: Boolean = false
    private val repository = SalesRepository()

    private var allConversations: List<Conversation> = emptyList()

    private val _filter = MutableLiveData(Filter.ALL)
    val filter: LiveData<Filter> = _filter

    private val _filteredConversations = MutableLiveData<List<Conversation>>(emptyList())
    val filteredConversations: LiveData<List<Conversation>> = _filteredConversations

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    // No init-time load() here - the fragment's onResume() already calls refresh() on first
    // display (and on every return to this screen), so an init-time load would double the call.
    // The push subscription below only reacts to live events, it doesn't load anything itself.
    init {
        viewModelScope.launch {
            MessagePushBus.events.collect { event ->
                if (event.admin == admin) refresh()
            }
        }
    }

    fun refresh() = load()

    fun setFilter(filter: Filter) {
        _filter.value = filter
        applyFilter()
    }

    private fun load() {
        viewModelScope.launch {
            repository.listMessages(admin).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isLoading.value = true
                    is ResultState.Success -> {
                        _isLoading.value = false
                        allConversations = result.data
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

    /** Admin messages classify "sent" by receiverId=="0" instead of sender=="vendor" - same
     * concept (did the vendor originate it), different field on the shared Conversation model. */
    protected open fun isSent(conversation: Conversation): Boolean = conversation.sender == "vendor"

    private fun applyFilter() {
        _filteredConversations.value = when (_filter.value) {
            Filter.SENT -> allConversations.filter { isSent(it) }
            Filter.RECEIVED -> allConversations.filter { !isSent(it) }
            else -> allConversations
        }
    }
}
