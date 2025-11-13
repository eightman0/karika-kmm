package karika.distribucija.ba.ui.view.main.profile.notifications

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.NotificationRepository
import karika.distribucija.ba.domain.model.Notification
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.util.PushHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    private val notificationRepository = NotificationRepository()
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    init {
        get()
    }

    fun get() {
        iOScope.launch {
            notificationRepository.get()
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _notifications.update { result.data }
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }

    fun markAsRead(item: Notification) {
       //if (item.isRead == "0") {
       //    iOScope.launch {
       //        notificationRepository.put(item.id)
       //            .collect { result ->
       //                when (result) {
       //                    is ResultState.Loading -> showLoader()
       //                    is ResultState.Success -> {
       //                        hideLoader()
       //                        get()
       //                    }

       //                    is ResultState.Error -> {
       //                        hideLoader()
       //                    }
       //                }
       //            }
       //        stateHolder.customerNotificationHandler.notificationReceived()
       //    }
       //}
        PushHandler.handleNewPushIfExists(item.route, this)
    }
}