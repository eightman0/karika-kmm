package karika.distribucija.ba.ui.view.salesrep.customers.detail

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.DiscountRule
import karika.distribucija.ba.domain.model.DiscountRuleBody
import karika.distribucija.ba.domain.model.DiscountRuleInput
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SalesDiscountFormComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    val customer: OperationalCustomer,
    val existingRule: DiscountRule? = null
) : CommonComponent(componentContext, stateHolder) {

    private val repository = SalesRepository()
    private val vendorId = stateHolder.salesSpecificHandler.vendorId

    val isEdit = existingRule != null

    // ── Search state ───────────────────────────────────────────────────────────
    private val _itemSearch = MutableStateFlow(
        existingRule?.productName ?: existingRule?.categoryName ?: ""
    )
    val itemSearch = _itemSearch.asStateFlow()

    private val _selectedItem = MutableStateFlow<DiscountSearchItem?>(
        when {
            existingRule?.productId != null -> DiscountSearchItem.ProductItem(
                Product(entityId = existingRule.productId.toString(), name = existingRule.productName)
            )
            existingRule?.categoryId != null -> DiscountSearchItem.CategoryItem(
                category = Category(id = existingRule.categoryId.toInt(), name = existingRule.categoryName ?: ""),
                fullPath = existingRule.categoryName ?: ""
            )
            else -> null
        }
    )
    val selectedItem = _selectedItem.asStateFlow()

    private val _searchResults = MutableStateFlow<List<DiscountSearchItem>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // ── Other form fields ──────────────────────────────────────────────────────
    private val _minQty = MutableStateFlow(existingRule?.minQty?.toInt()?.toString() ?: "")
    val minQty = _minQty.asStateFlow()

    private val _discountPercent = MutableStateFlow(existingRule?.discountPercent?.toInt()?.toString() ?: "")
    val discountPercent = _discountPercent.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private var searchJob: Job? = null

    // ── Search logic ───────────────────────────────────────────────────────────

    fun setItemSearch(query: String) {
        _itemSearch.value = query
        _selectedItem.value = null
        searchJob?.cancel()

        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }

        // Local category search
        val allCategories = stateHolder.commonHandler.categories.value
        val matchedCategories = mutableListOf<DiscountSearchItem.CategoryItem>()

        fun searchRecursive(categories: List<Category>, path: String, level: Int) {
            if (level > 4) return
            categories.forEach { cat ->
                val currentPath = if (path.isEmpty()) cat.name else "$path > ${cat.name}"
                if (cat.name.contains(query, ignoreCase = true)) {
                    matchedCategories.add(DiscountSearchItem.CategoryItem(cat, currentPath))
                }
                searchRecursive(cat.childrenData, currentPath, level + 1)
            }
        }

        searchRecursive(allCategories, "", 1)
        _searchResults.value = matchedCategories

        // Debounced product search
        searchJob = scope.launch {
            delay(350.milliseconds)
            productRepository.searchProductsByCategory(
                searchText = query,
                vendorId = vendorId,
                pageSize = 20,
                currentPage = 1
            ).collect { result ->
                when (result) {
                    is ResultState.Success -> {
                        val productItems = result.data.map { DiscountSearchItem.ProductItem(it) }
                        _searchResults.value = matchedCategories + productItems
                    }
                    else -> Unit
                }
            }
        }
    }

    fun selectItem(item: DiscountSearchItem) {
        _selectedItem.value = item
        _itemSearch.value = item.displayName
        _searchResults.value = emptyList()
    }

    fun clearItem() {
        _selectedItem.value = null
        _itemSearch.value = ""
        _searchResults.value = emptyList()
    }

    // ── Field setters ──────────────────────────────────────────────────────────

    fun setMinQty(value: String) { _minQty.value = value.filter { it.isDigit() } }

    fun setDiscountPercent(value: String) { _discountPercent.value = value.filter { it.isDigit() } }

    // ── Save ───────────────────────────────────────────────────────────────────

    fun save() {
        val percent = _discountPercent.value.toFloatOrNull() ?: run {
            showErrorMessage("Unesite rabat %")
            return
        }

        val selected = _selectedItem.value
        val productId = (selected as? DiscountSearchItem.ProductItem)?.product?.entityId?.toLongOrNull()
        val categoryId = (selected as? DiscountSearchItem.CategoryItem)?.category?.id?.toLong()

        val body = DiscountRuleBody(
            discountRule = DiscountRuleInput(
                discountType = "per_customer",
                discountPercent = percent,
                isActive = existingRule?.isActive ?: 1,
                productId = productId,
                categoryId = categoryId,
                minQty = _minQty.value.toFloatOrNull()
            )
        )

        scope.launch {
            val flow = if (isEdit && existingRule?.ruleId != null) {
                repository.updateDiscount(existingRule.ruleId, body)
            } else {
                repository.createCustomerDiscount(customer.customerId, body)
            }
            flow.collect { result ->
                when (result) {
                    is ResultState.Loading -> _isSaving.value = true
                    is ResultState.Success -> {
                        _isSaving.value = false
                        stateHolder.refreshDiscounts()
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

// ── Search item sealed class ───────────────────────────────────────────────────

sealed class DiscountSearchItem {
    data class ProductItem(val product: Product) : DiscountSearchItem()
    data class CategoryItem(val category: Category, val fullPath: String) : DiscountSearchItem()

    val displayName: String
        get() = when (this) {
            is ProductItem -> product.name.orEmpty()
            is CategoryItem -> fullPath
        }
}
