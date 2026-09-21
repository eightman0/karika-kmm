package karika.distribucija.ba.ui.view.salesrep.orders.detail

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import karika.distribucija.ba.domain.HttpClientProvider.imageUrl
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.File
import karika.distribucija.ba.domain.model.OnBehalfOrder
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

class SalesOrderDetailComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    order: OnBehalfOrder
) : CommonComponent(componentContext, stateHolder) {

    private val repository = DashRepository()

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
                    is ResultState.Loading -> showLoader()
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
