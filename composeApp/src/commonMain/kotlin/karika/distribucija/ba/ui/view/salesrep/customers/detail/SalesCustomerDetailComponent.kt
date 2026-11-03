package karika.distribucija.ba.ui.view.salesrep.customers.detail

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.DiscountRule
import karika.distribucija.ba.domain.model.OnBehalfOrder
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.ui.view.salesrep.dashboard.SalesRepConfig
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesCustomerDetailComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    val customer: OperationalCustomer
) : CommonComponent(componentContext, stateHolder) {

    private val repository = SalesRepository()

    private val _discounts = MutableStateFlow<List<DiscountRule>>(emptyList())
    val discounts = _discounts.asStateFlow()

    // ── "Narudžbe": this customer's orders, newest first ─────────────────────
    private val _orders = MutableStateFlow<List<OnBehalfOrder>>(emptyList())
    val orders = _orders.asStateFlow()

    private val _ordersTotalCount = MutableStateFlow(0L)
    val ordersTotalCount = _ordersTotalCount.asStateFlow()

    /** True while the first page loads (before anything is known). */
    private val _isLoadingOrders = MutableStateFlow(true)
    val isLoadingOrders = _isLoadingOrders.asStateFlow()

    private val _isLoadingMoreOrders = MutableStateFlow(false)
    val isLoadingMoreOrders = _isLoadingMoreOrders.asStateFlow()

    private var ordersPage = 0

    val canCreateDiscountFor: Boolean
        get() = stateHolder.salesSpecificHandler.me.value.capabilities.canCreateDiscountFor

    init {
        loadDiscounts()
        loadOrders(page = 1)
        scope.launch {
            stateHolder.refreshDiscounts.collect { loadDiscounts() }
        }
    }

    fun loadDiscounts() {
        scope.launch {
            repository.getCustomerDiscounts(customer.customerId).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _discounts.value = result.data.items
                    }
                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    private fun loadOrders(page: Int) {
        scope.launch {
            repository.getCustomerOrders(customer.customerId, page, ORDERS_PAGE_SIZE).collect { result ->
                when (result) {
                    is ResultState.Loading -> if (page > 1) _isLoadingMoreOrders.value = true
                    is ResultState.Success -> {
                        ordersPage = page
                        _ordersTotalCount.value = result.data.totalCount
                        _orders.value = if (page == 1) result.data.items else _orders.value + result.data.items
                        _isLoadingOrders.value = false
                        _isLoadingMoreOrders.value = false
                    }
                    is ResultState.Error -> {
                        _isLoadingOrders.value = false
                        _isLoadingMoreOrders.value = false
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun loadMoreOrders() {
        if (_isLoadingMoreOrders.value || _orders.value.size >= _ordersTotalCount.value) return
        loadOrders(ordersPage + 1)
    }

    fun openOrder(order: OnBehalfOrder) {
        salesRepPush(SalesRepConfig.OrderDetail(order, customer))
    }

    fun openNewDiscount() {
        if (!canCreateDiscount()) return
        salesRepPush(SalesRepConfig.DiscountForm(customer = customer))
    }

    fun openEditDiscount(rule: DiscountRule) {
        if (!canCreateDiscount()) return
        salesRepPush(SalesRepConfig.DiscountForm(customer = customer, existingRule = rule))
    }

    private fun canCreateDiscount(): Boolean {
        if (stateHolder.salesSpecificHandler.me.value.capabilities.canCreateDiscountFor) return true
        showErrorMessage("Nemate dozvolu za kreiranje rabata za ovog kupca.")
        return false
    }

    fun deleteDiscount(rule: DiscountRule) {
        val ruleId = rule.ruleId ?: return
        scope.launch {
            repository.deleteDiscount(ruleId).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        loadDiscounts()
                    }
                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun openOrderCatalog() {
        salesRepPush(SalesRepConfig.OrderCatalog(customer))
    }

    fun goBack() = salesRepBack()

    private companion object {
        const val ORDERS_PAGE_SIZE = 12
    }
}
