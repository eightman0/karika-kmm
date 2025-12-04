package karika.distribucija.ba.ui.view.main.product

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.model.EventType
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.RefType
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    product: Product
) : CommonComponent(componentContext, stateHolder) {

    private val repository = ProductRepository()
    override val title: String = product.name()
    private val _product = MutableStateFlow(product)
    val product = _product.asStateFlow()
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()
    val productQty = mutableStateOf(product.minQty())

    init {
        loadProducts()
    }

    private fun loadProducts() {
        iOScope.launch {
            repository.searchProductsByCategory(
                currentPage = 1,
                vendorId = product.value.vendorId().toIntOrNull()
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        showLoader()
                    }

                    is ResultState.Success -> {
                        hideLoader()
                        _products.update { result.data }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                    }
                }
            }
        }

        iOScope.launch {
            repository.productById(product.value.id.toString()).collect { result ->
                when (result) {
                    is ResultState.Loading -> {

                    }

                    is ResultState.Success -> {
                        _product.update {
                            result.data.items.find { it.sku == product.value.sku } ?: product.value
                        }
                        logEvent(
                            eventType = EventType.PAGE_OPEN,
                            refType = RefType.PRODUCT,
                            product = product.value,
                        )
                    }

                    is ResultState.Error -> {

                    }
                }
            }
        }
    }
}