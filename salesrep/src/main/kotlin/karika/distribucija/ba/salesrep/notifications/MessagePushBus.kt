package karika.distribucija.ba.salesrep.notifications

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** A Chat V2 message push landing while the app is running - [conversationId] is the
 * `route/chat?conversationId=` id (or a legacy `route/messages?threadId=`, the same id space).
 * Mirrors composeApp's KarikaStateHolder refresh*Messages + customerThreadPush/adminThreadPush. */
data class MessagePush(val conversationId: Long)

/** App-wide signal for live-refreshing an open message list/conversation screen on push receipt -
 * KarikaFcmService.onMessageReceived() publishes here (whether or not the app is foregrounded);
 * the messages ViewModels collect it via their own viewModelScope, so only screens whose ViewModel
 * is actually alive react. */
object MessagePushBus {
    private val _events = MutableSharedFlow<MessagePush>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun publish(conversationId: Long) {
        _events.tryEmit(MessagePush(conversationId))
    }
}
