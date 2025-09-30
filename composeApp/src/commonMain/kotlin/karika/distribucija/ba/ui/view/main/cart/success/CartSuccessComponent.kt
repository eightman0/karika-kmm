package karika.distribucija.ba.ui.view.main.cart.success

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartSuccessComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    orderId: String
) : CommonComponent(componentContext, stateHolder) {
    private val _orderId = MutableStateFlow(orderId)
    val orderId = _orderId.asStateFlow()

    fun finish() {
        mainBack()
        mainBack()
        mainBack()
        showHome()
    }
}