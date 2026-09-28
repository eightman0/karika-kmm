package karika.distribucija.ba.testutil

import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.ProductActions

/** Records what the product and vendor cards on a screen asked for. */
open class FakeProductActions(private val guest: Boolean = false) : ProductActions {
    val openedProducts = mutableListOf<Product>()
    val openedVendors = mutableListOf<Vendor>()

    /** Every add-to-cart, as product name to quantity. */
    val cartAdds = mutableListOf<Pair<String, Int>>()
    val cartUpdates = mutableListOf<Pair<String, Int>>()

    override fun navigateToProduct(product: Product) {
        openedProducts += product
    }

    override fun addToCart(product: Product, qty: Int, showSnack: Boolean) {
        cartAdds += product.name() to qty
    }

    override fun updateCart(
        product: Product,
        qty: Int,
        onSuccess: () -> Unit,
        errorCallback: () -> Unit
    ) {
        cartUpdates += product.name() to qty
    }

    override fun showVendor(vendor: Vendor) {
        openedVendors += vendor
    }

    override fun getUnit(unit: String) = "kom"

    override fun isGuest() = guest
}
