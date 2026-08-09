package karika.distribucija.ba.salesrep.ui.customers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.OperationalCustomer
import karika.distribucija.ba.salesrep.model.PartnershipRequest
import karika.distribucija.ba.salesrep.model.PartnershipRequestBody
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesInviteCustomerComponent.kt (email-only flow; phone/Viber unused there too). */
class InviteCustomerViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = SalesRepository()

    private val _searchQuery = MutableLiveData(savedStateHandle.get<String>("prefillEmail") ?: "")
    val searchQuery: LiveData<String> = _searchQuery

    private val _searchResults = MutableLiveData<List<OperationalCustomer>>(emptyList())
    val searchResults: LiveData<List<OperationalCustomer>> = _searchResults

    private val _email = MutableLiveData(savedStateHandle.get<String>("prefillEmail") ?: "")
    val email: LiveData<String> = _email

    private val _isSaving = MutableLiveData(false)
    val isSaving: LiveData<Boolean> = _isSaving

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>(null)
    val successMessage: LiveData<String?> = _successMessage

    private val _sent = MutableLiveData(false)
    val sent: LiveData<Boolean> = _sent

    private var searchJob: Job? = null

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.length < 3) {
            _searchResults.value = emptyList()
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            repository.getInvitableCustomers(page = 1, search = query).collect { result ->
                if (result is ResultState.Success) {
                    _searchResults.value = result.data.items
                }
            }
        }
    }

    fun selectCustomer(customer: OperationalCustomer) {
        _searchQuery.value = customer.company ?: customer.fullName
        _searchResults.value = emptyList()
        _email.value = customer.email ?: ""
    }

    fun setEmail(value: String) {
        _email.value = value
    }

    fun send(note: String) {
        val email = _email.value?.trim().orEmpty()
        if (email.isBlank()) {
            _errorMessage.value = "Izaberite kupca!"
            return
        }

        viewModelScope.launch {
            repository.requestPartnership(
                PartnershipRequestBody(
                    request = PartnershipRequest(
                        customerEmail = email,
                        note = note.trim().ifBlank { null }
                    )
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isSaving.value = true
                    is ResultState.Success -> {
                        _isSaving.value = false
                        _successMessage.value = "Zahtjev za partnerstvo uspješno poslan!"
                        _sent.value = true
                    }

                    is ResultState.Error -> {
                        _isSaving.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }
}
