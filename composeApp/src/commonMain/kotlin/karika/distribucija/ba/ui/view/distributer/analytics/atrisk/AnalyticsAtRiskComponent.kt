package karika.distribucija.ba.ui.view.distributer.analytics.atrisk

import androidx.compose.runtime.snapshotFlow
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesAnalyticsRepository
import karika.distribucija.ba.domain.model.AtRiskCustomers
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.distributer.analytics.toApiDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AtRiskFilter {
    All, ApproachingRisk, AtRisk, SeriouslyOverdue, NeverOrdered
}

class AnalyticsAtRiskComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
) : CommonComponent(componentContext, stateHolder) {

    private val repository = SalesAnalyticsRepository()
    private val filters get() = stateHolder.analyticsFilterState

    private val _data = MutableStateFlow(AtRiskCustomers())
    val data = _data.asStateFlow()

    private val _selectedFilter = MutableStateFlow(AtRiskFilter.All)
    val selectedFilter = _selectedFilter.asStateFlow()

    init {
        scope.launch {
            snapshotFlow { filters.applyTrigger.value }
                .collectLatest {
                    showLoader()
                    try {
                        val dateFrom = filters.dateFromMillis.value.toApiDate()
                        val dateTo = filters.dateToMillis.value.toApiDate()
                        repository.getAtRiskCustomers(dateFrom, dateTo).collect { result ->
                            if (result is ResultState.Success) _data.update { result.data }
                            if (result is ResultState.Error) showMessage(result.message ?: "")
                        }
                    } finally {
                        hideLoader()
                    }
                }
        }
    }

    fun selectFilter(filter: AtRiskFilter) {
        _selectedFilter.update { filter }
    }
}
