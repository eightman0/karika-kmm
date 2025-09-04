package karika.distribucija.ba.ui.view.prelogin.login

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.replaceAll
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.api.LoginRepository
import karika.distribucija.ba.domain.model.LoginDto
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.view.prelogin.PreLoginConfig
import kotlinx.coroutines.launch

class LoginComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {

    val forgotPassSheet = mutableStateOf(false)
    val email = mutableStateOf(stateHolder.getUserUsername())
    val pass = mutableStateOf(stateHolder.getUserPassword())
    val rememberMe = mutableStateOf(stateHolder.getUserPassword().isNotEmpty())
    val formValid = mutableStateOf(stateHolder.getUserPassword().isNotEmpty())
    private val repository = LoginRepository()

    fun login() {
        showLoader()
        iOScope.launch {
            repository.login(LoginDto(email.value, pass.value))
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> {
                            showLoader()
                        }

                        is ResultState.Success -> {
                            hideLoader()
                            stateHolder.accessToken.value = result.data
                            stateHolder.saveJWT(
                                result.data,
                                LoginDto(email.value, pass.value),
                                rememberMe.value
                            )
                            savePushHandle()
                            stateHolder.reloadCart()
                            navigatePostLogin()
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message ?: "")
                        }
                    }
                }
        }
    }

    fun forgotPassword(email: String) {
        iOScope.launch {
            userRepository.forgotPass(email)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            showMessage(result.data)
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }

    fun forgotPassword() {
        forgotPassSheet.negate()
    }

    fun navigateRegistration() {
        stateHolder.preLoginNavigation.bringToFront(PreLoginConfig.Registration)
    }

    private fun navigatePostLogin() {
        mainScope.launch {
            stateHolder.appNavigation.replaceAll(AppConfig.Main)
        }
    }
}