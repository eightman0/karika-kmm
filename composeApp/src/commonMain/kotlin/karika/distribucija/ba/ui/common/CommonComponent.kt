package karika.distribucija.ba.ui.common

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceAll
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.api.CartRepository
import karika.distribucija.ba.domain.api.MessagesRepository
import karika.distribucija.ba.domain.api.NotificationRepository
import karika.distribucija.ba.domain.api.OrdersRepository
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.api.UserRepository
import karika.distribucija.ba.domain.api.VendorRepository
import karika.distribucija.ba.domain.model.AddToCart
import karika.distribucija.ba.domain.model.CartItem
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.domain.model.Order
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.distributer.dashboard.DashConfig
import karika.distribucija.ba.ui.view.main.MainConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

open class CommonComponent(
    componentContext: ComponentContext,
    val stateHolder: KarikaStateHolder,
) : KoinComponent, ComponentContext by componentContext {
    open val title: String = ""
    val snackbarHostState = stateHolder.hostState
    val mainScope = CoroutineScope(Dispatchers.Main)
    val iOScope = CoroutineScope(Dispatchers.IO)
    private val cartRepository = CartRepository()
    val userRepository = UserRepository()
    val orderRepository = OrdersRepository()
    val messagesRepository = MessagesRepository()
    val vendorRepository = VendorRepository()
    val productRepository = ProductRepository()
    private val notificationRepository = NotificationRepository()

    val _promotedVendors = MutableStateFlow<List<PromotedVendor>>(emptyList())
    val promotedVendors = _promotedVendors.asStateFlow()
    val _promotedLogos = MutableStateFlow<List<PromotedVendor>>(emptyList())
    val promotedLogos = _promotedLogos.asStateFlow()
    val loader = stateHolder.loaderHandler.loader

    fun showMessage(message: String?) {
        mainScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message ?: return@launch
            )
        }
    }

    fun logout() {
        removePushHandle()
        stateHolder.sessionHandler.logout()
        HttpClientProvider.token = null
        stateHolder.logout()
    }

    open fun showVendor(vendor: Vendor) {
        mainScope.launch {
            stateHolder.mainNavigation.bringToFront(MainConfig.VendorDetails(vendor))
        }
    }

    fun navigateToProduct(product: Product) {
        mainScope.launch {
            stateHolder.mainNavigation.bringToFront(MainConfig.ProductDetails(product))
        }
    }

    fun addToCart(product: Product, qty: Int = 1, showSnack: Boolean = true) {
        iOScope.launch {
            cartRepository.addToCart(
                AddToCart(
                    CartItem(
                        sku = product.sku ?: return@launch,
                        qty = qty,
                        quoteId = stateHolder.cartHandler.cartId
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

    fun addToCartWithPut(product: Product, qty: Int = 1, showSnack: Boolean = true) {
        iOScope.launch {
            cartRepository.addToCart(
                AddToCart(
                    CartItem(
                        sku = product.sku ?: return@launch,
                        qty = 1,
                        quoteId = stateHolder.cartHandler.cartId
                    )
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        updateCart(product.copy(itemId = result.data.itemId), qty) {
                            if (showSnack) {
                                showMessage("Proizvod dodan u korpu!")
                            }
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

    fun updateCart(product: Product, qty: Int = 1, successCallback: () -> Unit = {}) {
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
                        quoteId = stateHolder.cartHandler.cartId,
                        itemId = product.itemId
                    )
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        successCallback.invoke()
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
    var pageSize = 30

    open fun loadNextPage(reset: Boolean = false) {

    }

    fun preLoginBack() {
        mainScope.launch {
            stateHolder.preLoginNavigation.pop()
        }
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
        navigateToMessagesOverview(
            Conversation(
                vendorId = product.vendorId,
                receiverName = product.vendorName
            )
        )
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
        stateHolder.cartHandler.placedOrder()
        mainScope.launch {
            stateHolder.mainNavigation.bringToFront(MainConfig.CartSuccess(id))
        }
    }

    fun reloadCart() {
        stateHolder.cartHandler.reloadCart()
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

    open fun navigateToMessagesOverview(item: Conversation) {
        appNavigate(AppConfig.MessagesOverview(item))
    }

    fun savePushHandle() {
        stateHolder.filePicker.getPushHandle { fId, token ->
            println("TEST_TEST: FCM_TOKEN: $token")
            iOScope.launch {
                notificationRepository
                    .savePushHandle(token, fId)
                    .collect()
            }
        }
    }

    private fun removePushHandle() {
        stateHolder.filePicker.getPushHandle { fId, _ ->
            iOScope.launch {
                notificationRepository
                    .savePushHandle(null, fId)
                    .collect()
            }
        }
    }

    fun showImagePreview(imageUrl: String) {
        stateHolder.imagePreview.value = imageUrl
    }

    fun dashNavigate(config: DashConfig, replace: Boolean = false) {
        //if (replace) {
        //    stateHolder.dashNavigation.replaceAll(config)
        //    return
        //}
        stateHolder.dashNavigation.bringToFront(config)
    }

    fun dashBack() {
        stateHolder.dashNavigation.pop()
    }

    fun showLoader() {
        stateHolder.loaderHandler.showLoader()
    }

    fun hideLoader() {
        stateHolder.loaderHandler.hideLoader()
    }
}