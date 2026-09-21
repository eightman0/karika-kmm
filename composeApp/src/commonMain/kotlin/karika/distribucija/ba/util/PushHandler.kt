package karika.distribucija.ba.util

import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.OnBehalfOrder
import karika.distribucija.ba.domain.model.Order
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.view.distributer.dashboard.DashConfig
import karika.distribucija.ba.ui.view.salesrep.dashboard.SalesRepConfig
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

            route.startsWith("route/chat") -> {
                handleNewChatMessagePush(route, component)
            }

            route.startsWith("route/orderStatusChange") -> {
                handleOrderStatusChangedPush(route, component)
            }

            // Legacy pre-Chat-V2 message notifications (`route/messages?threadId=...`) - same
            // id space as route/chat's conversationId, see handleLegacyMessagePushVendor().
            route.startsWith("route/messages") -> {
                handleLegacyMessagePush(route, component)
            }

            route.startsWith("route/partnerships") -> {
                CoroutineScope(Dispatchers.Main).launch {
                    component.appNavigate(AppConfig.PartnershipRequests)
                }
            }

            else -> {

            }
        }
    }

    private fun handleLegacyMessagePush(route: String, component: CommonComponent) {
        val threadId = Regex("""threadId=(\d+)""").find(route)
            ?.groupValues?.get(1)?.toLongOrNull() ?: return

        CoroutineScope(Dispatchers.Main).launch {
            component.chatRepository.getConversation(threadId).collect {
                if (it is ResultState.Success) {
                    component.navigateToMessagesOverview(it.data)
                }
            }
        }
    }

    fun handleNewPushIfExistsVendor(route: String, component: CommonComponent) {
        when {
            route.startsWith("route/orderComments") -> {
                handleOrderCommentPushVendor(route, component)
            }

            route.startsWith("route/chat") -> {
                handleNewChatMessagePushVendor(route, component)
            }

            route.startsWith("route/orderStatusChange") -> {
                handleOrderStatusChangedPushVendor(route, component)
            }

            // Legacy pre-Chat-V2 message notifications (`route/messages?threadId=...`).
            // KarikaStateHolder.notificationReceived() already treats this threadId as the
            // same id space as route/chat's conversationId, so resolve and open the exact
            // conversation the same way.
            route.startsWith("route/messages") -> {
                handleLegacyMessagePushVendor(route, component)
            }

            // No dedicated partnership screen exists yet - the customers list is the closest
            // working destination (shows each customer's partnership status).
            route.startsWith("route/partnerships") -> {
                CoroutineScope(Dispatchers.Main).launch {
                    component.dashNavigate(DashConfig.Customers)
                }
            }

            else -> {

            }
        }
    }

    private fun handleLegacyMessagePushVendor(route: String, component: CommonComponent) {
        val threadId = Regex("""threadId=(\d+)""").find(route)
            ?.groupValues?.get(1)?.toLongOrNull() ?: return

        CoroutineScope(Dispatchers.Main).launch {
            component.chatRepository.getConversation(threadId).collect {
                if (it is ResultState.Success) {
                    component.dashNavigate(DashConfig.MessageOverview(it.data))
                }
            }
        }
    }

    /** Mirrors handleNewPushIfExistsVendor(), but navigates the sales rep's own stack
     * (SalesRepConfig) instead of the distributer dashboard's (DashConfig). */
    fun handleNewPushIfExistsSalesRep(route: String, component: CommonComponent) {
        when {
            route.startsWith("route/orderComments") -> {
                handleOrderCommentPushSalesRep(route, component)
            }

            route.startsWith("route/chat") -> {
                handleNewChatMessagePushSalesRep(route, component)
            }

            route.startsWith("route/orderStatusChange") -> {
                handleOrderStatusChangedPushSalesRep(route, component)
            }

            // Legacy pre-Chat-V2 message notifications (`route/messages?threadId=...`).
            // KarikaStateHolder.notificationReceived() already treats this threadId as the
            // same id space as route/chat's conversationId (feeds the same thread-reload
            // flows), so resolve and open the exact conversation the same way.
            route.startsWith("route/messages") -> {
                handleLegacyMessagePushSalesRep(route, component)
            }

            // No dedicated partnership screen exists yet - the customers list is the closest
            // working destination (shows each customer's partnership status).
            route.startsWith("route/partnerships") -> {
                CoroutineScope(Dispatchers.Main).launch {
                    component.salesRepNavigate(SalesRepConfig.Customers)
                }
            }

            else -> {

            }
        }
    }

    private fun handleLegacyMessagePushSalesRep(route: String, component: CommonComponent) {
        val threadId = Regex("""threadId=(\d+)""").find(route)
            ?.groupValues?.get(1)?.toLongOrNull() ?: return
        navigateToChatConversationSalesRep(threadId, component)
    }

    private fun handleOrderCommentPushSalesRep(route: String, component: CommonComponent) {
        val regex = """orderId=(\d+)&vendorId=(\d+)""".toRegex()
        val matchResult = regex.find(route)

        matchResult?.let {
            val (orderId, _) = it.destructured
            CoroutineScope(Dispatchers.Main).launch {
                component.salesRepNavigate(
                    SalesRepConfig.OrderDetail(OnBehalfOrder(incrementId = orderId))
                )
            }
        }
    }

    private fun handleNewChatMessagePushSalesRep(route: String, component: CommonComponent) {
        val conversationId = Regex("""conversationId=(\d+)""").find(route)
            ?.groupValues?.get(1)?.toLongOrNull() ?: return
        navigateToChatConversationSalesRep(conversationId, component)
    }

    private fun navigateToChatConversationSalesRep(conversationId: Long, component: CommonComponent) {
        CoroutineScope(Dispatchers.Main).launch {
            component.chatRepository.getConversation(conversationId).collect {
                if (it is ResultState.Success) {
                    val config = when (it.data.axis) {
                        ChatAxis.VENDOR_ADMIN -> SalesRepConfig.AdminConversation(it.data)
                        ChatAxis.STAFF -> SalesRepConfig.InternalConversation(it.data)
                        else -> SalesRepConfig.CustomerConversation(it.data)
                    }
                    component.salesRepNavigate(config)
                }
            }
        }
    }

    private fun handleOrderStatusChangedPushSalesRep(route: String, component: CommonComponent) {
        val regex = """orderId=(\d+)&status=([a-zA-Z]+)""".toRegex()
        val matchResult = regex.find(route)

        matchResult?.let {
            val (orderId, _) = it.destructured
            CoroutineScope(Dispatchers.Main).launch {
                component.salesRepNavigate(
                    SalesRepConfig.OrderDetail(OnBehalfOrder(incrementId = orderId))
                )
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

    private fun handleNewChatMessagePushVendor(route: String, component: CommonComponent) {
        val conversationId = Regex("""conversationId=(\d+)""").find(route)
            ?.groupValues?.get(1)?.toLongOrNull() ?: return

        CoroutineScope(Dispatchers.Main).launch {
            component.chatRepository.getConversation(conversationId).collect {
                if (it is ResultState.Success) {
                    component.dashNavigate(DashConfig.MessageOverview(it.data))
                }
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

    private fun handleNewChatMessagePush(route: String, component: CommonComponent) {
        val conversationId = Regex("""conversationId=(\d+)""").find(route)
            ?.groupValues?.get(1)?.toLongOrNull() ?: return

        CoroutineScope(Dispatchers.Main).launch {
            component.chatRepository.getConversation(conversationId).collect {
                if (it is ResultState.Success) {
                    component.navigateToMessagesOverview(it.data)
                }
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
                        OrdersResponse(orderId = orderId, incrementId = orderId)
                    )
                )
            }
        }
    }
}