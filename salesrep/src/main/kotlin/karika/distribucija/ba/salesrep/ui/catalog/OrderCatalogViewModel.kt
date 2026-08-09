package karika.distribucija.ba.salesrep.ui.catalog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.OnBehalfProduct
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.session.CartState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Mirrors composeApp's SalesOrderCatalogComponent.kt, simplified to 2 tabs (ALL/PREVIOUSLY_ORDERED)
 * - the ON_SALE tab is dropped since it depends on hardcoded vendor-specific category IDs
 * (KarikaConfig.getActionId()/getOutletId()) not available here. Category filtering (hierarchical
 * tree) is also dropped, same simplification as the discount form's item search.
 */
class OrderCatalogViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    enum class Tab { ALL, PREVIOUSLY_ORDERED }

    private val repository = SalesRepository()
    val customerId: Long = savedStateHandle.get<Long>("customerId") ?: 0L

    private val _tab = MutableLiveData(Tab.ALL)
    val tab: LiveData<Tab> = _tab

    private val _products = MutableLiveData<List<OnBehalfProduct>>(emptyList())
    val products: LiveData<List<OnBehalfProduct>> = _products

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private var currentPage = 1
    private var hasNext = false
    private var searchQuery = ""
    private var searchJob: Job? = null
    private var loadJob: Job? = null
    private val qtyJobs = mutableMapOf<String, Job>()

    init {
        loadCart()
        loadProducts(reset = true)
    }

    private fun loadCart() {
        viewModelScope.launch {
            repository.getCart(customerId).collect { result ->
                if (result is ResultState.Success) {
                    CartState.cart.value = result.data
                }
            }
        }
    }

    fun selectTab(tab: Tab) {
        if (_tab.value == tab) return
        _tab.value = tab
        loadProducts(reset = true)
    }

    fun setSearch(query: String) {
        searchQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            loadProducts(reset = true)
        }
    }

    fun loadNextPage() {
        if (_isLoadingMore.value == true || _isLoading.value == true || !hasNext) return
        loadProducts(reset = false)
    }

    fun getCartQty(product: OnBehalfProduct): Int =
        CartState.cart.value?.items?.find { it.sku == product.sku }?.qty ?: 0

    fun changeQty(product: OnBehalfProduct, qty: Int) {
        qtyJobs[product.sku]?.cancel()
        qtyJobs[product.sku] = viewModelScope.launch {
            delay(400)

            val existingItemId = CartState.cart.value?.items?.find { it.sku == product.sku }?.itemId
            val resultFlow = if (qty <= 0) {
                if (existingItemId == null) return@launch
                repository.removeCartItem(customerId, existingItemId)
            } else {
                repository.addCartItem(customerId, product.sku, qty)
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

    private fun loadProducts(reset: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val page = if (reset) {
                currentPage = 1; 1
            } else currentPage

            if (reset) _isLoading.value = true else _isLoadingMore.value = true

            val pageSize = 20
            val flow = if (_tab.value == Tab.PREVIOUSLY_ORDERED) {
                repository.getPreviouslyOrderedProducts(
                    customerId = customerId,
                    page = page,
                    pageSize = pageSize,
                    search = searchQuery.takeIf { it.isNotBlank() }
                )
            } else {
                repository.getProducts(
                    page = page,
                    pageSize = pageSize,
                    search = searchQuery.takeIf { it.isNotBlank() }
                )
            }

            flow.collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> {
                        val items = result.data.items
                        hasNext = items.size >= pageSize
                        _products.value = if (reset) items else _products.value.orEmpty() + items
                        if (hasNext) currentPage++
                        _isLoading.value = false
                        _isLoadingMore.value = false
                    }

                    is ResultState.Error -> {
                        _isLoading.value = false
                        _isLoadingMore.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }
}
