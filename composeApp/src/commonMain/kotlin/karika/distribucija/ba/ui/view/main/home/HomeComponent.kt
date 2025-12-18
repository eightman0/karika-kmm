package karika.distribucija.ba.ui.view.main.home

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.util.KarikaConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
) : CommonComponent(componentContext, stateHolder) {

    private val _newArrivals = MutableStateFlow<List<Product>>(emptyList())
    val newArrivals = _newArrivals.asStateFlow()

    fun loadData() {
        loadBanners()
        loadNextPage(true)
    }

    override fun loadNextPage(reset: Boolean) {
        scope.launch {
            productRepository.searchProductsByCategory(
                categoryId = "${KarikaConfig.getKarikaProductsId()}",
                currentPage = 1,
                pageSize = 12
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

        }
    }
}