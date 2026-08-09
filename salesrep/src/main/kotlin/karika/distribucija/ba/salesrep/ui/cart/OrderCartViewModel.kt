package karika.distribucija.ba.salesrep.ui.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.OnBehalfCartResponse
import karika.distribucija.ba.salesrep.model.OnBehalfCartResponseItem
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.session.CartState
import karika.distribucija.ba.salesrep.session.CurrentUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesOrderCartComponent.kt. */
class OrderCartViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = SalesRepository()
    val customerId: Long = savedStateHandle.get<Long>("customerId") ?: 0L

    val canCreateDiscountFor: Boolean
        get() = CurrentUser.me?.capabilities?.canCreateDiscountFor ?: false

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val editJobs = mutableMapOf<Long, Job>()

    fun updateQty(item: OnBehalfCartResponseItem, qty: Int) = editItem(item, qty = qty)

    fun updateDiscount(item: OnBehalfCartResponseItem, discountPercent: Int) =
        editItem(item, discountPercent = discountPercent.takeIf { it > 0 })

    private fun editItem(
        item: OnBehalfCartResponseItem,
        qty: Int = item.qty,
        discountPercent: Int? = item.discountPercent
    ) {
        editJobs[item.itemId]?.cancel()
        editJobs[item.itemId] = viewModelScope.launch {
            delay(400)

            val resultFlow = if (qty <= 0) {
                repository.removeCartItem(customerId, item.itemId)
            } else {
                val allowedDiscount = discountPercent.takeIf { canCreateDiscountFor }
                repository.addCartItem(customerId, item.sku, qty, allowedDiscount)
            }

            resultFlow.collect { result ->
                when (result) {
                    is ResultState.Success -> CartState.cart.value = result.data
                    is ResultState.Error -> _errorMessage.value = result.message
                    is ResultState.Loading -> Unit
                }
            }
        }
    }

    fun removeItem(item: OnBehalfCartResponseItem) {
        viewModelScope.launch {
            repository.removeCartItem(customerId, item.itemId).collect { result ->
                when (result) {
                    is ResultState.Success -> CartState.cart.value = result.data
                    is ResultState.Error -> _errorMessage.value = result.message
                    is ResultState.Loading -> Unit
                }
            }
        }
    }

    fun clearCart() {
        val items = CartState.cart.value?.items.orEmpty()
        if (items.isEmpty()) return
        viewModelScope.launch {
            var lastCart: OnBehalfCartResponse? = null
            for (item in items) {
                repository.removeCartItem(customerId, item.itemId).collect { result ->
                    if (result is ResultState.Success) lastCart = result.data
                }
            }
            CartState.cart.value = lastCart
        }
    }
}
