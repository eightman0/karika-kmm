package karika.distribucija.ba.salesrep.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.model.StaffThread
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesInternalMessagesComponent.kt. */
class InternalMessagesViewModel : ViewModel() {

    private val repository = SalesRepository()

    private val _threads = MutableLiveData<List<StaffThread>>(emptyList())
    val threads: LiveData<List<StaffThread>> = _threads

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        load()
    }

    fun refresh() = load()

    private fun load() {
        viewModelScope.launch {
            repository.listConversations().collect { result ->
                when (result) {
                    is ResultState.Loading -> _isLoading.value = true
                    is ResultState.Success -> {
                        _isLoading.value = false
                        _threads.value = result.data.items
                    }
                    is ResultState.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }
}
