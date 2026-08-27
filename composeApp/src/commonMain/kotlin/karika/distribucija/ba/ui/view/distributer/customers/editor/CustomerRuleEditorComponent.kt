package karika.distribucija.ba.ui.view.distributer.customers.editor

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.CustomerRuleRequest
import karika.distribucija.ba.domain.model.DiscountRule
import karika.distribucija.ba.domain.model.KarikaUnit
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Shop
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.distributer.customers.CustomerRule
import karika.distribucija.ba.ui.view.distributer.customers.RuleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CustomerRuleEditorComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    val ruleScope: RuleScope,
    private val initialRule: CustomerRule? = null
) : CommonComponent(componentContext, stateHolder) {
    private val repository = DashRepository()

    val isEditing: Boolean = initialRule != null

    val target = mutableStateOf(initialRule?.targetName ?: "")
    val selectedShop = mutableStateOf<Shop?>(null)
    val selectedCustomerRegion = mutableStateOf(
        initialRule?.targetValue?.let { value ->
            stateHolder.commonHandler.config.value.customerRegionList.firstOrNull { it.unit() == value }
        }
    )
    val itemOrCategory = mutableStateOf(
        initialRule?.let { rule ->
            if (rule.itemId != null && rule.itemType != null) {
                Triple(rule.itemId, rule.itemOrCategoryName, rule.itemType)
            } else {
                null
            }
        }
    )
    val itemOrCategorySearchText = mutableStateOf(initialRule?.itemOrCategoryName ?: "")
    val minQtyForDiscount = mutableStateOf(initialRule?.minQtyForDiscount?.toDoubleOrNull()?.toInt()?.toString() ?: "")
    val discountPercent = mutableStateOf(initialRule?.discountPercent?.replace(".", ",") ?: "")

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops = _shops.asStateFlow()

    private val _items = MutableStateFlow<List<Product>>(emptyList())
    val items = _items.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchItem>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    val customerGroups = stateHolder.commonHandler.config.value.customerGroupList
    val customerRegions = stateHolder.commonHandler.config.value.customerRegionList
    val customerGroupLabels = mutableStateOf(customerGroups.map { it.label() })
    val customerRegionLabels = mutableStateOf(customerRegions.map { it.label() })

    private var searchJob: Job? = null
    private var itemSearchJob: Job? = null
    private var currentVendorId = stateHolder.vendorSpecificHandler.vendorDetails.value.entityId

    private fun loadShops(searchText: String = "") {
        showLoader()
        scope.launch {
            messagesRepository.shops(searchText = searchText).collect { result ->
                hideLoader()

                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> {
                        _shops.update { result.data }
                    }

                    is ResultState.Error -> {
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun onTargetChanged(value: String) {
        target.value = value
        if (ruleScope == RuleScope.CUSTOMER) {
            searchJob?.cancel()
            if (value.length < 2) {
                _shops.update { emptyList() }
                return
            }
            searchJob = scope.launch {
                delay(350)
                loadShops(value)
            }
        }
    }

    fun onShopSelected(shop: Shop) {
        target.value = shop.name.orEmpty()
        selectedShop.value = shop
        _shops.update { emptyList() }
    }

    fun onCustomerGroupSelected(label: String) {
        target.value = label
    }

    fun onCustomerRegionSelected(label: String) {
        val region = customerRegions.firstOrNull { it.label() == label } ?: return
        target.value = region.label()
        selectedCustomerRegion.value = region
    }

    fun onItemOrCategoryChanged(value: String) {
        itemOrCategorySearchText.value = value
        itemSearchJob?.cancel()
        if (value.length < 2) {
            _searchResults.update { emptyList() }
            return
        }

        val allCategories = stateHolder.commonHandler.categories.value
        val filteredCategories = mutableListOf<SearchItem.CategoryItem>()

        fun searchRecursive(categories: List<Category>, path: String, level: Int) {
            if (level > 3) return
            categories.forEach { category ->
                val currentPath = if (path.isEmpty()) category.name else "$path > ${category.name}"
                if (category.name.contains(value, ignoreCase = true)) {
                    filteredCategories.add(SearchItem.CategoryItem(category, currentPath))
                }
                searchRecursive(category.childrenData, currentPath, level + 1)
            }
        }

        searchRecursive(allCategories, "", 1)

        _searchResults.update { filteredCategories }

        itemSearchJob = scope.launch {
            delay(350)
            showLoader()
            productRepository.searchProductsByCategory(
                searchText = value,
                vendorId = currentVendorId,
                pageSize = 20,
                currentPage = 1
            ).collect { result ->
                hideLoader()
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> {
                        val productItems = result.data.map { SearchItem.ProductItem(it) }
                        _searchResults.update { filteredCategories + productItems }
                    }

                    is ResultState.Error -> {
                        // Možda logovati grešku, ali ne prikazivati poruku korisniku tokom kucanja da ne bude naporno
                    }
                }
            }
        }
    }

    fun onItemSelected(item: SearchItem) {
        val triple = when (item) {
            is SearchItem.ProductItem -> Triple(
                item.product.entityId.toString(),
                item.product.name.orEmpty(),
                "proizvod"
            )

            is SearchItem.CategoryItem -> Triple(
                item.category.id.toString(),
                item.fullPath,
                "category"
            )
        }
        itemOrCategory.value = triple
        itemOrCategorySearchText.value = triple.second
        _searchResults.update { emptyList() }
    }

    fun save() {
        if (!validate()) return

        scope.launch {
            val discount = discountPercent.value.replace(',', '.').toFloatOrNull() ?: 0f
            val minQty = minQtyForDiscount.value.replace(',', '.').toFloatOrNull()

            val productId = if (itemOrCategory.value?.third == "proizvod") itemOrCategory.value?.first?.toLongOrNull() else null
            val categoryId = if (itemOrCategory.value?.third == "category") itemOrCategory.value?.first?.toLongOrNull() else null

            val payload = CustomerRuleRequest(
                discountRule = DiscountRule(
                    discountType = ruleScope.toApiScope(),
                    customerId = if (ruleScope == RuleScope.CUSTOMER) selectedShop.value?.id?.toLongOrNull() else null,
                    customerGroupValue = if (ruleScope == RuleScope.CUSTOMER_TYPE) target.value.trim().ifBlank { null } else null,
                    customerRegionValue = if (ruleScope == RuleScope.CUSTOMER_REGION) selectedCustomerRegion.value?.unit()?.ifBlank { null } else null,
                    productId = productId,
                    categoryId = categoryId,
                    minQty = minQty,
                    discountPercent = discount,
                    isActive = 1
                )
            )

            val flow = if (isEditing) {
                repository.updateCustomerRule(initialRule?.id.orEmpty(), payload)
            } else {
                repository.createCustomerRule(payload)
            }

            flow.collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        showMessage(if (isEditing) "Pravilo izmijenjeno." else "Pravilo dodano.")
                        dashBack()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun delete() {
        if (!isEditing) return
        scope.launch {
            repository.deleteCustomerRule(initialRule?.id.orEmpty()).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        showMessage("Pravilo obrisano.")
                        dashBack()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun cancel() {
        dashBack()
    }

    private fun validate(): Boolean {
        if (ruleScope != RuleScope.CUSTOMER && target.value.isBlank()) {
            showErrorMessage("Molimo odaberite ${ruleScope.targetField()}.")
            return false
        }
        val discount = discountPercent.value.replace(',', '.').toDoubleOrNull()
        if (discount == null || discount <= 0.0 || discount > 100.0) {
            showErrorMessage("Rabat mora biti između 0 i 100 %.")
            return false
        }
        return true
    }
}

sealed class SearchItem {
    data class ProductItem(val product: Product) : SearchItem()
    data class CategoryItem(val category: Category, val fullPath: String) : SearchItem()

    val displayName: String
        get() = when (this) {
            is ProductItem -> product.name.orEmpty()
            is CategoryItem -> fullPath
        }
}

private fun RuleScope.targetField(): String = when (this) {
    RuleScope.CUSTOMER -> "kupca"
    RuleScope.CUSTOMER_TYPE -> "tip kupca"
    RuleScope.CUSTOMER_REGION -> "regiju kupca"
}

private fun RuleScope.toApiScope(): String = when (this) {
    RuleScope.CUSTOMER -> "per_customer"
    RuleScope.CUSTOMER_TYPE -> "per_customer_group"
    RuleScope.CUSTOMER_REGION -> "per_customer_region"
}
