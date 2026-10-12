package karika.distribucija.ba.salesrep.ui.customers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.DiscountRuleBody
import karika.distribucija.ba.salesrep.model.DiscountRuleInput
import karika.distribucija.ba.salesrep.model.OnBehalfProduct
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Mirrors composeApp's SalesDiscountFormComponent.kt, simplified to product-only targeting
 * (search hits vendor-operations/products via SalesApi.getProducts instead of the shop-wide
 * Product/Category search + recursive category tree the original form uses) - category-level
 * discount targeting is a follow-up. Leaving the item search empty still means "svi artikli".
 */
class DiscountFormViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = SalesRepository()

    val customerId: Long = savedStateHandle.get<Long>("customerId") ?: 0L
    private val ruleId: Long = savedStateHandle.get<Long>("ruleId") ?: 0L
    private val existingIsActive: Int = savedStateHandle.get<Int>("isActive") ?: 1

    val isEdit: Boolean = ruleId != 0L

    /** Shown read-only in the "Kupac" field. */
    val customerName: String = savedStateHandle.get<String>("customerName").orEmpty()

    // Pre-filled from the rule being edited, like SalesDiscountFormComponent's initial state
    private val _itemSearch = MutableLiveData(
        savedStateHandle.get<String>("productName")?.takeIf { it.isNotBlank() }
            ?: savedStateHandle.get<String>("categoryName")?.takeIf { it.isNotBlank() }
            ?: ""
    )
    val itemSearch: LiveData<String> = _itemSearch

    private var selectedProductId: Long? = savedStateHandle.get<Long>("productId")?.takeIf { it != 0L }

    /** Only ever set from an edited category rule - the search itself finds products only. Kept
     * so saving an edited category rule doesn't silently turn it into an all-items discount. */
    private var selectedCategoryId: Long? =
        savedStateHandle.get<Long>("categoryId")?.takeIf { it != 0L && selectedProductId == null }

    val hasSelectedItem: Boolean get() = selectedProductId != null || selectedCategoryId != null
    val isCategorySelected: Boolean get() = selectedCategoryId != null

    private val _searchResults = MutableLiveData<List<OnBehalfProduct>>(emptyList())
    val searchResults: LiveData<List<OnBehalfProduct>> = _searchResults

    private val _minQty = MutableLiveData(savedStateHandle.get<Float>("minQty")?.takeIf { it > 0 }?.toInt()?.toString() ?: "")
    val minQty: LiveData<String> = _minQty

    private val _discountPercent = MutableLiveData(savedStateHandle.get<Float>("discountPercent")?.takeIf { it > 0 }?.toInt()?.toString() ?: "")
    val discountPercent: LiveData<String> = _discountPercent

    private val _isSaving = MutableLiveData(false)
    val isSaving: LiveData<Boolean> = _isSaving

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    private var searchJob: Job? = null

    fun setItemSearch(query: String) {
        // EditText restores (and re-emits) its own text on view recreation - not a user edit
        if (query == _itemSearch.value) return
        _itemSearch.value = query
        selectedProductId = null
        selectedCategoryId = null
        searchJob?.cancel()

        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(350)
            repository.getProducts(page = 1, pageSize = 20, search = query).collect { result ->
                if (result is ResultState.Success) {
                    _searchResults.value = result.data.items
                }
            }
        }
    }

    fun selectProduct(product: OnBehalfProduct) {
        selectedProductId = product.entityId
        selectedCategoryId = null
        _itemSearch.value = product.name
        _searchResults.value = emptyList()
    }

    fun clearItem() {
        selectedProductId = null
        selectedCategoryId = null
        _itemSearch.value = ""
        _searchResults.value = emptyList()
    }

    fun setMinQty(value: String) {
        _minQty.value = value.filter { it.isDigit() }
    }

    fun setDiscountPercent(value: String) {
        _discountPercent.value = value.filter { it.isDigit() }
    }

    fun save() {
        val percent = _discountPercent.value?.toFloatOrNull()
        if (percent == null) {
            _errorMessage.value = "Unesite rabat %"
            return
        }

        val body = DiscountRuleBody(
            discountRule = DiscountRuleInput(
                discountPercent = percent,
                isActive = existingIsActive,
                productId = selectedProductId,
                categoryId = selectedCategoryId,
                minQty = _minQty.value?.toFloatOrNull()
            )
        )

        viewModelScope.launch {
            val flow = if (isEdit) {
                repository.updateDiscount(ruleId, body)
            } else {
                repository.createCustomerDiscount(customerId, body)
            }
            flow.collect { result ->
                when (result) {
                    is ResultState.Loading -> _isSaving.value = true
                    is ResultState.Success -> {
                        _isSaving.value = false
                        _saved.value = true
                    }

                    is ResultState.Error -> {
                        _isSaving.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }
}
