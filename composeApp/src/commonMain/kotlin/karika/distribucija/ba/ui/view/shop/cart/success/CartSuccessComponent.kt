package karika.distribucija.ba.ui.view.shop.cart.success

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface CartSuccessComponent {
    val orderId: StateFlow<String>

    /** "Nastavi kupovati": back past the cart and the shipping details, to the home screen. */
    fun finish()
}

class DefaultCartSuccessComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    orderId: String
) : CommonComponent(componentContext, stateHolder), CartSuccessComponent {
    private val _orderId = MutableStateFlow(orderId)
    override val orderId = _orderId.asStateFlow()

    override fun finish() {
        mainBack()
        mainBack()
        mainBack()
        showHome()
    }
}