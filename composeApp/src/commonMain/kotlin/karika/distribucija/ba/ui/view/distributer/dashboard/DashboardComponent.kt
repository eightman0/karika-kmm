package karika.distribucija.ba.ui.view.distributer.dashboard

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.domain.model.VendorEmployee
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.domain.model.VendorProduct
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.distributer.analytics.AnalyticsComponent
import karika.distribucija.ba.ui.view.distributer.analytics.AnalyticsFiltersComponent
import karika.distribucija.ba.ui.view.distributer.analytics.AnalyticsTab
import karika.distribucija.ba.ui.view.distributer.analytics.atrisk.AnalyticsAtRiskComponent
import karika.distribucija.ba.ui.view.distributer.analytics.products.AnalyticsProductsComponent
import karika.distribucija.ba.ui.view.distributer.customers.CustomerRule
import karika.distribucija.ba.ui.view.distributer.customers.CustomersComponent
import karika.distribucija.ba.ui.view.distributer.customers.RuleScope
import karika.distribucija.ba.ui.view.distributer.customers.editor.CustomerRuleEditorComponent
import karika.distribucija.ba.ui.view.distributer.employees.EmployeesComponent
import karika.distribucija.ba.ui.view.distributer.employees.locations.EmployeeLocationsComponent
import karika.distribucija.ba.ui.view.distributer.messages.admin.AdminMessagesComponent
import karika.distribucija.ba.ui.view.distributer.messages.customer.CustomerMessagesComponent
import karika.distribucija.ba.ui.view.distributer.messages.details.MessagesOverviewComponent
import karika.distribucija.ba.ui.view.distributer.messages.internal.InternalMessagesComponent
import karika.distribucija.ba.ui.view.distributer.notifications.NotificationsComponent
import karika.distribucija.ba.ui.view.distributer.orders.OrdersComponent
import karika.distribucija.ba.ui.view.distributer.orders.details.OrderDetailsComponent
import karika.distribucija.ba.ui.view.distributer.products.ProductsComponent
import karika.distribucija.ba.ui.view.distributer.products.details.ProductDetailsComponent
import karika.distribucija.ba.ui.view.distributer.profile.ProfileComponent
import kotlinx.serialization.Serializable

class DashboardComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    init {
        backHandler.register(BackCallback {
            when (stack.value.active.instance) {
                is DashChild.Analytics -> {
                    return@BackCallback
                }

                else -> {
                    dashBack()
                }
            }
        })

        stateHolder.vendorSpecificHandler.getVendorDetails()
        stateHolder.vendorNotificationHandler.notificationReceived()
        stateHolder.salesSpecificHandler.getMe()
    }

    val stack: Value<ChildStack<*, DashChild>> =
        childStack(
            source = stateHolder.dashNavigation,
            serializer = DashConfig.serializer(),
            initialConfiguration = DashConfig.Analytics(),
            handleBackButton = true,
            childFactory = ::child
        )

    private fun child(appConfig: DashConfig, componentContext: ComponentContext): DashChild =
        when (appConfig) {
            is DashConfig.Analytics -> DashChild.Analytics(
                AnalyticsComponent(
                    componentContext,
                    stateHolder,
                    appConfig.tab,
                    onOpenFilters = { dashNavigate(DashConfig.AnalyticsFilters) },
                    onOpenAtRiskCustomers = { dashNavigate(DashConfig.AnalyticsAtRisk, true) },
                )
            )

            is DashConfig.AnalyticsProducts -> DashChild.AnalyticsProducts(
                AnalyticsProductsComponent(
                    componentContext,
                    stateHolder
                )
            )

            is DashConfig.AnalyticsAtRisk -> DashChild.AnalyticsAtRisk(
                AnalyticsAtRiskComponent(
                    componentContext,
                    stateHolder
                )
            )

            is DashConfig.AnalyticsFilters -> DashChild.AnalyticsFilters(
                AnalyticsFiltersComponent(
                    componentContext,
                    stateHolder,
                    onBack = { dashBack() },
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

            is DashConfig.Customers -> DashChild.Customers(
                CustomersComponent(
                    componentContext,
                    stateHolder
                )
            )

            is DashConfig.CustomerRuleEditor -> DashChild.CustomerRuleEditor(
                CustomerRuleEditorComponent(
                    componentContext = componentContext,
                    stateHolder = stateHolder,
                    ruleScope = appConfig.scope,
                    initialRule = appConfig.rule
                )
            )

            is DashConfig.Employees -> DashChild.Employees(
                EmployeesComponent(
                    componentContext,
                    stateHolder
                )
            )

            is DashConfig.EmployeeLocations -> DashChild.EmployeeLocations(
                EmployeeLocationsComponent(
                    componentContext,
                    stateHolder,
                    appConfig.employee
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

            is DashConfig.InternalMessages -> DashChild.InternalMessages(
                InternalMessagesComponent(
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
    data class Analytics(val tab: AnalyticsTab = AnalyticsTab.Overview) : DashConfig()

    @Serializable
    data object AnalyticsProducts : DashConfig()

    @Serializable
    data object AnalyticsAtRisk : DashConfig()

    @Serializable
    data object AnalyticsFilters : DashConfig()

    @Serializable
    data object Orders : DashConfig()

    @Serializable
    data class OrderDetails(val order: VendorOrder) : DashConfig()

    @Serializable
    data object Products : DashConfig()

    @Serializable
    data class ProductDetails(val product: VendorProduct) : DashConfig()

    @Serializable
    data object Customers : DashConfig()

    @Serializable
    data class CustomerRuleEditor(
        val scope: RuleScope,
        val rule: CustomerRule? = null
    ) : DashConfig()

    @Serializable
    data object Employees : DashConfig()

    @Serializable
    data class EmployeeLocations(val employee: VendorEmployee) : DashConfig()

    @Serializable
    data object CustomerMessages : DashConfig()

    @Serializable
    data object AdminMessages : DashConfig()

    @Serializable
    data object InternalMessages : DashConfig()

    @Serializable
    data class MessageOverview(val conversation: ChatConversation) : DashConfig()

    @Serializable
    data object Profile : DashConfig()

    @Serializable
    data object Notifications : DashConfig()
}

sealed class DashChild {
    data class Analytics(val component: AnalyticsComponent) : DashChild()
    data class AnalyticsProducts(val component: AnalyticsProductsComponent) : DashChild()
    data class AnalyticsAtRisk(val component: AnalyticsAtRiskComponent) : DashChild()
    data class AnalyticsFilters(val component: AnalyticsFiltersComponent) : DashChild()
    data class Orders(val component: OrdersComponent) : DashChild()
    data class OrderDetails(val component: OrderDetailsComponent) : DashChild()
    data class Products(val component: ProductsComponent) : DashChild()
    data class ProductDetails(val component: ProductDetailsComponent) : DashChild()
    data class Customers(val component: CustomersComponent) : DashChild()
    data class CustomerRuleEditor(val component: CustomerRuleEditorComponent) : DashChild()
    data class Employees(val component: EmployeesComponent) : DashChild()
    data class EmployeeLocations(val component: EmployeeLocationsComponent) : DashChild()
    data class CustomerMessages(val component: CustomerMessagesComponent) : DashChild()
    data class AdminMessages(val component: AdminMessagesComponent) : DashChild()
    data class InternalMessages(val component: InternalMessagesComponent) : DashChild()
    data class MessageDetails(val component: MessagesOverviewComponent) : DashChild()
    data class Profile(val component: ProfileComponent) : DashChild()
    data class Notifications(val component: NotificationsComponent) : DashChild()
}