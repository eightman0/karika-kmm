package karika.distribucija.ba.ui.common.state

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.router.stack.replaceAll
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.ui.common.KarikaHandler
import karika.distribucija.ba.ui.common.state.customer.CartHandler
import karika.distribucija.ba.ui.common.state.customer.CustomerNotificationHandler
import karika.distribucija.ba.ui.common.state.customer.CustomerSpecificHandler
import karika.distribucija.ba.ui.common.state.salesrep.SalesSpecificHandler
import karika.distribucija.ba.ui.common.state.vendor.VendorNotificationHandler
import karika.distribucija.ba.ui.common.state.vendor.VendorSpecificHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class ImagePreviewState(val images: List<Any?>, val startIndex: Int = 0)

class KarikaStateHolder(val handler: KarikaHandler) : NavigationHandler() {
    var sessionHandler = SessionHandler()
    var messageHandler = MessageHandler()
    var loaderHandler = LoaderHandler()
    var commonHandler = CommonHandler()
    var customerSpecificHandler = CustomerSpecificHandler()
    var vendorSpecificHandler = VendorSpecificHandler()
    var salesSpecificHandler = SalesSpecificHandler()
    var vendorNotificationHandler = VendorNotificationHandler()
    var customerNotificationHandler = CustomerNotificationHandler()
    var cartHandler = CartHandler(commonHandler)

    val imagePreview = mutableStateOf<ImagePreviewState?>(null)

    private val _refreshDiscounts = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshDiscounts = _refreshDiscounts.asSharedFlow()

    fun refreshDiscounts() { _refreshDiscounts.tryEmit(Unit) }

    private val _refreshCustomerMessages = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshCustomerMessages = _refreshCustomerMessages.asSharedFlow()

    fun refreshCustomerMessages() { _refreshCustomerMessages.tryEmit(Unit) }

    private val _refreshAdminMessages = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshAdminMessages = _refreshAdminMessages.asSharedFlow()

    fun refreshAdminMessages() { _refreshAdminMessages.tryEmit(Unit) }

    private val _refreshInternalMessages = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshInternalMessages = _refreshInternalMessages.asSharedFlow()

    fun refreshInternalMessages() { _refreshInternalMessages.tryEmit(Unit) }

    /** Emits the threadId of a customer/admin conversation a push just landed a new message in,
     * so an already-open conversation screen for that exact thread can reload live. */
    private val _customerThreadPush = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val customerThreadPush = _customerThreadPush.asSharedFlow()

    private val _adminThreadPush = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val adminThreadPush = _adminThreadPush.asSharedFlow()

    fun logout() {
        sessionHandler = SessionHandler()
        messageHandler = MessageHandler()
        customerSpecificHandler = CustomerSpecificHandler()
        vendorSpecificHandler = VendorSpecificHandler()
        salesSpecificHandler = SalesSpecificHandler()
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

        if (route?.startsWith("route/messages") == true) {
            val params = Regex("""[?&]([^=]+)=([^&]*)""").findAll(route)
                .associate { it.groupValues[1] to it.groupValues[2] }
            val threadId = params["threadId"]
            if (params["admin"] == "1") {
                refreshAdminMessages()
                threadId?.let { _adminThreadPush.tryEmit(it) }
            } else {
                refreshCustomerMessages()
                refreshInternalMessages()
                threadId?.let { _customerThreadPush.tryEmit(it) }
            }
        }
    }
}