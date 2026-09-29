package karika.distribucija.ba.ui.view.distributer.analytics

import androidx.compose.runtime.snapshotFlow
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesAnalyticsRepository
import karika.distribucija.ba.domain.model.AnalyticsOverview
import karika.distribucija.ba.domain.model.AnalyticsTrends
import karika.distribucija.ba.domain.model.CustomerAnalytics
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.SalesRepsPerformance
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
enum class AnalyticsTab {
    Overview, Trends, Reps, Customers
}

class AnalyticsComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    initialTab: AnalyticsTab = AnalyticsTab.Overview,
    private val onOpenFilters: () -> Unit,
    private val onOpenAtRiskCustomers: () -> Unit,
) : CommonComponent(componentContext, stateHolder) {

    private val repository = SalesAnalyticsRepository()

    val selectedTab = MutableStateFlow(initialTab).asStateFlow()
    val filters get() = stateHolder.analyticsFilterState

    /** `GET /analytics/representatives` is 403 `forbidden` for a sales_employee - only fetch/show
     * it for vendor_admin/vendor_manager (`can_see_dashboard`). */
    val canSeeDashboard get() = stateHolder.salesSpecificHandler.me.value.capabilities.canSeeDashboard

    fun openFilters() {
        onOpenFilters()
    }

    private val _overview = MutableStateFlow(AnalyticsOverview())
    val overview = _overview.asStateFlow()

    private val _trends = MutableStateFlow(AnalyticsTrends())
    val trends = _trends.asStateFlow()

    private val _reps = MutableStateFlow(SalesRepsPerformance())
    val reps = _reps.asStateFlow()

    private val _customers = MutableStateFlow(CustomerAnalytics())
    val customers = _customers.asStateFlow()

    init {
        scope.launch {
            snapshotFlow { filters.applyTrigger.value }
                .collectLatest { refresh() }
        }
    }

    private suspend fun refresh() {
        showLoader()
        try {
            val dateFrom = filters.dateFromMillis.value.toApiDate()
            val dateTo = filters.dateToMillis.value.toApiDate()
            val comparisonBasis = filters.comparison.value.toApiComparisonBasis()
            val groupBy = filters.grouping.value.toApiGroupBy()

            repository.getOverview(dateFrom, dateTo, comparisonBasis).collect { result ->
                if (result is ResultState.Success) _overview.update { result.data }
                if (result is ResultState.Error) showMessage(result.message ?: "")
            }
            repository.getTrends(dateFrom, dateTo, groupBy, _overview.value.revenueGrowth).collect { result ->
                if (result is ResultState.Success) _trends.update { result.data }
                if (result is ResultState.Error) showMessage(result.message ?: "")
            }
            if (canSeeDashboard) {
                repository.getReps(dateFrom, dateTo).collect { result ->
                    if (result is ResultState.Success) _reps.update { result.data }
                    if (result is ResultState.Error) showMessage(result.message ?: "")
                }
            }
            repository.getCustomerAnalytics(dateFrom, dateTo).collect { result ->
                if (result is ResultState.Success) _customers.update { result.data }
                if (result is ResultState.Error) showMessage(result.message ?: "")
            }
        } finally {
            hideLoader()
        }
    }

    fun openAtRiskCustomers() {
        onOpenAtRiskCustomers()
    }
}
