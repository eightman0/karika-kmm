package karika.distribucija.ba.ui.view.distributer.orders.details

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.HttpClientProvider.imageUrl
import karika.distribucija.ba.domain.HttpClientProvider.orderPdf
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.File
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorDeliveryServiceData
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.domain.model.VendorProduct
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.common.openPdf
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
        //listenPackageChanges()
    }

    private fun getOrder() {
        iOScope.launch {
            repository.getOrder(order.value.orderId ?: "")
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _order.update { result.data }
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
        iOScope.launch {
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
        iOScope.launch {
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
        stateHolder.handler.pickFile(arrayOf("application/pdf")) { name, data ->
            iOScope.launch {
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
        iOScope.launch {
            repository.changeOrderStatus(
                type = "approve",
                orderId = order.value.orderId ?: "",
                message = message,
                withDelivery = withDelivery
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

    fun reject(message: String) {
        iOScope.launch {
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
        iOScope.launch {
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
        iOScope.launch {
            repository.createInvoice(
                orderId = order.value.orderId ?: "",
                bankAccountNumber = stateHolder.vendorSpecificHandler.vendorDetails.value.bankAccountNumber ?: ""
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

    fun getBill() {
        openPdf(url = orderPdf("${order.value.orderId}.pdf"))
        // iOScope.launch {
        //     repository.getPdf(
        //         orderId = order.value.orderId ?: "",
        //     ).collect { result ->
        //         when (result) {
        //             is ResultState.Loading -> showLoader()
        //             is ResultState.Success -> {
        //
        //             }
//
        //             is ResultState.Error -> {
        //                 hideLoader()
        //                 showMessage(result.message)
        //             }
        //         }
        //     }
        // }
    }

    fun calculateShipping() {
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

        iOScope.launch {
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
                    deliveryNotes.value
                )
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

    private fun listenPackageChanges() {
        iOScope.launch {
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
        iOScope.launch {
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