package karika.distribucija.ba.ui.view.shop.profile.notifications

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.NotificationRepository
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorNotification
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.components.NotificationReadFilter
import karika.distribucija.ba.util.PushHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** `/V1/vendor-operations/notifications` (Chat V2 mobile spec) - replaces the legacy
 * `mobile/customer/notifications` feed; also carries Chat V2 entries. Per spec, plain
 * customers skip the vendor-only feature guard on this route. */
class NotificationsComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    private val notificationRepository = NotificationRepository()
    private val _notifications = MutableStateFlow<List<VendorNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _readFilter = MutableStateFlow(NotificationReadFilter.ALL)
    val readFilter = _readFilter.asStateFlow()

    init {
        get()
    }

    fun setReadFilter(filter: NotificationReadFilter) {
        if (_readFilter.value == filter) return
        _readFilter.value = filter
        get()
    }

    fun get() {
        val isRead = when (_readFilter.value) {
            NotificationReadFilter.ALL -> null
            NotificationReadFilter.UNREAD -> false
            NotificationReadFilter.READ -> true
        }
        scope.launch {
            notificationRepository.vendorNotifications(isRead = isRead)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _notifications.update { result.data.items }
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }

    fun markAsRead(item: VendorNotification) {
        if (!item.isRead) {
            scope.launch {
                notificationRepository.markVendorNotificationRead(item.notificationId.toString())
                    .collect { result ->
                        when (result) {
                            is ResultState.Loading -> showLoader()
                            is ResultState.Success -> {
                                hideLoader()
                                get()
                            }

                            is ResultState.Error -> {
                                hideLoader()
                            }
                        }
                    }
                stateHolder.customerNotificationHandler.notificationReceived()
            }
        }
        PushHandler.handleNewPushIfExists(item.route, this)
    }

    fun markAllAsRead() {
        scope.launch {
            notificationRepository.markAllVendorNotificationsRead()
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            get()
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
            stateHolder.customerNotificationHandler.notificationReceived()
        }
    }
}
