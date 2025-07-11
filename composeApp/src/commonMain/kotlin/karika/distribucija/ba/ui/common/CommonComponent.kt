package karika.distribucija.ba.ui.common

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.api.CartRepository
import karika.distribucija.ba.domain.api.MessagesRepository
import karika.distribucija.ba.domain.api.OrdersRepository
import karika.distribucija.ba.domain.api.UserRepository
import karika.distribucija.ba.domain.model.AddToCart
import karika.distribucija.ba.domain.model.CartItem
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.domain.model.Order
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.view.main.MainConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

open class CommonComponent(
    componentContext: ComponentContext,
    val stateHolder: KarikaStateHolder,
) : LoaderViewComponent(), KoinComponent, ComponentContext by componentContext {
    open val title: String = ""
    val snackbarHostState = stateHolder.hostState
    val mainScope = CoroutineScope(Dispatchers.Main)
    val iOScope = CoroutineScope(Dispatchers.IO)
    private val cartRepository = CartRepository()
    val userRepository = UserRepository()
    val orderRepository = OrdersRepository()
    val messagesRepository = MessagesRepository()

    fun showMessage(message: String?) {
        mainScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message ?: return@launch
            )
        }
    }

    fun logout() {
        stateHolder.logout()
        stateHolder.appNavigation.replaceAll(AppConfig.PreLogin)
    }

    fun showVendor(vendor: Vendor) {
        mainScope.launch {
            stateHolder.appNavigation.pushNew(AppConfig.VendorDetails(vendor))
        }
    }

    fun navigateToProduct(product: Product) {
        mainScope.launch {
            stateHolder.appNavigation.pushNew(AppConfig.ProductDetails(product))
        }
    }

    fun addToCart(product: Product, qty: Int = 1, showSnack: Boolean = true) {
        iOScope.launch {
            cartRepository.addToCart(
                AddToCart(
                    CartItem(
                        sku = product.sku ?: return@launch,
                        qty = qty,
                        quoteId = stateHolder.cartId
                    )
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        if (showSnack) {
                            showMessage("Proizvod dodan u korpu!")
                        }
                        reloadCart()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message ?: "")
                    }
                }
            }
        }
    }

    fun updateCart(product: Product, qty: Int = 1) {
        if (product.itemId == null) {
            addToCart(product, qty)
            return
        }

        iOScope.launch {
            cartRepository.updateCart(
                AddToCart(
                    CartItem(
                        sku = product.sku ?: return@launch,
                        qty = qty,
                        quoteId = stateHolder.cartId,
                        itemId = product.itemId
                    )
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        reloadCart()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message ?: "")
                    }
                }
            }
        }
    }

    fun removeFromCart(product: Product) {
        iOScope.launch {
            cartRepository.removeFromCart(product.itemId?.toString() ?: return@launch)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            showMessage("Proizvod uklonjen iz korpe!")
                            reloadCart()
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message ?: "")
                        }
                    }
                }
        }
    }

    var currentPage: Int = 1
    var hasNextPage = true
    var pageSize = 10

    open fun loadNextPage(reset: Boolean = false) {

    }

    fun appBack() {
        mainScope.launch {
            stateHolder.appNavigation.pop()
        }
    }

    fun mainBack() {
        mainScope.launch {
            stateHolder.mainNavigation.pop()
        }
    }

    fun sendMessageToVendor(product: Product) {

    }

    fun showHome() {
        mainScope.launch {
            stateHolder.mainNavigation.bringToFront(MainConfig.Home)
        }
    }

    fun appNavigate(config: AppConfig) {
        mainScope.launch {
            stateHolder.appNavigation.bringToFront(config)
        }
    }

    fun mainNavigate(config: MainConfig) {
        mainScope.launch {
            stateHolder.mainNavigation.bringToFront(config)
        }
    }

    fun placedOrder(id: String) {
        stateHolder.placedOrder()
        mainScope.launch {
            stateHolder.mainNavigation.bringToFront(MainConfig.CartSuccess(id))
        }
    }

    fun reloadCart() {
        stateHolder.reloadCart()
    }

    fun navigateToComments(it: Order) {
        appNavigate(AppConfig.OrderComments(it))
    }

    open fun cancelOrder(
        orderId: String?,
        vendorId: String?,
        reason: String,
        com: String,
        callback: () -> Unit = {}
    ) {
        iOScope.launch {
            orderRepository.cancel(
                orderId = orderId,
                vendorId = vendorId,
                reason = reason
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        callback.invoke()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun navigateToCart() {
        mainNavigate(MainConfig.Cart)
    }

    open fun orderAgain(
        order: OrdersResponse,
        callback: () -> Unit = {
            appBack()
        }
    ) {
        iOScope.launch {
            showLoader()
            order.orders.flatMap { it.products }.forEach {
                addToCart(
                    product = Product(sku = it.sku),
                    it.qty?.toInt() ?: 1,
                    false
                )
            }
            hideLoader()
            callback()
            navigateToCart()
        }
    }

    fun navigateToMessagesOverview(item: Conversation) {
        appNavigate(AppConfig.MessagesOverview(item))
    }
}