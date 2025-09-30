package karika.distribucija.ba.ui.common.state

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.router.stack.replaceAll
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.ui.common.KarikaFilePicker
import karika.distribucija.ba.ui.common.state.customer.CartHandler
import karika.distribucija.ba.ui.common.state.customer.CustomerNotificationHandler
import karika.distribucija.ba.ui.common.state.customer.CustomerSpecificHandler
import karika.distribucija.ba.ui.common.state.vendor.VendorNotificationHandler
import karika.distribucija.ba.ui.common.state.vendor.VendorSpecificHandler

class KarikaStateHolder(val filePicker: KarikaFilePicker) : NavigationHandler() {
    var cartHandler = CartHandler()
    var sessionHandler = SessionHandler()
    var messageHandler = MessageHandler()
    var loaderHandler = LoaderHandler()
    var commonHandler = CommonHandler()
    var customerSpecificHandler = CustomerSpecificHandler()
    var vendorSpecificHandler = VendorSpecificHandler()
    var vendorNotificationHandler = VendorNotificationHandler()
    var customerNotificationHandler = CustomerNotificationHandler()

    val imagePreview = mutableStateOf("")

    fun logout() {
        cartHandler = CartHandler()
        sessionHandler = SessionHandler()
        messageHandler = MessageHandler()
        loaderHandler = LoaderHandler()
        commonHandler = CommonHandler()
        customerSpecificHandler = CustomerSpecificHandler()
        vendorSpecificHandler = VendorSpecificHandler()
        vendorNotificationHandler = VendorNotificationHandler()
        customerNotificationHandler = CustomerNotificationHandler()

        imagePreview.value = ""

        appNavigation.replaceAll(AppConfig.PreLogin)
    }
}