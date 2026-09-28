package karika.distribucija.ba.ui.view.shop.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.testutil.KarikaUiTest
import karika.distribucija.ba.testutil.testProduct
import kotlin.test.assertEquals
import org.junit.Test

class HomeViewTest : KarikaUiTest() {

    private val sok = testProduct(id = "1", name = "Sok od jabuke 1L", price = 12.5)
    private val kafa = testProduct(id = "2", name = "Kafa 500g", price = 24.0, specialPrice = 18.0)

    private fun show(component: FakeHomeComponent): FakeHomeComponent {
        compose.setContent { HomeView(component) }
        return component
    }

    @Test
    fun loadsDataOnceWhenShown() {
        val component = show(FakeHomeComponent())
        compose.waitForIdle()

        assertEquals(1, component.loads)
    }

    @Test
    fun showsNoSectionsWhileNothingIsLoaded() {
        show(FakeHomeComponent())

        compose.onNodeWithText("Karika preporučuje:").assertDoesNotExist()
        compose.onNodeWithText("Vidi sve").assertDoesNotExist()
        compose.onNodeWithText("Dobavljači").assertDoesNotExist()
    }

    @Test
    fun showsRecommendedProductsWithNameAndPrice() {
        show(FakeHomeComponent(listOf(sok, kafa)))

        compose.onNodeWithText("Karika preporučuje:").assertIsDisplayed()
        compose.onNodeWithText("Sok od jabuke 1L").assertExists()
        compose.onNodeWithText("12,50 KM").assertExists()
        compose.onNodeWithText("Kafa 500g").assertExists()
    }

    @Test
    fun productsAppearWhenTheyArrive() {
        val component = show(FakeHomeComponent())
        compose.onNodeWithText("Karika preporučuje:").assertDoesNotExist()

        component.newArrivals.value = listOf(sok)

        compose.onNodeWithText("Karika preporučuje:").assertIsDisplayed()
        compose.onNodeWithText("Sok od jabuke 1L").assertExists()
    }

    @Test
    fun vidiSveOpensAllRecommendedProducts() {
        val component = show(FakeHomeComponent(listOf(sok)))

        compose.onNodeWithText("Vidi sve").performClick()

        assertEquals(1, component.recommendedRequests)
    }

    @Test
    fun tappingAProductOpensIt() {
        val component = show(FakeHomeComponent(listOf(sok, kafa)))

        compose.onNodeWithTag(productCardTag(kafa)).performScrollTo().performClick()

        assertEquals(listOf("Kafa 500g"), component.openedProducts.map { it.name() })
    }

    @Test
    fun cartButtonAddsTheMinimumQuantity() {
        val gajba = testProduct(id = "3", name = "Voda 0.5L gajba", price = 9.0, minQty = 6)
        val component = show(FakeHomeComponent(listOf(gajba)))

        compose.onNodeWithContentDescription("Dodaj u korpu").performClick()

        assertEquals(listOf("Voda 0.5L gajba" to 6), component.cartAdds)
        assertEquals(emptyList(), component.openedProducts)
    }

    @Test
    fun soldOutProductIsMarkedAndCannotBeAdded() {
        val soldOut = testProduct(id = "4", name = "Čokolada", price = 3.0, inStock = false)
        show(FakeHomeComponent(listOf(soldOut)))

        compose.onNodeWithText("RASPRODANO").assertExists()
        compose.onNodeWithContentDescription("Dodaj u korpu").assertDoesNotExist()
    }

    @Test
    fun discountedProductShowsPercentAndDiscountedPrice() {
        show(FakeHomeComponent(listOf(kafa)))

        compose.onNodeWithText("-25%").assertExists()
        compose.onNodeWithText("18,00 KM").assertExists()
        compose.onNodeWithText("24,00 KM").assertDoesNotExist()
    }

    @Test
    fun productWithoutDiscountHasNoDiscountBadge() {
        show(FakeHomeComponent(listOf(sok)))

        compose.onNodeWithText("-", substring = true).assertDoesNotExist()
    }

    @Test
    fun bonusProductShowsBonusAmount() {
        val bonus = testProduct(id = "5", name = "Deterdžent", price = 20.0, bonus = 1.5)
        show(FakeHomeComponent(listOf(bonus)))

        compose.onNodeWithText("1,50 KM").assertExists()
    }

    @Test
    fun newProductIsMarkedNovo() {
        val novo = testProduct(id = "6", name = "Novi keks", price = 2.0, isNew = true)
        show(FakeHomeComponent(listOf(novo, sok)))

        compose.onNodeWithText("Novo").assertExists()
    }

    @Test
    fun loggedInCustomerSeesAndCanOpenTheVendor() {
        val component = show(FakeHomeComponent(listOf(sok)))

        compose.onNodeWithText("Test dobavljač").performScrollTo().performClick()

        assertEquals(listOf("Test dobavljač"), component.openedVendors.map { it.publicName })
    }

    @Test
    fun guestDoesNotSeeTheVendor() {
        show(FakeHomeComponent(listOf(sok), guest = true))

        compose.onNodeWithText("Sok od jabuke 1L").assertExists()
        compose.onNodeWithText("Test dobavljač").assertDoesNotExist()
    }

    @Test
    fun promotedVendorLogosGetTheirSection() {
        val logo = PromotedVendor(
            promoteVendorLogo = true,
            entityId = "7",
            name = "Test dobavljač",
            shopUrl = null,
            companyLogo = "logo.png",
            companyBanner = null,
            description = null,
            categories = null,
        )
        show(FakeHomeComponent(logos = listOf(logo)))

        compose.onNodeWithText("Dobavljači").performScrollTo().assertIsDisplayed()
    }
}
