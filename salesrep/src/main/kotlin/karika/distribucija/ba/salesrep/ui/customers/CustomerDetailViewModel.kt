package karika.distribucija.ba.salesrep.ui.customers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.DiscountRule
import karika.distribucija.ba.salesrep.model.OnBehalfOrder
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesCustomerDetailComponent.kt (discounts list for one customer). */
class CustomerDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = SalesRepository()
    val customerId: Long = savedStateHandle.get<Long>("customerId") ?: 0L

    private val _discounts = MutableLiveData<List<DiscountRule>>(emptyList())
    val discounts: LiveData<List<DiscountRule>> = _discounts

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _orders = MutableLiveData<List<OnBehalfOrder>>(emptyList())
    val orders: LiveData<List<OnBehalfOrder>> = _orders

    /** null until the first page arrives (keeps the empty state hidden while loading). */
    private val _ordersTotal = MutableLiveData<Long?>(null)
    val ordersTotal: LiveData<Long?> = _ordersTotal

    private val _isLoadingOrders = MutableLiveData(false)
    val isLoadingOrders: LiveData<Boolean> = _isLoadingOrders

    private var ordersPage = 0

    init {
        loadDiscounts()
        loadOrders(reset = true)
    }

    /** GET /V1/vendor-operations/orders filtered by customer_id eq, 12 per page, newest first. */
    fun loadOrders(reset: Boolean = false) {
        if (_isLoadingOrders.value == true) return
        if (!reset && !hasMoreOrders()) return
        val page = if (reset) 1 else ordersPage + 1
        _isLoadingOrders.value = true
        viewModelScope.launch {
            repository.getCustomerOrders(customerId, page, ORDERS_PAGE_SIZE).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isLoadingOrders.value = true
                    is ResultState.Success -> {
                        _isLoadingOrders.value = false
                        ordersPage = page
                        val current = if (reset) emptyList() else _orders.value.orEmpty()
                        _orders.value = (current + result.data.items).distinctBy { it.orderId }
                        _ordersTotal.value = result.data.totalCount
                    }

                    is ResultState.Error -> {
                        _isLoadingOrders.value = false
                        if (reset && _ordersTotal.value == null) _ordersTotal.value = 0
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }

    fun hasMoreOrders(): Boolean = (_ordersTotal.value ?: 0L) > _orders.value.orEmpty().size

    private companion object {
        const val ORDERS_PAGE_SIZE = 12
    }

    fun loadDiscounts() {
        viewModelScope.launch {
            repository.getCustomerDiscounts(customerId).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isLoading.value = true
                    is ResultState.Success -> {
                        _isLoading.value = false
                        _discounts.value = result.data.items
                    }

                    is ResultState.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }

    fun deleteDiscount(rule: DiscountRule) {
        val ruleId = rule.ruleId ?: return
        viewModelScope.launch {
            repository.deleteDiscount(ruleId).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isLoading.value = true
                    is ResultState.Success -> loadDiscounts()
                    is ResultState.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }
}
