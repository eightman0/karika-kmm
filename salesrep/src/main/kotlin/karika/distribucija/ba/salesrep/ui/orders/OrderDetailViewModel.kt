package karika.distribucija.ba.salesrep.ui.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.Comment
import karika.distribucija.ba.salesrep.model.OnBehalfOrder
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.model.VendorOrder
import kotlinx.coroutines.launch

/** Mirrors composeApp's ui/view/salesrep/orders/detail/SalesOrderDetailComponent.kt. */
class OrderDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = SalesRepository()

    private val seed = OnBehalfOrder(
        orderId = savedStateHandle.get<Long>("orderId") ?: 0L,
        incrementId = savedStateHandle.get<String>("incrementId").orEmpty(),
        customerId = savedStateHandle.get<Long>("customerId") ?: 0L,
        customerName = savedStateHandle.get<String>("customerName"),
        grandTotal = savedStateHandle.get<Float>("grandTotal") ?: 0f,
        status = savedStateHandle.get<String>("status").orEmpty(),
        createdAt = savedStateHandle.get<String>("createdAt")
    )

    private val _vendorOrder = MutableLiveData(seed.toVendorOrderSeed())
    val vendorOrder: LiveData<VendorOrder> = _vendorOrder

    private val _comments = MutableLiveData<List<Comment>>(emptyList())
    val comments: LiveData<List<Comment>> = _comments

    private val _isSendingComment = MutableLiveData(false)
    val isSendingComment: LiveData<Boolean> = _isSendingComment

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _pdfUrl = MutableLiveData<String?>(null)
    val pdfUrl: LiveData<String?> = _pdfUrl

    init {
        loadOrder()
        loadComments()
    }

    private fun loadOrder() {
        viewModelScope.launch {
            repository.getOrder(orderIdParam()).collect { result ->
                when (result) {
                    is ResultState.Success -> _vendorOrder.value = result.data
                    is ResultState.Error -> _errorMessage.value = result.message
                    is ResultState.Loading -> Unit
                }
            }
        }
    }

    fun loadComments() {
        viewModelScope.launch {
            repository.getOrderComments(orderIdParam()).collect { result ->
                when (result) {
                    is ResultState.Success -> _comments.value = result.data
                    is ResultState.Error -> _errorMessage.value = result.message
                    is ResultState.Loading -> Unit
                }
            }
        }
    }

    fun sendComment(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendOrderComment(orderIdParam(), text).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isSendingComment.value = true
                    is ResultState.Success -> {
                        _isSendingComment.value = false
                        loadComments()
                    }

                    is ResultState.Error -> {
                        _isSendingComment.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }

    fun printOrder() {
        viewModelScope.launch {
            repository.getOrderPdfUrl(orderIdParam()).collect { result ->
                when (result) {
                    is ResultState.Success -> _pdfUrl.value = result.data
                    is ResultState.Error -> _errorMessage.value = result.message
                    is ResultState.Loading -> Unit
                }
            }
        }
    }

    fun clearPdfUrl() {
        _pdfUrl.value = null
    }

    private fun orderIdParam(): String = _vendorOrder.value?.orderId.orEmpty()
}

private fun OnBehalfOrder.toVendorOrderSeed(): VendorOrder = VendorOrder(
    orderId = incrementId,
    customerId = customerId.toString(),
    billingName = customerName,
    orderTotal = (grandTotal / 1.17f).toString(),
    realOrderStatus = status,
    createdAt = createdAt
)
