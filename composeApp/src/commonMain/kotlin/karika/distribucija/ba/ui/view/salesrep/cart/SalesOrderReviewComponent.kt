package karika.distribucija.ba.ui.view.salesrep.cart

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.OnBehalfCartResponse
import karika.distribucija.ba.domain.model.OnBehalfCartResponseItem
import karika.distribucija.ba.domain.model.OnBehalfCartShippingDefaults
import karika.distribucija.ba.domain.model.OnBehalfOrder
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorDeliveryServiceData
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.salesrep.dashboard.SalesRepConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesOrderReviewComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    val customer: OperationalCustomer
) : CommonComponent(componentContext, stateHolder) {

    private val salesRepository = SalesRepository()

    // ── Cart state from shared stateHolder ────────────────────────────────────
    val cart: StateFlow<OnBehalfCartResponse?> = stateHolder.salesSpecificHandler.cart

    private val _shippingDefaults = MutableStateFlow<OnBehalfCartShippingDefaults?>(null)
    val shippingDefaults = _shippingDefaults.asStateFlow()

    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder = _isPlacingOrder.asStateFlow()

    val canPlaceOrderFor: Boolean
        get() = stateHolder.salesSpecificHandler.me.value.capabilities.canPlaceOrderFor

    val canCreateDiscountFor: Boolean
        get() = stateHolder.salesSpecificHandler.me.value.capabilities.canCreateDiscountFor

    init {
        scope.launch {
            salesRepository.getCart(customer.customerId).collect { result ->
                if (result is ResultState.Success) {
                    stateHolder.salesSpecificHandler.cart.value = result.data
                    _shippingDefaults.value = result.data.shippingDefaults
                    if (result.data.isEmpty) {
                        salesRepBack()
                    }
                }
            }
        }
    }

    // ── Actions ────────────────────────────────────────────────────────────────
    fun updateItem(item: OnBehalfCartResponseItem, qty: Int, discountPercent: Int) {
        scope.launch {
            val allowedDiscount = discountPercent.takeIf { it > 0 && canCreateDiscountFor }
            val resultFlow = if (qty <= 0) {
                salesRepository.removeCartItem(customer.customerId, item.itemId)
            } else {
                salesRepository.addCartItem(customer.customerId, item.sku, qty, allowedDiscount)
            }
            resultFlow.collect { result ->
                when (result) {
                    is ResultState.Success -> {
                        stateHolder.salesSpecificHandler.cart.value = result.data
                        if (result.data.isEmpty) salesRepBack()
                    }
                    is ResultState.Error -> showErrorMessage(result.message)
                    is ResultState.Loading -> { /* no-op */ }
                }
            }
        }
    }

    fun calculateShipping(width: String, height: String, depth: String, weight: String): Pair<Double?, Double?> {
        val w = width.toDoubleOrNull()
        val h = height.toDoubleOrNull()
        val d = depth.toDoubleOrNull()
        val wt = weight.toDoubleOrNull()
        if (w == null || h == null || d == null || wt == null) {
            showErrorMessage("Unesite sve dimenzije i težinu paketa.")
            return null to null
        }

        val chargeable = maxOf((w * h * d) / 5000, wt)
        val providers = stateHolder.commonHandler.config.value.shippingProveders
        val a2b = providers.find { it.code == "A2B" }?.priceFor(chargeable)
        val express = providers.find { it.code == "EURO_EXPRESS" }?.priceFor(chargeable)
        return a2b to express
    }

    fun confirmOrder(note: String, shippingForm: VendorDeliveryServiceData?) {
        if (_isPlacingOrder.value) return
        if (cart.value?.isEmpty != false) return
        if (!canPlaceOrderFor || !customer.isActive || customer.defaultShippingAddressId == null) return

        _isPlacingOrder.value = true
        scope.launch {
            salesRepository.placeOrder(customer.customerId, note.trim()).collect { result ->
                when (result) {
                    is ResultState.Loading -> { /* set above */ }
                    is ResultState.Success -> {
                        if (shippingForm != null) {
                            DashRepository().updateDelivery(
                                shippingForm.copy(id = result.data.incrementId)
                            ).collect { deliveryResult ->
                                if (deliveryResult is ResultState.Error) {
                                    showErrorMessage(
                                        "Narudžba je kreirana, ali spašavanje podataka o dostavi nije uspjelo. " +
                                            "Možete probati ponovo sa stranice detalja narudžbe."
                                    )
                                }
                            }
                        }

                        _isPlacingOrder.value = false
                        val message = if (result.data.status == "pending") {
                            "Narudžba ${result.data.incrementId} je poslana menadžeru na odobrenje."
                        } else {
                            "Narudžba ${result.data.incrementId} uspješno kreirana!"
                        }
                        showMessage(message)

                        val order = OnBehalfOrder(
                            orderId = result.data.orderId,
                            incrementId = result.data.incrementId,
                            customerId = customer.customerId,
                            customerName = customer.company ?: customer.fullName,
                            grandTotal = result.data.grandTotal.toFloat(),
                            status = result.data.status
                        )
                        stateHolder.salesSpecificHandler.clearCartState()
                        salesRepNavigate(SalesRepConfig.OrderDetail(order), true)
                    }
                    is ResultState.Error -> {
                        _isPlacingOrder.value = false
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun goBack() = salesRepBack()
}
