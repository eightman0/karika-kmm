package karika.distribucija.ba.e2e

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import karika.distribucija.ba.domain.api.CartRepository
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.components.vendorBannerTag
import karika.distribucija.ba.ui.components.vendorLogoTag
import karika.distribucija.ba.ui.view.shop.home.HOME_LIST_TAG
import karika.distribucija.ba.ui.view.shop.home.HOME_SEARCH_PLACEHOLDER
import karika.distribucija.ba.ui.view.shop.home.addToCartTag
import karika.distribucija.ba.ui.view.shop.home.productCardTag
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-end test of the customer's home screen on stage.karika.ba, see [StageE2ETest]: logs
 * the customer test account in and taps everything on home. What home should show is read from
 * the same stage endpoints the app uses. The add-to-cart test takes out again what it put in.
 */
@OptIn(ExperimentalTestApi::class)
class HomeE2ETest : StageE2ETest() {

    /** Cart item ids that were there before the test, so the test only takes out its own. */
    private var cartBefore: Set<Int>? = null

    @Before
    fun openHomeAsCustomer() {
        logInAsCustomer()
        // Home starts loading only once it is shown, so the loader may not have been up yet
        compose.waitUntilAtLeastOneExists(hasText("Karika preporučuje"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        cartBefore = currentCart().items.mapNotNull { it.itemId }.toSet()
    }

    @After
    fun takeOutWhatTheTestAdded() {
        val before = cartBefore ?: return
        currentCart().items
            .mapNotNull { it.itemId }
            .filter { it !in before }
            .forEach { runBlocking { CartRepository().removeFromCart(it.toString()).last() } }
    }

    @Test
    fun showsTheSearchAndTheCustomerShortcuts() {
        compose.onNodeWithText(HOME_SEARCH_PLACEHOLDER, useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText("Outlet").assertIsDisplayed()
        compose.onNodeWithText("Akcije").assertIsDisplayed()
        // A guest's shortcuts, not a customer's
        compose.onNodeWithText("Svi proizvodi").assertDoesNotExist()
    }

    @Test
    fun showsTheProductsKarikaRecommends() {
        val products = recommendedProducts()

        compose.onNodeWithText("Karika preporučuje").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Vidi sve").assertIsDisplayed()
        products.forEach { product ->
            compose.onNodeWithTag(productCardTag(product)).performScrollTo().assertIsDisplayed()
            compose.onAllNodesWithText(product.name()).onFirst().assertExists()
        }
    }

    @Test
    fun searchOpensTheSearchScreen() {
        compose.onNodeWithText(HOME_SEARCH_PLACEHOLDER, useUnmergedTree = true).performClick()

        compose.waitUntilDoesNotExist(hasTestTag(HOME_LIST_TAG), SCREEN_TIMEOUT_MS)
        compose.onNode(hasSetTextAction()).assertIsDisplayed()
        compose.onNodeWithText("Karika preporučuje").assertDoesNotExist()
    }

    @Test
    fun outletOpensTheOutletCategory() {
        opensItsCategory("Outlet", "OUTLET")
    }

    @Test
    fun akcijeOpensTheActionsCategory() {
        opensItsCategory("Akcije", "AKCIJE")
    }

    @Test
    fun vidiSveOpensTheRecommendedCategory() {
        compose.onNodeWithText("Vidi sve").performScrollTo().performClick()

        // The category has the same title as the home heading
        compose.waitUntilAtLeastOneExists(hasText("Filteri"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onNodeWithTag(HOME_LIST_TAG).assertDoesNotExist()
        compose.onNodeWithText("Karika preporučuje").assertIsDisplayed()

        pressBack()
        compose.waitUntilAtLeastOneExists(hasTestTag(HOME_LIST_TAG), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun tappingAProductOpensItsDetails() {
        val product = recommendedProducts().first()

        compose.onNodeWithTag(productCardTag(product)).performScrollTo().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Min. količina", substring = true), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onAllNodesWithText("zalihama", substring = true).onFirst().assertExists()
        // Once in the top bar, once above the image
        assertTrue(compose.onAllNodesWithText(product.name()).fetchSemanticsNodes().isNotEmpty())

        pressBack()
        compose.waitUntilAtLeastOneExists(hasTestTag(HOME_LIST_TAG), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun vendorNameOnAProductOpensTheVendor() {
        val product = recommendedProducts().first()

        compose.onNodeWithTag(productCardTag(product)).performScrollTo()
        compose.onAllNodesWithText(product.vendorName()).onFirst().performScrollTo().performClick()

        waitForVendorPage()
        assertTrue(compose.onAllNodesWithText(product.vendorName()).fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun addToCartPutsTheMinimumQuantityInTheCart() {
        val inCart = currentCart().items.map { it.sku }.toSet()
        val product = recommendedProducts().firstOrNull { it.hasOnStock() && it.sku !in inCart }
        assumeTrue("every recommended product in stock is already in the cart", product != null)
        product!!

        compose.onNodeWithTag(addToCartTag(product)).performScrollTo().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Proizvod dodan u korpu!"), SERVER_TIMEOUT_MS)
        val added = currentCart().items.singleOrNull { it.sku == product.sku }
        assertTrue("${product.sku} is not in the cart", added != null)
        assertEquals(product.minQty(), added!!.qty)
    }

    @Test
    fun vendorBannersMatchTheBackendAndOpenTheVendor() {
        val banners = promotedVendors().filter { it.promoteVendorBanner && it.companyBanner != null }
        if (banners.isEmpty()) {
            compose.onNode(hasTestTagStartingWith("vendor_banner_")).assertDoesNotExist()
            return
        }

        compose.onNodeWithTag(vendorBannerTag(banners.first())).performScrollTo().assertIsDisplayed().performClick()

        waitForVendorPage()
        compose.onNodeWithText(banners.first().name()).assertExists()
    }

    @Test
    fun vendorLogosMatchTheBackendAndOpenTheVendor() {
        val logos = promotedVendors().filter { it.promoteVendorLogo && it.companyLogo != null }
        if (logos.isEmpty()) {
            compose.onNode(hasTestTagStartingWith("vendor_logo_")).assertDoesNotExist()
            // Only the bottom bar tab, no "Dobavljači" section on home
            assertEquals(1, compose.onAllNodesWithText("Dobavljači").fetchSemanticsNodes().size)
            return
        }

        compose.onNodeWithTag(vendorLogoTag(logos.first())).performScrollTo().assertIsDisplayed().performClick()

        waitForVendorPage()
        compose.onNodeWithText(logos.first().name()).assertExists()
    }

    /** Taps a tile under the search, which opens the category with [title]. */
    private fun opensItsCategory(tile: String, title: String) {
        compose.onNodeWithText(tile).performClick()

        compose.waitUntilAtLeastOneExists(hasText("Filteri"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onNodeWithText(title).assertIsDisplayed()
        compose.onNodeWithTag(HOME_LIST_TAG).assertDoesNotExist()

        pressBack()
        compose.waitUntilAtLeastOneExists(hasTestTag(HOME_LIST_TAG), SCREEN_TIMEOUT_MS)
    }

    private fun hasTestTagStartingWith(prefix: String) =
        SemanticsMatcher("TestTag starts with '$prefix'") { node ->
            node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(prefix) == true
        }

    /** A vendor's page has no home heading and loads its products. */
    private fun waitForVendorPage() {
        compose.waitUntilDoesNotExist(hasTestTag(HOME_LIST_TAG), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
    }

    private fun promotedVendors(): List<PromotedVendor> {
        val result = runBlocking { ProductRepository().promotedVendors().last() }
        assertTrue("promoted vendors: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return (result as ResultState.Success<*>).data as List<PromotedVendor>
    }
}
