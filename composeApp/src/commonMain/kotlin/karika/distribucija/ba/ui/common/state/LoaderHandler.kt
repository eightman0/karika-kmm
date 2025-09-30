package karika.distribucija.ba.ui.common.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoaderHandler {
    private val _loader = MutableStateFlow(false)
    val loader = _loader.asStateFlow()
    fun showLoader() {
        _loader.update { true }
    }

    fun hideLoader() {
        _loader.update { false }
    }
}