package karika.distribucija.ba.salesrep.ui.analytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.logging.AppLogger
import karika.distribucija.ba.salesrep.api.SalesAnalyticsRepository
import karika.distribucija.ba.salesrep.model.AnalyticsTrends
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.launch

/**
 * Backs the "Trendovi prodaje" tab of the Analitika feature.
 *
 * The trends endpoint itself doesn't compute a revenue-growth percentage, so this mirrors
 * composeApp's AnalyticsComponent.kt: [karika.distribucija.ba.salesrep.api.SalesAnalyticsRepository.getOverview]
 * is fetched first (for the current [AnalyticsFilterState]) purely to read its `revenueGrowth`,
 * which is then threaded into [karika.distribucija.ba.salesrep.api.SalesAnalyticsRepository.getTrends]
 * so the "Rast" figure on the revenue card matches the Pregled tab.
 */
class AnalyticsTrendsViewModel : ViewModel() {

    companion object {
        private const val TAG = "AnalyticsTrendsViewModel"
    }

    private val repository = SalesAnalyticsRepository()

    private val _trends = MutableLiveData(AnalyticsTrends())
    val trends: LiveData<AnalyticsTrends> = _trends

    fun load() {
        viewModelScope.launch {
            var revenueGrowth = 0.0
            repository.getOverview(
                AnalyticsFilterState.dateFromApi(),
                AnalyticsFilterState.dateToApi(),
                AnalyticsFilterState.comparisonBasisApi(),
            ).collect { result ->
                when (result) {
                    is ResultState.Success -> revenueGrowth = result.data.revenueGrowth
                    is ResultState.Error -> AppLogger.e(TAG, "Failed to load overview for revenueGrowth: ${result.message}")
                    ResultState.Loading -> Unit
                }
            }

            repository.getTrends(
                AnalyticsFilterState.dateFromApi(),
                AnalyticsFilterState.dateToApi(),
                AnalyticsFilterState.groupByApi(),
                revenueGrowth,
            ).collect { result ->
                when (result) {
                    is ResultState.Success -> _trends.value = result.data
                    is ResultState.Error -> AppLogger.e(TAG, "Failed to load trends: ${result.message}")
                    ResultState.Loading -> Unit
                }
            }
        }
    }
}
