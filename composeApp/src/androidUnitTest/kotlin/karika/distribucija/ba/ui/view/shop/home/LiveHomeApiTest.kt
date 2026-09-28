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
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.testutil.LiveShopTest
import karika.distribucija.ba.ui.view.shop.MainConfig
import karika.distribucija.ba.util.KarikaConfig
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * The home screen of the logged-in customer test account against the real backend, through
 * the real HomeView and DefaultHomeComponent.
 */
class LiveHomeApiTest : LiveShopTest() {

    /** Shows the home screen and waits for its products. */
    private fun showHome(): List<Product> {
        val component = DefaultHomeComponent(componentContext(), stateHolder)
        compose.setContent { HomeView(component) }

        waitForServer { component.newArrivals.value.isNotEmpty() || snackbarMessage() != null }
        val products = component.newArrivals.value
        assertTrue(products.isNotEmpty(), "no recommended products, message: ${snackbarMessage()}")
        return products
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
        waitForServer { openedInShop != null }

        assertEquals(MainConfig.ProductDetails(product), openedInShop)
    }

    @Test
    fun vidiSveOpensTheRecommendedCategory() {
        showHome()

        compose.onNodeWithText("Vidi sve").performClick()
        waitForServer { openedInShop != null }

        val category = assertIs<MainConfig.CategoryProducts>(openedInShop).category
        assertEquals(KarikaConfig.getKarikaProductsId(), category.id)
        assertEquals("Karika preporučuje", category.name)
    }

    @Test
    fun cartButtonAddsTheProductToTheCart() {
        val products = showHome()
        useCustomerCart()

        val skusInCart = currentCart().items.map { it.sku }.toSet()
        val product = products.firstOrNull { it.hasOnStock() && it.sku !in skusInCart }
        assumeTrue("every recommended product is sold out or already in the cart", product != null)
        product!!

        dismissSnackbar()
        compose.onNode(
            hasContentDescription("Dodaj u korpu") and hasAnyAncestor(hasTestTag(productCardTag(product)))
        ).performScrollTo().performClick()
        waitForServer { snackbarMessage() != null }

        assertEquals("Proizvod dodan u korpu!", snackbarMessage())
        val item = currentCart().items.find { it.sku == product.sku }
        assertNotNull(item, "${product.sku} is not in the cart")
        item.itemId?.let { cartItemsToRemove += it }
        assertEquals(product.minQty(), item.qty)
    }
}
