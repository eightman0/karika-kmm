package karika.distribucija.ba.ui.view.shop.product

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.model.EventType
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.RefType
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.ProductActions
import karika.distribucija.ba.ui.common.ScreenComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface ProductComponent : ScreenComponent, ProductActions {
    /** The product, first as it was tapped, then as the details request returns it. */
    val product: StateFlow<Product>

    /** Other products of the same vendor. */
    val products: StateFlow<List<Product>>

    /** The quantity the "Dodaj u Korpu" button adds, in steps of the minimum quantity. */
    val productQty: MutableState<Int>

    fun back()

    fun addToCartWithPut(product: Product, qty: Int, showSnack: Boolean)

    fun sendMessageToVendor(product: Product)

    fun showImagesPreview(images: List<Any?>, startIndex: Int)
}

class DefaultProductComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    product: Product,
    val fromMain: Boolean = true
) : CommonComponent(componentContext, stateHolder), ProductComponent {

    private val repository = ProductRepository()
    override val title: String = product.name()
    private val _product = MutableStateFlow(product)
    override val product = _product.asStateFlow()
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    override val products = _products.asStateFlow()
    override val productQty = mutableStateOf(product.minQty())

    init {
        loadProducts()
    }

    private fun loadProducts() {
        scope.launch {
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
                        _products.update { result.data.filter { it.entityId != product.value.entityId } }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                    }
                }
            }
        }

        scope.launch {
            repository.productById(product.value.entityId ?: "").collect { result ->
                when (result) {
                    is ResultState.Loading -> {

                    }

                    is ResultState.Success -> {
                        _product.update {
                            result.data.firstOrNull() ?: return@collect
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

    override fun back() {
        if (fromMain) {
            mainBack()
        } else {
            appBack()
        }
    }
}