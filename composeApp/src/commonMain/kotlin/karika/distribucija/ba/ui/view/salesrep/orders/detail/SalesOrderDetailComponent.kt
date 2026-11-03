package karika.distribucija.ba.ui.view.salesrep.orders.detail

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import karika.distribucija.ba.domain.HttpClientProvider.imageUrl
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.File
import karika.distribucija.ba.domain.model.OnBehalfOrder
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.ReorderLine
import karika.distribucija.ba.domain.model.ReorderOutcome
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorDeliveryServiceData
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.domain.model.VendorProduct
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.openPdf
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.salesrep.dashboard.SalesRepConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class SalesOrderDetailComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    order: OnBehalfOrder,
    /** Set when the order was opened from the customer screen - "Naruči ponovo" reuses it for review. */
    private val customer: OperationalCustomer? = null
) : CommonComponent(componentContext, stateHolder) {

    private val repository = DashRepository()
    private val salesRepository = SalesRepository()

    private val _vendorOrder = MutableStateFlow(order.toVendorOrderSeed())
    val vendorOrder = _vendorOrder.asStateFlow()

    /** False until getOrder() responds (success or error) - the seed order only carries a
     * handful of fields, so the rest of "Informacije o narudžbi" stays blurred until then. */
    private val _isOrderLoaded = MutableStateFlow(false)
    val isOrderLoaded = _isOrderLoaded.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments = _comments.asStateFlow()

    private val _isSendingComment = MutableStateFlow(false)
    val isSendingComment = _isSendingComment.asStateFlow()

    val editOrderItem = mutableStateOf<VendorProduct?>(null)

    val canCreateDiscountFor: Boolean
        get() = stateHolder.salesSpecificHandler.me.value.capabilities.canCreateDiscountFor

    private val _isReordering = MutableStateFlow(false)
    val isReordering = _isReordering.asStateFlow()

    /** "Naruči ponovo" is shown for every status, when the caller may place orders and the
     * order belongs to a customer. */
    val canReorder: Boolean
        get() = stateHolder.salesSpecificHandler.me.value.capabilities.canPlaceOrderFor &&
            reorderCustomerId() != null

    private fun reorderCustomerId(): Long? =
        vendorOrder.value.customerId?.toLongOrNull()?.takeIf { it > 0 }

    val contactName = mutableStateOf("")
    val contactEmail = mutableStateOf("")
    val contactPhone = mutableStateOf("")
    val contactCity = mutableStateOf("")
    val contactAddress = mutableStateOf("")
    val contactPostal = mutableStateOf("")
    val packageWidth = mutableStateOf("")
    val packageHeight = mutableStateOf("")
    val packageDepth = mutableStateOf("")
    val packageWeight = mutableStateOf("")
    val deliveryNote = mutableStateOf("")
    val selectedCarrierCode = mutableStateOf("")

    init {
        refreshOrder()
        loadComments()
    }

    private fun refreshOrder() {
        scope.launch {
            repository.getOrder(vendorOrder.value.orderId ?: "").collect { result ->
                when (result) {
                    // On the first load the view already shows its own full-screen loader
                    // (isOrderLoaded == false), so the global one would just stack on top.
                    is ResultState.Loading -> if (_isOrderLoaded.value) showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _vendorOrder.value = result.data
                        _isOrderLoaded.value = true

                        val shipping = result.data.shippingDetails
                        contactName.value = shipping?.name ?: ""
                        contactEmail.value = shipping?.email ?: ""
                        contactPhone.value = shipping?.telephone ?: ""
                        contactCity.value = shipping?.city ?: ""
                        contactAddress.value = shipping?.street ?: ""
                        contactPostal.value = shipping?.postcode ?: ""
                        packageWidth.value = shipping?.width ?: ""
                        packageHeight.value = shipping?.height ?: ""
                        packageDepth.value = shipping?.depth ?: ""
                        packageWeight.value = shipping?.weight ?: ""
                        deliveryNote.value = shipping?.note ?: ""
                        selectedCarrierCode.value = shipping?.shippingCompany ?: ""
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        _isOrderLoaded.value = true
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun calculateShipping(width: String, height: String, depth: String, weight: String): Pair<Double?, Double?> {
        val w = width.toDoubleOrNull()
        val h = height.toDoubleOrNull()
        val d = depth.toDoubleOrNull()
        val wt = weight.toDoubleOrNull()
        if (w == null || h == null || d == null || wt == null) {
            showErrorMessage("Unesite sve dimenzije i težinu paketa.")
            return null to null
        }

        val chargeable = maxOf((w * h * d) / 5000, wt)
        val providers = stateHolder.commonHandler.config.value.shippingProveders
        val a2b = providers.find { it.code == "A2B" }?.priceFor(chargeable)
        val express = providers.find { it.code == "EURO_EXPRESS" }?.priceFor(chargeable)
        return a2b to express
    }

    fun saveShippingDetails() {
        val orderId = vendorOrder.value.orderId ?: return
        if (selectedCarrierCode.value.isBlank()) {
            showErrorMessage("Odaberite dostavljača.")
            return
        }
        val data = VendorDeliveryServiceData(
            id = orderId,
            name = contactName.value,
            email = contactEmail.value,
            telephone = contactPhone.value,
            city = contactCity.value,
            street = contactAddress.value,
            postcode = contactPostal.value,
            weight = packageWeight.value,
            width = packageWidth.value,
            height = packageHeight.value,
            depth = packageDepth.value,
            note = deliveryNote.value,
            companyCode = selectedCarrierCode.value
        )
        scope.launch {
            repository.updateDelivery(data).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        showMessage("Podaci o dostavi su sačuvani.")
                        refreshOrder()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun editOrderProduct(newQty: Int, newDiscount: Int) {
        val item = editOrderItem.value ?: return
        scope.launch {
            repository.updateOrder(
                orderId = vendorOrder.value.orderId ?: "",
                items = listOf(Triple(item.itemId ?: "", newQty.toString(), newDiscount.toString()))
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        editOrderItem.value = null
                        showLoader()
                    }

                    is ResultState.Success -> {
                        hideLoader()
                        refreshOrder()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun loadComments() {
        scope.launch {
            repository.getOrderComments(vendorOrder.value.orderId ?: "").collect { result ->
                when (result) {
                    is ResultState.Loading -> Unit
                    is ResultState.Success -> _comments.value = result.data
                    is ResultState.Error -> showErrorMessage(result.message)
                }
            }
        }
    }

    fun sendComment(text: String) {
        if (text.isBlank()) return
        scope.launch {
            repository.sendComment(vendorOrder.value.orderId ?: "", text).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isSendingComment.value = true
                    is ResultState.Success -> {
                        _isSendingComment.value = false
                        loadComments()
                    }

                    is ResultState.Error -> {
                        _isSendingComment.value = false
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    /**
     * "Naruči ponovo": replaces the customer's on-behalf cart with this order's lines
     * (GET cart -> DELETE each line -> POST each line) and opens the cart review.
     * The lines come from the already loaded GET /V1/mobile/vendor/order payload.
     */
    fun reorder() {
        if (_isReordering.value || !canReorder || !isOrderLoaded.value) return
        val customerId = reorderCustomerId() ?: return
        _isReordering.value = true
        scope.launch {
            try {
                val lines = vendorOrder.value.products.map { product ->
                    ReorderLine(
                        sku = product.sku,
                        name = product.name,
                        qty = product.qtyOrdered?.toDoubleOrNull()?.roundToInt() ?: 0
                    )
                }
                when (val outcome = salesRepository.reorderIntoCart(customerId, lines)) {
                    is ReorderOutcome.Failed -> showErrorMessage(outcome.message)
                    is ReorderOutcome.Added -> {
                        // The server cart now holds only the repeated order.
                        stateHolder.salesSpecificHandler.cart.value = outcome.cart
                        val reviewCustomer = resolveCustomer(customerId)
                        salesRepPush(SalesRepConfig.OrderReview(reviewCustomer))
                        if (outcome.skipped.isNotEmpty()) {
                            showMessage(outcome.skipped.joinToString("\n"))
                        }
                    }
                }
            } finally {
                _isReordering.value = false
            }
        }
    }

    private suspend fun resolveCustomer(customerId: Long): OperationalCustomer {
        customer?.takeIf { it.customerId == customerId }?.let { return it }
        val me = stateHolder.salesSpecificHandler.me.value
        salesRepository.findCustomer(
            customerId = customerId,
            employeeId = stateHolder.salesSpecificHandler.employeeId,
            canSeeAllVendorCustomers = me.capabilities.canSeeAllVendorCustomers
        )?.let { return it }
        // Not found in the caller's lists: the cart calls just succeeded, so the partnership is
        // active. Without the address id the review screen won't allow placing the order.
        val order = vendorOrder.value
        return OperationalCustomer(
            customerId = customerId,
            email = order.address?.email,
            company = order.billingName,
            partnershipId = 0,
            partnershipStatus = "active"
        )
    }

    fun goBack() {
        salesRepNavigate(SalesRepConfig.Orders, true)
    }

    /** Opens a comment attachment that isn't an image (e.g. a PDF) - mirrors distributer's
     * OrderDetailsComponent.downloadReceipt(File). */
    fun downloadReceipt(file: File) {
        openPdf(imageUrl(file.url))
    }

    fun printOrder() {
        scope.launch {
            repository.getPdf(
                orderId = vendorOrder.value.orderId ?: ""
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        openPdf(result.data)
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    /** Mirrors distributer's OrderDetailsComponent.createInvoice() - "Printaj predračun". */
    fun printEstimate() {
        scope.launch {
            repository.createInvoice(
                orderId = vendorOrder.value.orderId ?: "",
                bankAccountNumber = ""
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        openPdf(result.data)
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    /** Mirrors distributer's OrderDetailsComponent.estimate() - "Pošalji predračun". A null
     * [file] auto-generates the estimate PDF first, then attaches it to the status change. */
    fun sendEstimate(message: String, file: ByteArray?, filename: String) {
        scope.launch {
            if (file == null) {
                repository.createInvoice(
                    orderId = vendorOrder.value.orderId ?: "",
                    bankAccountNumber = ""
                ).collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            val attachment = withContext(Dispatchers.IO) { downloadFile(result.data) }
                            repository.changeOrderStatus(
                                type = "estimate",
                                orderId = vendorOrder.value.orderId ?: "",
                                message = message,
                                attachment = attachment,
                                filename = filename
                            ).collect { statusResult ->
                                when (statusResult) {
                                    is ResultState.Loading -> showLoader()
                                    is ResultState.Success -> {
                                        refreshOrder()
                                        loadComments()
                                    }

                                    is ResultState.Error -> {
                                        hideLoader()
                                        showMessage(statusResult.message)
                                    }
                                }
                            }
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
            } else {
                repository.changeOrderStatus(
                    type = "estimate",
                    orderId = vendorOrder.value.orderId ?: "",
                    message = message,
                    attachment = file,
                    filename = filename
                ).collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            refreshOrder()
                            loadComments()
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
            }
        }
    }

    private suspend fun downloadFile(url: String): ByteArray {
        return HttpClient().use { client ->
            client.get(url).body()
        }
    }
}

/** Lightweight placeholder shown until the real [VendorOrder] loads from the server. */
private fun OnBehalfOrder.toVendorOrderSeed(): VendorOrder = VendorOrder(
    orderId = incrementId,
    customerId = customerId.toString(),
    billingName = customerName,
    orderTotal = (grandTotal / 1.17f).toString(),
    realOrderStatus = status,
    createdAt = createdAt
)
