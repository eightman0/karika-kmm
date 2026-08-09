package karika.distribucija.ba.salesrep.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.LoginRepository
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = LoginRepository()

    private val _loginState = MutableLiveData<ResultState<String>>()
    val loginState: LiveData<ResultState<String>> = _loginState

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = ResultState.Error("Unesite korisničko ime i lozinku.")
            return
        }
        viewModelScope.launch {
            repository.login(username, password).collect { result ->
                _loginState.value = result
            }
        }
    }
}
