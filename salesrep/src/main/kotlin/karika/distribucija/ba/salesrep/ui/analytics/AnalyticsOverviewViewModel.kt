package karika.distribucija.ba.salesrep.ui.analytics

import karika.distribucija.ba.salesrep.session.CurrentUser
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.logging.AppLogger
import karika.distribucija.ba.salesrep.api.SalesAnalyticsRepository
import karika.distribucija.ba.salesrep.model.AnalyticsOverview
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.launch

/** Backs the "Pregled" (overview) tab of the Analitika feature. */
class AnalyticsOverviewViewModel : ViewModel() {

    companion object {
        private const val TAG = "AnalyticsOverviewViewModel"
    }

    private val repository = SalesAnalyticsRepository()

    private val _overview = MutableLiveData(AnalyticsOverview())
    val overview: LiveData<AnalyticsOverview> = _overview

    /** Fetches the overview using the current [AnalyticsFilterState]. This is a background-ish
     * refresh - errors are logged only, the existing data (or the default zeroed model) stays on
     * screen rather than showing a toast. */
    fun load() {
        // Nothing is fetched until `me` is known to allow analytics - MainActivity re-applies
        // the filters once it is, which reloads the visible screen.
        if (CurrentUser.analyticsAccess != true) return
        viewModelScope.launch {
            repository.getOverview(
                AnalyticsFilterState.dateFromApi(),
                AnalyticsFilterState.dateToApi(),
                AnalyticsFilterState.comparisonBasisApi()
            ).collect { result ->
                when (result) {
                    is ResultState.Success -> _overview.value = result.data
                    is ResultState.Error -> AppLogger.e(TAG, "Failed to load analytics overview: ${result.message}")
                    ResultState.Loading -> Unit
                }
            }
        }
    }
}
