package karika.distribucija.ba.e2e

import android.util.Log
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.ui.view.shop.cart.CART_LIST_TAG
import karika.distribucija.ba.ui.view.shop.cart.cartProductTag
import karika.distribucija.ba.ui.view.shop.cart.cartQtyMinusTag
import karika.distribucija.ba.ui.view.shop.cart.cartQtyPlusTag
import karika.distribucija.ba.ui.view.shop.cart.removeFromCartTag
import kotlin.math.ceil
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-end test of the customer's cart on stage.karika.ba, see [StageE2ETest]. Each test
 * starts from an empty cart and puts in what it needs through the cart API; afterwards the
 * account's cart is put back as it was (and logged under the "CartE2ETest" tag first, so it
 * can be put back by hand if a run is killed). No order is placed: "Nastavi dalje" only
 * opens the shipping details.
 */
@OptIn(ExperimentalTestApi::class)
class CartE2ETest : StageE2ETest() {

    /** The account's cart before the test, as sku to quantity. */
    private var cartBefore: List<Pair<String, Int>>? = null

    @Before
    fun startFromAnEmptyCart() {
        logInAsCustomer()
        cartBefore = currentCart().items.map { it.sku to it.qty }.also {
            Log.i("CartE2ETest", "cart before the test: $it")
        }
        emptyCartByApi()
    }

    @After
    fun putTheCartBack() {
        val before = cartBefore ?: return
        emptyCartByApi()
        before.forEach { (sku, qty) -> addByApi(sku, qty) }
    }

    @Test
    fun emptyCartSaysSo() {
        openCart()

        compose.onNodeWithText("Nema artikala u korpi.").assertIsDisplayed()
        compose.onNodeWithText("Nastavi dalje").assertDoesNotExist()
    }

    @Test
    fun showsTheLineWithItsPricesAndTheTotal() {
        val product = putInCart()

        compose.onNodeWithTag(CART_LIST_TAG).assertIsDisplayed()
        compose.onNodeWithTag(cartProductTag(product)).assertIsDisplayed()
        assertShown(product.name())
        assertShown(product.vendorName())
        assertShown("Min. ${product.minQty()} ", substring = true)
        assertShown("VPC ", substring = true)
        compose.onNodeWithText("Ukupno sa PDV").assertIsDisplayed()
        // With one line the total is that line's price with PDV: the line's price (the text
        // before its "VPC …") is shown twice, on the line and as the total
        val linePrice = textOf(texts().first { it.startsWith("VPC ") }, offset = -1)
        assertEquals(2, compose.onAllNodes(hasText(linePrice), useUnmergedTree = true).fetchSemanticsNodes().size)
        compose.onNodeWithText("Nastavi dalje").assertIsDisplayed()
    }

    @Test
    fun plusAddsTheMinimumQuantityAgain() {
        val product = putInCart()

        compose.onNodeWithTag(cartQtyPlusTag(product)).performClick()

        waitForQuantity(product, 2 * product.minQty())
    }

    @Test
    fun minusGoesBackButNotBelowTheMinimum() {
        val product = putInCart(qtyOf = 2)

        compose.onNodeWithTag(cartQtyMinusTag(product)).performClick()
        waitForQuantity(product, product.minQty())

        // At the minimum "-" does nothing
        compose.onNodeWithTag(cartQtyMinusTag(product)).performClick()
        compose.mainClock.advanceTimeBy(QTY_DEBOUNCE_MS)
        compose.waitForIdle()
        waitUntilLoaded()
        assertEquals(product.minQty(), qtyInCart(product))
    }

    @Test
    fun deleteIconTakesTheProductOut() {
        val product = putInCart()

        compose.onNodeWithTag(removeFromCartTag(product)).performClick()

        compose.waitUntilAtLeastOneExists(hasText("Proizvod uklonjen iz korpe!"), SERVER_TIMEOUT_MS)
        compose.waitUntilAtLeastOneExists(hasText("Nema artikala u korpi."), SERVER_TIMEOUT_MS)
        assertTrue(currentCart().items.none { it.sku == product.sku })
    }

    @Test
    fun isprazniKorpuAsksFirstAndNeKeepsTheCart() {
        val product = putInCart()

        compose.onNodeWithText("Isprazni").performClick()

        compose.waitUntilAtLeastOneExists(hasText("Isprazniti korpu?"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Ova akcija će ukloniti sve artikle iz korpe.").assertIsDisplayed()
        compose.onNodeWithText("Ne").performClick()

        compose.waitUntilDoesNotExist(hasText("Isprazniti korpu?"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithTag(cartProductTag(product)).assertIsDisplayed()
        assertEquals(product.minQty(), qtyInCart(product))
    }

    @Test
    fun isprazniKorpuDaEmptiesTheCart() {
        putInCart()

        compose.onNodeWithText("Isprazni").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Isprazniti korpu?"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Da, isprazni").performClick()

        compose.waitUntilAtLeastOneExists(hasText("Nema artikala u korpi."), SERVER_TIMEOUT_MS)
        waitForServer({ "an empty cart" }) { currentCart().items.isEmpty() }
    }

    @Test
    fun tappingTheProductOpensItsDetails() {
        val product = putInCart()

        compose.onNodeWithTag(cartProductTag(product)).performClick()

        compose.waitUntilAtLeastOneExists(hasText("Min. količina", substring = true), SCREEN_TIMEOUT_MS)
        compose.onNodeWithTag(CART_LIST_TAG).assertDoesNotExist()
    }

    @Test
    fun vendorNameOpensTheVendor() {
        val product = putInCart()

        compose.onAllNodesWithText(product.vendorName(), useUnmergedTree = true).onFirst().performClick()

        compose.waitUntilDoesNotExist(hasTestTag(CART_LIST_TAG), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        assertShown(product.vendorName())
    }

    @Test
    fun nastaviDaljeBelowTheVendorMinimumSaysSo() {
        // The first recommended product whose minimum quantity stays below its vendor's minimum
        val below = inStock().firstOrNull { candidate ->
            putInCart(product = candidate)
            val minimum = vendorMinimum()
            (minimum != null && total() < minimum).also { if (!it) emptyCartByApi() }
        }
        assumeTrue("every recommended product reaches its vendor's minimum on its own", below != null)

        compose.onNodeWithText("Nastavi dalje").performClick()

        compose.waitUntilAtLeastOneExists(
            hasText("Nije zadovoljena minimalna vrijednost narudžbe za dobavljača!"),
            SCREEN_TIMEOUT_MS
        )
        compose.onNodeWithTag(CART_LIST_TAG).assertIsDisplayed()
        compose.onNodeWithText("Informacije za dostavu").assertDoesNotExist()
    }

    @Test
    fun nastaviDaljeAtTheVendorMinimumOpensTheShippingDetails() {
        val product = putInCart()
        val minimum = vendorMinimum()
        if (minimum != null && total() < minimum) {
            // Enough minimum quantities to reach the vendor's minimum order
            val steps = ceil(minimum / total()).toInt()
            assumeTrue("the minimum takes $steps minimum quantities", steps <= MAX_STEPS)
            val qty = steps * product.minQty()
            product.stockData?.salableQty?.let { assumeTrue("not enough in stock for $qty", qty <= it) }
            setQtyByApi(product, qty)
            openCart()
        }
        assertShown("Nedostaje: 0,00KM", substring = true, required = minimum != null)

        compose.onNodeWithText("Nastavi dalje").performClick()

        compose.waitUntilAtLeastOneExists(hasText("Informacije za dostavu"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onNodeWithText("Napomena za dobavljača").assertExists()
        // Going no further: the next step would place an order
        pressBack()
        compose.waitUntilAtLeastOneExists(hasTestTag(CART_LIST_TAG), SCREEN_TIMEOUT_MS)
    }

    /**
     * Puts one recommended product in stock into the cart through the API, [qtyOf] times its
     * minimum quantity, and opens the cart tab.
     */
    private fun putInCart(qtyOf: Int = 1, product: Product? = inStock().firstOrNull()): Product {
        assumeTrue("no recommended product is in stock", product != null)
        addByApi(product!!.sku!!, qtyOf * product.minQty())
        openCart()
        compose.waitUntilAtLeastOneExists(hasTestTag(cartProductTag(product)), SERVER_TIMEOUT_MS)
        // The reload that brought the line in has its own loader, which takes taps
        waitUntilLoaded()
        return product
    }

    private fun inStock() = recommendedProducts().filter { it.hasOnStock() && it.sku != null }

    /** Opens the cart tab, which reloads the cart, and waits for it. */
    private fun openCart() {
        compose.onNode(bottomTab("Korpa")).performClick()
        compose.waitUntil(SERVER_TIMEOUT_MS) {
            exists(hasTestTag(CART_LIST_TAG)) || exists(hasText("Nema artikala u korpi."))
        }
        waitUntilLoaded()
    }

    /** Waits until the cart on stage has [qty] of [product] and the screen shows it. */
    private fun waitForQuantity(product: Product, qty: Int) {
        waitForServer({ "${product.sku} at $qty, the cart has ${currentCart().items.map { it.sku to it.qty }}" }) {
            qtyInCart(product) == qty
        }
        waitUntilLoaded()
        compose.waitUntil(SCREEN_TIMEOUT_MS) { exists(hasText("$qty")) }
    }

    /**
     * Polls stage (not in a tight loop) until [condition] holds. Delays inside the app's
     * composition, like the pause before a changed quantity is sent, run on the test's clock,
     * so that is moved along too; a plain sleep would leave them waiting forever.
     */
    private fun waitForServer(what: () -> String, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + SERVER_TIMEOUT_MS
        while (!condition()) {
            assertTrue("stage did not get to ${what()} in time", System.currentTimeMillis() < deadline)
            compose.mainClock.advanceTimeBy(POLL_MS)
            compose.waitForIdle()
            Thread.sleep(POLL_MS)
        }
    }

    private fun qtyInCart(product: Product) = currentCart().items.firstOrNull { it.sku == product.sku }?.qty

    /** The cart's total with PDV, as shown. */
    private fun total(): Double {
        val shown = texts().first { it.endsWith(" KM") && it.first().isDigit() }
        return shown.removeSuffix(" KM").replace(',', '.').toDouble()
    }

    /** The vendor's minimum order as shown ("Minimum: 50KM • Nedostaje: …"), if it has one. */
    private fun vendorMinimum(): Double? =
        texts().firstNotNullOfOrNull { MINIMUM.find(it)?.groupValues?.get(1) }
            ?.replace(',', '.')?.toDoubleOrNull()

    /** The text shown [offset] texts after [label], e.g. the price before "VPC 7,40 KM". */
    private fun textOf(label: String, offset: Int): String {
        val all = texts()
        return all[all.indexOf(label) + offset]
    }

    /** Every text on screen, in order, from the unmerged tree (the cart list merges its texts). */
    private fun texts(): List<String> =
        compose.onAllNodes(hasText("", substring = true), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .flatMap { node -> node.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text } }

    private fun assertShown(text: String, substring: Boolean = false, required: Boolean = true) {
        if (!required) return
        assertTrue(
            "\"$text\" is not shown",
            exists(hasText(text, substring = substring), unmerged = true)
        )
    }

    private fun exists(matcher: androidx.compose.ui.test.SemanticsMatcher, unmerged: Boolean = false) =
        compose.onAllNodes(matcher, useUnmergedTree = unmerged).fetchSemanticsNodes().isNotEmpty()

    private companion object {
        /** ProductQtyAction waits this long before it sends a changed quantity. */
        const val QTY_DEBOUNCE_MS = 1_000L
        const val POLL_MS = 500L
        /** More minimum quantities than this for a vendor's minimum is not a sensible cart. */
        const val MAX_STEPS = 20
        val MINIMUM = Regex("""Minimum: ([\d.,]+)KM""")
    }
}
