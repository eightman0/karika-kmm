package karika.distribucija.ba.salesrep.ui.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.OnBehalfOrderResult
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.session.CartState
import karika.distribucija.ba.salesrep.session.CurrentUser
import kotlinx.coroutines.launch

/**
 * Mirrors composeApp's SalesOrderReviewComponent.kt, simplified: no shipping-service sub-form
 * (VendorDeliveryServiceData/carrier price calculator) - confirmOrder's shippingForm=null path,
 * which the original component already supports, is the only path used here.
 */
class OrderReviewViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = SalesRepository()

    val customerId: Long = savedStateHandle.get<Long>("customerId") ?: 0L
    private val customerActive: Boolean = savedStateHandle.get<Boolean>("customerActive") ?: false
    private val hasShippingAddress: Boolean = savedStateHandle.get<Boolean>("hasShippingAddress") ?: false

    val canPlaceOrderFor: Boolean
        get() = CurrentUser.me?.capabilities?.canPlaceOrderFor ?: false

    val isEligible: Boolean
        get() = canPlaceOrderFor && customerActive && hasShippingAddress

    val ineligibleReason: String?
        get() = when {
            !canPlaceOrderFor -> "no_capability"
            !customerActive -> "inactive"
            !hasShippingAddress -> "no_shipping"
            else -> null
        }

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
                    if (result.data.isEmpty) {
                        _shouldGoBack.value = true
                    }
                }
            }
        }
    }

    fun confirmOrder(note: String) {
        if (_isPlacingOrder.value == true) return
        if (CartState.cart.value?.isEmpty != false) return
        if (!isEligible) return

        _isPlacingOrder.value = true
        viewModelScope.launch {
            repository.placeOrder(customerId, note.trim()).collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> {
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
