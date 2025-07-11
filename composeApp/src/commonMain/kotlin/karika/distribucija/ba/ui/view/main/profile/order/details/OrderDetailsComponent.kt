package karika.distribucija.ba.ui.view.main.profile.order.details

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrderDetailsComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    ordersResponse: OrdersResponse
) : CommonComponent(componentContext, stateHolder) {

    private val _order = MutableStateFlow(ordersResponse)
    val order = _order.asStateFlow()

    override fun loadNextPage(reset: Boolean) {

    }

    override fun orderAgain(order: OrdersResponse, callback: () -> Unit) {
        super.orderAgain(order) {
            appBack()
            appBack()
        }
    }
}