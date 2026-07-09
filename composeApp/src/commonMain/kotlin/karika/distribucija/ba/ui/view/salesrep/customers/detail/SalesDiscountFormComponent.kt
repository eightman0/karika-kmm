package karika.distribucija.ba.ui.view.salesrep.customers.detail

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.CustomerRuleRequest
import karika.distribucija.ba.domain.model.DiscountRule
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorProduct
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesDiscountFormComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    val customer: OperationalCustomer,
    val existingRule: DiscountRule? = null
) : CommonComponent(componentContext, stateHolder) {

    private val repository = DashRepository()

    val isEdit = existingRule != null

    private val _productSearch = MutableStateFlow(
        existingRule?.productName ?: existingRule?.categoryName ?: ""
    )
    val productSearch = _productSearch.asStateFlow()

    private val _selectedProduct = MutableStateFlow<VendorProduct?>(null)
    val selectedProduct = _selectedProduct.asStateFlow()

    private val _searchResults = MutableStateFlow<List<VendorProduct>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _showDropdown = MutableStateFlow(false)
    val showDropdown = _showDropdown.asStateFlow()

    private val _minQty = MutableStateFlow(
        existingRule?.minQty?.toInt()?.toString() ?: ""
    )
    val minQty = _minQty.asStateFlow()

    private val _discountPercent = MutableStateFlow(
        existingRule?.discountPercent?.toInt()?.toString() ?: ""
    )
    val discountPercent = _discountPercent.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private var searchJob: Job? = null

    fun setProductSearch(query: String) {
        _productSearch.value = query
        _selectedProduct.value = null
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _showDropdown.value = false
            return
        }
        searchJob = scope.launch {
            delay(400)
            repository.getProducts(
                pageSize = 10,
                currentPage = 1,
                queryParams = listOf(
                    "&searchCriteria[filter_groups][0][filters][0][field]=name",
                    "&searchCriteria[filter_groups][0][filters][0][value]=%25${query}%25",
                    "&searchCriteria[filter_groups][0][filters][0][condition_type]=like"
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Success -> {
                        _searchResults.value = result.data
                        _showDropdown.value = result.data.isNotEmpty()
                    }
                    else -> Unit
                }
            }
        }
    }

    fun selectProduct(product: VendorProduct) {
        _selectedProduct.value = product
        _productSearch.value = product.name ?: ""
        _searchResults.value = emptyList()
        _showDropdown.value = false
    }

    fun clearProduct() {
        _selectedProduct.value = null
        _productSearch.value = ""
        _searchResults.value = emptyList()
        _showDropdown.value = false
    }

    fun setMinQty(value: String) {
        _minQty.value = value.filter { it.isDigit() }
    }

    fun setDiscountPercent(value: String) {
        _discountPercent.value = value.filter { it.isDigit() }
    }

    fun save() {
        val percent = _discountPercent.value.toFloatOrNull() ?: run {
            showErrorMessage("Unesite rabat %")
            return
        }
        val product = _selectedProduct.value
        val request = CustomerRuleRequest(
            discountRule = DiscountRule(
                ruleId = existingRule?.ruleId,
                discountType = existingRule?.discountType ?: "percentage",
                customerId = customer.customerId,
                productId = product?.productId?.toLongOrNull(),
                productName = product?.name,
                categoryId = existingRule?.categoryId,
                categoryName = existingRule?.categoryName,
                minQty = _minQty.value.toFloatOrNull(),
                discountPercent = percent,
                isActive = existingRule?.isActive ?: 1
            )
        )
        scope.launch {
            val flow = if (isEdit && existingRule?.ruleId != null) {
                repository.updateCustomerRule(existingRule.ruleId.toString(), request)
            } else {
                repository.createCustomerRule(request)
            }
            flow.collect { result ->
                when (result) {
                    is ResultState.Loading -> _isSaving.value = true
                    is ResultState.Success -> {
                        _isSaving.value = false
                        salesRepBack()
                    }
                    is ResultState.Error -> {
                        _isSaving.value = false
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun goBack() = salesRepBack()
}
