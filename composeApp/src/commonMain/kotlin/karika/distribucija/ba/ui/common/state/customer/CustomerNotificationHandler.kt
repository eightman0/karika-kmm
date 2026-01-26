package karika.distribucija.ba.ui.common.state.customer

import karika.distribucija.ba.domain.api.MessagesRepository
import karika.distribucija.ba.domain.api.NotificationRepository
import karika.distribucija.ba.domain.model.MessagesCount
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CustomerNotificationHandler {
    val messageUnreadCount = MutableStateFlow(MessagesCount("0", "0"))
    val notificationCount = MutableStateFlow(0)

    fun notificationReceived() {
        reloadMessageCount()
        CoroutineScope(Dispatchers.Main).launch {
            NotificationRepository()
                .get()
                .collect {
                    if (it is ResultState.Success) {
                        notificationCount.value = it.data.count { it1 -> it1.isRead == "0" }
                    }
                }
        }
    }

    fun reloadMessageCount() {
        CoroutineScope(Dispatchers.Main).launch {
            MessagesRepository()
                .messageUnreadCount()
                .collect {
                    if (it is ResultState.Success) {
                        messageUnreadCount.value = it.data
                    }
                }
        }
    }

}