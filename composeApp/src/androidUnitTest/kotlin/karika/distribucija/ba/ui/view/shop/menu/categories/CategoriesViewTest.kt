package karika.distribucija.ba.ui.view.shop.menu.categories

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.testutil.KarikaUiTest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class CategoriesViewTest : KarikaUiTest() {

    private val sokovi = Category(id = 11, name = "Sokovi")
    private val voda = Category(id = 12, name = "Voda")
    private val pica = Category(id = 1, name = "Pića", childrenData = mutableListOf(sokovi, voda))
    private val svi = Category(name = "SVI PROIZVODI")
    private val kozmetika = Category(id = 2, name = "Kozmetika")

    private fun show(): FakeCategoriesComponent {
        val component = FakeCategoriesComponent(listOf(svi, pica, kozmetika))
        compose.setContent { CategoriesView(component) }
        return component
    }

    /** The "Kategorije proizvoda" link back to the top level; the top bar title has no click. */
    private fun backToTopLevel() = compose.onNode(hasText("Kategorije proizvoda") and hasClickAction())

    @Test
    fun listsTheTopLevelCategories() {
        show()

        compose.onNodeWithText("SVI PROIZVODI").assertIsDisplayed()
        compose.onNodeWithText("Pića").assertIsDisplayed()
        compose.onNodeWithText("Kozmetika").assertIsDisplayed()
        compose.onNodeWithText("Sokovi").assertDoesNotExist()
        backToTopLevel().assertDoesNotExist()
    }

    @Test
    fun categoryWithSubcategoriesOpensThem() {
        val component = show()

        compose.onNodeWithText("Pića").performClick()

        assertEquals(pica, component.subCategory.value)
        compose.onNodeWithText("Sokovi").assertIsDisplayed()
        compose.onNodeWithText("Voda").assertIsDisplayed()
        compose.onNodeWithText("Vidi sve u pića").assertIsDisplayed()
        compose.onNodeWithText("Kozmetika").assertDoesNotExist()
        assertEquals(emptyList(), component.shownProducts)
    }

    @Test
    fun categoryWithoutSubcategoriesOpensItsProducts() {
        val component = show()

        compose.onNodeWithText("Kozmetika").performClick()

        assertEquals(listOf(kozmetika), component.shownProducts)
        assertNull(component.subCategory.value)
    }

    @Test
    fun allProductsOpensTheProductList() {
        val component = show()

        compose.onNodeWithText("SVI PROIZVODI").performClick()

        assertEquals(listOf(svi), component.shownProducts)
    }

    @Test
    fun subcategoryOpensItsProducts() {
        val component = show()
        compose.onNodeWithText("Pića").performClick()

        compose.onNodeWithText("Sokovi").performClick()

        assertEquals(listOf(sokovi), component.shownProducts)
    }

    @Test
    fun vidiSveOpensTheWholeCategory() {
        val component = show()
        compose.onNodeWithText("Pića").performClick()

        compose.onNodeWithText("Vidi sve u pića").performClick()

        assertEquals(listOf(pica), component.shownProducts)
    }

    @Test
    fun backLinkReturnsToTheTopLevel() {
        val component = show()
        compose.onNodeWithText("Pića").performClick()

        backToTopLevel().performClick()

        assertNull(component.subCategory.value)
        compose.onNodeWithText("Kozmetika").assertIsDisplayed()
    }

    @Test
    fun backArrowLeavesCategories() {
        val component = show()

        compose.onNodeWithContentDescription("Nazad").performClick()

        assertEquals(1, component.backRequests)
    }
}
