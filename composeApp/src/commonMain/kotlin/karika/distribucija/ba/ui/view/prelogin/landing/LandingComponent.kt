package karika.distribucija.ba.ui.view.prelogin.landing

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.bringToFront
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder
import karika.distribucija.ba.ui.view.prelogin.PreLoginConfig
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LandingComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {
    fun navigateLogin() {
        stateHolder.preLoginNavigation.bringToFront(PreLoginConfig.Login)
    }

    init {
        loadData()
    }

    private fun loadData() {
        iOScope.launch {
            productRepository.promotedVendors().collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        showLoader()
                    }

                    is ResultState.Success -> {
                        hideLoader()
                        _promotedVendors.update {
                            result.data
                                .filter { f -> f.promoteVendorBanner }
                                .filter { f -> f.companyBanner != null }
                        }
                        _promotedLogos.update {
                            result.data
                                .filter { f -> f.promoteVendorLogo }
                                .filter { f -> f.companyLogo != null }
                        }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message ?: "")
                    }
                }
            }
        }
    }

    override fun showVendor(vendor: Vendor) {

    }
}