package karika.distribucija.ba.ui.view.shop.cart

import androidx.compose.material3.SnackbarHostState
import karika.distribucija.ba.domain.model.CartData
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.testutil.FakeProductActions
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCartComponent(items: Map<Vendor, List<Pair<Product, Int>>> = emptyMap()) :
    FakeProductActions(), CartComponent {
    override val snackbarHostState = SnackbarHostState()
    override val loader = MutableStateFlow(false)
    override val cart = MutableStateFlow(CartData(items = items))

    var shippingRequests = 0
    var clearRequests = 0
    val removed = mutableListOf<Product>()
    val messages = mutableListOf<String?>()

    override fun shippingDetails() {
        shippingRequests++
    }

    override fun clearCart() {
        clearRequests++
    }

    override fun removeFromCart(product: Product) {
        removed += product
    }

    override fun showMessage(message: String?) {
        messages += message
    }
}
