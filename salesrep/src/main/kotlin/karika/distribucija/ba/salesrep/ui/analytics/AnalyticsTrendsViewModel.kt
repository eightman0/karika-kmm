package karika.distribucija.ba.salesrep.ui.analytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.logging.AppLogger
import karika.distribucija.ba.salesrep.api.SalesAnalyticsRepository
import karika.distribucija.ba.salesrep.model.AnalyticsTrends
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Backs the "Trendovi prodaje" tab of the Analitika feature (`GET /analytics/trends` for the
 * current [AnalyticsFilterState] range and grouping). */
class AnalyticsTrendsViewModel : ViewModel() {

    companion object {
        private const val TAG = "AnalyticsTrendsViewModel"
    }

    private val repository = SalesAnalyticsRepository()

    private val _trends = MutableLiveData<AnalyticsTrends>()
    val trends: LiveData<AnalyticsTrends> = _trends

    private var loadJob: Job? = null

    /** Errors are logged only - the existing charts stay on screen rather than showing a toast. */
    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            repository.getTrends(
                AnalyticsFilterState.dateFromApi(),
                AnalyticsFilterState.dateToApi(),
                AnalyticsFilterState.groupByApi(),
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
