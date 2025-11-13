package karika.distribucija.ba.ui.view.main.profile.order

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.bringToFront
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.api.OrdersRepository
import karika.distribucija.ba.domain.model.Order
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.main.MainConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrdersComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {
    private val repository = OrdersRepository()

    private val _orders = MutableStateFlow<Set<OrdersResponse>>(emptySet())
    val orders = _orders.asStateFlow()
    var status = ""
    var sortDirection = "DESC"
    private val _shouldScrollToTop = MutableStateFlow(false)
    val shouldScrollToTop: StateFlow<Boolean> = _shouldScrollToTop

    init {
        iOScope.launch {
            stateHolder.customerSpecificHandler.refreshOrders.collect {
                loadNextPage(true)
            }
        }
    }

    fun scrollHandled() {
        _shouldScrollToTop.value = false
    }

    override fun loadNextPage(reset: Boolean) {
        if (reset) {
            hasNextPage = true
            currentPage = 1
        }

        if (!hasNextPage) {
            return
        }

        iOScope.launch {
            repository.orders(
                currentPage = currentPage,
                pageSize = pageSize,
                filterValue = status,
                sortBy = "created_at",
                sortDirection = sortDirection
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _orders.update {
                            if (reset) {
                                result.data.toSet()
                            } else {
                                it.plus(result.data.toSet())
                            }
                        }
                        hasNextPage = result.data.size == pageSize
                        currentPage++
                        if (reset) {
                            _shouldScrollToTop.value = true
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

    fun navigateDetails(order: OrdersResponse) {
        appNavigate(AppConfig.OrderDetails(order))
    }

    override fun cancelOrder(
        orderId: String?,
        vendorId: String?,
        reason: String,
        com: String,
        callback: () -> Unit
    ) {
        super.cancelOrder(orderId, vendorId, reason, com) {
            loadNextPage(true)
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