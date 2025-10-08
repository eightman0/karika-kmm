package karika.distribucija.ba.ui.common.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class LoaderHandler {
    private val _loader = MutableStateFlow(false)
    val loader = _loader.asStateFlow()
    private var startTime = 0L

    @OptIn(ExperimentalTime::class)
    private fun now(): Long = Clock.System.now().toEpochMilliseconds()


    fun showLoader() {
        startTime = now()
        _loader.update { true }
    }

    fun hideLoader() {
        val elapsed = now() - startTime
        if (elapsed > 500) {
            _loader.update { false }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            delay(500 - elapsed)
            _loader.update { false }
        }
    }
}