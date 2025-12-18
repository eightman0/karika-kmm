package karika.distribucija.ba.ui.view.distributer.orders.details

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.HttpClientProvider.imageUrl
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.File
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorDeliveryServiceData
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.domain.model.VendorProduct
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.openPdf
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.components.isPostalCodeValid
import karika.distribucija.ba.util.karikaPriceFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrderDetailsComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    order: VendorOrder
) :
    CommonComponent(componentContext, stateHolder) {

    private val repository = DashRepository()
    private val _order = MutableStateFlow(order)
    val order = _order.asStateFlow()

    val contactName = mutableStateOf(order.shippingDetails?.name ?: "")
    val contactEmail = mutableStateOf(order.shippingDetails?.email ?: "")
    val contactPhone = mutableStateOf(order.shippingDetails?.telephone ?: "")
    val contactCity = mutableStateOf(order.shippingDetails?.city ?: "")
    val contactAddress = mutableStateOf(order.shippingDetails?.street ?: "")
    val contactPostal = mutableStateOf(order.shippingDetails?.postcode ?: "")
    val packageWidth = mutableStateOf(order.shippingDetails?.width ?: "")
    val packageHeight = mutableStateOf(order.shippingDetails?.height ?: "")
    val packageDepth = mutableStateOf(order.shippingDetails?.depth ?: "")
    val packageWeight = mutableStateOf(order.shippingDetails?.weight ?: "")
    val deliveryNotes = mutableStateOf(order.shippingDetails?.note ?: "")
    val a2b = mutableStateOf("-")
    val express = mutableStateOf("-")

    val newComment = mutableStateOf("")
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments = _comments.asStateFlow()
    val editOrderItem = mutableStateOf<VendorProduct?>(null)

    init {
        getOrder()
        getComments()

        scope.launch {
            stateHolder.vendorNotificationHandler.notificationCount
                .collect {
                    getOrder()
                    getComments()
                }
        }
    }

    private fun getOrder() {
        scope.launch {
            repository.getOrder(order.value.orderId ?: "")
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _order.update { result.data }

                            contactName.value = result.data.shippingDetails?.name ?: ""
                            contactEmail.value = result.data.shippingDetails?.email ?: ""
                            contactPhone.value = result.data.shippingDetails?.telephone ?: ""
                            contactCity.value = result.data.shippingDetails?.city ?: ""
                            contactAddress.value = result.data.shippingDetails?.street ?: ""
                            contactPostal.value = result.data.shippingDetails?.postcode ?: ""
                            packageWidth.value = result.data.shippingDetails?.width ?: ""
                            packageHeight.value = result.data.shippingDetails?.height ?: ""
                            packageDepth.value = result.data.shippingDetails?.depth ?: ""
                            packageWeight.value = result.data.shippingDetails?.weight ?: ""
                            deliveryNotes.value = result.data.shippingDetails?.note ?: ""

                            calculateShipping(true)
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }

    private fun getComments() {
        scope.launch {
            repository.getOrderComments(order.value.orderId ?: "").collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _comments.update { result.data }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun sendComment() {
        scope.launch {
            repository.sendComment(
                orderId = order.value.orderId ?: "",
                comment = newComment.value
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        getComments()
                        newComment.value = ""
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun pickFile() {
        stateHolder.handler.pickFile { name, data ->
            scope.launch {
                repository.sendComment(
                    orderId = order.value.orderId ?: return@launch,
                    comment = newComment.value,
                    attachment = data,
                    filename = name
                ).collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            getComments()
                            newComment.value = ""
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

    fun approve(message: String, type: String, withDelivery: Boolean) {
        if (withDelivery) {
            saveShippingOrderDetails(if (type.startsWith("A2B")) "A2B" else "EURO_EXPRESS") {
                approve(message = message, withDelivery = withDelivery)
            }
        } else {
            approve(message = message, withDelivery = withDelivery)
        }

    }

    fun approve(message: String, withDelivery: Boolean) {
        scope.launch {
            repository.changeOrderStatus(
                type = "approve",
                orderId = order.value.orderId ?: "",
                message = message,
                withDelivery = withDelivery
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        showMessage("Narudžba je uspješno odobrena.")
                        getOrder()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun reject(message: String) {
        scope.launch {
            repository.changeOrderStatus(
                type = "reject",
                orderId = order.value.orderId ?: "",
                message = message
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        getOrder()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun estimate(message: String, file: ByteArray?, filename: String) {
        scope.launch {
            repository.changeOrderStatus(
                type = "estimate",
                orderId = order.value.orderId ?: "",
                message = message,
                attachment = file,
                filename = filename
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        getOrder()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun createInvoice() {
        scope.launch {
            repository.createInvoice(
                orderId = order.value.orderId ?: "",
                bankAccountNumber = stateHolder.vendorSpecificHandler.vendorDetails.value.bankAccountNumber
                    ?: ""
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        scope.launch {
                            openPdf(result.data)
                        }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun getBill() {
        scope.launch {
            repository.getPdf(
                orderId = order.value.orderId ?: "",
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

    fun calculateShipping(ignoreValidations: Boolean = false) {
        if (!ignoreValidations) {
            if (contactName.value.isEmpty()) {
                showMessage("Kontakt osoba je obavezno polje!")
                return
            }
            if (contactEmail.value.isEmpty()) {
                showMessage("Email je obavezno polje!")
                return
            }
            if (contactPhone.value.isEmpty()) {
                showMessage("Telefon je obavezno polje!")
                return
            }
            if (contactCity.value.isEmpty()) {
                showMessage("Grad je obavezno polje!")
                return
            }
            if (contactAddress.value.isEmpty()) {
                showMessage("Adresa je obavezno polje!")
                return
            }
            if (contactPostal.value.isEmpty()) {
                showMessage("Poštanski Broj je obavezno polje!")
                return
            }
            if (!contactPostal.value.isPostalCodeValid()) {
                showMessage("Poštanski broj nije u odgovarajućem formatu!")
                return
            }
            if (packageWidth.value.isEmpty()) {
                showMessage("Širina je obavezno polje!")
                return
            }
            if (packageHeight.value.isEmpty()) {
                showMessage("Visina je obavezno polje!")
                return
            }
            if (packageDepth.value.isEmpty()) {
                showMessage("Dubina je obavezno polje!")
                return
            }
            if (packageWeight.value.isEmpty()) {
                showMessage("Težina je obavezno polje!")
                return
            }
        }

        if (packageWidth.value.isEmpty() ||
            packageHeight.value.isEmpty() ||
            packageDepth.value.isEmpty() ||
            packageWeight.value.isEmpty()
        ) {
            return
        }

        a2b.value = karikaPriceFormat(
            stateHolder.commonHandler.getPackageVolume(
                width = packageWidth.value.toDoubleOrNull() ?: 0.0,
                height = packageHeight.value.toDoubleOrNull() ?: 0.0,
                depth = packageDepth.value.toDoubleOrNull() ?: 0.0,
                weight = packageWeight.value.toDoubleOrNull() ?: 0.0
            )
        )
        express.value = karikaPriceFormat(
            stateHolder.commonHandler.getPackageVolume(
                width = packageWidth.value.toDoubleOrNull() ?: 0.0,
                height = packageHeight.value.toDoubleOrNull() ?: 0.0,
                depth = packageDepth.value.toDoubleOrNull() ?: 0.0,
                weight = packageWeight.value.toDoubleOrNull() ?: 0.0
            )
        )
    }

    fun saveShippingOrderDetails(companyCode: String, onSuccess: () -> Unit) {
        scope.launch {
            repository.updateDelivery(
                VendorDeliveryServiceData(
                    order.value.orderId ?: "",
                    contactName.value,
                    contactEmail.value,
                    contactPhone.value,
                    contactCity.value,
                    contactAddress.value,
                    contactPostal.value,
                    packageWeight.value,
                    packageWidth.value,
                    packageHeight.value,
                    packageDepth.value,
                    deliveryNotes.value,
                    companyCode
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        getOrder()
                        onSuccess()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    private fun listenPackageChanges() {
        scope.launch {
            snapshotFlow { packageWidth.value }
                .collect {
                    a2b.value = karikaPriceFormat(
                        stateHolder.commonHandler.getPackageVolume(
                            width = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            height = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            depth = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            weight = packageWidth.value.toDoubleOrNull() ?: 0.0,
                        )
                    )
                    express.value = karikaPriceFormat(
                        stateHolder.commonHandler.getPackageVolume(
                            width = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            height = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            depth = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            weight = packageWidth.value.toDoubleOrNull() ?: 0.0,
                        )
                    )
                }
            snapshotFlow { packageHeight.value }
                .collect {
                    a2b.value = karikaPriceFormat(
                        stateHolder.commonHandler.getPackageVolume(
                            width = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            height = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            depth = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            weight = packageWidth.value.toDoubleOrNull() ?: 0.0,
                        )
                    )
                    express.value = karikaPriceFormat(
                        stateHolder.commonHandler.getPackageVolume(
                            width = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            height = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            depth = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            weight = packageWidth.value.toDoubleOrNull() ?: 0.0,
                        )
                    )
                }
            snapshotFlow { packageDepth.value }
                .collect {
                    a2b.value = karikaPriceFormat(
                        stateHolder.commonHandler.getPackageVolume(
                            width = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            height = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            depth = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            weight = packageWidth.value.toDoubleOrNull() ?: 0.0,
                        )
                    )
                    express.value = karikaPriceFormat(
                        stateHolder.commonHandler.getPackageVolume(
                            width = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            height = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            depth = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            weight = packageWidth.value.toDoubleOrNull() ?: 0.0,
                        )
                    )
                }
            snapshotFlow { packageWeight.value }
                .collect {
                    a2b.value = karikaPriceFormat(
                        stateHolder.commonHandler.getPackageVolume(
                            width = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            height = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            depth = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            weight = packageWidth.value.toDoubleOrNull() ?: 0.0,
                        )
                    )
                    express.value = karikaPriceFormat(
                        stateHolder.commonHandler.getPackageVolume(
                            width = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            height = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            depth = packageWidth.value.toDoubleOrNull() ?: 0.0,
                            weight = packageWidth.value.toDoubleOrNull() ?: 0.0,
                        )
                    )
                }
        }
    }

    fun getDelivery(): Pair<String, String> {
        return Pair(
            a2b.value.removeSuffix("-").takeIf { it.isNotEmpty() }?.let { it + "KM" }
                ?: "Unesite dimenzije u prethodnom koraku",
            express.value.removeSuffix("-").takeIf { it.isNotEmpty() }?.let { it + "KM" }
                ?: "Unesite dimenzije u prethodnom koraku"
        )
    }

    fun editOrderProduct(discount: String, discountAll: Boolean, newQty: String) {
        scope.launch {
            repository.updateOrder(
                orderId = order.value.orderId ?: "",
                mutableListOf(
                    Triple(
                        editOrderItem.value?.itemId ?: "",
                        newQty,
                        discount
                    )
                ).apply {
                    if (discountAll) {
                        order.value.products.filter { it.itemId != editOrderItem.value?.itemId }
                            .forEach {
                                add(
                                    Triple(
                                        it.itemId ?: "",
                                        it.qtyOrdered ?: "",
                                        discount
                                    )
                                )
                            }
                    }
                }
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        editOrderItem.value = null
                        showLoader()
                    }

                    is ResultState.Success -> {
                        hideLoader()
                        getOrder()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                    }
                }
            }
        }
    }

    fun downloadReceipt(it: File) {
        openPdf(imageUrl(it.url))
    }
}