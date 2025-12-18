package karika.distribucija.ba.ui.common.state.vendor

import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.api.MessagesRepository
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class VendorNotificationHandler {
    val messageUnreadCountAdmin = MutableStateFlow(0)
    val messageUnreadCountUser = MutableStateFlow(0)
    val notificationCount = MutableStateFlow(0)

    fun notificationReceived() {
        CoroutineScope(Dispatchers.Main).launch {
            MessagesRepository()
                .messageUnreadCount()
                .collect {
                    if (it is ResultState.Success) {
                        messageUnreadCountAdmin.value = it.data.admin()
                        messageUnreadCountUser.value = it.data.user()
                    }
                }
            DashRepository()
                .notifications()
                .collect {
                    if (it is ResultState.Success) {
                        notificationCount.value = it.data.count { it1 -> it1.isRead == "0" }
                    }
                }
        }
    }
}