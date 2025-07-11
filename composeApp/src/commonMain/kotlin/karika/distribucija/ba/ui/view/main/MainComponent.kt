package karika.distribucija.ba.ui.view.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder
import karika.distribucija.ba.ui.view.main.cart.CartComponent
import karika.distribucija.ba.ui.view.main.cart.nextstep.ShippingDetailsComponent
import karika.distribucija.ba.ui.view.main.cart.success.CartSuccessComponent
import karika.distribucija.ba.ui.view.main.home.HomeComponent
import karika.distribucija.ba.ui.view.main.menu.MenuComponent
import karika.distribucija.ba.ui.view.main.profile.ProfileComponent
import karika.distribucija.ba.ui.view.main.vendor.VendorComponent
import kotlinx.serialization.Serializable

@Serializable
sealed class MainConfig {
    @Serializable
    data object Home : MainConfig()

    @Serializable
    data object Vendor : MainConfig()

    @Serializable
    data object Menu : MainConfig()

    @Serializable
    data object Cart : MainConfig()

    @Serializable
    data object CartShippingDetails : MainConfig()

    @Serializable
    data class CartSuccess(val orderId: String) : MainConfig()

    @Serializable
    data object Profile : MainConfig()
}

sealed class MainChild {
    class Home(val component: HomeComponent) : MainChild()
    class Vendor(val component: VendorComponent) : MainChild()
    class Menu(val component: MenuComponent) : MainChild()

    class Cart(val component: CartComponent) : MainChild()
    class CartShippingDetails(val component: ShippingDetailsComponent) : MainChild()
    class CartSuccess(val component: CartSuccessComponent) : MainChild()

    class Profile(val component: ProfileComponent) : MainChild()
}

class MainComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    val stack: Value<ChildStack<*, MainChild>> =
        childStack(
            source = stateHolder.mainNavigation,
            serializer = MainConfig.serializer(),
            initialConfiguration = MainConfig.Home,
            handleBackButton = true,
            childFactory = ::child,
        )

    private val backCallback = BackCallback {
        when (stack.value.active.instance) {
            is MainChild.CartSuccess -> {

            }

            else -> {
                if (stack.value.items.any { it.instance is MainChild.CartSuccess }) {
                    mainBack()
                    mainBack()
                }
                mainBack()
            }
        }
    }

    init {
        backHandler.register(backCallback)
    }

    private fun child(config: MainConfig, componentContext: ComponentContext): MainChild =
        when (config) {
            is MainConfig.Home -> MainChild.Home(
                HomeComponent(componentContext, stateHolder)
            )

            is MainConfig.Vendor -> MainChild.Vendor(
                VendorComponent(componentContext, stateHolder)
            )

            is MainConfig.Menu -> MainChild.Menu(
                MenuComponent(componentContext, stateHolder)
            )

            is MainConfig.Cart -> MainChild.Cart(
                CartComponent(componentContext, stateHolder)
            )

            is MainConfig.CartShippingDetails -> MainChild.CartShippingDetails(
                ShippingDetailsComponent(componentContext, stateHolder)
            )

            is MainConfig.CartSuccess -> MainChild.CartSuccess(
                CartSuccessComponent(componentContext, stateHolder, config.orderId)
            )

            is MainConfig.Profile -> MainChild.Profile(
                ProfileComponent(componentContext, stateHolder)
            )
        }

    fun navigate(config: MainConfig) {
        stateHolder.mainNavigation.bringToFront(config)
    }
}