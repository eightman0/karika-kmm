package karika.distribucija.ba.ui.view.main.vendor.details

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.api.VendorRepository
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VendorDetailsComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    vendor: Vendor,
) : CommonComponent(componentContext, stateHolder) {

    private val vendorRepository = VendorRepository()
    private val productRepository = ProductRepository()
    private val _vendor = MutableStateFlow(vendor)
    val vendor = _vendor.asStateFlow()
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()
    val searchText = mutableStateOf("")

    init {
        getVendor()
    }

    override fun loadNextPage(reset: Boolean) {
        if (reset) {
            hasNextPage = true
            currentPage = 1
        }

        if (!hasNextPage || loader.value) {
            return
        }

        iOScope.launch {
            productRepository.searchProductsByCategory(
                currentPage = currentPage,
                pageSize = 10,
                vendorId = vendor.value.entityId,
                searchText = searchText.value
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        showLoader()
                    }

                    is ResultState.Success -> {
                        hideLoader()
                        _products.update {
                            if (reset) {
                                emptyList()
                            } else {
                                it.plus(result.data)
                            }
                        }
                        hasNextPage = result.data.isNotEmpty()
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
        iOScope.launch {
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