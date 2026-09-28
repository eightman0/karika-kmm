package karika.distribucija.ba.ui.view.shop.search

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.testutil.LiveShopTest
import karika.distribucija.ba.ui.view.shop.MainConfig
import karika.distribucija.ba.ui.view.shop.home.productCardTag
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

/**
 * The search screen of the logged-in customer test account against the real backend,
 * through the real SearchView and DefaultSearchComponent.
 */
class LiveSearchApiTest : LiveShopTest() {

    private lateinit var component: DefaultSearchComponent

    /** Shows the search screen and waits for the first page it loads on its own. */
    private fun showSearch(): List<Product> {
        component = DefaultSearchComponent(componentContext(), stateHolder)
        compose.setContent { SearchView(component) }
        waitForServer { component.products.value.isNotEmpty() && !component.loader.value }
        return component.products.value
    }

    /** Types [text], lets the 500 ms debounce run out, and waits for the new results. */
    private fun search(text: String, resultsChanged: (List<Product>) -> Boolean) {
        compose.onNode(hasSetTextAction()).performTextInput(text)
        compose.mainClock.advanceTimeBy(DEBOUNCE_MS)
        waitForServer { !component.loader.value && resultsChanged(component.products.value) }
    }

    @Test
    fun openingSearchListsProducts() {
        val products = showSearch()

        assertTrue(products.size <= 30, "search pages are 30 products, got ${products.size}")
        compose.onNodeWithText("Proizvodi").assertExists()
        compose.onNodeWithText("Nema rezultata.").assertDoesNotExist()
    }

    @Test
    fun searchingByNameFindsTheProduct() {
        val target = recommendedProducts().first()
        val firstPage = showSearch()

        search(target.name()) { it != firstPage }

        val skus = component.products.value.map { it.sku }
        assertTrue(target.sku in skus, "'${target.name()}' (${target.sku}) not in the results: $skus")
        compose.onNodeWithTag(productCardTag(target)).assertExists()
    }

    @Test
    fun searchingByVendorNameFindsTheVendor() {
        val vendorName = recommendedProducts().first { it.vendorName() != "-" }.vendorName()
        val firstPage = showSearch()

        search(vendorName) { it != firstPage }
        waitForServer { component.vendors.value.isNotEmpty() }

        val names = component.vendors.value.map { it.name() }
        assertTrue(
            // A product carries the vendor's name as its public name, or close to it
            names.any { it.isNotBlank() && (it.contains(vendorName, true) || vendorName.contains(it, true)) },
            "'$vendorName' not among the vendors found: $names"
        )
        compose.onNodeWithText("Dobavljači").assertExists()
    }

    @Test
    fun nonsenseFindsNothing() {
        showSearch()

        search("zqxjwvkq") { it.isEmpty() }

        assertEquals(emptyList(), component.vendors.value)
        compose.onNodeWithText("Nema rezultata.").assertExists()
    }

    @Test
    fun tappingAResultOpensIt() {
        val product = showSearch().first()

        compose.onNodeWithTag(productCardTag(product)).performScrollTo().performClick()
        waitForServer { openedInShop != null }

        assertEquals(MainConfig.ProductDetails(product), openedInShop)
    }

    private companion object {
        const val DEBOUNCE_MS = 600L
    }
}
