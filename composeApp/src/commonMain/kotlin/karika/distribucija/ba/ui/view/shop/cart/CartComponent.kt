package karika.distribucija.ba.ui.view.shop.cart

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.bringToFront
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.shop.MainConfig
import kotlinx.coroutines.launch

class CartComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
) : CommonComponent(componentContext, stateHolder) {

    fun shippingDetails() {
        scope.launch {
            stateHolder.mainNavigation.bringToFront(MainConfig.CartShippingDetails)
        }
    }

    fun clearCart() {
        showLoader()
        stateHolder.cartHandler.clearCart {
            scope.launch {
                hideLoader()
            }
        }
    }
}