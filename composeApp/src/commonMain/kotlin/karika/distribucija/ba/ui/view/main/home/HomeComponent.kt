package karika.distribucija.ba.ui.view.main.home

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
) : CommonComponent(componentContext, stateHolder) {

    private val repository = ProductRepository()
    private val _newArrivals = MutableStateFlow<List<Product>>(emptyList())
    val newArrivals = _newArrivals.asStateFlow()

    private val _promotedVendors =
        MutableStateFlow<List<Pair<PromotedVendor, PromotedVendor>>>(emptyList())
    val promotedVendors = _promotedVendors.asStateFlow()

    fun loadData() {
        iOScope.launch {
            repository.searchProductsByCategory(
                categoryId = "439",
                currentPage = 1
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        showLoader()
                    }

                    is ResultState.Success -> {
                        hideLoader()
                        _newArrivals.update { result.data }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message ?: "")
                    }
                }
            }

            repository.promotedVendors().collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        showLoader()
                    }

                    is ResultState.Success -> {
                        hideLoader()
                        _promotedVendors.update {
                            result.data
                                .filter { f -> f.promoteVendorBanner }
                                .filter { f -> f.companyBanner != null }
                                .toMutableList()
                                .apply {
                                    if (isNotEmpty()) {
                                        repeat(4 - this.size % 4) { index ->
                                            add(this[index])
                                        }
                                    }
                                }
                                .chunked(2)
                                .map { it[0] to it[1] }
                        }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message ?: "")
                    }
                }
            }
        }
    }
}