package karika.distribucija.ba.ui.view.main.menu.categories.products

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.KarikaUnit
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductByCategoryComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    category: Category
) :
    CommonComponent(componentContext, stateHolder) {

    private val repository = ProductRepository()
    private val _category = MutableStateFlow(category)
    val category = _category.asStateFlow()
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()
    val searchText = mutableStateOf("")
    val filter = mutableStateOf(Pair("", 0))
    val sortBy = mutableStateOf("Po datumu")

    private val _vendors = MutableStateFlow<List<Vendor>>(emptyList())
    val vendors = _vendors.asStateFlow()
    var filterPriceFrom = mutableStateOf("")
    var filterPriceTo = mutableStateOf("")
    var selectedVendor = mutableStateOf(Pair("", 0))
    val selectedRegion = mutableStateOf<List<KarikaUnit>>(listOf())

    override fun loadNextPage(reset: Boolean) {
        if (reset) {
            hasNextPage = true
            currentPage = 1
        }
        if (!hasNextPage || loader.value) {
            return
        }

        iOScope.launch {
            repository.searchProductsByCategory(
                vendorId = if (selectedVendor.value.second == 0) null else selectedVendor.value.second,
                searchText = searchText.value,
                categoryId = category.value.getAllCategoryIds(),
                regionId = selectedRegion.value.joinToString(separator = ",") { it.unit() },
                currentPage = currentPage,
                from = filterPriceFrom.value,
                to = filterPriceTo.value,
                sortType = sortBy.value.sortType(),
                sortBy = sortBy.value.sortBy()
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
                        hasNextPage = result.data.isNotEmpty()
                        currentPage++
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun vendors(searchText: String) {
        if (searchText.length < 3) {
            return
        }
        iOScope.launch {
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

    fun clear() {
        _vendors.update { emptyList() }
    }
}

private fun String.sortBy(): String {
    return when (this) {
        "Najjeftiniji" -> "price"
        "Najskuplji" -> "price"
        "Minimalna količina" -> "b2b_min_qty"
        "Po datumu" -> "created_at"
        else -> ""
    }
}

private fun String.sortType(): String {
    return when (this) {
        "Najjeftiniji" -> "ASC"
        "Najskuplji" -> "DESC"
        "Minimalna količina" -> "ASC"
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