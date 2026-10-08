package karika.distribucija.ba.ui.common.state.vendor

import androidx.compose.runtime.mutableStateOf
import karika.distribucija.ba.domain.api.ChatRepository
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.ChatUnreadCount
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class VendorNotificationHandler {
    val chatUnreadCount = mutableStateOf(ChatUnreadCount())
    val notificationCount = MutableStateFlow(0)

    private val _pushReceived = MutableSharedFlow<Unit>(extraBufferCapacity = 2)

    /**
     * Emits on every push, even one that leaves [notificationCount] as it was, so that the open
     * order screens load again.
     */
    val pushReceived: SharedFlow<Unit> = _pushReceived.asSharedFlow()

    fun onPush() {
        _pushReceived.tryEmit(Unit)
    }

    fun notificationReceived() {
        reloadChatMessageCount()
        CoroutineScope(Dispatchers.Main).launch {
            // page_size=1: only total_count is needed for the badge.
            DashRepository()
                .vendorNotifications(isRead = false, pageSize = 1)
                .collect {
                    if (it is ResultState.Success) {
                        notificationCount.value = it.data.totalCount.toInt()
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
