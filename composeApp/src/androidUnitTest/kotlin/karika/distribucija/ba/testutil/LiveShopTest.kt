package karika.distribucija.ba.testutil

import android.os.Looper
import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import java.time.Duration
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.di.PersistenceManager
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.api.CartRepository
import karika.distribucija.ba.domain.api.OrdersRepository
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.model.Cart
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.getEnvJwt
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.shop.MainConfig
import karika.distribucija.ba.ui.view.shop.cart.DefaultCartComponent
import karika.distribucija.ba.ui.view.shop.cart.nextstep.DefaultShippingDetailsComponent
import karika.distribucija.ba.ui.view.shop.cart.orderValid
import karika.distribucija.ba.util.KarikaConfig
import kotlin.math.ceil
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.Shadows.shadowOf

/**
 * Base for the screens after login, run against the real backend of the flavor under test
 * (uat -> test.karika.ba) as the customer test account (see [LiveTestAccounts]). Records
 * where the screen navigates to, and puts the test account's cart back as it was.
 */
abstract class LiveShopTest : KarikaUiTest() {

    protected val persistence = InMemoryPersistenceManager()
    protected val handler = FakeKarikaHandler()
    protected lateinit var stateHolder: KarikaStateHolder
    private val subscriptions = mutableListOf<Cancellation>()

    /** The shop screen (main navigation) the screen under test asked for, once it does. */
    protected var openedInShop: MainConfig? = null

    /** The app-level screen (app navigation) the screen under test asked for, once it does. */
    protected var openedInApp: AppConfig? = null

    /** Cart items a test added, removed again after it. */
    protected val cartItemsToRemove = mutableListOf<Int>()

    @Before
    fun setUpLiveShop() {
        startKoin {
            modules(module { single<PersistenceManager> { persistence } })
        }
        stateHolder = KarikaStateHolder(handler)
        subscriptions += stateHolder.mainNavigation.subscribe { event ->
            event.transformer(listOf(MainConfig.Home)).lastOrNull()?.let { openedInShop = it }
        }
        subscriptions += stateHolder.appNavigation.subscribe { event ->
            event.transformer(listOf(AppConfig.Main)).lastOrNull()?.let { openedInApp = it }
        }
        LiveTestAccounts.logInCustomer()
    }

    @After
    fun tearDownLiveShop() {
        // A test that failed after placing its order still takes it back
        cancelTestOrder()
        cartItemsToRemove.forEach { itemId ->
            runBlocking { CartRepository().removeFromCart(itemId.toString()).last() }
        }
        subscriptions.forEach { it.cancel() }
        HttpClientProvider.token = getEnvJwt()
        stopKoin()
    }

    protected fun componentContext(): ComponentContext {
        val lifecycle = LifecycleRegistry()
        lifecycle.resume()
        return DefaultComponentContext(lifecycle)
    }

    protected fun snackbarMessage() = stateHolder.hostState.currentSnackbarData?.visuals?.message

    /** Drops whatever the screen reported so far, so the next message is the one under test. */
    protected fun dismissSnackbar() {
        stateHolder.hostState.currentSnackbarData?.dismiss()
        compose.waitForIdle()
    }

    /**
     * Waits for [condition] while the real requests run. Robolectric's main looper keeps its
     * own clock, so it is moved along too: the app's delays on Dispatchers.Main (hiding the
     * loader after at least 700 ms, for one) would otherwise never finish.
     */
    protected fun waitForServer(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + SERVER_TIMEOUT_MS
        while (!condition()) {
            check(System.currentTimeMillis() < deadline) {
                "the server did not answer within ${SERVER_TIMEOUT_MS / 1000} s"
            }
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(POLL_MS))
            compose.waitForIdle()
            Thread.sleep(POLL_MS)
        }
    }

    /**
     * Waits until the screen's loading has come and gone, for lists that may be empty. The
     * loader stays on for at least 700 ms of looper time, so the polling cannot miss it.
     */
    protected fun waitForLoaded() {
        var loading = false
        waitForServer {
            loading = loading || stateHolder.loaderHandler.loader.value
            loading && !stateHolder.loaderHandler.loader.value
        }
    }

    /** The "Karika preporučuje" products, the same list the home screen shows. */
    protected fun recommendedProducts(): List<Product> {
        val result = runBlocking {
            ProductRepository().searchProductsByCategory(
                categoryId = "${KarikaConfig.getKarikaProductsId()}",
                currentPage = 1,
                pageSize = 12
            ).last()
        }
        val products = assertIs<ResultState.Success<*>>(result, "no recommended products: $result").data
        @Suppress("UNCHECKED_CAST")
        return (products as List<Product>).also {
            assertTrue(it.isNotEmpty(), "the recommended category is empty")
        }
    }

    /** Loads the customer's profile into the state holder, as the app does after login. */
    protected fun loadCustomer() {
        stateHolder.customerSpecificHandler.getUserDetails()
        waitForServer { stateHolder.customerSpecificHandler.userDetails.value.id != null }
    }

    /** Makes sure the customer has a cart and gives the state holder its id, as login does. */
    protected fun useCustomerCart() {
        // POST carts/mine returns the active cart, creating it only if there is none
        val result = runBlocking { CartRepository().createCart().last() }
        stateHolder.cartHandler.cartId =
            assertIs<ResultState.Success<*>>(result, "no cart for the customer: $result").data as String
    }

    protected fun currentCart(): Cart {
        val result = runBlocking { CartRepository().getCart().last() }
        return assertIs<ResultState.Success<*>>(result, "could not read the cart: $result").data as Cart
    }


    /** An order a test placed, as order id to vendor id; cancelled after the test if still open. */
    protected var testOrder: Pair<String, String?>? = null

    /** Cancels [testOrder] on the backend, and forgets it if that worked. */
    protected fun cancelTestOrder(): ResultState<Boolean>? {
        val (orderId, vendorId) = testOrder ?: return null
        val result = runBlocking { OrdersRepository().cancel(orderId, vendorId, CANCEL_REASON).last() }
        if (result is ResultState.Success) {
            testOrder = null
        }
        return result
    }

    /**
     * Fills the empty cart with one recommended product, in steps of its minimum quantity until
     * the vendor's minimum order is reached, and returns it; skips products whose stock or a
     * sensible quantity does not reach the minimum.
     */
    protected fun fillOrderableCart(): Product {
        val cartComponent = DefaultCartComponent(componentContext(), stateHolder)
        for (product in recommendedProducts().filter { it.hasOnStock() }) {
            cartComponent.addToCart(product, product.minQty(), showSnack = false)
            waitForServer { currentCart().items.any { it.sku == product.sku } }
            val itemId = currentCart().items.first { it.sku == product.sku }.itemId
            stateHolder.cartHandler.reloadCart()
            waitForServer { cartHolds(product) }

            val vendor = stateHolder.cartHandler.cart.value.items.keys.single()
            val minimum = vendor.minOrderAmount()?.toDoubleOrNull() ?: 0.0
            val stepPrice = product.currentPrice() * 1.17 * product.minQty()
            val steps = if (stepPrice > 0) ceil(minimum / stepPrice).toInt().coerceAtLeast(1) else 1
            val qty = steps * product.minQty()
            val stock = product.stockData?.salableQty ?: Long.MAX_VALUE

            if (steps <= MAX_STEPS && qty <= stock) {
                if (qty > product.minQty()) {
                    val inCart = stateHolder.cartHandler.cart.value.items.values.flatten()
                        .first { it.first.sku == product.sku }.first
                    cartComponent.updateCart(inCart, qty)
                    waitForServer { currentCart().items.any { it.sku == product.sku && it.qty == qty } }
                    stateHolder.cartHandler.reloadCart()
                    waitForServer {
                        stateHolder.cartHandler.cart.value.items.values.flatten()
                            .any { it.first.sku == product.sku && it.second == qty }
                    }
                }
                if (stateHolder.cartHandler.cart.value.items.orderValid()) {
                    return product
                }
            }
            itemId?.let { runBlocking { CartRepository().removeFromCart(it.toString()).last() } }
            stateHolder.cartHandler.reloadCart()
            waitForServer { stateHolder.cartHandler.cart.value.items.isEmpty() }
        }
        assumeTrue("no recommended product can reach its vendor's minimum order", false)
        error("unreachable")
    }

    protected fun cartHolds(product: Product) =
        stateHolder.cartHandler.cart.value.items.values.flatten().any { it.first.sku == product.sku }

    /**
     * Places an order for one product as the customer, the way "Završi narudžbu" does but
     * without the screen, and returns it; [tearDownLiveShop] cancels it. Only for an account
     * whose cart is empty, as an order takes the whole cart.
     */
    protected fun placeTestOrder(): Product {
        useCustomerCart()
        assumeTrue("the test account's cart is not empty", currentCart().items.isEmpty())
        loadCustomer()
        val product = fillOrderableCart()
        val shipping = DefaultShippingDetailsComponent(componentContext(), stateHolder)
        assumeTrue("the test account has no shipping address", shipping.addresses.value.isNotEmpty())
        if (shipping.addresses.value.none { it.id?.toString() == shipping.selectedAddress.value }) {
            shipping.selectedAddress.value = shipping.addresses.value.first().id.toString()
        }
        shipping.vendorNote.value = TEST_ORDER_NOTE
        openedInShop = null
        shipping.handleShippingAddress()
        waitForServer { openedInShop is MainConfig.CartSuccess || snackbarMessage() != null }
        val success = assertIs<MainConfig.CartSuccess>(openedInShop, "no test order: ${snackbarMessage()}")
        testOrder = success.orderId.trim('"') to product.vendorId()
        openedInShop = null
        return product
    }

    private companion object {
        const val SERVER_TIMEOUT_MS = 30_000L
        const val POLL_MS = 50L

        /** More than this many minimum quantities for one vendor minimum is not a sensible test order. */
        const val MAX_STEPS = 20
        const val TEST_ORDER_NOTE = "Automatski UI test, narudzba se odmah otkazuje"
        const val CANCEL_REASON = "Automatski UI test"
    }
}
