package karika.distribucija.ba.salesrep.ui.customers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.OperationalCustomer
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.session.CurrentUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Mirrors composeApp's ui/view/salesrep/customers/SalesCustomersComponent.kt */
class CustomersViewModel : ViewModel() {

    enum class Tab { ALL, MINE }

    private val repository = SalesRepository()

    private val _tab = MutableLiveData(Tab.ALL)
    val tab: LiveData<Tab> = _tab

    private val _customers = MutableLiveData<List<OperationalCustomer>>(emptyList())
    val customers: LiveData<List<OperationalCustomer>> = _customers

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private var currentPage = 1
    private var totalCount = 0L
    private var searchQuery = ""
    private var statusFilter: String? = "active"
    private var searchJob: Job? = null

    val hasMore: Boolean
        get() = (_customers.value?.size ?: 0).toLong() < totalCount

    val canSeeAllCustomers: Boolean
        get() = CurrentUser.me?.capabilities?.canSeeAllVendorCustomers ?: false

    // No init { loadPage(...) } here - the fragment's onResume() already calls refresh() on
    // first display (and on every return to this screen), so an init-time load would double the call.

    fun selectTab(tab: Tab) {
        if (_tab.value == tab) return
        _tab.value = tab
        searchQuery = ""
        statusFilter = "active"
        loadPage(page = 1, replace = true)
    }

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
        if (replace) _customers.value = emptyList()
        viewModelScope.launch {
            val flow = if (_tab.value == Tab.MINE || !canSeeAllCustomers) {
                repository.getEmployeeCustomers(
                    employeeId = CurrentUser.employeeId ?: 0L,
                    page = page,
                    pageSize = 10,
                    search = searchQuery.takeIf { it.isNotBlank() },
                    status = statusFilter
                )
            } else {
                repository.getCustomers(
                    page = page,
                    pageSize = 10,
                    search = searchQuery.takeIf { it.isNotBlank() },
                    status = statusFilter
                )
            }
            flow.collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        if (replace) _isRefreshing.value = true else _isLoadingMore.value = true
                    }

                    is ResultState.Success -> {
                        if (replace) _isRefreshing.value = false else _isLoadingMore.value = false
                        currentPage = page
                        totalCount = result.data.totalCount
                        _customers.value = if (replace) {
                            result.data.items
                        } else {
                            _customers.value.orEmpty() + result.data.items
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
