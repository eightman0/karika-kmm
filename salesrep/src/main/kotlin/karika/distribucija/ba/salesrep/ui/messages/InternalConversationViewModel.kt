package karika.distribucija.ba.salesrep.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.model.StaffThreadMessage
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesInternalConversationComponent.kt. */
class InternalConversationViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = SalesRepository()
    val threadId: Long = savedStateHandle.get<Long>("threadId") ?: 0L
    val counterpartName: String = savedStateHandle.get<String>("counterpartName").orEmpty()

    private val _messages = MutableLiveData<List<StaffThreadMessage>>(emptyList())
    val messages: LiveData<List<StaffThreadMessage>> = _messages

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        markRead()
        load()
    }

    private fun load() {
        viewModelScope.launch {
            repository.getConversationMessages(threadId).collect { result ->
                if (result is ResultState.Success) {
                    _messages.value = result.data.items
                } else if (result is ResultState.Error) {
                    _errorMessage.value = result.message
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val message = text.trim()
        if (message.isBlank()) return
        viewModelScope.launch {
            repository.sendConversationMessage(threadId, message).collect { result ->
                if (result is ResultState.Success) {
                    load()
                } else if (result is ResultState.Error) {
                    _errorMessage.value = result.message
                }
            }
        }
    }

    private fun markRead() {
        viewModelScope.launch {
            repository.markConversationRead(threadId).collect {}
        }
    }
}
