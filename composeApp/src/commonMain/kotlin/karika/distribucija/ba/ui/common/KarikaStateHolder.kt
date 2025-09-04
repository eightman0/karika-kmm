package karika.distribucija.ba.ui.common

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock

class KarikaStateHolder(val filePicker: KarikaFilePicker) : NavigationHandler() {
    private val scope = CoroutineScope(Dispatchers.IO)
    val adminMessagesReloadState = MutableStateFlow(Clock.System.now().nanosecondsOfSecond)
    val vendorMessagesReloadState = MutableStateFlow(Clock.System.now().nanosecondsOfSecond)
    var imagePreview = mutableStateOf("")

    fun reloadAdminMessages() {
        adminMessagesReloadState.value = Clock.System.now().nanosecondsOfSecond
    }

    fun reloadVendorMessages() {
        vendorMessagesReloadState.value = Clock.System.now().nanosecondsOfSecond
    }
}