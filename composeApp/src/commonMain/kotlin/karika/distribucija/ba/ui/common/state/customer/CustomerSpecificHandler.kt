package karika.distribucija.ba.ui.common.state.customer

import androidx.compose.runtime.mutableStateOf
import karika.distribucija.ba.domain.api.UserRepository
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.UserDetails
import karika.distribucija.ba.ui.components.negate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CustomerSpecificHandler {
    private val _userDetails = MutableStateFlow(UserDetails())
    val userDetails = _userDetails.asStateFlow()

    fun getUserDetails(callback: () -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            UserRepository().get()
                .collect { result ->
                    callback.invoke()
                    if (result is ResultState.Success) {
                        _userDetails.update { result.data }
                    }
                }
        }
    }
}