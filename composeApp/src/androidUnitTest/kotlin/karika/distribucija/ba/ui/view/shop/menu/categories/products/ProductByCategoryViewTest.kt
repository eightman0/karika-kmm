package karika.distribucija.ba.ui.view.shop.menu.categories.products

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.KarikaUnit
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.testutil.KarikaUiTest
import karika.distribucija.ba.testutil.testProduct
import karika.distribucija.ba.ui.view.shop.home.productCardTag
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

class ProductByCategoryViewTest : KarikaUiTest() {

    private val pica = Category(id = 1, name = "Pića")
    private val sok = testProduct(id = "1", name = "Sok od jabuke 1L", price = 12.5, minQty = 6)
    private val kafa = testProduct(id = "2", name = "Kafa 500g", price = 24.0)
    private val sarajevo = KarikaUnit(label = "Sarajevo", unit = "|1|")
    private val tuzla = KarikaUnit(label = "Tuzla", unit = "|2|")
    private val mljekara = Vendor(entityId = 9, publicName = "Mljekara d.o.o.")

    private fun show(component: FakeProductByCategoryComponent): FakeProductByCategoryComponent {
        compose.setContent { ProductByCategoryView(component) }
        compose.waitForIdle()
        return component
    }

    private fun showList() = show(FakeProductByCategoryComponent(pica, products = listOf(sok, kafa)))

    private fun showWithFilters() = show(
        FakeProductByCategoryComponent(
            pica,
            products = listOf(sok),
            regions = listOf(sarajevo, tuzla),
            vendorsFound = listOf(mljekara),
        )
    )

    private fun openFilters() {
        compose.onNodeWithText("Filteri").performClick()
        compose.onNodeWithText("FILTERI").assertIsDisplayed()
    }

    private fun apply() {
        compose.onNodeWithText("Primijeni").performClick()
    }

    @Test
    fun titleIsTheCategoryName() {
        showList()

        compose.onNodeWithText("Pića").assertIsDisplayed()
    }

    @Test
    fun openingLoadsTheFirstPageAndTheFeaturedProducts() {
        val component = showList()

        assertEquals(1, component.featuredLoads)
        assertTrue(component.pageRequests.any { !it.reset }, "no first page: ${component.pageRequests}")
    }

    @Test
    fun productsShowTheirMinimumQuantity() {
        showList()

        compose.onNodeWithText("Sok od jabuke 1L").assertExists()
        compose.onNodeWithText("Min. kol: 6 kom").assertExists()
        compose.onNodeWithText("Kafa 500g").assertExists()
    }

    @Test
    fun tappingAProductOpensIt() {
        val component = showList()

        compose.onNodeWithTag(productCardTag(kafa)).performScrollTo().performClick()

        assertEquals(listOf(kafa), component.openedProducts)
    }

    @Test
    fun emptyCategoryShowsNoResults() {
        show(FakeProductByCategoryComponent(pica))

        compose.onNodeWithText("Nema rezultata.").assertIsDisplayed()
    }

    @Test
    fun featuredProductsGetTheirSponsoredRow() {
        val featured = testProduct(id = "3", name = "Energetsko piće", price = 3.0, minQty = 24)
        val component = show(FakeProductByCategoryComponent(pica, featured = listOf(featured)))

        compose.onNodeWithText("ISTAKNUTI ARTIKLI").assertIsDisplayed()
        compose.onNodeWithText("Sponzorisano").assertIsDisplayed()
        compose.onNodeWithText("ISTAKNUTO").assertExists()

        compose.onNodeWithContentDescription("Dodaj u korpu").performClick()
        assertEquals(listOf("Energetsko piće" to 24), component.cartAdds)

        // Away from the cart button in the card's bottom-right corner
        compose.onNodeWithText("Energetsko piće").performTouchInput {
            click(Offset(width * 0.2f, height * 0.2f))
        }
        assertEquals(listOf(featured), component.openedProducts)
    }

    @Test
    fun noFeaturedProductsNoSponsoredRow() {
        showList()

        compose.onNodeWithText("ISTAKNUTI ARTIKLI").assertDoesNotExist()
    }

    @Test
    fun sortingReloadsFromTheFirstPage() {
        val component = showList()

        compose.onNodeWithText("Najnoviji").performClick()
        compose.onNodeWithText("Najjeftiniji").performClick()

        assertEquals("Najjeftiniji", component.sortBy.value)
        assertEquals("Najjeftiniji", component.resets.last().sortBy)
    }

    @Test
    fun searchingInTheCategoryReloadsWithTheText() {
        val component = showList()

        compose.onNodeWithTag(CategoryProductsTestTags.SEARCH).performTextInput("sok")
        compose.mainClock.advanceTimeBy(DEBOUNCE_MS)
        compose.waitForIdle()

        assertEquals("sok", component.resets.last().searchText)
    }

    @Test
    fun priceFilterIsAppliedOnPrimijeni() {
        val component = showWithFilters()
        openFilters()

        compose.onNodeWithTag(CategoryProductsTestTags.PRICE_FROM).performTextInput("10")
        compose.onNodeWithTag(CategoryProductsTestTags.PRICE_TO).performTextInput("50")
        assertEquals(emptyList(), component.resets)
        apply()

        val request = component.resets.single()
        assertEquals("10" to "50", request.priceFrom to request.priceTo)
        compose.onNodeWithText("FILTERI").assertDoesNotExist()
    }

    @Test
    fun zatvoriClosesTheFiltersWithoutReloading() {
        val component = showWithFilters()
        openFilters()

        compose.onNodeWithText("Zatvori").performClick()

        compose.onNodeWithText("FILTERI").assertDoesNotExist()
        assertEquals(emptyList(), component.resets)
    }

    @Test
    fun soldOutFilterIsAppliedAndCanBeRemoved() {
        val component = showWithFilters()
        openFilters()

        compose.onNodeWithText("Prikaži rasprodate").performScrollTo().performClick()
        apply()

        assertTrue(component.resets.last().withSoldOut)
        compose.onNodeWithText("Uključeni filter: ").assertIsDisplayed()

        compose.onNodeWithText("Prikaži rasprodate").performClick()

        assertEquals(false, component.resets.last().withSoldOut)
        compose.onNodeWithText("Uključeni filter: ").assertDoesNotExist()
    }

    @Test
    fun regionFilterIsAppliedAndCanBeRemoved() {
        val component = showWithFilters()
        openFilters()

        compose.onNodeWithText("Sarajevo").performScrollTo().performClick()
        apply()

        assertEquals(listOf("Sarajevo"), component.resets.last().regions)
        compose.onNodeWithText("Sarajevo").performClick()

        assertEquals(emptyList(), component.resets.last().regions)
    }

    @Test
    fun allRegionsSelectsEveryRegion() {
        val component = showWithFilters()
        openFilters()

        compose.onNodeWithText("Svi regioni").performScrollTo().performClick()
        apply()

        assertEquals(listOf("Sarajevo", "Tuzla"), component.resets.last().regions)
    }

    @Test
    fun vendorFilterLooksUpVendorsAndIsApplied() {
        val component = showWithFilters()
        openFilters()

        compose.onNodeWithTag(CategoryProductsTestTags.VENDOR_SEARCH).performScrollTo().performTextInput("mlj")
        compose.mainClock.advanceTimeBy(DEBOUNCE_MS)
        compose.waitForIdle()
        assertTrue("mlj" in component.vendorLookups)

        compose.onNodeWithText("Mljekara d.o.o.").performScrollTo().performClick()
        apply()

        assertEquals(9, component.resets.last().vendorId)
        compose.onNodeWithText("Mljekara d.o.o.").performClick()
        assertEquals(0, component.resets.last().vendorId)
    }

    @Test
    fun backArrowLeavesTheCategory() {
        val component = showList()

        compose.onNodeWithContentDescription("Nazad").performClick()

        assertEquals(1, component.backRequests)
    }

    private companion object {
        const val DEBOUNCE_MS = 600L
    }
}
