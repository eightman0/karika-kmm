package karika.distribucija.ba.ui.view.shop.cart

import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.testutil.LiveShopTest
import karika.distribucija.ba.ui.view.shop.MainConfig
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * The cart of the logged-in customer test account against the real backend, through the
 * real CartView and DefaultCartComponent. Each test puts one product the cart did not have
 * into it, and the base class removes it again afterwards.
 */
class LiveCartApiTest : LiveShopTest() {

    private lateinit var component: DefaultCartComponent

    /** Puts an in-stock recommended product that is not in the cart yet into it, and shows the cart. */
    private fun showCartWithNewProduct(): Product {
        useCustomerCart()
        val skusInCart = currentCart().items.map { it.sku }.toSet()
        val product = recommendedProducts().firstOrNull { it.hasOnStock() && it.sku !in skusInCart }
        assumeTrue("every recommended product is sold out or already in the cart", product != null)
        product!!

        component = DefaultCartComponent(componentContext(), stateHolder)
        component.addToCart(product, product.minQty(), showSnack = false)
        waitForServer { currentCart().items.any { it.sku == product.sku } }
        currentCart().items.first { it.sku == product.sku }.itemId?.let { cartItemsToRemove += it }

        compose.setContent { CartView(component) }
        stateHolder.cartHandler.reloadCart()
        waitForServer { component.cart.value.items.values.flatten().any { it.first.sku == product.sku } }
        return product
    }

    private fun quantityInCartOnServer(product: Product) = currentCart().items.find { it.sku == product.sku }?.qty

    @Test
    fun cartListsTheAddedProduct() {
        val product = showCartWithNewProduct()

        compose.onNodeWithText("Pregled korpe:").assertExists()
        compose.onNode(hasTestTag(cartItemTag(product))).performScrollTo()
        compose.onNodeWithText("Ukupno sa PDV:").assertExists()
        compose.onNodeWithText(component.cart.value.items.calculateTotal()).assertExists()
    }

    @Test
    fun plusRaisesTheQuantityOnTheServer() {
        val product = showCartWithNewProduct()
        val minQty = product.minQty()

        plusOf(product).performScrollTo().performClick()
        compose.mainClock.advanceTimeBy(QTY_DEBOUNCE_MS)
        waitForServer { quantityInCartOnServer(product) == 2 * minQty }

        assertEquals(2 * minQty, quantityInCartOnServer(product))
    }

    @Test
    fun removeIconTakesTheProductOut() {
        val product = showCartWithNewProduct()

        removeOf(product).performScrollTo().performClick()
        waitForServer { quantityInCartOnServer(product) == null }

        assertNull(quantityInCartOnServer(product))
        waitForServer { snackbarMessage() != null }
        assertEquals("Proizvod uklonjen iz korpe!", snackbarMessage())
    }

    @Test
    fun nastaviDaljeFollowsTheVendorMinimums() {
        showCartWithNewProduct()
        val valid = component.cart.value.items.orderValid()
        dismissSnackbar()

        compose.onNodeWithText("Nastavi dalje").performClick()

        if (valid) {
            waitForServer { openedInShop != null }
            assertEquals(MainConfig.CartShippingDetails, openedInShop)
        } else {
            waitForServer { snackbarMessage() != null }
            assertEquals("Nije zadovoljena minimalna vrijednost narudžbe za dobavljača!", snackbarMessage())
            assertNull(openedInShop)
        }
    }

    @Test
    fun emptyingTheCartEmptiesItOnTheServer() {
        useCustomerCart()
        // Emptying takes everything; only do it to a cart that held nothing of the tester's
        assumeTrue("the test account's cart is not empty", currentCart().items.isEmpty())
        showCartWithNewProduct()

        compose.onNodeWithText("Isprazni korpu").performClick()
        compose.onNodeWithText("Da").performClick()
        waitForServer { currentCart().items.isEmpty() }

        cartItemsToRemove.clear()
        compose.onNodeWithText("Nema artikala u korpi.").assertExists()
    }

    private fun plusOf(product: Product) =
        compose.onNode(hasText("+") and hasAnyAncestor(hasTestTag(cartItemTag(product))))

    private fun removeOf(product: Product) =
        compose.onNode(hasContentDescription("Ukloni iz korpe") and hasAnyAncestor(hasTestTag(cartItemTag(product))))

    private companion object {
        const val QTY_DEBOUNCE_MS = 700L
    }
}
