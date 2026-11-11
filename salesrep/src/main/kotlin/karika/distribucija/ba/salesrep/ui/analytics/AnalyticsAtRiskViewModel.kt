package karika.distribucija.ba.salesrep.ui.analytics

import karika.distribucija.ba.salesrep.session.CurrentUser
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesAnalyticsRepository
import karika.distribucija.ba.salesrep.model.AtRiskCustomers
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.launch

/** Backs the "Kupci koji zahtijevaju pažnju" (at-risk customers) tab of the Analitika feature.
 * Loading is only ever triggered by [AnalyticsFilterState.applyTrigger] (observed by the
 * fragment) - there is no init-block load() here, mirroring [AnalyticsOverviewViewModel]. */
class AnalyticsAtRiskViewModel : ViewModel() {

    private val repository = SalesAnalyticsRepository()

    private val _atRiskCustomers = MutableLiveData(AtRiskCustomers())
    val atRiskCustomers: LiveData<AtRiskCustomers> = _atRiskCustomers

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun load() {
        // Nothing is fetched until `me` is known to allow analytics - MainActivity re-applies
        // the filters once it is, which reloads the visible screen.
        if (CurrentUser.analyticsAccess != true) return
        viewModelScope.launch {
            repository.getAtRiskCustomers(
                AnalyticsFilterState.dateFromApi(),
                AnalyticsFilterState.dateToApi()
            ).collect { result ->
                when (result) {
                    ResultState.Loading -> _isLoading.value = true
                    is ResultState.Success -> {
                        _isLoading.value = false
                        _atRiskCustomers.value = result.data
                    }
                    is ResultState.Error -> _isLoading.value = false
                }
            }
        }
    }
}
