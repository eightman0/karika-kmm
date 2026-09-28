package karika.distribucija.ba.ui.view.shop.menu.categories

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.testutil.LiveShopTest
import karika.distribucija.ba.ui.view.shop.MainConfig
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * The category tree of the logged-in customer test account against the real backend,
 * through the real CategoriesView and DefaultCategoriesComponent.
 */
class LiveCategoriesApiTest : LiveShopTest() {

    private lateinit var component: DefaultCategoriesComponent

    private fun showCategories(): List<Category> {
        component = DefaultCategoriesComponent(componentContext(), stateHolder)
        compose.setContent { CategoriesView(component) }
        waitForServer { component.categories.value.isNotEmpty() || snackbarMessage() != null }
        return component.categories.value
    }

    @Test
    fun categoriesLoadWithAllProductsFirst() {
        val categories = showCategories()

        assertEquals("SVI PROIZVODI", categories.first().name)
        assertTrue(categories.size > 1, "no categories besides SVI PROIZVODI")
        compose.onNodeWithText(categories[1].name).performScrollTo()
    }

    @Test
    fun allProductsOpensTheProductList() {
        showCategories()

        compose.onNodeWithText("SVI PROIZVODI").performClick()
        waitForServer { openedInShop != null }

        assertEquals("SVI PROIZVODI", assertIs<MainConfig.CategoryProducts>(openedInShop).category.name)
    }

    @Test
    fun categoryWithSubcategoriesOpensThemAndASubcategoryItsProducts() {
        val parent = showCategories().firstOrNull { it.childrenData.isNotEmpty() }
        assumeTrue("no category has subcategories", parent != null)
        parent!!
        val child = parent.childrenData.first()

        compose.onNodeWithText(parent.name).performScrollTo().performClick()
        compose.onNodeWithText("Vidi sve u ${parent.name.lowercase()}").assertExists()
        compose.onNodeWithText(child.name).performScrollTo().performClick()
        waitForServer { openedInShop != null }

        assertEquals(child.id, assertIs<MainConfig.CategoryProducts>(openedInShop).category.id)
    }
}
