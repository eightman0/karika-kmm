package karika.distribucija.ba.salesrep.notifications

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** A customer/admin message push landing while the app is running. Mirrors composeApp's
 * KarikaStateHolder customerThreadPush/adminThreadPush signals. */
data class MessagePush(val threadId: String, val admin: Boolean)

/** App-wide signal for live-refreshing an open message list/conversation screen on push receipt -
 * KarikaFcmService.onMessageReceived() publishes here (whether or not the app is foregrounded);
 * the messages ViewModels collect it via their own viewModelScope, so only screens whose ViewModel
 * is actually alive react. */
object MessagePushBus {
    private val _events = MutableSharedFlow<MessagePush>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun publish(threadId: String, admin: Boolean) {
        _events.tryEmit(MessagePush(threadId, admin))
    }
}
