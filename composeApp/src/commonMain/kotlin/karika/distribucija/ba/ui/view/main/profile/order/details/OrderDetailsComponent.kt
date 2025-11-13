package karika.distribucija.ba.ui.view.main.profile.order.details

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.bringToFront
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.model.Order
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrderDetailsComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    ordersResponse: OrdersResponse
) : CommonComponent(componentContext, stateHolder) {

    private val _order = MutableStateFlow(ordersResponse)
    val order = _order.asStateFlow()

    init {
        if (ordersResponse.createdAt == null) {
            loadOrder()
        }

        iOScope.launch {
            stateHolder.customerSpecificHandler.refreshOrders.collect {
                loadOrder()
            }
        }
    }

    override fun loadNextPage(reset: Boolean) {

    }

    override fun orderAgain(order: OrdersResponse, callback: () -> Unit) {
        super.orderAgain(order) {
            appBack()
            appBack()
        }
    }

    fun attachBill(order: Order?, message: String, file: Pair<String, ByteArray>) {
        iOScope.launch {
            orderRepository.sendBill(
                orderId = order?.orderId ?: return@launch,
                vendorId = order.vendorId.toString(),
                comment = message,
                attachment = file.second,
                filename = file.first
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        showMessage("Predračun je uspješno poslan!")
                        pageSize *= currentPage
                        loadNextPage(true)
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun loadOrder() {
        iOScope.launch {
            orderRepository.order(
                orderId = order.value.incrementId ?: ""
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _order.update { result.data }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    override fun showVendor(vendor: Vendor) {
        if (isGuest()) {
            stateHolder.commonHandler.showLoginRequired("*Potrebna registracija za pristup dobavljačima")
            return
        }
        mainScope.launch {
            stateHolder.appNavigation.bringToFront(AppConfig.VendorDetails(vendor, false))
        }
    }
}