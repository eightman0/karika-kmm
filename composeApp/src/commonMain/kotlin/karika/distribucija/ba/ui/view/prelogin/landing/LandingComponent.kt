package karika.distribucija.ba.ui.view.prelogin.landing

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.bringToFront
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder
import karika.distribucija.ba.ui.view.prelogin.PreLoginConfig

class LandingComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {
    fun navigateLogin() {
        stateHolder.preLoginNavigation.bringToFront(PreLoginConfig.Login)
    }

}