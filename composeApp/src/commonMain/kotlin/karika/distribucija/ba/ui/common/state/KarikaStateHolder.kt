package karika.distribucija.ba.ui.common.state

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.router.stack.replaceAll
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.ui.common.KarikaHandler
import karika.distribucija.ba.ui.common.state.customer.CartHandler
import karika.distribucija.ba.ui.common.state.customer.CustomerNotificationHandler
import karika.distribucija.ba.ui.common.state.customer.CustomerSpecificHandler
import karika.distribucija.ba.ui.common.state.vendor.VendorNotificationHandler
import karika.distribucija.ba.ui.common.state.vendor.VendorSpecificHandler

data class ImagePreviewState(val images: List<Any?>, val startIndex: Int = 0)

class KarikaStateHolder(val handler: KarikaHandler) : NavigationHandler() {
    var sessionHandler = SessionHandler()
    var messageHandler = MessageHandler()
    var loaderHandler = LoaderHandler()
    var commonHandler = CommonHandler()
    var customerSpecificHandler = CustomerSpecificHandler()
    var vendorSpecificHandler = VendorSpecificHandler()
    var vendorNotificationHandler = VendorNotificationHandler()
    var customerNotificationHandler = CustomerNotificationHandler()
    var cartHandler = CartHandler(commonHandler)

    val imagePreview = mutableStateOf<ImagePreviewState?>(null)

    fun logout() {
        sessionHandler = SessionHandler()
        messageHandler = MessageHandler()
        customerSpecificHandler = CustomerSpecificHandler()
        vendorSpecificHandler = VendorSpecificHandler()
        vendorNotificationHandler = VendorNotificationHandler()
        customerNotificationHandler = CustomerNotificationHandler()
        cartHandler = CartHandler(commonHandler)

        imagePreview.value = null

        appNavigation.replaceAll(sessionHandler.mainConfig())
    }

    fun notificationReceived(route: String?) {
        println("Notification received: $route")
        if (sessionHandler.mainConfig() == AppConfig.Main) {
            customerNotificationHandler.notificationReceived()
        } else {
            vendorNotificationHandler.notificationReceived()
        }

        if (route?.contains("admin=1") == true) {
            messageHandler.reloadAdminMessages()
        } else {
            messageHandler.reloadVendorMessages()
        }
        messageHandler.reloadThread()

        customerSpecificHandler.refreshOrders()
    }
}