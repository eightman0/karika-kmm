package karika.distribucija.ba.salesrep.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.Notification
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.launch

/** Where tapping a notification should navigate, resolved from its `route` field - mirrors
 * composeApp's PushHandler.handleNewPushIfExistsVendor() (the vendor/sales-rep branch only; the
 * distributer/customer branch doesn't apply here). Compose re-fetches the full Conversation by
 * threadId before navigating; this module has no "get conversation by id" endpoint of its own,
 * so the Conversation screen's args are built directly from the route's own query params instead
 * (receiverName/subject/vendorId are already present there) - same destination, one less round trip. */
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

/** Mirrors composeApp's SalesNotificationsComponent.kt. */
class NotificationsViewModel : ViewModel() {

    private val repository = SalesRepository()

    private val _notifications = MutableLiveData<List<Notification>>(emptyList())
    val notifications: LiveData<List<Notification>> = _notifications

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _navigateTo = MutableLiveData<NotificationDestination?>(null)
    val navigateTo: LiveData<NotificationDestination?> = _navigateTo

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            repository.notifications().collect { result ->
                if (result is ResultState.Success) {
                    _notifications.value = result.data
                } else if (result is ResultState.Error) {
                    _errorMessage.value = result.message
                }
            }
        }
    }

    fun markAsRead(item: Notification) {
        viewModelScope.launch {
            repository.markNotificationRead(item.id).collect { result ->
                if (result is ResultState.Success) load()
            }
            resolveDestination(item.route)?.let { _navigateTo.value = it }
        }
    }

    fun clearNavigation() {
        _navigateTo.value = null
    }

    private fun resolveDestination(route: String): NotificationDestination? = when {
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
