package karika.distribucija.ba.ui.view.shop.menu.categories.products

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.Config
import karika.distribucija.ba.domain.model.KarikaUnit
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.testutil.FakeProductActions
import kotlinx.coroutines.flow.MutableStateFlow

class FakeProductByCategoryComponent(
    category: Category,
    products: List<Product> = emptyList(),
    featured: List<Product> = emptyList(),
    regions: List<KarikaUnit> = emptyList(),
    private val vendorsFound: List<Vendor> = emptyList(),
) : FakeProductActions(), ProductByCategoryComponent {
    override val snackbarHostState = SnackbarHostState()
    override val loader = MutableStateFlow(false)
    override val category = MutableStateFlow(category)
    override val products = MutableStateFlow(products)
    override val featuredProducts = MutableStateFlow(featured)
    override val vendors = MutableStateFlow<List<Vendor>>(emptyList())
    override val config = MutableStateFlow(Config(customerRegionList = regions))
    override val searchText = mutableStateOf("")
    override val filter = mutableStateOf(Pair("", 0))
    override val sortBy = mutableStateOf("Najnoviji")
    override val filterPriceFrom = mutableStateOf("")
    override val filterPriceTo = mutableStateOf("")
    override val selectedVendor = mutableStateOf(Pair("", 0))
    override val selectedRegion = mutableStateOf<List<KarikaUnit>>(emptyList())
    override val isInStock = mutableStateOf("")

    /** A page request with the filters it was made with. */
    data class PageRequest(
        val reset: Boolean,
        val searchText: String,
        val sortBy: String,
        val priceFrom: String,
        val priceTo: String,
        val vendorId: Int,
        val regions: List<String>,
        val withSoldOut: Boolean,
    )

    val pageRequests = mutableListOf<PageRequest>()
    val vendorLookups = mutableListOf<String>()
    var featuredLoads = 0
    var backRequests = 0

    /** The requests that started over, which is what every filter change does. */
    val resets get() = pageRequests.filter { it.reset }

    override fun loadNextPage(reset: Boolean) {
        pageRequests += PageRequest(
            reset = reset,
            searchText = searchText.value,
            sortBy = sortBy.value,
            priceFrom = filterPriceFrom.value,
            priceTo = filterPriceTo.value,
            vendorId = selectedVendor.value.second,
            regions = selectedRegion.value.map { it.label() },
            withSoldOut = isInStock.value == "1",
        )
    }

    override fun vendors(searchText: String) {
        vendorLookups += searchText
        vendors.value = vendorsFound
    }

    override fun clear() {
        vendors.value = emptyList()
    }

    override fun loadFeatureProducts() {
        featuredLoads++
    }

    override fun mainBack() {
        backRequests++
    }
}
