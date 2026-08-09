package karika.distribucija.ba.ui.view.salesrep.cart

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.OnBehalfCartResponse
import karika.distribucija.ba.domain.model.OnBehalfCartResponseItem
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.salesrep.dashboard.SalesRepConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SalesOrderCartComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    val customer: OperationalCustomer
) : CommonComponent(componentContext, stateHolder) {

    private val salesRepository = SalesRepository()

    // ── Cart state from shared stateHolder ────────────────────────────────────
    val cart: StateFlow<OnBehalfCartResponse?> = stateHolder.salesSpecificHandler.cart

    val cartCount: StateFlow<Int> = stateHolder.salesSpecificHandler.cart
        .map { it?.itemsCount ?: 0 }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    private val editJobs = mutableMapOf<Long, Job>()

    val canCreateDiscountFor: Boolean
        get() = stateHolder.salesSpecificHandler.me.value.capabilities.canCreateDiscountFor

    // ── Actions ────────────────────────────────────────────────────────────────
    fun updateQty(item: OnBehalfCartResponseItem, qty: Int) = editItem(item, qty = qty)

    fun updateDiscount(item: OnBehalfCartResponseItem, discountPercent: Int) =
        editItem(item, discountPercent = discountPercent.takeIf { it > 0 })

    private fun editItem(item: OnBehalfCartResponseItem, qty: Int = item.qty, discountPercent: Int? = item.discountPercent) {
        editJobs[item.itemId]?.cancel()
        editJobs[item.itemId] = scope.launch {
            delay(400.milliseconds)

            val resultFlow = if (qty <= 0) {
                salesRepository.removeCartItem(customer.customerId, item.itemId)
            } else {
                val allowedDiscount = discountPercent.takeIf { canCreateDiscountFor }
                salesRepository.addCartItem(customer.customerId, item.sku, qty, allowedDiscount)
            }

            resultFlow.collect { result ->
                when (result) {
                    is ResultState.Success -> stateHolder.salesSpecificHandler.cart.value = result.data
                    is ResultState.Error -> showErrorMessage(result.message)
                    is ResultState.Loading -> { /* no-op */ }
                }
            }
        }
    }

    fun removeItem(item: OnBehalfCartResponseItem) {
        scope.launch {
            salesRepository.removeCartItem(customer.customerId, item.itemId).collect { result ->
                when (result) {
                    is ResultState.Success -> stateHolder.salesSpecificHandler.cart.value = result.data
                    is ResultState.Error -> showErrorMessage(result.message)
                    is ResultState.Loading -> { /* no-op */ }
                }
            }
        }
    }

    fun clearCart() {
        val items = cart.value?.items.orEmpty()
        if (items.isEmpty()) return
        scope.launch {
            var lastCart: OnBehalfCartResponse? = null
            for (item in items) {
                salesRepository.removeCartItem(customer.customerId, item.itemId).collect { result ->
                    if (result is ResultState.Success) lastCart = result.data
                }
            }
            stateHolder.salesSpecificHandler.cart.value = lastCart
        }
    }

    fun openOrderReview() {
        if (cart.value?.isEmpty != false) return
        salesRepPush(SalesRepConfig.OrderReview(customer))
    }

    fun goBack() = salesRepBack()
}
