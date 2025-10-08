package karika.distribucija.ba.ui.view.prelogin.landing

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.bringToFront
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.common.isKiosk
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.prelogin.PreLoginConfig

class LandingComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {

    fun navigateLogin(type: KarikaType) {
        stateHolder.preLoginNavigation.bringToFront(PreLoginConfig.Login(type))
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