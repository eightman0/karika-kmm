package karika.distribucija.ba.ui.view.distributer.customers

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.DiscountRule
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.distributer.dashboard.DashConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class CustomersComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {
    private val repository = DashRepository()

    private val _customerRules = MutableStateFlow<List<CustomerRule>>(emptyList())
    val customerRules = _customerRules.asStateFlow()

    private val _customerTypeRules = MutableStateFlow<List<CustomerRule>>(emptyList())
    val customerTypeRules = _customerTypeRules.asStateFlow()

    private val _customerRegionRules = MutableStateFlow<List<CustomerRule>>(emptyList())
    val customerRegionRules = _customerRegionRules.asStateFlow()

    fun addCustomerRule() {
        dashNavigate(DashConfig.CustomerRuleEditor(scope = RuleScope.CUSTOMER))
    }

    fun addCustomerTypeRule() {
        dashNavigate(DashConfig.CustomerRuleEditor(scope = RuleScope.CUSTOMER_TYPE))
    }

    fun addCustomerRegionRule() {
        dashNavigate(DashConfig.CustomerRuleEditor(scope = RuleScope.CUSTOMER_REGION))
    }

    fun editRule(scope: RuleScope, rule: CustomerRule) {
        dashNavigate(DashConfig.CustomerRuleEditor(scope = scope, rule = rule))
    }

    fun deleteRule(rule: CustomerRule) {
        scope.launch {
            repository.deleteCustomerRule(rule.id).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        loadRules() // Reload list after deletion
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun loadRules() {
        scope.launch {
            repository.getCustomerRules().collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        val grouped = result.data.items.map { it.toUiRule(this@CustomersComponent) }
                            .groupBy { it.scope }
                        _customerRules.update {
                            grouped[RuleScope.CUSTOMER].orEmpty().map { it.rule }
                        }
                        _customerTypeRules.update {
                            grouped[RuleScope.CUSTOMER_TYPE].orEmpty().map { it.rule }
                        }
                        _customerRegionRules.update {
                            grouped[RuleScope.CUSTOMER_REGION].orEmpty().map { it.rule }
                        }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }
}

private data class ScopedRule(
    val scope: RuleScope,
    val rule: CustomerRule
)

private fun DiscountRule.toUiRule(component: CustomersComponent): ScopedRule {
    val parsedScope = when (discountType.lowercase()) {
        "per_customer_group" -> RuleScope.CUSTOMER_TYPE
        "per_customer_region" -> RuleScope.CUSTOMER_REGION
        else -> RuleScope.CUSTOMER
    }

    val targetLabel = when (parsedScope) {
        RuleScope.CUSTOMER -> "$customerName"
        RuleScope.CUSTOMER_TYPE -> customerGroupValue ?: ""
        RuleScope.CUSTOMER_REGION -> customerRegionValue ?: ""
    }

    val itemLabel = when {
        productId != null -> "$productName"
        categoryId != null -> categoryName ?: ""
        else -> "Svi artikli"
    }

    val itemTitle = when {
        productId != null -> "Artikal: "
        categoryId != null -> "Kategorija: "
        else -> "Svi artikli"
    }

    val uiRule = CustomerRule(
        id = ruleId?.toString() ?: "",
        label = "$targetLabel — $itemLabel ($discountPercent%)",
        targetName = targetLabel,
        itemOrCategoryLabel = itemTitle,
        itemOrCategoryName = itemLabel,
        minQtyForDiscount = minQty?.toString() ?: "",
        discountPercent = discountPercent.toString(),
        itemId = productId?.toString() ?: categoryId?.toString(),
        itemType = when {
            productId != null -> "proizvod"
            categoryId != null -> "category"
            else -> null
        }
    )

    return ScopedRule(parsedScope, uiRule)
}

@Serializable
enum class RuleScope {
    CUSTOMER,
    CUSTOMER_TYPE,
    CUSTOMER_REGION
}

fun RuleScope.title(): String = when (this) {
    RuleScope.CUSTOMER -> "Postavke po kupcu"
    RuleScope.CUSTOMER_TYPE -> "Postavke po tipu kupca"
    RuleScope.CUSTOMER_REGION -> "Postavke po regiji kupca"
}

fun RuleScope.targetFieldTitle(): String = when (this) {
    RuleScope.CUSTOMER -> "Odaberi kupca"
    RuleScope.CUSTOMER_TYPE -> "Odaberi tip kupca"
    RuleScope.CUSTOMER_REGION -> "Odaberi regiju kupca"
}

fun RuleScope.targetFieldPlaceholder(): String = when (this) {
    RuleScope.CUSTOMER -> "Odaberi kupca"
    RuleScope.CUSTOMER_TYPE -> "Odaberi tip kupca"
    RuleScope.CUSTOMER_REGION -> "Odaberi regiju kupca"
}

fun RuleScope.backLabel(): String = "Nazad na upravljanje rabatima"

@Serializable
data class CustomerRule(
    val id: String = "",
    val label: String = "",
    val targetName: String = "",
    val itemOrCategoryLabel: String = "",
    val itemOrCategoryName: String = "",
    val minQtyForDiscount: String = "",
    val discountPercent: String = "",
    val itemId: String? = null,
    val itemType: String? = null // "proizvod" ili "category"
)
