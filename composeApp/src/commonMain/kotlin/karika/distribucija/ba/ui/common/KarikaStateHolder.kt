package karika.distribucija.ba.ui.common

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock

class KarikaStateHolder(val filePicker: KarikaFilePicker) : NavigationHandler() {
    val adminMessagesReloadState = MutableStateFlow(Clock.System.now().nanosecondsOfSecond)
    val vendorMessagesReloadState = MutableStateFlow(Clock.System.now().nanosecondsOfSecond)
    var imagePreview = mutableStateOf("")

    fun reloadAdminMessages() {
        adminMessagesReloadState.value = Clock.System.now().nanosecondsOfSecond
    }

    fun reloadVendorMessages() {
        vendorMessagesReloadState.value = Clock.System.now().nanosecondsOfSecond
    }

    fun getPackageVolume(width: Double, height: Double, depth: Double, weight: Double): Double {
        val volume = maxOf((width * height * depth) / 5000, weight)
        val price =
            config.value.a2b()?.find { it.min() <= volume && it.max() >= volume }?.price()
                ?: config.value.a2b()?.lastOrNull()?.price() ?: 0.0
        return price + (price * 0.1)
    }
}