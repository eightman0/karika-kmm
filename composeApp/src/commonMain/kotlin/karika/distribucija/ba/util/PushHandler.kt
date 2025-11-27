package karika.distribucija.ba.util

import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.domain.model.Order
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.view.distributer.dashboard.DashConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PushHandler {
    fun handleNewPushIfExists(route: String, component: CommonComponent) {
        if (component.stateHolder.sessionHandler.mainConfig() == AppConfig.Dashboard) {
            handleNewPushIfExistsVendor(route, component)
            return
        }

        when {
            route.startsWith("route/orderComments") -> {
                handleOrderCommentPush(route, component)
            }

            route.startsWith("route/messages") -> {
                handleNewMessagePush(route, component)
            }

            route.startsWith("route/orderStatusChange") -> {
                handleOrderStatusChangedPush(route, component)
            }

            else -> {

            }
        }
    }

    fun handleNewPushIfExistsVendor(route: String, component: CommonComponent) {
        when {
            route.startsWith("route/orderComments") -> {
                handleOrderCommentPushVendor(route, component)
            }

            route.startsWith("route/messages") -> {
                handleNewMessagePushVendor(route, component)
            }

            route.startsWith("route/orderStatusChange") -> {
                handleOrderStatusChangedPushVendor(route, component)
            }

            else -> {

            }
        }
    }

    private fun handleOrderCommentPushVendor(route: String, component: CommonComponent) {
        val regex = """orderId=(\d+)&vendorId=(\d+)""".toRegex()
        val matchResult = regex.find(route)

        matchResult?.let {
            val (orderId, vendorId) = it.destructured
            CoroutineScope(Dispatchers.Main).launch {
                //delay(300)
                component.dashNavigate(
                    DashConfig.OrderDetails(
                        VendorOrder(
                            vendorId = vendorId,
                            orderId = orderId
                        )
                    )
                )
            }
        }
    }

    private fun handleNewMessagePushVendor(route: String, component: CommonComponent) {
        val regex = """[?&]([^=]+)=([^&]*)""".toRegex()
        val params = regex.findAll(route)
            .map { it.groupValues[1] to it.groupValues[2] }.toMap()

        if (
            params.containsKey("threadId") &&
            params.containsKey("receiverName") &&
            params.containsKey("vendorId") &&
            params.containsKey("admin") &&
            params.containsKey("subject")
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                //delay(300)
                component.dashNavigate(
                    DashConfig.MessageOverview(
                        Conversation(
                            id = params["threadId"],
                            subject = params["subject"],
                            receiverName = params["receiverName"],
                            admin = params["admin"] == "1"
                        )
                    )
                )
            }
        }
    }

    private fun handleOrderStatusChangedPushVendor(route: String, component: CommonComponent) {
        val regex = """orderId=(\d+)&status=([a-zA-Z]+)""".toRegex()
        val matchResult = regex.find(route)

        matchResult?.let {
            val (orderId, status) = it.destructured
            CoroutineScope(Dispatchers.Main).launch {
                //delay(300)
                component.dashNavigate(
                    DashConfig.OrderDetails(
                        VendorOrder(
                            orderId = orderId
                        )
                    )
                )
            }
        }
    }

    private fun handleOrderCommentPush(route: String, component: CommonComponent) {
        val regex = """orderId=(\d+)&vendorId=(\d+)""".toRegex()
        val matchResult = regex.find(route)

        matchResult?.let {
            val (orderId, vendorId) = it.destructured
            CoroutineScope(Dispatchers.Main).launch {
                //delay(300)
                component.navigateToComments(
                    Order(
                        total = 0.0,
                        qty = 0.0,
                        status = "",
                        vendorName = "",
                        vendorId = vendorId.toIntOrNull(),
                        orderId = orderId
                    )
                )
            }
        }
    }

    private fun handleNewMessagePush(route: String, component: CommonComponent) {
        val regex = """[?&]([^=]+)=([^&]*)""".toRegex()
        val params = regex.findAll(route)
            .map { it.groupValues[1] to it.groupValues[2] }.toMap()

        if (
            params.containsKey("threadId") &&
            params.containsKey("receiverName") &&
            params.containsKey("vendorId") &&
            params.containsKey("admin") &&
            params.containsKey("subject")
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                //delay(300)
                component.navigateToMessagesOverview(
                    Conversation(
                        id = params["threadId"],
                        subject = params["subject"],
                        receiverName = params["receiverName"],
                        admin = params["admin"] == "1"
                    )
                )
            }
        }
    }

    private fun handleOrderStatusChangedPush(route: String, component: CommonComponent) {
        val regex = """orderId=(\d+)&status=([a-zA-Z]+)""".toRegex()
        val matchResult = regex.find(route)

        matchResult?.let {
            val (orderId, status) = it.destructured
            CoroutineScope(Dispatchers.Main).launch {
                component.appNavigate(
                    AppConfig.OrderDetails(
                        OrdersResponse(orderId = orderId)
                    )
                )
            }
        }
    }
}