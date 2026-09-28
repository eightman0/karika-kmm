package karika.distribucija.ba.ui.view.shop.search

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.testutil.FakeProductActions
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSearchComponent(
    products: List<Product> = emptyList(),
    vendors: List<Vendor> = emptyList(),
) : FakeProductActions(), SearchComponent {
    override val snackbarHostState = SnackbarHostState()
    override val loader = MutableStateFlow(false)
    override val searchText = mutableStateOf("")
    override val products = MutableStateFlow(products)
    override val vendors = MutableStateFlow(vendors)

    /** Every search, as the text searched for and whether it started over. */
    val searches = mutableListOf<Pair<String, Boolean>>()
    var backRequests = 0

    override fun search(reset: Boolean) {
        searches += searchText.value to reset
    }

    override fun mainBack() {
        backRequests++
    }
}
