package karika.distribucija.ba.ui.view.shop.menu.categories.products

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.Config
import karika.distribucija.ba.domain.model.Filters
import karika.distribucija.ba.domain.model.KarikaUnit
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.ProductActions
import karika.distribucija.ba.ui.common.ScreenComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface ProductByCategoryComponent : ScreenComponent, ProductActions {
    val category: StateFlow<Category>
    val products: StateFlow<List<Product>>

    /** The category's sponsored products ("ISTAKNUTI ARTIKLI"). */
    val featuredProducts: StateFlow<List<Product>>

    /** Vendors matching the name typed in the filter sheet. */
    val vendors: StateFlow<List<Vendor>>

    /** Holds the region list the filter sheet offers. */
    val config: StateFlow<Config>

    val searchText: MutableState<String>
    val filter: MutableState<Pair<String, Int>>

    /** One of the sort options, "Najnoviji" by default. */
    val sortBy: MutableState<String>
    val filterPriceFrom: MutableState<String>
    val filterPriceTo: MutableState<String>

    /** The vendor filter as name to id; id 0 is no vendor filter. */
    val selectedVendor: MutableState<Pair<String, Int>>
    val selectedRegion: MutableState<List<KarikaUnit>>

    /** "1" when sold out products are included ("Prikaži rasprodate"). */
    val isInStock: MutableState<String>

    /** Loads the next page with the current filters; [reset] starts over from the first. */
    fun loadNextPage(reset: Boolean)

    /** Looks vendors up by name for the filter sheet, from three letters on. */
    fun vendors(searchText: String)

    /** Drops the vendors found for the filter sheet. */
    fun clear()

    fun loadFeatureProducts()

    fun mainBack()
}

class DefaultProductByCategoryComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    category: Category
) :
    CommonComponent(componentContext, stateHolder), ProductByCategoryComponent {

    private val repository = ProductRepository()
    private val _category = MutableStateFlow(category)
    override val category = _category.asStateFlow()
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    override val products = _products.asStateFlow()
    private val _featuredProducts = MutableStateFlow<List<Product>>(emptyList())
    override val featuredProducts = _featuredProducts.asStateFlow()
    override val config: StateFlow<Config>
        get() = stateHolder.commonHandler.config
    override val searchText = mutableStateOf("")
    override val filter = mutableStateOf(Pair("", 0))
    override val sortBy = mutableStateOf("Najnoviji")

    private val _vendors = MutableStateFlow<List<Vendor>>(emptyList())
    override val vendors = _vendors.asStateFlow()
    override val filterPriceFrom = mutableStateOf("")
    override val filterPriceTo = mutableStateOf("")
    override val selectedVendor = mutableStateOf(Pair("", 0))
    override val selectedRegion = mutableStateOf<List<KarikaUnit>>(listOf())
    override val isInStock = mutableStateOf("")

    override fun loadNextPage(reset: Boolean) {
        if (reset) {
            hasNextPage = true
            currentPage = 1
        }
        if (!hasNextPage || loader.value) {
            return
        }

        scope.launch {
            repository.searchProductsByCategory(
                vendorId = if (selectedVendor.value.second == 0) null else selectedVendor.value.second,
                searchText = searchText.value,
                categoryId = category.value.getAllCategoryIds(),
                regionId = selectedRegion.value.joinToString(separator = ",") { it.unit() },
                currentPage = currentPage,
                from = filterPriceFrom.value,
                to = filterPriceTo.value,
                sortType = sortBy.value.sortType(),
                sortBy = sortBy.value.sortBy(),
                isInStock = isInStock.value
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _products.update {
                            if (reset) {
                                result.data
                            } else {
                                it.plus(result.data)
                            }
                        }
                        hasNextPage = result.data.size == pageSize
                        currentPage++
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
                logProductFilterEvent(
                    filters = Filters(
                        priceMin = filterPriceFrom.value,
                        priceMax = filterPriceTo.value,
                        regions = selectedRegion.value.joinToString(separator = ",") { it.unit() },
                        vendors = "${if (selectedVendor.value.second == 0) null else selectedVendor.value.second}"
                    ),
                    sort = sortBy.value.sortType(),
                    categoryIds = "${category.value.getAllCategoryIds()}"
                )
            }
        }
    }

    override fun vendors(searchText: String) {
        if (searchText.length < 3) {
            return
        }
        scope.launch {
            messagesRepository.vendors(
                searchText
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _vendors.update { result.data }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    override fun clear() {
        _vendors.update { emptyList() }
    }

    override fun loadFeatureProducts() {
        scope.launch {
            repository.loadFeaturedProducts(
                categoryId = category.value.getAllCategoryIds() ?: return@launch
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> {}
                    is ResultState.Success -> {
                        _featuredProducts.update { result.data }
                    }
                    is ResultState.Error -> {}
                }
            }
        }
    }
}

private fun String.sortBy(): String {
    return when (this) {
        "Najnoviji" -> "created_at"
        "Najstariji" -> "created_at"
        "Najjeftiniji" -> "price"
        "Najskuplji" -> "price"
        "Min. Količina" -> "b2b_min_qty"
        "Po datumu" -> "created_at"
        "Sa popustom" -> "best_special_price"
        else -> ""
    }
}

private fun String.sortType(): String {
    return when (this) {
        "Najnoviji" -> "DESC"
        "Najstariji" -> "ASC"
        "Najjeftiniji" -> "ASC"
        "Najskuplji" -> "DESC"
        "Min. Količina" -> "ASC"
        "Po datumu" -> "DESC"
        else -> ""
    }
}

/*
private fun Int.toDate(): String {
    return when (this) {
        0 -> ""
        else -> Clock.System.now().toString()
    }
}

private fun Int.fromDate(): String {
    return when (this) {
        0 -> ""
        1 -> Clock.System.now().toString()
        2 -> Clock.System.now().minusDays(7).toString()
        3 -> Clock.System.now().minusDays(15).toString()
        else -> Clock.System.now().minusDays(30).toString()
    }
}
 */