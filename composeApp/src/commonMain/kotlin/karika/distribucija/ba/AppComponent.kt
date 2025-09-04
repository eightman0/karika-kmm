package karika.distribucija.ba

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.domain.model.Order
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaFilePicker
import karika.distribucija.ba.ui.common.KarikaStateHolder
import karika.distribucija.ba.ui.view.main.MainComponent
import karika.distribucija.ba.ui.view.main.menu.blog.BlogsComponent
import karika.distribucija.ba.ui.view.main.menu.blog.overview.BlogOverviewComponent
import karika.distribucija.ba.ui.view.main.product.ProductComponent
import karika.distribucija.ba.ui.view.main.profile.account.AccountComponent
import karika.distribucija.ba.ui.view.main.profile.messages.admin.AdminMessagesComponent
import karika.distribucija.ba.ui.view.main.profile.messages.overview.MessagesOverviewComponent
import karika.distribucija.ba.ui.view.main.profile.messages.vendor.VendorMessagesComponent
import karika.distribucija.ba.ui.view.main.profile.notifications.NotificationsComponent
import karika.distribucija.ba.ui.view.main.profile.order.OrdersComponent
import karika.distribucija.ba.ui.view.main.profile.order.comments.CommentsComponent
import karika.distribucija.ba.ui.view.main.profile.order.details.OrderDetailsComponent
import karika.distribucija.ba.ui.view.main.profile.points.PointsComponent
import karika.distribucija.ba.ui.view.main.vendor.details.VendorDetailsComponent
import karika.distribucija.ba.ui.view.prelogin.PreLoginComponent
import kotlinx.serialization.Serializable

@Serializable
sealed class AppConfig {
    @Serializable
    data object Loading : AppConfig()

    @Serializable
    data object Main : AppConfig()

    @Serializable
    data object PreLogin : AppConfig()

    @Serializable
    data class ProductDetails(val product: Product) : AppConfig()

    @Serializable
    data class VendorDetails(val vendor: Vendor) : AppConfig()

    @Serializable
    data object Account : AppConfig()

    @Serializable
    data object Blogs : AppConfig()

    @Serializable
    data class Blog(val blog: karika.distribucija.ba.domain.model.Blog) : AppConfig()

    @Serializable
    data object Orders : AppConfig()

    @Serializable
    data class OrderDetails(val order: OrdersResponse) : AppConfig()

    @Serializable
    data class OrderComments(val order: Order) : AppConfig()

    @Serializable
    data object AdminMessages : AppConfig()

    @Serializable
    data object VendorMessages : AppConfig()

    @Serializable
    data class MessagesOverview(val conversation: Conversation) : AppConfig()

    @Serializable
    data object Points : AppConfig()

    @Serializable
    data object Notifications : AppConfig()
}

sealed class Child {
    data object Loading : Child()
    class Main(val component: MainComponent) : Child()
    class PreLogin(val component: PreLoginComponent) : Child()
    class ProductDetails(val component: ProductComponent) : Child()
    class VendorDetails(val component: VendorDetailsComponent) : Child()

    class Account(val component: AccountComponent) : Child()
    class Blogs(val component: BlogsComponent) : Child()
    class Blog(val component: BlogOverviewComponent) : Child()

    class Orders(val component: OrdersComponent) : Child()
    class OrderDetails(val component: OrderDetailsComponent) : Child()
    class OrderComments(val component: CommentsComponent) : Child()

    class AdminMessages(val component: AdminMessagesComponent) : Child()
    class VendorMessages(val component: VendorMessagesComponent) : Child()
    class MessagesOverview(val component: MessagesOverviewComponent) : Child()

    class Points(val component: PointsComponent) : Child()
    class Notifications(val component: NotificationsComponent) : Child()
}

class AppComponent(
    componentContext: ComponentContext,
    filePicker: KarikaFilePicker
) : CommonComponent(componentContext, KarikaStateHolder(filePicker)) {
    companion object {
        var refreshHandler: () -> Unit = {}
    }

    init {
        refreshHandler = {
            stateHolder.notificationReceived()
        }
    }

    val stack: Value<ChildStack<*, Child>> =
        childStack(
            source = stateHolder.appNavigation,
            serializer = AppConfig.serializer(),
            initialConfiguration = if (stateHolder.hasJWT()) AppConfig.Main else AppConfig.PreLogin,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(appConfig: AppConfig, componentContext: ComponentContext): Child =
        when (appConfig) {
            is AppConfig.Loading -> Child.Loading

            is AppConfig.Main -> Child.Main(
                MainComponent(
                    componentContext,
                    stateHolder
                )
            )

            is AppConfig.PreLogin -> Child.PreLogin(
                PreLoginComponent(componentContext, stateHolder)
            )

            is AppConfig.ProductDetails -> Child.ProductDetails(
                ProductComponent(componentContext, stateHolder, appConfig.product)
            )

            is AppConfig.VendorDetails -> Child.VendorDetails(
                VendorDetailsComponent(componentContext, stateHolder, appConfig.vendor)
            )

            is AppConfig.Account -> Child.Account(
                AccountComponent(componentContext, stateHolder)
            )

            is AppConfig.Blogs -> Child.Blogs(
                BlogsComponent(componentContext, stateHolder)
            )

            is AppConfig.Blog -> Child.Blog(
                BlogOverviewComponent(componentContext, stateHolder, appConfig.blog)
            )

            is AppConfig.Orders -> Child.Orders(
                OrdersComponent(componentContext, stateHolder)
            )

            is AppConfig.OrderDetails -> Child.OrderDetails(
                OrderDetailsComponent(componentContext, stateHolder, appConfig.order)
            )

            is AppConfig.OrderComments -> Child.OrderComments(
                CommentsComponent(componentContext, stateHolder, appConfig.order)
            )

            is AppConfig.AdminMessages -> Child.AdminMessages(
                AdminMessagesComponent(componentContext, stateHolder)
            )

            is AppConfig.VendorMessages -> Child.VendorMessages(
                VendorMessagesComponent(componentContext, stateHolder)
            )

            is AppConfig.MessagesOverview -> Child.MessagesOverview(
                MessagesOverviewComponent(componentContext, stateHolder, appConfig.conversation)
            )

            is AppConfig.Points -> Child.Points(
                PointsComponent(componentContext, stateHolder)
            )

            is AppConfig.Notifications -> Child.Notifications(
                NotificationsComponent(componentContext, stateHolder)
            )
        }
}