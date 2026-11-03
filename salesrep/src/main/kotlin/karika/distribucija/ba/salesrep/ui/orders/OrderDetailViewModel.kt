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
import karika.distribucija.ba.salesrep.model.ReorderLine
import karika.distribucija.ba.salesrep.model.ReorderOutcome
import karika.distribucija.ba.salesrep.session.CartState
import karika.distribucija.ba.salesrep.session.CurrentUser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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

    /** True once GET /V1/mobile/vendor/order has returned (the nav-arg seed has no lines). */
    private var orderLoaded = false

    private val _isReordering = MutableLiveData(false)
    val isReordering: LiveData<Boolean> = _isReordering

    /** One-shot "Naruči ponovo" result, consumed by the fragment via [clearReorderOutcome]. */
    private val _reorderOutcome = MutableLiveData<ReorderOutcome?>(null)
    val reorderOutcome: LiveData<ReorderOutcome?> = _reorderOutcome

    /** "Naruči ponovo" shows only with can_place_order_for and an order customer_id. */
    fun canReorder(order: VendorOrder): Boolean =
        CurrentUser.me?.capabilities?.canPlaceOrderFor == true && order.reorderCustomerId() != null

    fun reorder() {
        if (_isReordering.value == true) return
        _isReordering.value = true
        viewModelScope.launch {
            val order = if (orderLoaded) _vendorOrder.value else fetchOrder()
            val customerId = order?.reorderCustomerId()
            if (order == null || customerId == null) {
                _isReordering.value = false
                _reorderOutcome.value = ReorderOutcome.Failed(code = null, message = null)
                return@launch
            }
            if (order.products.isEmpty()) {
                _isReordering.value = false
                _reorderOutcome.value = ReorderOutcome.NoLines
                return@launch
            }

            val lines = mutableListOf<ReorderLine>()
            val skipped = mutableListOf<String>()
            order.products.forEach { product ->
                val sku = product.sku?.trim().orEmpty()
                val qty = product.qtyOrdered?.toDoubleOrNull()?.roundToInt() ?: 0
                val name = product.name?.takeIf { it.isNotBlank() } ?: sku
                if (sku.isEmpty() || qty <= 0) skipped += name else lines += ReorderLine(sku, name, qty)
            }

            // Nothing can be added: leave the current cart alone and stay here.
            val outcome = if (lines.isEmpty()) {
                ReorderOutcome.AllSkipped(cart = null, skippedNames = skipped)
            } else {
                repository.reorderIntoCart(customerId, lines, skipped)
            }

            when (outcome) {
                is ReorderOutcome.Added -> outcome.cart?.let { CartState.cart.value = it }
                is ReorderOutcome.AllSkipped -> outcome.cart?.let { CartState.cart.value = it }
                else -> Unit
            }
            _isReordering.value = false
            _reorderOutcome.value = outcome
        }
    }

    fun clearReorderOutcome() {
        _reorderOutcome.value = null
    }

    private suspend fun fetchOrder(): VendorOrder? {
        val result = repository.getOrder(orderIdParam()).first { it !is ResultState.Loading }
        return (result as? ResultState.Success)?.data?.also {
            orderLoaded = true
            _vendorOrder.value = it
        }
    }

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
                        orderLoaded = true
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

private fun VendorOrder.reorderCustomerId(): Long? = customerId?.trim()?.toLongOrNull()?.takeIf { it > 0 }
