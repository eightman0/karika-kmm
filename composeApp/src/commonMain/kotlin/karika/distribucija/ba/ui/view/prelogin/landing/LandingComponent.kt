package karika.distribucija.ba.ui.view.prelogin.landing

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.bringToFront
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.common.ScreenComponent
import karika.distribucija.ba.ui.common.isKiosk
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.prelogin.PreLoginConfig
import kotlinx.coroutines.flow.StateFlow

interface LandingComponent : ScreenComponent {
    val promotedLogos: StateFlow<List<PromotedVendor>>

    fun navigateLogin(type: KarikaType)

    fun continueAsGuest()

    fun loadBanners()
}

class DefaultLandingComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder), LandingComponent {

    override fun navigateLogin(type: KarikaType) {
        stateHolder.preLoginNavigation.bringToFront(PreLoginConfig.Login(type))
    }

    override fun continueAsGuest() {
        appNavigate(AppConfig.Main)
    }

    override fun showVendor(vendor: Vendor) {

    }

    override fun loadBanners() {
        if (isKiosk()) {
            return
        }
        super.loadBanners()
    }
}
