package karika.distribucija.ba.ui.view.salesrep.customers.invite

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.PartnershipRequest
import karika.distribucija.ba.domain.model.PartnershipRequestBody
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesInviteCustomerComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    prefillEmail: String = ""
) : CommonComponent(componentContext, stateHolder) {

    private val repository = SalesRepository()

    private val _email = MutableStateFlow(prefillEmail)
    val email = _email.asStateFlow()

    private val _note = MutableStateFlow("")
    val note = _note.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    fun setEmail(v: String) { _email.value = v }
    fun setNote(v: String) { _note.value = v }

    fun send() {
        val email = _email.value.trim()
        if (email.isBlank()) { showErrorMessage("Unesite email adresu kupca"); return }

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

    fun goBack() = salesRepBack()
}
