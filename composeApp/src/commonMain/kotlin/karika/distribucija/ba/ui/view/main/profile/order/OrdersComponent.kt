package karika.distribucija.ba.ui.view.main.profile.order

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.api.OrdersRepository
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrdersComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {
    private val repository = OrdersRepository()

    private val _orders = MutableStateFlow<Set<OrdersResponse>>(emptySet())
    val orders = _orders.asStateFlow()
    var status = ""
    override fun loadNextPage(reset: Boolean) {
        if (reset) {
            hasNextPage = true
            currentPage = 1
        }

        if (!hasNextPage || loader.value) {
            return
        }

        iOScope.launch {
            repository.vendors(
                currentPage = currentPage,
                pageSize = pageSize,
                filterValue = status
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _orders.update {
                            if (reset) {
                                result.data.toSet()
                            } else {
                                it.plus(result.data.toSet())
                            }
                        }
                        hasNextPage = result.data.isNotEmpty()
                        currentPage++
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message ?: "")
                    }
                }
            }
        }
    }

    fun navigateDetails(order: OrdersResponse) {
        appNavigate(AppConfig.OrderDetails(order))
    }

    override fun cancelOrder(
        orderId: String?,
        vendorId: String?,
        reason: String,
        com: String,
        callback: () -> Unit
    ) {
        super.cancelOrder(orderId, vendorId, reason, com) {
            loadNextPage(true)
        }
    }
}