package karika.distribucija.ba.ui.view.main.search

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    val searchText = mutableStateOf("")
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _vendors = MutableStateFlow<List<Vendor>>(emptyList())
    val vendors = _vendors.asStateFlow()

    fun search(reset: Boolean = false) {
        if (reset) {
            hasNextPage = true
            currentPage = 1
        }

        if (loader.value || !hasNextPage) {
            return
        }

        iOScope.launch {
            productRepository.searchProductsByCategory(
                searchText = searchText.value,
                pageSize = pageSize,
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

        if (searchText.value.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                vendorRepository.vendors(
                    searchText = searchText.value,
                    currentPage = currentPage,
                    pageSize = 30
                ).collect { result ->
                    when (result) {
                        is ResultState.Loading -> {}
                        is ResultState.Success -> {
                            _vendors.update {
                                if (reset) {
                                    result.data
                                } else {
                                    it.plus(result.data)
                                }
                            }
                        }

                        is ResultState.Error -> {}
                    }
                }
            }
        }
    }
}