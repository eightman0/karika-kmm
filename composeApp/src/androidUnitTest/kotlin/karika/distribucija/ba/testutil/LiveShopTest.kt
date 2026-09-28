package karika.distribucija.ba.testutil

import android.os.Looper
import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.di.PersistenceManager
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.api.CartRepository
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.model.Cart
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.getEnvJwt
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.shop.MainConfig
import karika.distribucija.ba.util.KarikaConfig
import java.time.Duration
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.After
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
        stateHolder = KarikaStateHolder(FakeKarikaHandler())
        subscriptions += stateHolder.mainNavigation.subscribe { event ->
            openedInShop = event.transformer(listOf(MainConfig.Home)).last()
        }
        subscriptions += stateHolder.appNavigation.subscribe { event ->
            openedInApp = event.transformer(listOf(AppConfig.Main)).last()
        }
        LiveTestAccounts.logInCustomer()
    }

    @After
    fun tearDownLiveShop() {
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

    private companion object {
        const val SERVER_TIMEOUT_MS = 30_000L
        const val POLL_MS = 50L
    }
}
