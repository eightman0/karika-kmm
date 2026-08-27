package karika.distribucija.ba.salesrep.ui.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.OnBehalfOrder
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Mirrors composeApp's ui/view/salesrep/orders/SalesOrdersComponent.kt pagination/search logic. */
class OrdersViewModel : ViewModel() {
    private val repository = SalesRepository()

    private val _orders = MutableLiveData<List<OnBehalfOrder>>(emptyList())
    val orders: LiveData<List<OnBehalfOrder>> = _orders

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private var currentPage = 1
    private var totalCount = 0L
    private var searchQuery = ""
    private var statusFilter: String? = null
    private var searchJob: Job? = null

    private val hasMore: Boolean
        get() = (_orders.value?.size ?: 0).toLong() < totalCount

    // No init { loadPage(...) } here - the fragment's onResume() already calls refresh() on
    // first display (and on every return to this screen), so an init-time load would double the call.

    fun setSearch(query: String) {
        searchQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            loadPage(page = 1, replace = true)
        }
    }

    fun setStatus(status: String?) {
        statusFilter = status
        loadPage(page = 1, replace = true)
    }

    fun loadNextPage() {
        if (!hasMore || _isLoadingMore.value == true) return
        loadPage(page = currentPage + 1, replace = false)
    }

    fun refresh() {
        loadPage(page = 1, replace = true)
    }

    private fun loadPage(page: Int, replace: Boolean) {
        if (replace) _orders.value = emptyList()
        viewModelScope.launch {
            repository.getOrders(
                page = page,
                pageSize = 10,
                search = searchQuery.takeIf { it.isNotBlank() },
                status = statusFilter
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        if (replace) _isRefreshing.value = true else _isLoadingMore.value = true
                    }

                    is ResultState.Success -> {
                        if (replace) _isRefreshing.value = false else _isLoadingMore.value = false
                        currentPage = page
                        totalCount = result.data.totalCount
                        _orders.value = if (replace) {
                            result.data.items
                        } else {
                            _orders.value.orEmpty() + result.data.items
                        }
                        _errorMessage.value = null
                    }

                    is ResultState.Error -> {
                        if (replace) _isRefreshing.value = false else _isLoadingMore.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }
}
