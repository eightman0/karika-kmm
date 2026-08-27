package karika.distribucija.ba.salesrep.notifications

/** Where a notification's `route` field should navigate - shared by the in-app Notifications
 * list (NotificationsViewModel) and by push notification taps (KarikaFcmService/MainActivity),
 * since both receive the exact same route format from the backend. Mirrors composeApp's
 * PushHandler.handleNewPushIfExistsVendor() (the vendor/sales-rep branch only). */
sealed class NotificationDestination {
    data class OrderDetail(val orderId: String) : NotificationDestination()
    data class Conversation(
        val threadId: String,
        val customerName: String,
        val subject: String?,
        val receiverId: Int,
        val admin: Boolean
    ) : NotificationDestination()
}

object PushRouteResolver {
    fun resolve(route: String): NotificationDestination? = when {
        route.startsWith("route/orderComments") ->
            Regex("""orderId=(\d+)&vendorId=(\d+)""").find(route)?.let {
                NotificationDestination.OrderDetail(it.groupValues[1])
            }

        route.startsWith("route/orderStatusChange") ->
            Regex("""orderId=(\d+)&status=([a-zA-Z]+)""").find(route)?.let {
                NotificationDestination.OrderDetail(it.groupValues[1])
            }

        route.startsWith("route/messages") -> {
            val params = Regex("""[?&]([^=]+)=([^&]*)""").findAll(route)
                .associate { it.groupValues[1] to it.groupValues[2] }
            val threadId = params["threadId"]
            val receiverName = params["receiverName"]
            val vendorId = params["vendorId"]
            val admin = params["admin"]
            val subject = params["subject"]
            if (threadId != null && receiverName != null && vendorId != null && admin != null && subject != null) {
                NotificationDestination.Conversation(
                    threadId = threadId,
                    customerName = receiverName,
                    subject = subject,
                    receiverId = vendorId.toIntOrNull() ?: -1,
                    admin = admin == "1"
                )
            } else null
        }

        else -> null
    }
}
