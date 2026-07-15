package karika.distribucija.ba.ui.common

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.chatImage
import karika.distribucija.ba.domain.api.AnalyticsRepository
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
import karika.distribucija.ba.domain.model.EventType
import karika.distribucija.ba.domain.model.Filters
import karika.distribucija.ba.domain.model.KarikaTracking
import karika.distribucija.ba.domain.model.Order
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.domain.model.RefType
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.TrackingPayload
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.state.ImagePreviewState
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.distributer.dashboard.DashConfig
import karika.distribucija.ba.ui.view.salesrep.dashboard.SalesRepConfig
import karika.distribucija.ba.ui.view.shop.MainConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

open class CommonComponent(
    componentContext: ComponentContext,
    val stateHolder: KarikaStateHolder,
) : KoinComponent, ComponentContext by componentContext {
    @OptIn(ExperimentalUuidApi::class)
    private val sessionId = Uuid.random().toString()
    open val title: String = ""
    val snackbarHostState = stateHolder.hostState
    val scope = CoroutineScope(Dispatchers.Main)
    val iOScope = CoroutineScope(Dispatchers.Default)
    private val cartRepository = CartRepository()
    val userRepository = UserRepository()
    val orderRepository = OrdersRepository()
    val messagesRepository = MessagesRepository()
    val vendorRepository = VendorRepository()
    val productRepository = ProductRepository()
    private val notificationRepository = NotificationRepository()
    private val analyticsRepository = AnalyticsRepository()

    val _promotedVendors = MutableStateFlow<List<PromotedVendor>>(emptyList())
    val promotedVendors = _promotedVendors.asStateFlow()
    private val _promotedLogos = MutableStateFlow<List<PromotedVendor>>(emptyList())
    val promotedLogos = _promotedLogos.asStateFlow()
    val loader = stateHolder.loaderHandler.loader

    fun showMessage(message: String?) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message ?: return@launch,
                actionLabel = "SUCCESS"
            )
        }
    }

    fun showErrorMessage(message: String?) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message ?: return@launch,
                actionLabel = "ERROR"
            )
        }
    }

    fun showWarningMessage(message: String?) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message ?: return@launch,
                actionLabel = "WARNING"
            )
        }
    }

    fun logout() {
        removePushHandle()
        stateHolder.sessionHandler.logout()
        HttpClientProvider.token = getEnvJwt()
        stateHolder.logout()
    }

    fun deleteUser() {
        removePushHandle()
        stateHolder.sessionHandler.delete()
        HttpClientProvider.token = getEnvJwt()
        stateHolder.logout()
    }

    open fun showVendor(vendor: Vendor) {
        if (isGuest()) {
            stateHolder.commonHandler.showLoginRequired("*Potrebna registracija za pristup dobavljačima")
            return
        }
        scope.launch {
            stateHolder.mainNavigation.bringToFront(MainConfig.VendorDetails(vendor))
        }
    }

    open fun navigateToProduct(product: Product) {
        scope.launch {
            stateHolder.mainNavigation.bringToFront(MainConfig.ProductDetails(product))
        }
    }

    fun addToCart(product: Product, qty: Int = 1, showSnack: Boolean = true) {
        if (isGuest()) {
            stateHolder.commonHandler.showLoginRequired("*Potrebna registracija za dodavanje u korpu")
            return
        }
        logEvent(
            eventType = EventType.ADD_TO_CART,
            refType = RefType.CART_PANEL,
            product = product,
            qty = qty
        )
        scope.launch {
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
                        if (result.message == "Current customer does not have an active cart.") {
                            stateHolder.cartHandler.createCart {
                                addToCart(product, qty, showSnack)
                            }
                            return@collect
                        }

                        if (showSnack) {
                            showMessage(result.message ?: "")
                        }
                    }
                }
            }
        }
    }

    fun addToCartWithPut(product: Product, qty: Int = 1, showSnack: Boolean = true) {
        if (isGuest()) {
            stateHolder.commonHandler.showLoginRequired("*Potrebna registracija za dodavanje u korpu")
            return
        }

        val cartItem = stateHolder.cartHandler.cart.value.items
            .values.flatMap { it }
            .find { it.first.sku == product.sku }

        scope.launch {
            if (cartItem != null) {
                cartRepository.updateCart(
                    AddToCart(
                        CartItem(
                            sku = product.sku ?: return@launch,
                            qty = cartItem.second + qty,
                            quoteId = stateHolder.cartHandler.cartId,
                            itemId = cartItem.first.itemId
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

                            if (result.message == "Current customer does not have an active cart.") {
                                stateHolder.cartHandler.createCart {
                                    addToCartWithPut(product, qty, showSnack)
                                }
                                return@collect
                            }

                            if (showSnack) {
                                showMessage(result.message ?: "")
                                reloadCart()
                            }
                        }
                    }
                }
                return@launch
            }

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
                        if (result.message == "Current customer does not have an active cart.") {
                            stateHolder.cartHandler.createCart {
                                addToCartWithPut(product, qty, showSnack)
                            }
                            return@collect
                        }

                        showMessage(result.message ?: "")
                    }
                }
            }
        }
    }

    fun updateCart(product: Product, qty: Int = 1, errorCallback: () -> Unit = {}) {
        if (isGuest()) {
            stateHolder.commonHandler.showLoginRequired("*Potrebna registracija za dodavanje u korpu")
            return
        }
        if (product.itemId == null) {
            addToCart(product, qty)
            return
        }

        scope.launch {
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
                        hideLoader()
                        reloadCart()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        if (result.message == "Current customer does not have an active cart.") {
                            stateHolder.cartHandler.createCart {
                                showMessage("Postojeca korpa je završena na drugom uredjaju!")
                                reloadCart()
                                errorCallback.invoke()
                            }
                        } else {
                            showMessage(result.message ?: "")
                            reloadCart()
                            errorCallback.invoke()
                        }
                    }
                }
            }
        }
    }

    fun removeFromCart(product: Product) {
        logEvent(
            eventType = EventType.REMOVE_FROM_CART,
            refType = RefType.CART_PANEL,
            product = product
        )
        scope.launch {
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
        scope.launch {
            stateHolder.preLoginNavigation.pop()
        }
    }

    fun appBack() {
        scope.launch {
            stateHolder.appNavigation.pop()
        }
    }

    fun mainBack() {
        scope.launch {
            stateHolder.mainNavigation.pop()
        }
    }

    fun sendMessageToVendor(product: Product) {
        if (isGuest()) {
            stateHolder.commonHandler.showLoginRequired("*Potrebna registracija za dodavanje u korpu")
            return
        }
        navigateToMessagesOverview(
            Conversation(
                vendorId = product.vendorId,
                receiverName = product.vendorName,
                subject = product.name
            )
        )
    }

    fun showHome() {
        scope.launch {
            stateHolder.mainNavigation.bringToFront(MainConfig.Home)
        }
    }

    fun appNavigate(config: AppConfig) {
        scope.launch {
            if (config is AppConfig.PreLogin) {
                stateHolder.appNavigation.replaceAll(config)
            } else {
                stateHolder.appNavigation.bringToFront(config)
            }
        }
    }

    fun mainNavigate(config: MainConfig) {
        scope.launch {
            stateHolder.mainNavigation.bringToFront(config)
        }
    }

    fun placedOrder(id: String) {
        stateHolder.cartHandler.createCart()
        scope.launch {
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
        scope.launch {
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
        showLoader()
        stateHolder.cartHandler.clearCart {
            stateHolder.cartHandler.createCart {
                scope.launch {
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
        }
    }

    open fun navigateToMessagesOverview(item: Conversation) {
        appNavigate(AppConfig.MessagesOverview(item))
    }

    fun savePushHandle() {
        stateHolder.handler.getPushHandle { fId, token ->
            println("TEST_TEST: FCM_TOKEN: $token")
            scope.launch {
                notificationRepository
                    .savePushHandle(token, fId)
                    .collect()
            }
        }

        logEvent(
            eventType = EventType.USER_LOGIN,
            refType = RefType.USER_LOGIN,
            product = Product(name = "FCM Token", sku = "", id = 0),
            qty = 0
        )
    }

    private fun removePushHandle() {
        stateHolder.handler.getPushHandle { fId, _ ->
            scope.launch {
                notificationRepository
                    .savePushHandle(null, fId)
                    .collect()
            }
        }

        logEvent(
            eventType = EventType.LOGOUT,
            refType = RefType.LOGOUT,
            product = Product(name = "FCM Token", sku = "", id = 0),
            qty = 0
        )
    }

    fun showImagePreview(imageUrl: Any?) {
        stateHolder.imagePreview.value = imageUrl?.let { ImagePreviewState(listOf(it)) }
    }

    fun showImagesPreview(images: List<Any?>, startIndex: Int = 0) {
        stateHolder.imagePreview.value = if (images.isEmpty()) null else ImagePreviewState(images, startIndex)
    }

    fun dashNavigate(config: DashConfig, replace: Boolean = false) {
        if (replace) {
            stateHolder.dashNavigation.replaceAll(config)
            return
        }
        stateHolder.dashNavigation.bringToFront(config)
    }

    fun dashBack() {
        stateHolder.dashNavigation.pop()
    }

    fun salesRepNavigate(config: SalesRepConfig, replace: Boolean = false) {
        if (replace) {
            stateHolder.salesRepNavigation.replaceAll(config)
            return
        }
        stateHolder.salesRepNavigation.bringToFront(config)
    }

    fun salesRepPush(config: SalesRepConfig) {
        stateHolder.salesRepNavigation.push(config)
    }

    fun salesRepBack() {
        stateHolder.salesRepNavigation.pop()
    }

    fun showLoader() {
        stateHolder.loaderHandler.showLoader()
    }

    fun hideLoader() {
        stateHolder.loaderHandler.hideLoader()
    }

    open fun loadBanners() {
        iOScope.launch {
            productRepository.promotedVendors().collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        showLoader()
                    }

                    is ResultState.Success -> {
                        hideLoader()
                        _promotedVendors.update {
                            result.data
                                .filter { f -> f.promoteVendorBanner }
                                .filter { f -> f.companyBanner != null }
                        }
                        _promotedLogos.update {
                            result.data
                                .filter { f -> f.promoteVendorLogo }
                                .filter { f -> f.companyLogo != null }
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

    fun getUnit(unit: String): String {
        return stateHolder.commonHandler.getUnit(unit)
    }

    fun isGuest() = HttpClientProvider.token == getEnvJwt()

    fun logEvent(
        eventType: EventType,
        refType: RefType,
        product: Product? = null,
        qty: Int? = null
    ) {
        iOScope.launch {
            analyticsRepository.post(
                KarikaTracking(
                    platform = "mobile",
                    sessionId = sessionId,
                    userType = stateHolder.sessionHandler.userType().value,
                    eventType = eventType.value,
                    payload = TrackingPayload(
                        ref = refType.value,
                        product = product?.name,
                        sku = product?.sku,
                        qty = qty
                    ),
                    fullUrl = when (eventType) {
                        EventType.USER_LOGIN -> "karika-mobile://login"
                        EventType.LOGOUT -> "karika-mobile://logout"
                        else -> "karika-mobile://product/${product?.id}"
                    },
                    url = when (eventType) {
                        EventType.USER_LOGIN -> "karika-mobile://login"
                        EventType.LOGOUT -> "karika-mobile://logout"
                        else -> "karika-mobile://product/${product?.id}"
                    },
                    userName = stateHolder.customerSpecificHandler.userDetails.value.companyNameNullable(),
                    userEmail = stateHolder.customerSpecificHandler.userDetails.value.email
                )
            ).collect()
        }
    }

    fun logSearchEvent(
        eventType: EventType,
        refType: RefType,
        query: String,
        results: String
    ) {
        iOScope.launch {
            analyticsRepository.post(
                KarikaTracking(
                    platform = "mobile",
                    sessionId = sessionId,
                    userType = stateHolder.sessionHandler.userType().value,
                    eventType = eventType.value,
                    payload = when (eventType) {
                        EventType.USER_LOGIN, EventType.LOGOUT -> null
                        else -> TrackingPayload(
                            ref = refType.value,
                            query = query,
                            results = results
                        )
                    },
                    fullUrl = when (eventType) {
                        else -> "karika-mobile://search?query=${query}"
                    },
                    url = when (eventType) {
                        else -> "karika-mobile://search?query=${query}"
                    },
                    userName = stateHolder.customerSpecificHandler.userDetails.value.companyNameNullable(),
                    userEmail = stateHolder.customerSpecificHandler.userDetails.value.email
                )
            ).collect()
        }
    }

    fun logProductFilterEvent(
        filters: Filters,
        sort: String,
        categoryIds: String
    ) {
        iOScope.launch {
            analyticsRepository.post(
                KarikaTracking(
                    platform = "mobile",
                    sessionId = sessionId,
                    userType = stateHolder.sessionHandler.userType().value,
                    eventType = EventType.PRODUCT_FILTER.value,
                    payload = TrackingPayload(
                        ref = RefType.VENDOR_PRODUCTS_PAGE.value,
                        filters = filters,
                        sort = sort,
                        categoryIds = categoryIds
                    ),
                    fullUrl = "karika-mobile://products?query=${Json.encodeToString(filters)}?sort=$sort",
                    url = "karika-mobile://products?query=${Json.encodeToString(filters)}?sort=$sort",
                    userName = stateHolder.customerSpecificHandler.userDetails.value.companyNameNullable(),
                    userEmail = stateHolder.customerSpecificHandler.userDetails.value.email
                )
            ).collect()
        }
    }

    fun markAsReadMessage(threadId: String?, admin: Boolean) {
        scope.launch {
            messagesRepository.markAsRead(threadId)
                .collect {
                    if (it is ResultState.Success) {
                        if (admin) {
                            stateHolder.messageHandler.reloadAdminMessages()
                        } else {
                            stateHolder.messageHandler.reloadVendorMessages()
                        }
                        stateHolder.customerNotificationHandler.reloadMessageCount()
                    }
                }
        }
    }

    fun markAsReadMessageVendor(threadId: String?, admin: Boolean) {
        scope.launch {
            messagesRepository.markAsRead(threadId)
                .collect {
                    if (it is ResultState.Success) {
                        if (admin) {
                            stateHolder.messageHandler.reloadAdminMessages()
                        } else {
                            stateHolder.messageHandler.reloadVendorMessages()
                        }
                        stateHolder.vendorNotificationHandler.reloadMessageCount()
                    }
                }
        }
    }

    fun downloadReceipt(it: String) {
        openPdf(chatImage("/$it"))
    }
}