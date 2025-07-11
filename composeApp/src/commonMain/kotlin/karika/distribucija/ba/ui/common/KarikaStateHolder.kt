package karika.distribucija.ba.ui.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock

class KarikaStateHolder : NavigationHandler() {
    val adminMessagesReloadState = MutableStateFlow(Clock.System.now().nanosecondsOfSecond)
    val vendorMessagesReloadState = MutableStateFlow(Clock.System.now().nanosecondsOfSecond)

    fun reloadAdminMessages() {
        adminMessagesReloadState.value = Clock.System.now().nanosecondsOfSecond
    }

    fun reloadVendorMessages() {
        vendorMessagesReloadState.value = Clock.System.now().nanosecondsOfSecond
    }
}