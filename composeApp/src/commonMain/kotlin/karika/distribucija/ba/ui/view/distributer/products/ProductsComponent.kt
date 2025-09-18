package karika.distribucija.ba.ui.view.distributer.products

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorProduct
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductsComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    private val _products = MutableStateFlow<List<VendorProduct>>(emptyList())
    val products = _products.asStateFlow()
    val searchText = mutableStateOf("")

    override fun loadNextPage(reset: Boolean) {
        if (reset) {
            hasNextPage = true
            currentPage = 1
        }

        if (!hasNextPage || loader.value) {
            return
        }

        iOScope.launch {
            DashRepository().getProducts(
                pageSize = pageSize,
                currentPage = currentPage,
                queryParams = listOf(
                    "&searchCriteria[sortOrders][0][field]=product_id&searchCriteria[sortOrders][0][direction]=DESC",
                    "&searchCriteria[filterGroups][0][filters][0][field]=name" +
                            "&searchCriteria[filterGroups][0][filters][0][value]=${searchText.value}" +
                            "&searchCriteria[filterGroups][0][filters][0][conditionType]=like"
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _products.update {
                            if (reset) {
                                result.data
                            } else {
                                it.plus(result.data)
                            }
                        }
                        currentPage++
                        hasNextPage = result.data.isNotEmpty()
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