package karika.distribucija.ba.ui.common.state.vendor

import androidx.compose.runtime.mutableStateOf
import karika.distribucija.ba.domain.api.ChatRepository
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.ChatUnreadCount
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class VendorNotificationHandler {
    val chatUnreadCount = mutableStateOf(ChatUnreadCount())
    val notificationCount = MutableStateFlow(0)

    fun notificationReceived() {
        reloadChatMessageCount()
        CoroutineScope(Dispatchers.Main).launch {
            DashRepository()
                .notifications()
                .collect {
                    if (it is ResultState.Success) {
                        notificationCount.value = it.data.count { it1 -> it1.isRead == "0" }
                    }
                }
        }
    }

    fun reloadChatMessageCount() {
        CoroutineScope(Dispatchers.Main).launch {
            ChatRepository()
                .getUnreadCount()
                .collect {
                    if (it is ResultState.Success) {
                        chatUnreadCount.value = it.data
                    }
                }
        }
    }
}
