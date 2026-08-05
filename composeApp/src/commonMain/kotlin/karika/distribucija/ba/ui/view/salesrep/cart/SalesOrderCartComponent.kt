package karika.distribucija.ba.ui.view.salesrep.cart

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.OnBehalfProduct
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.salesrep.dashboard.SalesRepConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class SalesOrderCartComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    val customer: OperationalCustomer
) : CommonComponent(componentContext, stateHolder) {

    // ── Cart state from shared stateHolder ────────────────────────────────────
    val cartItems: StateFlow<Map<String, Pair<OnBehalfProduct, Int>>> = stateHolder.salesSpecificHandler.salesRepCart

    val cartCount: StateFlow<Int> = stateHolder.salesSpecificHandler.salesRepCart
        .map { map -> map.values.sumOf { it.second } }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    val cartDiscounts: StateFlow<Map<String, Int>> = stateHolder.salesSpecificHandler.cartDiscounts

    // ── Actions ────────────────────────────────────────────────────────────────
    fun updateQty(product: OnBehalfProduct, qty: Int) {
        stateHolder.salesSpecificHandler.salesRepCart.update { cart ->
            if (qty <= 0) cart - product.key else cart + (product.key to (product to qty))
        }
    }

    fun updateDiscount(product: OnBehalfProduct, discount: Int) {
        stateHolder.salesSpecificHandler.cartDiscounts.update { discounts ->
            if (discount <= 0) discounts - product.key else discounts + (product.key to discount)
        }
    }

    fun removeItem(product: OnBehalfProduct) {
        stateHolder.salesSpecificHandler.salesRepCart.update { it - product.key }
        stateHolder.salesSpecificHandler.cartDiscounts.update { it - product.key }
    }

    fun clearCart() {
        stateHolder.salesSpecificHandler.clearSalesRepCart()
    }

    fun openOrderReview() {
        if (stateHolder.salesSpecificHandler.salesRepCart.value.isEmpty()) return
        salesRepPush(SalesRepConfig.OrderReview(customer))
    }

    fun goBack() = salesRepBack()
}
