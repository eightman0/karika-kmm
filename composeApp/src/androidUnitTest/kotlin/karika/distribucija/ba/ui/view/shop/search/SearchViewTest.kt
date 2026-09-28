package karika.distribucija.ba.ui.view.shop.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.testutil.KarikaUiTest
import karika.distribucija.ba.testutil.testProduct
import karika.distribucija.ba.ui.view.shop.home.productCardTag
import karika.distribucija.ba.ui.view.shop.vendor.vendorCardTag
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

class SearchViewTest : KarikaUiTest() {

    private val sok = testProduct(id = "1", name = "Sok od jabuke 1L", price = 12.5)
    private val vendor = Vendor(entityId = 7, publicName = "Mljekara d.o.o.")

    private fun show(component: FakeSearchComponent = FakeSearchComponent()): FakeSearchComponent {
        compose.setContent { SearchView(component) }
        return component
    }

    private fun searchField() = compose.onNode(hasSetTextAction())

    /** Types [text] and lets the search box's 500 ms debounce run out. */
    private fun type(text: String) {
        searchField().performTextInput(text)
        compose.mainClock.advanceTimeBy(DEBOUNCE_MS)
        compose.waitForIdle()
    }

    /** The searches that started over, which is what typing triggers. */
    private fun FakeSearchComponent.newSearches() = searches.filter { it.second }.map { it.first }

    @Test
    fun openingSearchLoadsTheFirstPageOfEverything() {
        val component = show()
        compose.waitForIdle()

        assertEquals(listOf("" to false), component.searches)
    }

    @Test
    fun nothingFoundShowsEmptyState() {
        show()

        compose.onNodeWithText("Nema rezultata.").assertIsDisplayed()
        compose.onNodeWithText("Proizvodi").assertDoesNotExist()
        compose.onNodeWithText("Dobavljači").assertDoesNotExist()
    }

    @Test
    fun typingSearchesAfterTheDebounce() {
        val component = show()

        type("sok")

        assertEquals("sok", component.searchText.value)
        assertEquals(listOf("sok"), component.newSearches())
    }

    @Test
    fun twoLettersAreNotSearched() {
        val component = show()

        type("so")

        assertEquals(emptyList(), component.newSearches())
    }

    @Test
    fun keyboardSearchActionSearches() {
        val component = show()
        searchField().performTextInput("kafa")

        searchField().performImeAction()

        assertTrue("kafa" in component.newSearches())
    }

    @Test
    fun clearingTheTextSearchesEverythingAgain() {
        val component = show()
        type("sok")

        searchField().performTextClearance()
        compose.waitForIdle()

        assertEquals("", component.searchText.value)
        assertEquals(listOf("sok", ""), component.newSearches())
    }

    @Test
    fun resultsShowProductsAndVendors() {
        show(FakeSearchComponent(products = listOf(sok), vendors = listOf(vendor)))

        compose.onNodeWithText("Dobavljači").assertIsDisplayed()
        compose.onNodeWithText("Mljekara d.o.o.").assertExists()
        compose.onNodeWithText("Proizvodi").assertExists()
        compose.onNodeWithText("Sok od jabuke 1L").assertExists()
        compose.onNodeWithText("Nema rezultata.").assertDoesNotExist()
    }

    @Test
    fun tappingAProductOpensIt() {
        val component = show(FakeSearchComponent(products = listOf(sok)))

        compose.onNodeWithTag(productCardTag(sok)).performClick()

        assertEquals(listOf(sok), component.openedProducts)
    }

    @Test
    fun tappingAVendorOpensIt() {
        val component = show(FakeSearchComponent(vendors = listOf(vendor)))

        compose.onNodeWithTag(vendorCardTag(vendor)).performClick()

        assertEquals(listOf(7), component.openedVendors.map { it.entityId })
    }

    @Test
    fun reachingTheEndLoadsTheNextPage() {
        val many = (1..30).map { testProduct(id = "$it", name = "Proizvod $it", price = 1.0) }
        val component = show(FakeSearchComponent(products = many))
        compose.waitForIdle()
        val before = component.searches.count { !it.second }

        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Proizvod 30"))
        compose.waitForIdle()

        assertTrue(component.searches.count { !it.second } > before, "no next page was requested")
    }

    @Test
    fun backArrowLeavesSearch() {
        val component = show()

        compose.onNodeWithContentDescription("Nazad").performClick()

        assertEquals(1, component.backRequests)
    }

    private companion object {
        const val DEBOUNCE_MS = 600L

    }
}
