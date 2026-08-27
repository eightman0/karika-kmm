package karika.distribucija.ba.salesrep.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.Notification
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.notifications.NotificationDestination
import karika.distribucija.ba.salesrep.notifications.PushRouteResolver
import kotlinx.coroutines.launch

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

    /** Mirrors SalesNotificationsComponent.kt's markAsRead(): the mark-read call (+ list refresh
     * on success) and the navigation resolution are siblings, not sequential - Compose fires
     * PushHandler.handleNewPushIfExistsVendor() right after launching the mark-read coroutine,
     * not after it completes, so navigation doesn't wait on that network round trip either. */
    fun markAsRead(item: Notification) {
        viewModelScope.launch {
            repository.markNotificationRead(item.id).collect { result ->
                if (result is ResultState.Success) load()
            }
        }
        PushRouteResolver.resolve(item.route)?.let { _navigateTo.value = it }
    }

    fun clearNavigation() {
        _navigateTo.value = null
    }
}
