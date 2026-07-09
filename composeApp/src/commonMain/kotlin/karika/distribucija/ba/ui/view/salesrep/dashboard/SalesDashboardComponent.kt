package karika.distribucija.ba.ui.view.salesrep.dashboard

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.salesrep.customers.SalesCustomersComponent
import karika.distribucija.ba.ui.view.salesrep.customers.SalesNewCustomerComponent
import karika.distribucija.ba.ui.view.salesrep.customers.detail.SalesCustomerDetailComponent
import karika.distribucija.ba.ui.view.salesrep.messages.admin.SalesAdminConversationComponent
import karika.distribucija.ba.ui.view.salesrep.messages.admin.SalesAdminMessagesComponent
import karika.distribucija.ba.ui.view.salesrep.messages.customer.SalesCustomerConversationComponent
import karika.distribucija.ba.ui.view.salesrep.messages.customer.SalesCustomerMessagesComponent
import karika.distribucija.ba.ui.view.salesrep.messages.internal.SalesInternalMessagesComponent
import karika.distribucija.ba.ui.view.salesrep.operations.SalesOperationsComponent
import karika.distribucija.ba.ui.view.salesrep.orders.SalesOrdersComponent
import karika.distribucija.ba.ui.view.salesrep.orders.detail.SalesOrderDetailComponent
import karika.distribucija.ba.ui.view.salesrep.customers.detail.SalesDiscountFormComponent
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.domain.model.DiscountRule
import karika.distribucija.ba.domain.model.OnBehalfOrder
import karika.distribucija.ba.domain.model.OperationalCustomer
import kotlinx.serialization.Serializable

class SalesDashboardComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {

    init {
        backHandler.register(BackCallback {
            when (stack.value.active.instance) {
                is SalesChild.Orders -> return@BackCallback
                else -> salesRepBack()
            }
        })
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
                SalesDiscountFormComponent(componentContext, stateHolder, config.customer, config.existingRule)
            )
            is SalesRepConfig.NewCustomer -> SalesChild.NewCustomer(
                SalesNewCustomerComponent(componentContext, stateHolder)
            )
            is SalesRepConfig.AdminConversation -> SalesChild.AdminConversation(
                SalesAdminConversationComponent(componentContext, stateHolder, config.conversation)
            )
            is SalesRepConfig.CustomerConversation -> SalesChild.CustomerConversation(
                SalesCustomerConversationComponent(componentContext, stateHolder, config.conversation)
            )
        }
}

@Serializable
sealed class SalesRepConfig {
    @Serializable data object Orders : SalesRepConfig()
    @Serializable data object Customers : SalesRepConfig()
    @Serializable data object CustomerMessages : SalesRepConfig()
    @Serializable data object AdminMessages : SalesRepConfig()
    @Serializable data object InternalMessages : SalesRepConfig()
    @Serializable data object Operations : SalesRepConfig()
    @Serializable data class CustomerDetail(val customer: OperationalCustomer) : SalesRepConfig()
    @Serializable data class OrderDetail(val order: OnBehalfOrder) : SalesRepConfig()
    @Serializable data class DiscountForm(val customer: OperationalCustomer, val existingRule: DiscountRule? = null) : SalesRepConfig()
    @Serializable data object NewCustomer : SalesRepConfig()
    @Serializable data class AdminConversation(val conversation: Conversation) : SalesRepConfig()
    @Serializable data class CustomerConversation(val conversation: Conversation) : SalesRepConfig()
}

sealed class SalesChild {
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
    data class CustomerConversation(val component: SalesCustomerConversationComponent) : SalesChild()
}
