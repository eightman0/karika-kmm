package karika.distribucija.ba.ui.view.salesrep.orders.detail

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.OnBehalfOrder
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesOrderDetailComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    val order: OnBehalfOrder
) : CommonComponent(componentContext, stateHolder) {

    private val repository = DashRepository()

    private val _vendorOrder = MutableStateFlow<VendorOrder?>(null)
    val vendorOrder = _vendorOrder.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments = _comments.asStateFlow()

    private val _isSendingComment = MutableStateFlow(false)
    val isSendingComment = _isSendingComment.asStateFlow()

    init {
        scope.launch {
            repository.getOrder(order.incrementId).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _vendorOrder.value = result.data
                    }
                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
        loadComments()
    }

    fun loadComments() {
        scope.launch {
            repository.getOrderComments(order.incrementId).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> _comments.value = result.data
                    is ResultState.Error -> showErrorMessage(result.message)
                }
            }
        }
    }

    fun sendComment(text: String) {
        if (text.isBlank()) return
        scope.launch {
            repository.sendComment(order.incrementId, text).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isSendingComment.value = true
                    is ResultState.Success -> {
                        _isSendingComment.value = false
                        loadComments()
                    }
                    is ResultState.Error -> {
                        _isSendingComment.value = false
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun goBack() = salesRepBack()
}
