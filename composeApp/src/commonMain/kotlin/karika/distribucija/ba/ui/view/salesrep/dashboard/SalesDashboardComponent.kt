package karika.distribucija.ba.ui.view.salesrep.dashboard

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.lifecycle.doOnDestroy
import karika.distribucija.ba.domain.api.DeviceIdentifier
import karika.distribucija.ba.domain.api.EmployeeLocationRepository
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.domain.model.EmployeeLocationSubmit
import karika.distribucija.ba.domain.model.DiscountRule
import karika.distribucija.ba.domain.model.OnBehalfOrder
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.distributer.analytics.AnalyticsComponent
import karika.distribucija.ba.ui.view.distributer.analytics.AnalyticsFiltersComponent
import karika.distribucija.ba.ui.view.distributer.analytics.AnalyticsTab
import karika.distribucija.ba.ui.view.distributer.analytics.atrisk.AnalyticsAtRiskComponent
import karika.distribucija.ba.ui.view.distributer.analytics.products.AnalyticsProductsComponent
import karika.distribucija.ba.ui.view.salesrep.cart.SalesOrderCartComponent
import karika.distribucija.ba.ui.view.salesrep.cart.SalesOrderReviewComponent
import karika.distribucija.ba.ui.view.salesrep.catalog.SalesOrderCatalogComponent
import karika.distribucija.ba.ui.view.salesrep.customers.SalesCustomersComponent
import karika.distribucija.ba.ui.view.salesrep.customers.detail.SalesCustomerDetailComponent
import karika.distribucija.ba.ui.view.salesrep.customers.detail.SalesDiscountFormComponent
import karika.distribucija.ba.ui.view.salesrep.customers.invite.SalesInviteCustomerComponent
import karika.distribucija.ba.ui.view.salesrep.customers.newcustomer.SalesNewCustomerComponent
import karika.distribucija.ba.ui.view.salesrep.messages.admin.SalesAdminConversationComponent
import karika.distribucija.ba.ui.view.salesrep.messages.admin.SalesAdminMessagesComponent
import karika.distribucija.ba.ui.view.salesrep.messages.admin.SalesAdminNewMessageComponent
import karika.distribucija.ba.ui.view.salesrep.messages.customer.SalesCustomerConversationComponent
import karika.distribucija.ba.ui.view.salesrep.messages.customer.SalesCustomerMessagesComponent
import karika.distribucija.ba.ui.view.salesrep.messages.customer.SalesCustomerNewMessageComponent
import karika.distribucija.ba.ui.view.salesrep.messages.internal.SalesInternalConversationComponent
import karika.distribucija.ba.ui.view.salesrep.messages.internal.SalesInternalMessagesComponent
import karika.distribucija.ba.ui.view.salesrep.messages.internal.SalesInternalNewMessageComponent
import karika.distribucija.ba.ui.view.salesrep.notifications.SalesNotificationsComponent
import karika.distribucija.ba.ui.common.currentDeviceLocation
import karika.distribucija.ba.ui.common.requestLocationPermission
import karika.distribucija.ba.ui.view.salesrep.operations.SalesOperationsComponent
import karika.distribucija.ba.ui.view.salesrep.orders.SalesOrdersComponent
import karika.distribucija.ba.ui.view.salesrep.orders.detail.SalesOrderDetailComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

class SalesDashboardComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {

    private val locationRepository = EmployeeLocationRepository()

    init {
        backHandler.register(BackCallback {
            when (stack.value.active.instance) {
                is SalesChild.Orders -> return@BackCallback
                else -> salesRepBack()
            }
        })

        stateHolder.salesSpecificHandler.getMe()
        stateHolder.vendorNotificationHandler.notificationReceived()

        val locationJob = scope.launch {
            while (true) {
                try {
                    submitCurrentLocation()
                } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Best-effort background telemetry - never let a GPS/network hiccup kill the hourly loop.
                }
                delay(1.hours)
            }
        }
        lifecycle.doOnDestroy { locationJob.cancel() }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun submitCurrentLocation() {
        if (!requestLocationPermission()) return
        val fix = currentDeviceLocation() ?: return
        locationRepository.submit(
            EmployeeLocationSubmit(
                deviceId = DeviceIdentifier.deviceId(),
                latitude = fix.latitude,
                longitude = fix.longitude,
                accuracy = fix.accuracy,
                altitude = fix.altitude,
                speed = fix.speed,
                heading = fix.heading,
                timestamp = Clock.System.now().toString(),
            )
        ).collect { }
    }

    val stack: Value<ChildStack<*, SalesChild>> =
        childStack(
            source = stateHolder.salesRepNavigation,
            serializer = SalesRepConfig.serializer(),
            initialConfiguration = SalesRepConfig.Orders,
            handleBackButton = true,
            childFactory = ::child
        )

    private fun child(config: SalesRepConfig, componentContext: ComponentContext): SalesChild =
        when (config) {
            is SalesRepConfig.Analytics -> SalesChild.Analytics(
                AnalyticsComponent(
                    componentContext,
                    stateHolder,
                    config.tab,
                    onOpenFilters = { salesRepNavigate(SalesRepConfig.AnalyticsFilters) },
                    onOpenAtRiskCustomers = { salesRepNavigate(SalesRepConfig.AnalyticsAtRisk, true) },
                )
            )

            is SalesRepConfig.AnalyticsProducts -> SalesChild.AnalyticsProducts(
                AnalyticsProductsComponent(componentContext, stateHolder)
            )

            is SalesRepConfig.AnalyticsAtRisk -> SalesChild.AnalyticsAtRisk(
                AnalyticsAtRiskComponent(componentContext, stateHolder)
            )

            is SalesRepConfig.AnalyticsFilters -> SalesChild.AnalyticsFilters(
                AnalyticsFiltersComponent(
                    componentContext,
                    stateHolder,
                    onBack = { salesRepBack() },
                )
            )

            is SalesRepConfig.Orders -> SalesChild.Orders(
                SalesOrdersComponent(componentContext, stateHolder)
            )

            is SalesRepConfig.Customers -> SalesChild.Customers(
                SalesCustomersComponent(componentContext, stateHolder)
            )

            is SalesRepConfig.CustomerMessages -> SalesChild.CustomerMessages(
                SalesCustomerMessagesComponent(componentContext, stateHolder)
            )

            is SalesRepConfig.AdminMessages -> SalesChild.AdminMessages(
                SalesAdminMessagesComponent(componentContext, stateHolder)
            )

            is SalesRepConfig.InternalMessages -> SalesChild.InternalMessages(
                SalesInternalMessagesComponent(componentContext, stateHolder)
            )

            is SalesRepConfig.Operations -> SalesChild.Operations(
                SalesOperationsComponent(componentContext, stateHolder)
            )

            is SalesRepConfig.CustomerDetail -> SalesChild.CustomerDetail(
                SalesCustomerDetailComponent(componentContext, stateHolder, config.customer)
            )

            is SalesRepConfig.OrderDetail -> SalesChild.OrderDetail(
                SalesOrderDetailComponent(componentContext, stateHolder, config.order)
            )

            is SalesRepConfig.DiscountForm -> SalesChild.DiscountForm(
                SalesDiscountFormComponent(
                    componentContext,
                    stateHolder,
                    config.customer,
                    config.existingRule
                )
            )

            is SalesRepConfig.NewCustomer -> SalesChild.NewCustomer(
                SalesNewCustomerComponent(componentContext, stateHolder)
            )

            is SalesRepConfig.AdminConversation -> SalesChild.AdminConversation(
                SalesAdminConversationComponent(componentContext, stateHolder, config.conversation)
            )

            is SalesRepConfig.CustomerConversation -> SalesChild.CustomerConversation(
                SalesCustomerConversationComponent(
                    componentContext,
                    stateHolder,
                    config.conversation
                )
            )

            is SalesRepConfig.AdminNewMessage -> SalesChild.AdminNewMessage(
                SalesAdminNewMessageComponent(componentContext, stateHolder)
            )

            is SalesRepConfig.CustomerNewMessage -> SalesChild.CustomerNewMessage(
                SalesCustomerNewMessageComponent(componentContext, stateHolder, config.initialCustomer)
            )

            is SalesRepConfig.InternalConversation -> SalesChild.InternalConversation(
                SalesInternalConversationComponent(
                    componentContext,
                    stateHolder,
                    config.conversation
                )
            )

            is SalesRepConfig.InternalNewMessage -> SalesChild.InternalNewMessage(
                SalesInternalNewMessageComponent(componentContext, stateHolder)
            )

            is SalesRepConfig.OrderCatalog -> SalesChild.OrderCatalog(
                SalesOrderCatalogComponent(componentContext, stateHolder, config.customer)
            )

            is SalesRepConfig.OrderCart -> SalesChild.OrderCart(
                SalesOrderCartComponent(componentContext, stateHolder, config.customer)
            )

            is SalesRepConfig.OrderReview -> SalesChild.OrderReview(
                SalesOrderReviewComponent(componentContext, stateHolder, config.customer)
            )

            is SalesRepConfig.InviteCustomer -> SalesChild.InviteCustomer(
                SalesInviteCustomerComponent(componentContext, stateHolder, config.email)
            )

            is SalesRepConfig.Notifications -> SalesChild.Notifications(
                SalesNotificationsComponent(componentContext, stateHolder)
            )
        }
}

@Serializable
sealed class SalesRepConfig {
    @Serializable
    data class Analytics(val tab: AnalyticsTab = AnalyticsTab.Overview) : SalesRepConfig()
    @Serializable
    data object AnalyticsProducts : SalesRepConfig()
    @Serializable
    data object AnalyticsAtRisk : SalesRepConfig()
    @Serializable
    data object AnalyticsFilters : SalesRepConfig()
    @Serializable
    data object Orders : SalesRepConfig()
    @Serializable
    data object Customers : SalesRepConfig()
    @Serializable
    data object CustomerMessages : SalesRepConfig()
    @Serializable
    data object AdminMessages : SalesRepConfig()
    @Serializable
    data object InternalMessages : SalesRepConfig()
    @Serializable
    data object Operations : SalesRepConfig()
    @Serializable
    data class CustomerDetail(val customer: OperationalCustomer) : SalesRepConfig()
    @Serializable
    data class OrderDetail(val order: OnBehalfOrder) : SalesRepConfig()
    @Serializable
    data class DiscountForm(
        val customer: OperationalCustomer,
        val existingRule: DiscountRule? = null
    ) : SalesRepConfig()

    @Serializable
    data object NewCustomer : SalesRepConfig()
    @Serializable
    data class AdminConversation(val conversation: ChatConversation) : SalesRepConfig()
    @Serializable
    data class CustomerConversation(val conversation: ChatConversation) : SalesRepConfig()
    @Serializable
    data object AdminNewMessage : SalesRepConfig()
    @Serializable
    data class CustomerNewMessage(val initialCustomer: OperationalCustomer? = null) : SalesRepConfig()
    @Serializable
    data class InternalConversation(val conversation: ChatConversation) : SalesRepConfig()
    @Serializable
    data object InternalNewMessage : SalesRepConfig()
    @Serializable
    data class OrderCatalog(val customer: OperationalCustomer) : SalesRepConfig()
    @Serializable
    data class OrderCart(val customer: OperationalCustomer) : SalesRepConfig()
    @Serializable
    data class OrderReview(val customer: OperationalCustomer) : SalesRepConfig()
    @Serializable
    data class InviteCustomer(val email: String = "") : SalesRepConfig()
    @Serializable
    data object Notifications : SalesRepConfig()
}

sealed class SalesChild {
    data class Analytics(val component: AnalyticsComponent) : SalesChild()
    data class AnalyticsProducts(val component: AnalyticsProductsComponent) : SalesChild()
    data class AnalyticsAtRisk(val component: AnalyticsAtRiskComponent) : SalesChild()
    data class AnalyticsFilters(val component: AnalyticsFiltersComponent) : SalesChild()
    data class Orders(val component: SalesOrdersComponent) : SalesChild()
    data class Customers(val component: SalesCustomersComponent) : SalesChild()
    data class CustomerMessages(val component: SalesCustomerMessagesComponent) : SalesChild()
    data class AdminMessages(val component: SalesAdminMessagesComponent) : SalesChild()
    data class InternalMessages(val component: SalesInternalMessagesComponent) : SalesChild()
    data class Operations(val component: SalesOperationsComponent) : SalesChild()
    data class CustomerDetail(val component: SalesCustomerDetailComponent) : SalesChild()
    data class OrderDetail(val component: SalesOrderDetailComponent) : SalesChild()
    data class DiscountForm(val component: SalesDiscountFormComponent) : SalesChild()
    data class NewCustomer(val component: SalesNewCustomerComponent) : SalesChild()
    data class AdminConversation(val component: SalesAdminConversationComponent) : SalesChild()
    data class CustomerConversation(val component: SalesCustomerConversationComponent) :
        SalesChild()

    data class AdminNewMessage(val component: SalesAdminNewMessageComponent) : SalesChild()
    data class CustomerNewMessage(val component: SalesCustomerNewMessageComponent) : SalesChild()
    data class InternalConversation(val component: SalesInternalConversationComponent) : SalesChild()
    data class InternalNewMessage(val component: SalesInternalNewMessageComponent) : SalesChild()
    data class OrderCatalog(val component: SalesOrderCatalogComponent) : SalesChild()
    data class OrderCart(val component: SalesOrderCartComponent) : SalesChild()
    data class OrderReview(val component: SalesOrderReviewComponent) : SalesChild()
    data class InviteCustomer(val component: SalesInviteCustomerComponent) : SalesChild()
    data class Notifications(val component: SalesNotificationsComponent) : SalesChild()
}
