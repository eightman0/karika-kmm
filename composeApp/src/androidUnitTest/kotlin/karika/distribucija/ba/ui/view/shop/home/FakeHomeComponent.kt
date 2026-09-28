package karika.distribucija.ba.ui.view.shop.home

import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.domain.model.Vendor
import kotlinx.coroutines.flow.MutableStateFlow

class FakeHomeComponent(
    products: List<Product> = emptyList(),
    logos: List<PromotedVendor> = emptyList(),
    private val guest: Boolean = false,
) : HomeComponent {
    override val newArrivals = MutableStateFlow(products)
    override val promotedVendors = MutableStateFlow<List<PromotedVendor>>(emptyList())
    override val promotedLogos = MutableStateFlow(logos)

    var loads = 0
    var recommendedRequests = 0
    val openedProducts = mutableListOf<Product>()
    val openedVendors = mutableListOf<Vendor>()

    /** Every add-to-cart, as product name to quantity. */
    val cartAdds = mutableListOf<Pair<String, Int>>()

    override fun loadData() {
        loads++
    }

    override fun openRecommended() {
        recommendedRequests++
    }

    override fun navigateToProduct(product: Product) {
        openedProducts += product
    }

    override fun addToCart(product: Product, qty: Int, showSnack: Boolean) {
        cartAdds += product.name() to qty
    }

    override fun showVendor(vendor: Vendor) {
        openedVendors += vendor
    }

    override fun getUnit(unit: String) = "kom"

    override fun isGuest() = guest
}
