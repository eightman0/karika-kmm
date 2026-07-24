package karika.distribucija.ba.ui.view.salesrep.customers.invite

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.PartnershipRequest
import karika.distribucija.ba.domain.model.PartnershipRequestBody
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Shop
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ContactMethod(val label: String) {
    PHONE("Broj telefona"),
    EMAIL("Email"),
    VIBER("Viber")
}

class SalesInviteCustomerComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    prefillEmail: String = ""
) : CommonComponent(componentContext, stateHolder) {

    private val repository = SalesRepository()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Shop>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    private val _email = MutableStateFlow(prefillEmail)
    val email = _email.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone = _phone.asStateFlow()

    private val _note = MutableStateFlow("")
    val note = _note.asStateFlow()

    private val _contactMethod = MutableStateFlow(ContactMethod.EMAIL)
    val contactMethod = _contactMethod.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    fun setSearchQuery(v: String) {
        _searchQuery.value = v
        if (v.length < 3) {
            _searchResults.value = emptyList()
            return
        }
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(300)
            messagesRepository.shops(searchText = v).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isSearching.value = true
                    is ResultState.Success -> {
                        _isSearching.value = false
                        _searchResults.value = result.data
                    }

                    is ResultState.Error -> _isSearching.value = false
                }
            }
        }
    }

    fun selectShop(shop: Shop) {
        _searchQuery.value = shop.name ?: ""
        _searchResults.value = emptyList()
        _email.update { shop.email ?: "" }
    }

    fun setEmail(v: String) {
        _email.value = v
    }

    fun setPhone(v: String) {
        _phone.value = v
    }

    fun setNote(v: String) {
        _note.value = v
    }

    fun setContactMethod(v: ContactMethod) {
        _contactMethod.value = v
    }

    fun send() {
        val email = _email.value.trim()
        if (email.isBlank()) {
            showErrorMessage("Izaberite kupca!"); return
        }

        scope.launch {
            repository.requestPartnership(
                PartnershipRequestBody(
                    request = PartnershipRequest(
                        customerEmail = email,
                        note = _note.value.trim().ifBlank { null }
                    )
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isSaving.value = true
                    is ResultState.Success -> {
                        _isSaving.value = false
                        showMessage("Zahtjev za partnerstvo uspješno poslan!")
                        salesRepBack()
                    }

                    is ResultState.Error -> {
                        _isSaving.value = false
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun callTarget(): String? {
        val sanitized = _phone.value.trim().filter { it.isDigit() || it == '+' }
        if (sanitized.isBlank()) {
            showErrorMessage("Unesite broj telefona kupca")
            return null
        }
        return when (_contactMethod.value) {
            ContactMethod.VIBER -> "viber://contact?number=$sanitized"
            else -> "tel:$sanitized"
        }
    }

    fun goBack() = salesRepBack()
}
