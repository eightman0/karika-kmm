package karika.distribucija.ba.ui.view.main.search

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    val searchText = mutableStateOf("")
    private val repository = ProductRepository()
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    fun search(reset: Boolean = false) {
        if (reset) {
            hasNextPage = true
            currentPage = 1
        }

        if (loader.value || !hasNextPage) {
            return
        }

        iOScope.launch {
            repository.searchProductsByCategory(
                searchText = searchText.value,
                currentPage = currentPage
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
                        hasNextPage = result.data.isNotEmpty()
                        currentPage++
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }
}