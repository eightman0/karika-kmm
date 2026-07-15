package karika.distribucija.ba.ui.view.salesrep.customers

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SalesCustomersComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {

    enum class CustomerTab { ALL_CUSTOMERS, MY_CUSTOMERS }

    private val repository = SalesRepository()

    private val _selectedTab = MutableStateFlow(CustomerTab.ALL_CUSTOMERS)
    val selectedTab = _selectedTab.asStateFlow()

    private val _customers = MutableStateFlow<List<OperationalCustomer>>(emptyList())
    val customers = _customers.asStateFlow()

    private val _totalCount = MutableStateFlow(0L)
    val totalCount = _totalCount.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore = _isLoadingMore.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter = _statusFilter.asStateFlow()

    val hasMore: Boolean
        get() = _customers.value.size.toLong() < _totalCount.value

    private var searchJob: Job? = null

    fun selectTab(tab: CustomerTab) {
        if (_selectedTab.value == tab) return
        _selectedTab.value = tab
        _searchQuery.value = ""
        _statusFilter.value = null
        loadPage(page = 1, replace = true)
    }

    /** Debounced — resets to page 1 after 400 ms of no input. */
    fun setSearch(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(400.milliseconds)
            loadPage(page = 1, replace = true)
        }
    }

    /** Immediate — applies status filter and resets to page 1. */
    fun setStatus(status: String?) {
        _statusFilter.value = status
        loadPage(page = 1, replace = true)
    }

    fun loadNextPage() {
        if (!hasMore || _isLoadingMore.value) return
        loadPage(page = currentPage + 1, replace = false)
    }

    fun openCustomer(customer: OperationalCustomer) {
        salesRepPush(
            karika.distribucija.ba.ui.view.salesrep.dashboard.SalesRepConfig.CustomerDetail(
                customer
            )
        )
    }

    fun openNewCustomer() {
        salesRepPush(karika.distribucija.ba.ui.view.salesrep.dashboard.SalesRepConfig.NewCustomer)
    }

    fun openInviteCustomer() {
        salesRepPush(karika.distribucija.ba.ui.view.salesrep.dashboard.SalesRepConfig.InviteCustomer())
    }

    fun openOrderCatalog(customer: OperationalCustomer) {
        salesRepPush(
            karika.distribucija.ba.ui.view.salesrep.dashboard.SalesRepConfig.OrderCatalog(
                customer
            )
        )
    }

    fun loadPage(page: Int, replace: Boolean) {
        scope.launch {
            val flow = if (_selectedTab.value == CustomerTab.MY_CUSTOMERS) {
                repository.getEmployeeCustomers(
                    employeeId = stateHolder.salesSpecificHandler.employeeId,
                    page = page,
                    pageSize = 10,
                    search = _searchQuery.value.takeIf { it.isNotBlank() }
                )
            } else {
                repository.getCustomers(
                    page = page,
                    pageSize = 10,
                    search = _searchQuery.value.takeIf { it.isNotBlank() },
                    status = _statusFilter.value
                )
            }
            flow.collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        if (replace) showLoader() else _isLoadingMore.value = true
                    }

                    is ResultState.Success -> {
                        if (replace) hideLoader() else _isLoadingMore.value = false
                        currentPage = page
                        _totalCount.value = result.data.totalCount
                        if (replace) {
                            _customers.value = result.data.items
                        } else {
                            _customers.update { it + result.data.items }
                        }
                    }

                    is ResultState.Error -> {
                        if (replace) hideLoader() else _isLoadingMore.value = false
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }
}
