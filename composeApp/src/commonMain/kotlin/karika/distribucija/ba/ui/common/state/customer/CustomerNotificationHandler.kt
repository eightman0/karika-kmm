package karika.distribucija.ba.ui.common.state.customer

import karika.distribucija.ba.domain.api.ChatRepository
import karika.distribucija.ba.domain.api.NotificationRepository
import karika.distribucija.ba.domain.model.ChatUnreadCount
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CustomerNotificationHandler {
    val messageUnreadCount = MutableStateFlow(ChatUnreadCount())
    val notificationCount = MutableStateFlow(0)

    fun notificationReceived() {
        reloadMessageCount()
        CoroutineScope(Dispatchers.Main).launch {
            // page_size=1: only total_count is needed for the badge.
            NotificationRepository()
                .vendorNotifications(isRead = false, pageSize = 1)
                .collect {
                    if (it is ResultState.Success) {
                        notificationCount.value = it.data.totalCount.toInt()
                    }
                }
        }
    }

    fun reloadMessageCount() {
        CoroutineScope(Dispatchers.Main).launch {
            ChatRepository()
                .getUnreadCount()
                .collect {
                    if (it is ResultState.Success) {
                        messageUnreadCount.value = it.data
                    }
                }
        }
    }

}
