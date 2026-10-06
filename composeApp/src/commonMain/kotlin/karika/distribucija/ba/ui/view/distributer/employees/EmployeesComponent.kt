package karika.distribucija.ba.ui.view.distributer.employees

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.EmployeesRepository
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorEmployee
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.distributer.dashboard.DashConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EmployeeStatusFilter(val label: String, val apiValue: String?) {
    All("Svi", null),
    Active("Aktivni", "active"),
    Invited("Pozvani", "invited"),
    Suspended("Suspendovani", "suspended"),
}

/** Vendor employee roster (`GET /V1/vendor-operations/employees`). Gated on `/me`
 * `vendor_operations_enabled` + `capabilities.can_view_employees` - never inferred from role. */
class EmployeesComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    private val repository = EmployeesRepository()

    private val _employees = MutableStateFlow<List<VendorEmployee>>(emptyList())
    val employees = _employees.asStateFlow()

    private val _totalCount = MutableStateFlow(0)
    val totalCount = _totalCount.asStateFlow()

    private val _statusFilter = MutableStateFlow(EmployeeStatusFilter.All)
    val statusFilter = _statusFilter.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded = _loaded.asStateFlow()

    private var search: String? = null

    init {
        // Same page size as the vendor web employees list.
        pageSize = 12
    }

    val canViewEmployees: Boolean
        get() = stateHolder.salesSpecificHandler.me.value.let {
            it.vendorOperationsEnabled && it.capabilities.canViewEmployees
        }

    fun search(query: String) {
        val normalized = query.trim().ifBlank { null }
        if (normalized == search) return
        search = normalized
        loadNextPage(true)
    }

    fun setStatusFilter(filter: EmployeeStatusFilter) {
        if (_statusFilter.value == filter) return
        _statusFilter.value = filter
        loadNextPage(true)
    }

    override fun loadNextPage(reset: Boolean) {
        if (!canViewEmployees) return

        if (reset) {
            hasNextPage = true
            currentPage = 1
        }

        if (!hasNextPage || loader.value) {
            return
        }

        scope.launch {
            repository.getEmployees(
                page = currentPage,
                pageSize = pageSize,
                search = search,
                status = _statusFilter.value.apiValue
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _employees.update {
                            if (reset) result.data.items else (it + result.data.items).distinctBy { e -> e.employeeId }
                        }
                        _totalCount.value = result.data.totalCount
                        _loaded.value = true
                        currentPage++
                        hasNextPage = _employees.value.size < result.data.totalCount &&
                            result.data.items.size == pageSize
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        _loaded.value = true
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun openLocations(employee: VendorEmployee) {
        if (!employee.canOpenLocations) return
        dashNavigate(DashConfig.EmployeeLocations(employee))
    }
}
