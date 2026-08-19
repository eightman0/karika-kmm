package karika.distribucija.ba.ui.view.salesrep.customers.detail

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.DiscountRule
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

    init {
        loadDiscounts()
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
}
