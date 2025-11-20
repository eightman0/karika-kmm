package karika.distribucija.ba.ui.common.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class MessageHandler {
    val adminMessagesReloadState = MutableStateFlow(Clock.System.now().nanosecondsOfSecond)
    val vendorMessagesReloadState = MutableStateFlow(Clock.System.now().nanosecondsOfSecond)

    fun reloadAdminMessages() {
        adminMessagesReloadState.value = Clock.System.now().nanosecondsOfSecond
    }

    fun reloadVendorMessages() {
        vendorMessagesReloadState.value = Clock.System.now().nanosecondsOfSecond
    }
}