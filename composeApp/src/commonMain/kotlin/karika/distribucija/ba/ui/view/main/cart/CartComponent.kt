package karika.distribucija.ba.ui.view.main.cart

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.bringToFront
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.main.MainConfig
import kotlinx.coroutines.launch

class CartComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
) : CommonComponent(componentContext, stateHolder) {

    fun shippingDetails() {
        mainScope.launch {
            stateHolder.mainNavigation.bringToFront(MainConfig.CartShippingDetails)
        }
    }

}