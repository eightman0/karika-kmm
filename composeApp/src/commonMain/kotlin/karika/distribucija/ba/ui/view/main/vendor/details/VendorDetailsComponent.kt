package karika.distribucija.ba.ui.view.main.vendor.details

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VendorDetailsComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    vendor: Vendor,
    val fromMain: Boolean = true
) : CommonComponent(componentContext, stateHolder) {

    private val _vendor = MutableStateFlow(vendor)
    val vendor = _vendor.asStateFlow()
    private val _vendorCategories = MutableStateFlow<List<Category>>(emptyList())
    val vendorCategories = _vendorCategories.asStateFlow()
    private val _products = MutableStateFlow<Set<Product>>(emptySet())
    val products = _products.asStateFlow()
    val searchText = mutableStateOf("")
    val selectedCategories = mutableStateOf<List<Category>>(emptyList())
    val isInStock = mutableStateOf("")

    init {
        getVendor()
        _vendorCategories.update {
            stateHolder.commonHandler.categories.value.plus(stateHolder.commonHandler.categories.value.flatMap { it.childrenData.flatMap { it.childrenData.flatMap { it.childrenData } } })
                .filter {
                    vendor.categories?.contains(it.id.toString()) ?: false
                }
        }
    }

    override fun loadNextPage(reset: Boolean) {
        if (reset) {
            hasNextPage = true
            currentPage = 1
        }

        if (!hasNextPage || loader.value) {
            return
        }

        scope.launch {
            productRepository.searchProductsByCategory(
                currentPage = currentPage,
                pageSize = pageSize,
                vendorId = vendor.value.entityId,
                categoryId = selectedCategories.value.joinToString(",") { it.id.toString() }.ifEmpty { null },
                searchText = searchText.value,
                isInStock = isInStock.value
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        showLoader()
                    }

                    is ResultState.Success -> {
                        hideLoader()
                        _products.update {
                            if (reset) {
                                result.data.toSet()
                            } else {
                                it.plus(result.data)
                            }
                        }
                        hasNextPage = result.data.size == pageSize
                        currentPage++
                    }

                    is ResultState.Error -> {
                        hideLoader()
                    }
                }
            }
        }
    }

    private fun getVendor() {
        scope.launch {
            vendorRepository.vendors(
                currentPage = currentPage,
                pageSize = 1,
                searchText = vendor.value.publicName ?: ""
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        //showLoader()
                    }

                    is ResultState.Success -> {
                        // hideLoader()
                        result.data.firstOrNull()?.let { v ->
                            _vendor.update { v }
                        }
                    }

                    is ResultState.Error -> {
                        //hideLoader()
                        // showMessage(result.message ?: "")
                    }
                }
            }
        }
    }
}