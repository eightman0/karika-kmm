package karika.distribucija.ba.salesrep.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.LoginRepository
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.launch

/** Mirrors composeApp's LoginComponent.forgotPassword(email). */
class ForgotPasswordViewModel : ViewModel() {

    private val repository = LoginRepository()

    private val _state = MutableLiveData<ResultState<String>?>(null)
    val state: LiveData<ResultState<String>?> = _state

    fun submit(email: String) {
        viewModelScope.launch {
            repository.forgotPassword(email).collect { _state.value = it }
        }
    }
}
