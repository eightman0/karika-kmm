package karika.distribucija.ba.salesrep.ui.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.Comment
import karika.distribucija.ba.salesrep.model.Config
import karika.distribucija.ba.salesrep.model.OnBehalfOrder
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.model.VendorDeliveryServiceData
import karika.distribucija.ba.salesrep.model.VendorOrder
import karika.distribucija.ba.salesrep.model.VendorProduct
import karika.distribucija.ba.salesrep.session.CurrentUser
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

    private val _infoMessage = MutableLiveData<String?>(null)
    val infoMessage: LiveData<String?> = _infoMessage

    /** Mirrors composeApp's showLoader()/hideLoader() around loadOrder/editOrderProduct/
     * saveShippingDetails/printOrder - a blocking full-screen spinner for these mutations, since
     * (unlike sendComment's inline button spinner) there's no persistent button to attach one to
     * once the edit-item bottom sheet has already dismissed. */
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val canCreateDiscountFor: Boolean
        get() = CurrentUser.me?.capabilities?.canCreateDiscountFor ?: false

    private var deliveryConfig: Config? = null

    init {
        loadOrder()
        loadComments()
        viewModelScope.launch {
            repository.config().collect { result ->
                if (result is ResultState.Success) deliveryConfig = result.data
            }
        }
    }

    private fun loadOrder() {
        viewModelScope.launch {
            repository.getOrder(orderIdParam()).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isLoading.value = true
                    is ResultState.Success -> {
                        _isLoading.value = false
                        _vendorOrder.value = result.data
                    }

                    is ResultState.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
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

    /** Chargeable weight is the greater of volumetric weight (w*h*d/5000) and actual weight,
     * matching composeApp's SalesOrderDetailComponent.calculateShipping(). */
    fun calculateShipping(width: String, height: String, depth: String, weight: String): Pair<Double?, Double?> {
        val w = width.toDoubleOrNull()
        val h = height.toDoubleOrNull()
        val d = depth.toDoubleOrNull()
        val wt = weight.toDoubleOrNull()
        if (w == null || h == null || d == null || wt == null) {
            _errorMessage.value = "Unesite sve dimenzije i težinu paketa."
            return null to null
        }

        val chargeable = maxOf((w * h * d) / 5000, wt)
        val providers = deliveryConfig?.shippingProveders.orEmpty()
        val a2b = providers.find { it.code == "A2B" }?.priceFor(chargeable)
        val express = providers.find { it.code == "EURO_EXPRESS" }?.priceFor(chargeable)
        return a2b to express
    }

    fun saveShippingDetails(data: VendorDeliveryServiceData) {
        if (data.companyCode.isBlank()) {
            _errorMessage.value = "Odaberite dostavljača."
            return
        }
        viewModelScope.launch {
            repository.updateDelivery(data.copy(id = orderIdParam())).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isLoading.value = true
                    is ResultState.Success -> {
                        _isLoading.value = false
                        _infoMessage.value = "Podaci o dostavi su sačuvani."
                        loadOrder()
                    }

                    is ResultState.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }

    fun editOrderProduct(item: VendorProduct, newQty: Int, newDiscount: Int) {
        viewModelScope.launch {
            repository.updateOrder(
                orderId = orderIdParam(),
                items = listOf(Triple(item.itemId ?: "", newQty.toString(), newDiscount.toString()))
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isLoading.value = true
                    is ResultState.Success -> loadOrder()
                    is ResultState.Error -> {
                        _isLoading.value = false
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
                    is ResultState.Loading -> _isLoading.value = true
                    is ResultState.Success -> {
                        _isLoading.value = false
                        _pdfUrl.value = result.data
                    }

                    is ResultState.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
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
