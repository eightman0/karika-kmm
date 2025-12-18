package karika.distribucija.ba.ui.view.prelogin.login

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.essenty.lifecycle.Lifecycle
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.api.LoginRepository
import karika.distribucija.ba.domain.model.LoginDto
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.view.prelogin.PreLoginConfig
import kotlinx.coroutines.launch

class LoginComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    private val userType: KarikaType,
) : CommonComponent(componentContext, stateHolder) {

    val forgotPassSheet = mutableStateOf(false)
    val email = mutableStateOf(stateHolder.sessionHandler.getUserUsername(userType))
    val pass = mutableStateOf(stateHolder.sessionHandler.getUserPassword(userType))
    val rememberMe =
        mutableStateOf(stateHolder.sessionHandler.getUserPassword(userType).isNotEmpty())
    val formValid =
        mutableStateOf(stateHolder.sessionHandler.getUserPassword(userType).isNotEmpty())
    private val repository = LoginRepository()

    init {
        val state = scope.launch {
            stateHolder.commonHandler.deepLinkToken.collect {
                login(it.first, it.second)
            }
        }

        lifecycle.subscribe(callbacks = object : Lifecycle.Callbacks {
            override fun onResume() {
                super.onResume()
                state.start()
            }

            override fun onDestroy() {
                super.onDestroy()
                state.cancel()
            }
        })
    }


    fun login(emailToken: String = "", token: String = "", callback: () -> Unit = {}) {
        showLoader()
        scope.launch {
            repository.login(
                LoginDto(
                    emailToken.ifEmpty { email.value },
                    token.ifEmpty { pass.value },
                    userType
                )
            )
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> {
                            showLoader()
                        }

                        is ResultState.Success -> {
                            hideLoader()
                            stateHolder.sessionHandler.setAccessToken(result.data)
                            stateHolder.sessionHandler.saveJWT(
                                result.data,
                                LoginDto(email.value, pass.value, userType),
                                rememberMe.value
                            )
                            savePushHandle()
                            navigatePostLogin()
                            callback()
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
        scope.launch {
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
        stateHolder.preLoginNavigation.bringToFront(PreLoginConfig.Registration(userType))
    }

    fun navigateLanding() {
        stateHolder.preLoginNavigation.replaceAll(PreLoginConfig.Landing)
    }

    private fun navigatePostLogin() {
        scope.launch {
            stateHolder.appNavigation.replaceAll(
                if (userType.isShop()) AppConfig.Main else AppConfig.Dashboard
            )
        }
    }

    fun title(): String {
        return if (userType.isShop()) "Prijava kupac" else "Prijava dobavljač"
    }

    fun wifi() {
        stateHolder.handler.openWifi()
    }

    fun isShop() = userType == KarikaType.SHOP

    private var exitCount = 0
    fun exitKiosk() {
        exitCount++
        if (exitCount == 10) {
            stateHolder.handler.exitKiosk()
        }
    }
}