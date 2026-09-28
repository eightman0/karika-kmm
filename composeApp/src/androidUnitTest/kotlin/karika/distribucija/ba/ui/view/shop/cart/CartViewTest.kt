package karika.distribucija.ba.ui.view.shop.cart

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.testutil.KarikaUiTest
import karika.distribucija.ba.testutil.testProduct
import kotlin.test.assertEquals
import org.junit.Test

class CartViewTest : KarikaUiTest() {

    private val sok = testProduct(id = "1", name = "Sok od jabuke 1L", price = 10.0, minQty = 2).copy(itemId = 501)

    private fun vendor(minimum: String? = null) =
        Vendor(entityId = 7, publicName = "Mljekara d.o.o.", minOrderAmount = minimum)

    private fun show(items: Map<Vendor, List<Pair<Product, Int>>>): FakeCartComponent {
        val component = FakeCartComponent(items)
        compose.setContent { CartView(component) }
        return component
    }

    /** Two cans of juice (20 KM, 23,40 KM with PDV) from a vendor with [minimum]. */
    private fun showSok(minimum: String? = null) = show(mapOf(vendor(minimum) to listOf(sok to 2)))

    @Test
    fun emptyCartSaysSo() {
        show(emptyMap())

        compose.onNodeWithText("Nema artikala u korpi.").assertIsDisplayed()
        compose.onNodeWithText("Nastavi dalje").assertDoesNotExist()
    }

    @Test
    fun listsTheProductsPerVendorWithPrices() {
        showSok()

        compose.onNodeWithText("Pregled korpe:").assertIsDisplayed()
        compose.onNodeWithText("Mljekara d.o.o.").assertExists()
        compose.onNodeWithText("Sok od jabuke 1L").assertExists()
        compose.onNodeWithText("20,00").assertExists()
        compose.onNodeWithText("23,40").assertExists()
        compose.onNodeWithText("2 kom").assertExists()
    }

    @Test
    fun footerShowsTheTotalWithPdv() {
        showSok()

        compose.onNodeWithText("Ukupno sa PDV:").assertIsDisplayed()
        compose.onNodeWithText("23,40 KM").assertIsDisplayed()
    }

    @Test
    fun vendorMinimumShowsWhatIsMissing() {
        showSok(minimum = "100")

        compose.onNodeWithText("Minimum: 100KM", substring = true).assertExists()
        compose.onNodeWithText("Nedostaje: 76,60KM", substring = true).assertExists()
    }

    @Test
    fun vendorWithoutMinimumShowsNoMinimum() {
        showSok()

        compose.onNodeWithText("Minimum:", substring = true).assertDoesNotExist()
    }

    @Test
    fun cannotContinueBelowAVendorsMinimum() {
        val component = showSok(minimum = "100")

        compose.onNodeWithText("Nastavi dalje").performClick()

        assertEquals(0, component.shippingRequests)
        assertEquals(
            listOf<String?>("Nije zadovoljena minimalna vrijednost narudžbe za dobavljača!"),
            component.messages
        )
    }

    @Test
    fun continuesToShippingOnceEveryMinimumIsReached() {
        val component = showSok(minimum = "20")

        compose.onNodeWithText("Nastavi dalje").performClick()

        assertEquals(1, component.shippingRequests)
        assertEquals(emptyList(), component.messages)
    }

    @Test
    fun emptyingTheCartAsksFirst() {
        val component = showSok()

        compose.onNodeWithText("Isprazni korpu").performClick()
        compose.onNodeWithText("Da li želite da ispraznite korpu?").assertIsDisplayed()
        assertEquals(0, component.clearRequests)

        compose.onNodeWithText("Da").performClick()

        assertEquals(1, component.clearRequests)
        compose.onNodeWithText("Da li želite da ispraznite korpu?").assertDoesNotExist()
    }

    @Test
    fun notConfirmingKeepsTheCart() {
        val component = showSok()

        compose.onNodeWithText("Isprazni korpu").performClick()
        compose.onNodeWithText("Ne").performClick()

        assertEquals(0, component.clearRequests)
        compose.onNodeWithText("Da li želite da ispraznite korpu?").assertDoesNotExist()
    }

    @Test
    fun removeIconRemovesTheProduct() {
        val component = showSok()

        compose.onNodeWithContentDescription("Ukloni iz korpe").performClick()

        assertEquals(listOf(sok), component.removed)
    }

    @Test
    fun changingTheQuantityUpdatesTheCartAfterAPause() {
        val component = showSok()

        compose.onNodeWithText("+").performScrollTo().performClick()
        assertEquals(emptyList(), component.cartUpdates)

        compose.mainClock.advanceTimeBy(QTY_DEBOUNCE_MS)
        compose.waitForIdle()

        assertEquals(listOf("Sok od jabuke 1L" to 4), component.cartUpdates)
    }

    @Test
    fun vendorNameOpensTheVendor() {
        val component = showSok()

        compose.onNodeWithText("Mljekara d.o.o.").performClick()

        assertEquals(listOf("Mljekara d.o.o."), component.openedVendors.map { it.publicName })
    }

    private companion object {
        const val QTY_DEBOUNCE_MS = 700L
    }
}
