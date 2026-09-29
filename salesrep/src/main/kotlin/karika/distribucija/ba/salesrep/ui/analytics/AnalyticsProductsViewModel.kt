package karika.distribucija.ba.salesrep.ui.analytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesAnalyticsRepository
import karika.distribucija.ba.salesrep.model.AnalyticsCategory
import karika.distribucija.ba.salesrep.model.AnalyticsProduct
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.launch

/** Backs the "Proizvodi i kategorije" tab of the Analitika feature. Fetches both lists
 * sequentially (products, then categories) for the currently selected [AnalyticsFilterState]
 * range - [isLoading] only clears once the second (categories) call settles, so both lists are
 * ready together by the time the screen stops showing its loading state. */
class AnalyticsProductsViewModel : ViewModel() {

    private val repository = SalesAnalyticsRepository()

    private val _products = MutableLiveData<List<AnalyticsProduct>>(emptyList())
    val products: LiveData<List<AnalyticsProduct>> = _products

    private val _categories = MutableLiveData<List<AnalyticsCategory>>(emptyList())
    val categories: LiveData<List<AnalyticsCategory>> = _categories

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true

            repository.getProducts(
                AnalyticsFilterState.dateFromApi(),
                AnalyticsFilterState.dateToApi()
            ).collect { result ->
                if (result is ResultState.Success) {
                    _products.value = result.data
                }
            }

            repository.getCategories(
                AnalyticsFilterState.dateFromApi(),
                AnalyticsFilterState.dateToApi()
            ).collect { result ->
                when (result) {
                    is ResultState.Success -> {
                        _categories.value = result.data
                        _isLoading.value = false
                    }
                    is ResultState.Error -> _isLoading.value = false
                    ResultState.Loading -> Unit
                }
            }
        }
    }
}
