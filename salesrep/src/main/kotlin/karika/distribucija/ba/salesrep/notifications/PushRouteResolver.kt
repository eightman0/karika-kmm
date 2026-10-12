package karika.distribucija.ba.salesrep.notifications

/** Where a notification's `route` field should navigate - shared by the in-app Notifications
 * list (NotificationsViewModel) and by push notification taps (KarikaFcmService/MainActivity),
 * since both receive the exact same route format from the backend. Mirrors composeApp's
 * PushHandler.handleNewPushIfExistsSalesRep(). */
sealed class NotificationDestination {
    data class OrderDetail(val orderId: String) : NotificationDestination()

    /** A Chat V2 conversation; which screen opens depends on its axis, so it is looked up first
     * (see openChatConversation()). */
    data class Conversation(val conversationId: Long) : NotificationDestination()
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

        route.startsWith("route/chat") ->
            Regex("""conversationId=(\d+)""").find(route)?.groupValues?.get(1)?.toLongOrNull()?.let {
                NotificationDestination.Conversation(it)
            }

        // Legacy pre-Chat-V2 message notifications (`route/messages?threadId=...`) - the same id
        // space as route/chat's conversationId, so they open the same way.
        route.startsWith("route/messages") ->
            Regex("""threadId=(\d+)""").find(route)?.groupValues?.get(1)?.toLongOrNull()?.let {
                NotificationDestination.Conversation(it)
            }

        else -> null
    }
}
