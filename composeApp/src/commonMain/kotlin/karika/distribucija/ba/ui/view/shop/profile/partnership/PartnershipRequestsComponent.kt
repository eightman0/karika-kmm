package karika.distribucija.ba.ui.view.shop.profile.partnership

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.PartnershipRepository
import karika.distribucija.ba.domain.model.PartnershipRequest
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PartnershipRequestsComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) :
    CommonComponent(componentContext, stateHolder) {
    private val repository = PartnershipRepository()

    private val _requests = MutableStateFlow<List<PartnershipRequest>>(emptyList())
    val requests = _requests.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val approveRequest = mutableStateOf<PartnershipRequest?>(null)
    val rejectRequest = mutableStateOf<PartnershipRequest?>(null)

    init {
        load()
    }

    fun load() {
        scope.launch {
            repository.list().collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _error.update { null }
                        showLoader()
                    }

                    is ResultState.Success -> {
                        hideLoader()
                        _requests.update { result.data }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        _error.update { result.message ?: "Došlo je do greške. Pokušajte ponovo!" }
                    }
                }
            }
        }
    }

    fun approve(request: PartnershipRequest) {
        scope.launch {
            repository.approve(request.partnershipId ?: return@launch).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        showMessage("Zahtjev za partnerstvo je prihvaćen.")
                        load()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message ?: "Došlo je do greške. Pokušajte ponovo!")
                    }
                }
            }
        }
    }

    fun reject(request: PartnershipRequest, reason: String?) {
        scope.launch {
            repository.reject(request.partnershipId ?: return@launch, reason).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        showMessage("Zahtjev za partnerstvo je odbijen.")
                        load()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message ?: "Došlo je do greške. Pokušajte ponovo!")
                    }
                }
            }
        }
    }
}
