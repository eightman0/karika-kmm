package karika.distribucija.ba.salesrep.ui.customers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.DiscountRule
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesCustomerDetailComponent.kt (discounts list for one customer). */
class CustomerDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = SalesRepository()
    val customerId: Long = savedStateHandle.get<Long>("customerId") ?: 0L

    private val _discounts = MutableLiveData<List<DiscountRule>>(emptyList())
    val discounts: LiveData<List<DiscountRule>> = _discounts

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadDiscounts()
    }

    fun loadDiscounts() {
        viewModelScope.launch {
            repository.getCustomerDiscounts(customerId).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isLoading.value = true
                    is ResultState.Success -> {
                        _isLoading.value = false
                        _discounts.value = result.data.items
                    }

                    is ResultState.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }

    fun deleteDiscount(rule: DiscountRule) {
        val ruleId = rule.ruleId ?: return
        viewModelScope.launch {
            repository.deleteDiscount(ruleId).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isLoading.value = true
                    is ResultState.Success -> loadDiscounts()
                    is ResultState.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }
}
