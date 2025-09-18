package karika.distribucija.ba.ui.view.distributer.dashboard

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.domain.model.VendorProduct
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder
import karika.distribucija.ba.ui.view.distributer.board.BoardComponent
import karika.distribucija.ba.ui.view.distributer.messages.admin.AdminMessagesComponent
import karika.distribucija.ba.ui.view.distributer.messages.customer.CustomerMessagesComponent
import karika.distribucija.ba.ui.view.distributer.messages.details.MessagesOverviewComponent
import karika.distribucija.ba.ui.view.distributer.notifications.NotificationsComponent
import karika.distribucija.ba.ui.view.distributer.orders.OrdersComponent
import karika.distribucija.ba.ui.view.distributer.orders.details.OrderDetailsComponent
import karika.distribucija.ba.ui.view.distributer.products.ProductsComponent
import karika.distribucija.ba.ui.view.distributer.products.details.ProductDetailsComponent
import karika.distribucija.ba.ui.view.distributer.profile.ProfileComponent
import karika.distribucija.ba.ui.view.main.MainChild
import kotlinx.serialization.Serializable

class DashboardComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    init {
        backHandler.register(BackCallback {
            when (stack.value.active.instance) {
                is DashChild.ControlBoard -> {
                    return@BackCallback
                }

                else -> {
                    dashBack()
                }
            }
        })
    }

    val stack: Value<ChildStack<*, DashChild>> =
        childStack(
            source = stateHolder.dashNavigation,
            serializer = DashConfig.serializer(),
            initialConfiguration = DashConfig.ControlBoard,
            handleBackButton = true,
            childFactory = ::child
        )

    private fun child(appConfig: DashConfig, componentContext: ComponentContext): DashChild =
        when (appConfig) {
            is DashConfig.ControlBoard -> DashChild.ControlBoard(
                BoardComponent(
                    componentContext,
                    stateHolder
                )
            )

            is DashConfig.Orders -> DashChild.Orders(OrdersComponent(componentContext, stateHolder))
            is DashConfig.OrderDetails -> DashChild.OrderDetails(
                OrderDetailsComponent(
                    componentContext,
                    stateHolder,
                    appConfig.order
                )
            )

            is DashConfig.Products -> DashChild.Products(
                ProductsComponent(
                    componentContext,
                    stateHolder
                )
            )

            is DashConfig.ProductDetails -> DashChild.ProductDetails(
                ProductDetailsComponent(
                    componentContext,
                    stateHolder,
                    appConfig.product
                )
            )

            is DashConfig.CustomerMessages -> DashChild.CustomerMessages(
                CustomerMessagesComponent(
                    componentContext,
                    stateHolder
                )
            )

            is DashConfig.AdminMessages -> DashChild.AdminMessages(
                AdminMessagesComponent(
                    componentContext,
                    stateHolder
                )
            )

            is DashConfig.MessageOverview -> DashChild.MessageDetails(
                MessagesOverviewComponent(
                    componentContext,
                    stateHolder,
                    appConfig.conversation
                )
            )

            is DashConfig.Profile -> DashChild.Profile(
                ProfileComponent(
                    componentContext,
                    stateHolder
                )
            )

            is DashConfig.Notifications -> DashChild.Notifications(
                NotificationsComponent(
                    componentContext,
                    stateHolder
                )
            )
        }
}

@Serializable
sealed class DashConfig {
    @Serializable
    data object ControlBoard : DashConfig()

    @Serializable
    data object Orders : DashConfig()

    @Serializable
    data class OrderDetails(val order: VendorOrder) : DashConfig()

    @Serializable
    data object Products : DashConfig()

    @Serializable
    data class ProductDetails(val product: VendorProduct) : DashConfig()

    @Serializable
    data object CustomerMessages : DashConfig()

    @Serializable
    data object AdminMessages : DashConfig()

    @Serializable
    data class MessageOverview(val conversation: Conversation) : DashConfig()

    @Serializable
    data object Profile : DashConfig()

    @Serializable
    data object Notifications : DashConfig()
}

sealed class DashChild {
    data class ControlBoard(val component: BoardComponent) : DashChild()
    data class Orders(val component: OrdersComponent) : DashChild()
    data class OrderDetails(val component: OrderDetailsComponent) : DashChild()
    data class Products(val component: ProductsComponent) : DashChild()
    data class ProductDetails(val component: ProductDetailsComponent) : DashChild()
    data class CustomerMessages(val component: CustomerMessagesComponent) : DashChild()
    data class AdminMessages(val component: AdminMessagesComponent) : DashChild()
    data class MessageDetails(val component: MessagesOverviewComponent) : DashChild()
    data class Profile(val component: ProfileComponent) : DashChild()
    data class Notifications(val component: NotificationsComponent) : DashChild()
}