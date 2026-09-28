package karika.distribucija.ba.ui.view.shop.product

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.testutil.FakeProductActions
import kotlinx.coroutines.flow.MutableStateFlow

class FakeProductComponent(
    product: Product,
    sameVendor: List<Product> = emptyList(),
    guest: Boolean = false,
) : FakeProductActions(guest), ProductComponent {
    override val snackbarHostState = SnackbarHostState()
    override val loader = MutableStateFlow(false)
    override val product = MutableStateFlow(product)
    override val products = MutableStateFlow(sameVendor)
    override val productQty = mutableStateOf(product.minQty())

    var backRequests = 0
    val messagedAbout = mutableListOf<Product>()
    val imagePreviews = mutableListOf<Int>()

    override fun back() {
        backRequests++
    }

    override fun addToCartWithPut(product: Product, qty: Int, showSnack: Boolean) {
        cartAdds += product.name() to qty
    }

    override fun sendMessageToVendor(product: Product) {
        messagedAbout += product
    }

    override fun showImagesPreview(images: List<Any?>, startIndex: Int) {
        imagePreviews += startIndex
    }
}
