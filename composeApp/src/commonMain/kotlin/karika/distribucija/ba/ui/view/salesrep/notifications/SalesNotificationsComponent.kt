package karika.distribucija.ba.ui.view.salesrep.notifications

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.DashRepository
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
 * `mobile/vendor/notifications` feed; also carries Chat V2 entries. */
class SalesNotificationsComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    private val dashRepository = DashRepository()
    private val _notifications = MutableStateFlow<List<VendorNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _readFilter = MutableStateFlow(NotificationReadFilter.ALL)
    val readFilter = _readFilter.asStateFlow()

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
            dashRepository.vendorNotifications(isRead = isRead)
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
        scope.launch {
            dashRepository.markVendorNotificationRead(item.notificationId.toString())
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
            stateHolder.vendorNotificationHandler.notificationReceived()
        }
        PushHandler.handleNewPushIfExistsSalesRep(item.route, this)
    }

    fun markAllAsRead() {
        scope.launch {
            dashRepository.markAllVendorNotificationsRead()
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
            stateHolder.vendorNotificationHandler.notificationReceived()
        }
    }

    fun goBack() = salesRepBack()
}
