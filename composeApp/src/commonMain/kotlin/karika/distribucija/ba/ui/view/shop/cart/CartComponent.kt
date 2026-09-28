package karika.distribucija.ba.ui.view.shop.cart

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.bringToFront
import karika.distribucija.ba.domain.model.CartData
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.ProductActions
import karika.distribucija.ba.ui.common.ScreenComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.shop.MainConfig
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface CartComponent : ScreenComponent, ProductActions {
    /** The customer's cart, grouped per vendor. */
    val cart: StateFlow<CartData>

    /** "Nastavi dalje": on to the shipping details. */
    fun shippingDetails()

    fun clearCart()

    fun removeFromCart(product: Product)

    fun showMessage(message: String?)
}

class DefaultCartComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
) : CommonComponent(componentContext, stateHolder), CartComponent {

    override val cart: StateFlow<CartData>
        get() = stateHolder.cartHandler.cart

    override fun shippingDetails() {
        scope.launch {
            stateHolder.mainNavigation.bringToFront(MainConfig.CartShippingDetails)
        }
    }

    override fun clearCart() {
        showLoader()
        stateHolder.cartHandler.clearCart {
            scope.launch {
                hideLoader()
            }
        }
    }
}
