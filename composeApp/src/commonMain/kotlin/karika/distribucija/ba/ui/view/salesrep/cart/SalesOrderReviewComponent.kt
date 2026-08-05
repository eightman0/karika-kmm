package karika.distribucija.ba.ui.view.salesrep.cart

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.OnBehalfProduct
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SalesOrderReviewComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    val customer: OperationalCustomer
) : CommonComponent(componentContext, stateHolder) {

    private val salesRepository = SalesRepository()

    // ── Cart state from shared stateHolder ────────────────────────────────────
    val cartItems: StateFlow<Map<String, Pair<OnBehalfProduct, Int>>> = stateHolder.salesSpecificHandler.salesRepCart
    val cartDiscounts: StateFlow<Map<String, Int>> = stateHolder.salesSpecificHandler.cartDiscounts

    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder = _isPlacingOrder.asStateFlow()

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

    fun updateDiscountForAll(discount: Int) {
        val keys = stateHolder.salesSpecificHandler.salesRepCart.value.keys
        stateHolder.salesSpecificHandler.cartDiscounts.update { discounts ->
            if (discount <= 0) discounts - keys else discounts + keys.associateWith { discount }
        }
    }

    fun confirmOrder() {
        if (_isPlacingOrder.value) return
        val items = stateHolder.salesSpecificHandler.salesRepCart.value.values.map { (product, qty) -> product to qty }
        if (items.isEmpty()) return

        _isPlacingOrder.value = true
        scope.launch {
            salesRepository.placeOrder(customer.customerId, items).collect { result ->
                when (result) {
                    is ResultState.Loading -> { /* set above */ }
                    is ResultState.Success -> {
                        _isPlacingOrder.value = false
                        showMessage("Narudžba ${result.data.incrementId} uspješno kreirana!")
                        stateHolder.salesSpecificHandler.clearSalesRepCart()
                        salesRepBack()
                        salesRepBack()
                        salesRepBack()
                    }
                    is ResultState.Error -> {
                        _isPlacingOrder.value = false
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun calculateShipping() {
        showMessage("Kalkulator dostave će uskoro biti dostupan.")
    }

    fun goBack() = salesRepBack()
}
