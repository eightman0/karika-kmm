package karika.distribucija.ba.ui.view.salesrep.catalog

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.OnBehalfCartResponse
import karika.distribucija.ba.domain.model.OnBehalfProduct
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.salesrep.dashboard.SalesRepConfig
import karika.distribucija.ba.util.KarikaConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SalesOrderCatalogComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    val customer: OperationalCustomer
) : CommonComponent(componentContext, stateHolder) {

    enum class CatalogTab { ALL_ITEMS, ON_SALE, PREVIOUSLY_ORDERED }

    private val salesRepository = SalesRepository()

    // ── Tabs ───────────────────────────────────────────────────────────────────
    private val _selectedTab = MutableStateFlow(CatalogTab.ALL_ITEMS)
    val selectedTab = _selectedTab.asStateFlow()

    // ── Products ───────────────────────────────────────────────────────────────
    private val _products = MutableStateFlow<List<OnBehalfProduct>>(emptyList())
    val products = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore = _isLoadingMore.asStateFlow()

    private val _hasNext = MutableStateFlow(false)
    val hasNext = _hasNext.asStateFlow()

    // ── Search + category filter ───────────────────────────────────────────────
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    // ── Shared server cart via stateHolder ─────────────────────────────────────
    val cart: StateFlow<OnBehalfCartResponse?> = stateHolder.salesSpecificHandler.cart

    val cartCount: StateFlow<Int> = stateHolder.salesSpecificHandler.cart
        .map { it?.itemsCount ?: 0 }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    private var searchJob: Job? = null
    private var loadJob: Job? = null
    private val qtyJobs = mutableMapOf<String, Job>()

    init {
        loadCart()
        loadProducts(reset = true)
    }

    private fun loadCart() {
        scope.launch {
            salesRepository.getCart(customer.customerId).collect { result ->
                if (result is ResultState.Success) {
                    stateHolder.salesSpecificHandler.cart.value = result.data
                }
            }
        }
    }

    fun selectTab(tab: CatalogTab) {
        if (_selectedTab.value == tab) return
        _selectedTab.value = tab
        loadProducts(reset = true)
    }

    fun setSearch(text: String) {
        _searchText.value = text
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(400.milliseconds)
            loadProducts(reset = true)
        }
    }

    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
        loadProducts(reset = true)
    }

    fun loadNextPage() {
        if (_isLoadingMore.value || _isLoading.value || !_hasNext.value) return
        loadProducts(reset = false)
    }

    fun getCartQty(product: OnBehalfProduct): Int =
        cart.value?.items?.find { it.sku == product.sku }?.qty ?: 0

    fun changeQty(product: OnBehalfProduct, qty: Int) {
        showLoader()
        qtyJobs[product.sku]?.cancel()
        qtyJobs[product.sku] = scope.launch {
            delay(400.milliseconds)

            val existingItemId = cart.value?.items?.find { it.sku == product.sku }?.itemId
            val resultFlow = if (qty <= 0) {
                if (existingItemId == null) {
                    hideLoader()
                    return@launch
                }
                salesRepository.removeCartItem(customer.customerId, existingItemId)
            } else {
                salesRepository.addCartItem(customer.customerId, product.sku, qty)
            }

            resultFlow.collect { result ->
                when (result) {
                    is ResultState.Success -> {
                        hideLoader()
                        stateHolder.salesSpecificHandler.cart.value = result.data
                    }
                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                    is ResultState.Loading -> { /* no-op */ }
                }
            }
        }
    }

    fun openCart() = salesRepPush(SalesRepConfig.OrderCart(customer))

    fun goBack() = salesRepBack()

    private fun loadProducts(reset: Boolean) {
        loadJob?.cancel()

        loadJob = scope.launch {
            val page = if (reset) {
                currentPage = 1; 1
            } else currentPage

            if (reset) _isLoading.value = true else _isLoadingMore.value = true

            val resultFlow = when (_selectedTab.value) {
                CatalogTab.PREVIOUSLY_ORDERED -> salesRepository.getPreviouslyOrderedProducts(
                    customerId = customer.customerId,
                    page = page,
                    pageSize = pageSize,
                    search = _searchText.value.takeIf { it.isNotBlank() }
                )

                CatalogTab.ON_SALE -> salesRepository.getProducts(
                    page = page,
                    pageSize = pageSize,
                    search = _searchText.value.takeIf { it.isNotBlank() },
                    categoryId = "${KarikaConfig.getActionId()},${KarikaConfig.getOutletId()}"
                )

                else -> salesRepository.getProducts(
                    page = page,
                    pageSize = pageSize,
                    search = _searchText.value.takeIf { it.isNotBlank() },
                    categoryId = _selectedCategory.value?.id?.toString()
                )
            }

            resultFlow.collect { result ->
                when (result) {
                    is ResultState.Loading -> { /* loader set above */
                    }

                    is ResultState.Success -> {
                        val items = result.data.items
                        _hasNext.value = items.size >= pageSize
                        if (reset) {
                            _products.value = items
                        } else {
                            _products.value += items
                        }
                        if (_hasNext.value) currentPage++
                        _isLoading.value = false
                        _isLoadingMore.value = false
                    }

                    is ResultState.Error -> {
                        _isLoading.value = false
                        _isLoadingMore.value = false
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }
}
