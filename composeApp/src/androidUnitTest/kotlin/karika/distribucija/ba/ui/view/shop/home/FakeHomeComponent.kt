package karika.distribucija.ba.ui.view.shop.home

import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.testutil.FakeProductActions
import kotlinx.coroutines.flow.MutableStateFlow

class FakeHomeComponent(
    products: List<Product> = emptyList(),
    logos: List<PromotedVendor> = emptyList(),
    guest: Boolean = false,
) : FakeProductActions(guest), HomeComponent {
    override val newArrivals = MutableStateFlow(products)
    override val promotedVendors = MutableStateFlow<List<PromotedVendor>>(emptyList())
    override val promotedLogos = MutableStateFlow(logos)

    var loads = 0
    var recommendedRequests = 0

    override fun loadData() {
        loads++
    }

    override fun openRecommended() {
        recommendedRequests++
    }
}
