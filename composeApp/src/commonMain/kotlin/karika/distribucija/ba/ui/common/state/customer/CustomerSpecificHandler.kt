package karika.distribucija.ba.ui.common.state.customer

import karika.distribucija.ba.domain.api.UserRepository
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.UserDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
                    if (result is ResultState.Success) {
                        callback.invoke()
                        _userDetails.update { result.data }
                    }
                }
        }
    }


    private val _refreshOrders = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 2
    )
    val refreshOrders: SharedFlow<Unit> = _refreshOrders.asSharedFlow()

    fun refreshOrders() {
        _refreshOrders.tryEmit(Unit)
    }
}