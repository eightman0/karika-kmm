package karika.distribucija.ba.salesrep.ui.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.Config
import karika.distribucija.ba.salesrep.model.OnBehalfCartResponseItem
import karika.distribucija.ba.salesrep.model.OnBehalfCartShippingDefaults
import karika.distribucija.ba.salesrep.model.OnBehalfOrderResult
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.model.VendorDeliveryServiceData
import karika.distribucija.ba.salesrep.session.CartState
import karika.distribucija.ba.salesrep.session.CurrentUser
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesOrderReviewComponent.kt. */
class OrderReviewViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = SalesRepository()

    val customerId: Long = savedStateHandle.get<Long>("customerId") ?: 0L
    val customerCompany: String? = savedStateHandle.get<String>("customerCompany")
    val customerEmail: String? = savedStateHandle.get<String>("customerEmail")
    val partnershipStatus: String = savedStateHandle.get<String>("partnershipStatus").orEmpty()
    val customerActive: Boolean = savedStateHandle.get<Boolean>("customerActive") ?: false
    private val hasShippingAddress: Boolean = savedStateHandle.get<Boolean>("hasShippingAddress") ?: false

    val canPlaceOrderFor: Boolean
        get() = CurrentUser.me?.capabilities?.canPlaceOrderFor ?: false

    val canCreateDiscountFor: Boolean
        get() = CurrentUser.me?.capabilities?.canCreateDiscountFor ?: false

    val isEligible: Boolean
        get() = canPlaceOrderFor && customerActive && hasShippingAddress

    val ineligibleReason: String?
        get() = when {
            !canPlaceOrderFor -> "no_capability"
            !customerActive -> "inactive"
            !hasShippingAddress -> "no_shipping"
            else -> null
        }

    private val _shippingDefaults = MutableLiveData<OnBehalfCartShippingDefaults?>(null)
    val shippingDefaults: LiveData<OnBehalfCartShippingDefaults?> = _shippingDefaults

    private var deliveryConfig: Config? = null

    private val _isPlacingOrder = MutableLiveData(false)
    val isPlacingOrder: LiveData<Boolean> = _isPlacingOrder

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _orderPlaced = MutableLiveData<OnBehalfOrderResult?>(null)
    val orderPlaced: LiveData<OnBehalfOrderResult?> = _orderPlaced

    private val _shouldGoBack = MutableLiveData(false)
    val shouldGoBack: LiveData<Boolean> = _shouldGoBack

    init {
        viewModelScope.launch {
            repository.getCart(customerId).collect { result ->
                if (result is ResultState.Success) {
                    CartState.cart.value = result.data
                    _shippingDefaults.value = result.data.shippingDefaults
                    if (result.data.isEmpty) {
                        _shouldGoBack.value = true
                    }
                }
            }
        }
        viewModelScope.launch {
            repository.config().collect { result ->
                if (result is ResultState.Success) deliveryConfig = result.data
            }
        }
    }

    fun updateItem(item: OnBehalfCartResponseItem, qty: Int, discountPercent: Int) {
        viewModelScope.launch {
            val allowedDiscount = discountPercent.takeIf { it > 0 && canCreateDiscountFor }
            val resultFlow = if (qty <= 0) {
                repository.removeCartItem(customerId, item.itemId)
            } else {
                repository.addCartItem(customerId, item.sku, qty, allowedDiscount)
            }
            resultFlow.collect { result ->
                when (result) {
                    is ResultState.Success -> {
                        CartState.cart.value = result.data
                        if (result.data.isEmpty) _shouldGoBack.value = true
                    }
                    is ResultState.Error -> _errorMessage.value = result.message
                    is ResultState.Loading -> Unit
                }
            }
        }
    }

    /** Chargeable weight is the greater of volumetric weight (w*h*d/5000) and actual weight,
     * matching composeApp's calculateShipping(). */
    fun calculateShipping(width: String, height: String, depth: String, weight: String): Pair<Double?, Double?> {
        val w = width.toDoubleOrNull()
        val h = height.toDoubleOrNull()
        val d = depth.toDoubleOrNull()
        val wt = weight.toDoubleOrNull()
        if (w == null || h == null || d == null || wt == null) {
            _errorMessage.value = "Unesite sve dimenzije i težinu paketa."
            return null to null
        }

        val chargeable = maxOf((w * h * d) / 5000, wt)
        val providers = deliveryConfig?.shippingProveders.orEmpty()
        val a2b = providers.find { it.code == "A2B" }?.priceFor(chargeable)
        val express = providers.find { it.code == "EURO_EXPRESS" }?.priceFor(chargeable)
        return a2b to express
    }

    fun confirmOrder(note: String, shippingForm: VendorDeliveryServiceData?) {
        if (_isPlacingOrder.value == true) return
        if (CartState.cart.value?.isEmpty != false) return
        if (!isEligible) return

        _isPlacingOrder.value = true
        viewModelScope.launch {
            repository.placeOrder(customerId, note.trim()).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> {
                        if (shippingForm != null) {
                            repository.updateDelivery(
                                shippingForm.copy(id = result.data.incrementId)
                            ).collect { deliveryResult ->
                                if (deliveryResult is ResultState.Error) {
                                    _errorMessage.value = "Narudžba je kreirana, ali spašavanje podataka o dostavi " +
                                        "nije uspjelo. Možete probati ponovo sa stranice detalja narudžbe."
                                }
                            }
                        }

                        _isPlacingOrder.value = false
                        CartState.clear()
                        _orderPlaced.value = result.data
                    }

                    is ResultState.Error -> {
                        _isPlacingOrder.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }
}
