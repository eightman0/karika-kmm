package karika.distribucija.ba.ui.view.shop.home

import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import karika.distribucija.ba.di.PersistenceManager
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.api.CartRepository
import karika.distribucija.ba.domain.model.Cart
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.testutil.FakeKarikaHandler
import karika.distribucija.ba.testutil.InMemoryPersistenceManager
import karika.distribucija.ba.testutil.KarikaUiTest
import karika.distribucija.ba.testutil.LiveTestAccounts
import karika.distribucija.ba.ui.common.getEnvJwt
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.shop.MainConfig
import karika.distribucija.ba.util.KarikaConfig
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

/**
 * The home screen of a logged-in customer against the real backend of the flavor under test
 * (uat -> test.karika.ba), through the real HomeView and DefaultHomeComponent. Needs the
 * customer test account, see [LiveTestAccounts].
 */
class LiveHomeApiTest : KarikaUiTest() {

    private val persistence = InMemoryPersistenceManager()
    private lateinit var stateHolder: KarikaStateHolder
    private var mainNavigationSubscription: Cancellation? = null

    /** The screen the home screen asked the shop to open, once it does. */
    private var opened: MainConfig? = null

    private var cartItemToRemove: Int? = null

    @Before
    fun setUp() {
        startKoin {
            modules(module { single<PersistenceManager> { persistence } })
        }
        stateHolder = KarikaStateHolder(FakeKarikaHandler())
        mainNavigationSubscription = stateHolder.mainNavigation.subscribe { event ->
            opened = event.transformer(listOf(MainConfig.Home)).last()
        }
    }

    @After
    fun tearDown() {
        // Leave the test account's cart as it was
        cartItemToRemove?.let { itemId ->
            runBlocking { CartRepository().removeFromCart(itemId.toString()).last() }
        }
        mainNavigationSubscription?.cancel()
        HttpClientProvider.token = getEnvJwt()
        stopKoin()
    }

    private fun snackbarMessage() = stateHolder.hostState.currentSnackbarData?.visuals?.message

    private fun waitForServer(condition: () -> Boolean) {
        compose.waitUntil(timeoutMillis = SERVER_TIMEOUT_MS, condition = condition)
    }

    /** Logs the customer in, shows the home screen and waits for its products. */
    private fun showHome(): List<Product> {
        LiveTestAccounts.logInCustomer()
        val lifecycle = LifecycleRegistry()
        lifecycle.resume()
        val component = DefaultHomeComponent(DefaultComponentContext(lifecycle), stateHolder)
        compose.setContent { HomeView(component) }

        waitForServer { component.newArrivals.value.isNotEmpty() || snackbarMessage() != null }
        val products = component.newArrivals.value
        assertTrue(products.isNotEmpty(), "no recommended products, message: ${snackbarMessage()}")
        return products
    }

    private fun currentCart(): Cart {
        val result = runBlocking { CartRepository().getCart().last() }
        return assertIs<ResultState.Success<*>>(result, "could not read the cart: $result").data as Cart
    }

    @Test
    fun recommendedProductsLoadForTheCustomer() {
        val products = showHome()

        compose.onNodeWithText("Karika preporučuje:").assertExists()
        compose.onNodeWithText("Vidi sve").assertExists()
        compose.onNodeWithTag(productCardTag(products.first())).assertExists()
        assertTrue(products.size <= 12, "home asks for at most 12 products, got ${products.size}")
    }

    @Test
    fun loggedInCustomerSeesVendorNames() {
        val product = showHome().first { it.vendorName() != "-" }

        // Several products can come from the same vendor
        compose.onAllNodesWithText(product.vendorName()).onFirst().assertExists()
    }

    @Test
    fun tappingAProductOpensItsDetails() {
        val product = showHome().first()

        compose.onNodeWithTag(productCardTag(product)).performScrollTo().performClick()
        waitForServer { opened != null }

        assertEquals(MainConfig.ProductDetails(product), opened)
    }

    @Test
    fun vidiSveOpensTheRecommendedCategory() {
        showHome()

        compose.onNodeWithText("Vidi sve").performClick()
        waitForServer { opened != null }

        val category = assertIs<MainConfig.CategoryProducts>(opened).category
        assertEquals(KarikaConfig.getKarikaProductsId(), category.id)
        assertEquals("Karika preporučuje", category.name)
    }

    @Test
    fun cartButtonAddsTheProductToTheCart() {
        val products = showHome()
        // The app has the customer's cart id from right after login; the add needs it.
        // POST carts/mine returns the active cart, creating it only if there is none.
        val cartId = runBlocking { CartRepository().createCart().last() }
        stateHolder.cartHandler.cartId =
            assertIs<ResultState.Success<*>>(cartId, "no cart for the customer: $cartId").data as String

        val skusInCart = currentCart().items.map { it.sku }.toSet()
        val product = products.firstOrNull { it.hasOnStock() && it.sku !in skusInCart }
        assumeTrue("every recommended product is sold out or already in the cart", product != null)
        product!!

        // Drop whatever loading the screen reported, so the next message is the add's
        stateHolder.hostState.currentSnackbarData?.dismiss()
        compose.waitForIdle()

        compose.onNode(
            hasContentDescription("Dodaj u korpu") and hasAnyAncestor(hasTestTag(productCardTag(product)))
        ).performScrollTo().performClick()
        waitForServer { snackbarMessage() != null }

        assertEquals("Proizvod dodan u korpu!", snackbarMessage())
        val item = currentCart().items.find { it.sku == product.sku }
        assertNotNull(item, "${product.sku} is not in the cart")
        cartItemToRemove = item.itemId
        assertEquals(product.minQty(), item.qty)
    }

    private companion object {
        const val SERVER_TIMEOUT_MS = 30_000L
    }
}
