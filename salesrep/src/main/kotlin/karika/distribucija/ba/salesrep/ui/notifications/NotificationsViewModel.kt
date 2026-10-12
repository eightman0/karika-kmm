package karika.distribucija.ba.salesrep.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.VendorNotificationsRepository
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.model.VendorNotification
import karika.distribucija.ba.salesrep.notifications.NotificationDestination
import karika.distribucija.ba.salesrep.notifications.PushRouteResolver
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesNotificationsComponent.kt (`/V1/vendor-operations/notifications`). */
class NotificationsViewModel : ViewModel() {

    private val repository = VendorNotificationsRepository()

    private val _notifications = MutableLiveData<List<VendorNotification>>(emptyList())
    val notifications: LiveData<List<VendorNotification>> = _notifications

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    /** Sve / Nepročitano / Pročitano - NotificationReadFilter in composeApp. */
    enum class ReadFilter { ALL, UNREAD, READ }

    private val _readFilter = MutableLiveData(ReadFilter.ALL)
    val readFilter: LiveData<ReadFilter> = _readFilter

    private val _navigateTo = MutableLiveData<NotificationDestination?>(null)
    val navigateTo: LiveData<NotificationDestination?> = _navigateTo

    fun setReadFilter(filter: ReadFilter) {
        if (_readFilter.value == filter) return
        _readFilter.value = filter
        load()
    }

    fun load() {
        val isRead = when (_readFilter.value) {
            ReadFilter.UNREAD -> false
            ReadFilter.READ -> true
            else -> null
        }
        viewModelScope.launch {
            repository.notifications(isRead = isRead).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isLoading.value = true
                    is ResultState.Success -> {
                        _isLoading.value = false
                        _notifications.value = result.data.items
                    }
                    is ResultState.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }

    /** Like SalesNotificationsComponent.markAsRead(): the mark-read call (+ reload on success) and
     * the navigation are siblings - navigation doesn't wait on the network round trip. */
    fun markAsRead(item: VendorNotification) {
        viewModelScope.launch {
            repository.markRead(item.notificationId.toString()).collect { result ->
                if (result is ResultState.Success) load()
            }
        }
        PushRouteResolver.resolve(item.route)?.let { _navigateTo.value = it }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllRead().collect { result ->
                when (result) {
                    is ResultState.Loading -> _isLoading.value = true
                    is ResultState.Success -> {
                        _isLoading.value = false
                        load()
                    }
                    is ResultState.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }

    fun clearNavigation() {
        _navigateTo.value = null
    }
}
