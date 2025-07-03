package karika.distribucija.ba.ui.view.prelogin.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import karika.distribucija.ba.Screen
import karika.distribucija.ba.domain.LoginRepository
import karika.distribucija.ba.domain.model.LoginDto
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonViewModel
import karika.distribucija.ba.ui.common.KarikaStateHolder
import kotlinx.coroutines.launch

class LoginViewModel(navController: NavController, stateHolder: KarikaStateHolder) :
    CommonViewModel(navController, stateHolder) {
    private val repository = LoginRepository()

    fun forgotPassword() {

    }

    fun login() {
        showLoader()
        viewModelScope.launch {
            repository.login(LoginDto("abuljubasic@iteontech.ba", "NewPass"))
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> {
                            showLoader()
                        }

                        is ResultState.Success -> {
                            hideLoader()
                            stateHolder.accessToken.value = result.data
                            navigate(Screen.Main)
                        }

                        else -> {
                            hideLoader()
                        }
                    }
                }
        }
    }

    val email = mutableStateOf("")
    val pass = mutableStateOf("")
}