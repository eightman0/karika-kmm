package karika.distribucija.ba.ui.view.distributer.analytics.products

import androidx.compose.runtime.snapshotFlow
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesAnalyticsRepository
import karika.distribucija.ba.domain.model.AnalyticsCategory
import karika.distribucija.ba.domain.model.AnalyticsProduct
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.distributer.analytics.toApiDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProductsSubTab {
    Products, Categories
}

class AnalyticsProductsComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
) : CommonComponent(componentContext, stateHolder) {

    private val repository = SalesAnalyticsRepository()
    private val filters get() = stateHolder.analyticsFilterState

    private val _selectedSubTab = MutableStateFlow(ProductsSubTab.Products)
    val selectedSubTab = _selectedSubTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _products = MutableStateFlow<List<AnalyticsProduct>>(emptyList())
    val products = _products.asStateFlow()

    private val _categories = MutableStateFlow<List<AnalyticsCategory>>(emptyList())
    val categories = _categories.asStateFlow()

    init {
        scope.launch {
            snapshotFlow { filters.applyTrigger.value }
                .collectLatest {
                    showLoader()
                    try {
                        val dateFrom = filters.dateFromMillis.value.toApiDate()
                        val dateTo = filters.dateToMillis.value.toApiDate()
                        repository.getProducts(dateFrom, dateTo).collect { result ->
                            if (result is ResultState.Success) _products.update { result.data }
                            if (result is ResultState.Error) showMessage(result.message ?: "")
                        }
                        repository.getCategories(dateFrom, dateTo).collect { result ->
                            if (result is ResultState.Success) _categories.update { result.data }
                            if (result is ResultState.Error) showMessage(result.message ?: "")
                        }
                    } finally {
                        hideLoader()
                    }
                }
        }
    }

    fun selectSubTab(tab: ProductsSubTab) {
        _selectedSubTab.update { tab }
    }

    fun search(query: String) {
        _searchQuery.update { query }
    }
}
