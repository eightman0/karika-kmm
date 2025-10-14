package karika.distribucija.ba.ui.view.prelogin

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.common.isKiosk
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.prelogin.landing.LandingComponent
import karika.distribucija.ba.ui.view.prelogin.login.LoginComponent
import karika.distribucija.ba.ui.view.prelogin.registration.RegistrationComponent
import kotlinx.serialization.Serializable

@Serializable
sealed class PreLoginConfig {
    @Serializable
    data object Landing : PreLoginConfig()

    @Serializable
    data class Login(val userType: KarikaType) : PreLoginConfig()

    @Serializable
    data class Registration(val userType: KarikaType) : PreLoginConfig()
}

sealed class PreLoginChild {
    class Landing(val component: LandingComponent) : PreLoginChild()
    class Login(val component: LoginComponent) : PreLoginChild()
    class Registration(val component: RegistrationComponent) : PreLoginChild()
}

class PreLoginComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    showLogin: Boolean = true
) : CommonComponent(componentContext, stateHolder) {
    val stack: Value<ChildStack<*, PreLoginChild>> =
        childStack(
            source = stateHolder.preLoginNavigation,
            serializer = PreLoginConfig.serializer(),
            initialConfiguration = if (isKiosk() || showLogin) PreLoginConfig.Login(KarikaType.SHOP) else PreLoginConfig.Landing,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(config: PreLoginConfig, componentContext: ComponentContext): PreLoginChild =
        when (config) {
            is PreLoginConfig.Landing -> PreLoginChild.Landing(
                LandingComponent(componentContext, stateHolder)
            )

            is PreLoginConfig.Login -> PreLoginChild.Login(
                LoginComponent(componentContext, stateHolder, config.userType)
            )

            is PreLoginConfig.Registration -> PreLoginChild.Registration(
                RegistrationComponent(componentContext, stateHolder, config.userType)
            )
        }
}