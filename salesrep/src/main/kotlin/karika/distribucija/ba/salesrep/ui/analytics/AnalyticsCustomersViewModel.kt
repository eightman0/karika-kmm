package karika.distribucija.ba.salesrep.ui.analytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.logging.AppLogger
import karika.distribucija.ba.salesrep.api.SalesAnalyticsRepository
import karika.distribucija.ba.salesrep.model.CustomerAnalytics
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.launch

/** Backs the "Analitika kupaca" (customer analytics) tab of the Analitika feature. */
class AnalyticsCustomersViewModel : ViewModel() {

    companion object {
        private const val TAG = "AnalyticsCustomersViewModel"
    }

    private val repository = SalesAnalyticsRepository()

    private val _customerAnalytics = MutableLiveData(CustomerAnalytics())
    val customerAnalytics: LiveData<CustomerAnalytics> = _customerAnalytics

    /** Fetches customer analytics using the current [AnalyticsFilterState]. This is a
     * background-ish refresh - errors are logged only, the existing data (or the default zeroed
     * model) stays on screen rather than showing a toast. */
    fun load() {
        viewModelScope.launch {
            repository.getCustomerAnalytics(
                AnalyticsFilterState.dateFromApi(),
                AnalyticsFilterState.dateToApi()
            ).collect { result ->
                when (result) {
                    is ResultState.Success -> _customerAnalytics.value = result.data
                    is ResultState.Error -> AppLogger.e(TAG, "Failed to load customer analytics: ${result.message}")
                    ResultState.Loading -> Unit
                }
            }
        }
    }
}
