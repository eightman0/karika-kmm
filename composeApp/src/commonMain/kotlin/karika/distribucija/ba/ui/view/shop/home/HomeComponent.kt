package karika.distribucija.ba.ui.view.shop.home

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.ProductActions
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.shop.MainConfig
import karika.distribucija.ba.util.KarikaConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface HomeComponent : ProductActions {
    /** The "Karika preporučuje" products. */
    val newArrivals: StateFlow<List<Product>>

    /** Vendors promoted with a banner (top carousel) and with a logo (bottom carousel). */
    val promotedVendors: StateFlow<List<PromotedVendor>>
    val promotedLogos: StateFlow<List<PromotedVendor>>

    fun loadData()

    /** "Vidi sve" next to "Karika preporučuje". */
    fun openRecommended()
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
) : CommonComponent(componentContext, stateHolder), HomeComponent {

    private val _newArrivals = MutableStateFlow<List<Product>>(emptyList())
    override val newArrivals = _newArrivals.asStateFlow()

    override fun loadData() {
        loadBanners()
        loadNextPage(true)
    }

    override fun openRecommended() {
        mainNavigate(
            MainConfig.CategoryProducts(
                Category(
                    id = KarikaConfig.getKarikaProductsId(),
                    name = "Karika preporučuje"
                )
            )
        )
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
