package karika.distribucija.ba.salesrep.ui.analytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesAnalyticsRepository
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.model.SalesRepsPerformance
import karika.distribucija.ba.salesrep.session.CurrentUser
import kotlinx.coroutines.launch

/**
 * Backs the "Komercijalisti" (sales rep performance) screen. Mirrors [karika.distribucija.ba.salesrep.ui.notifications.NotificationsViewModel]'s
 * shape: a plain repository-backed ViewModel exposing a data LiveData and a loading flag.
 */
class AnalyticsRepsViewModel : ViewModel() {

    private val repository = SalesAnalyticsRepository()

    private val _reps = MutableLiveData(SalesRepsPerformance())
    val reps: LiveData<SalesRepsPerformance> = _reps

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    /** `GET /analytics/representatives` is 403 `forbidden` for a `sales_employee` without
     * `can_see_dashboard` - the drawer entry to this screen is already hidden for such users, but
     * this is a defensive second layer: never call the repository unless the capability is present. */
    fun load() {
        if (CurrentUser.me?.capabilities?.canSeeDashboard != true) {
            _reps.value = SalesRepsPerformance()
            _loading.value = false
            return
        }
        viewModelScope.launch {
            repository.getReps(AnalyticsFilterState.dateFromApi(), AnalyticsFilterState.dateToApi())
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> _loading.value = true
                        is ResultState.Success -> {
                            _loading.value = false
                            _reps.value = result.data
                        }
                        is ResultState.Error -> _loading.value = false
                    }
                }
        }
    }
}
