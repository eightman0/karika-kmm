package karika.distribucija.ba.ui.view.shop.product

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.testutil.KarikaUiTest
import karika.distribucija.ba.testutil.testProduct
import karika.distribucija.ba.ui.view.shop.home.productCardTag
import kotlin.test.assertEquals
import org.junit.Test

class ProductViewTest : KarikaUiTest() {

    private val voda = testProduct(id = "1", name = "Voda 0.5L gajba", price = 9.0, minQty = 6)

    private fun show(component: FakeProductComponent): FakeProductComponent {
        compose.setContent { ProductView(component) }
        return component
    }

    private fun show(product: Product) = show(FakeProductComponent(product))

    private fun text(text: String) = compose.onNodeWithText(text).performScrollTo()

    @Test
    fun showsNameVendorAndDetails() {
        show(voda)

        // Once in the top bar, once above the image
        assertEquals(2, compose.onAllNodesWithText("Voda 0.5L gajba").fetchSemanticsNodes().size)
        text("Test dobavljač").assertIsDisplayed()
        text("9,00 KM").assertIsDisplayed()
        text("Dostupnost:").assertIsDisplayed()
        text("NA ZALIHAMA").assertIsDisplayed()
        text("Minimalna količina:").assertIsDisplayed()
        text("6 kom").assertIsDisplayed()
    }

    @Test
    fun showsNothingUntilTheProductHasLoaded() {
        show(voda.copy(createdAt = null))

        compose.onNodeWithText("Dostupnost:").assertDoesNotExist()
        compose.onNodeWithText("Dodaj u Korpu").assertDoesNotExist()
    }

    @Test
    fun discountedProductShowsBothPrices() {
        show(testProduct(id = "2", name = "Kafa 500g", price = 24.0, specialPrice = 18.0))

        text("18,00 KM").assertIsDisplayed()
        text("24,00 KM").assertIsDisplayed()
        compose.onNodeWithText("-25%").assertExists()
    }

    @Test
    fun recommendedRetailPriceIsShownWhenSet() {
        show(testProduct(id = "3", name = "Keks", price = 2.0, mpc = 2.5))

        text("Preporučena MPC:").assertIsDisplayed()
        text("2,50 KM").assertIsDisplayed()
    }

    @Test
    fun recommendedRetailPriceIsHiddenWhenNotSet() {
        show(voda)

        compose.onNodeWithText("Preporučena MPC:").assertDoesNotExist()
    }

    @Test
    fun bonusIsShown() {
        show(testProduct(id = "4", name = "Deterdžent", price = 20.0, bonus = 1.5))

        text("Bonus za kupovinu proizvoda:").assertIsDisplayed()
        text("1,50 KM").assertIsDisplayed()
    }

    @Test
    fun quantityStartsAtTheMinimumAndStepsByIt() {
        val component = show(voda)
        assertEquals(6, component.productQty.value)

        text("+").performClick()
        assertEquals(12, component.productQty.value)

        text("+").performClick()
        assertEquals(18, component.productQty.value)

        text("-").performClick()
        assertEquals(12, component.productQty.value)
    }

    @Test
    fun quantityDoesNotGoBelowTheMinimum() {
        val component = show(voda)

        text("-").performClick()

        assertEquals(6, component.productQty.value)
    }

    @Test
    fun changingTheQuantityDoesNotTouchTheCart() {
        val component = show(voda)

        text("+").performClick()
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()

        assertEquals(emptyList(), component.cartUpdates)
        assertEquals(emptyList(), component.cartAdds)
    }

    @Test
    fun addToCartAddsTheChosenQuantity() {
        val component = show(voda)

        text("+").performClick()
        text("Dodaj u Korpu").performClick()

        assertEquals(listOf("Voda 0.5L gajba" to 12), component.cartAdds)
    }

    @Test
    fun soldOutProductCannotBeAdded() {
        show(testProduct(id = "5", name = "Čokolada", price = 3.0, inStock = false))

        compose.onNodeWithText("Dodaj u Korpu").assertDoesNotExist()
        compose.onNodeWithText("RASPRODANO").assertExists()
        text("NEMA NA ZALIHAMA").assertIsDisplayed()
    }

    @Test
    fun messageButtonOpensTheConversationWithTheVendor() {
        val component = show(voda)

        text("Pošalji poruku dobavljaču").performClick()

        assertEquals(listOf(voda), component.messagedAbout)
    }

    @Test
    fun vendorNameOpensTheVendor() {
        val component = show(voda)

        text("Test dobavljač").performClick()

        assertEquals(listOf("Test dobavljač"), component.openedVendors.map { it.publicName })
    }

    @Test
    fun guestDoesNotSeeTheVendor() {
        show(FakeProductComponent(voda, guest = true))

        compose.onNodeWithText("Test dobavljač").assertDoesNotExist()
    }

    @Test
    fun backArrowGoesBack() {
        val component = show(voda)

        compose.onNodeWithContentDescription("Nazad").performClick()

        assertEquals(1, component.backRequests)
    }

    @Test
    fun descriptionToggleIsOnlyThereWithADescription() {
        show(voda)
        compose.onNodeWithText("Opis proizvoda").assertDoesNotExist()
    }

    @Test
    fun descriptionToggleIsShownWithADescription() {
        show(testProduct(id = "6", name = "Sok", price = 3.0, description = "<p>Prirodni sok.</p>"))

        text("Opis proizvoda").assertIsDisplayed()
    }

    @Test
    fun otherProductsOfTheVendorAreListedAndOpen() {
        val sok = testProduct(id = "7", name = "Sok od jabuke 1L", price = 12.5)
        val kafa = testProduct(id = "8", name = "Kafa 500g", price = 24.0)
        val component = show(FakeProductComponent(voda, sameVendor = listOf(sok, kafa)))

        text("Proizvodi istog dobavljača:").assertIsDisplayed()
        compose.onAllNodesWithText("Sok od jabuke 1L").onFirst().assertExists()

        compose.onNodeWithTag(productCardTag(kafa))
            .performScrollTo()
            .performClick()
        assertEquals(listOf(kafa), component.openedProducts)
    }

    @Test
    fun noOtherProductsHidesTheSection() {
        show(voda)

        compose.onNodeWithText("Proizvodi istog dobavljača:").assertDoesNotExist()
    }
}
