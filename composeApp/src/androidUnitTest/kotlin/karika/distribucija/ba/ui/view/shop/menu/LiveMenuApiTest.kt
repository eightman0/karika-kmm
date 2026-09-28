package karika.distribucija.ba.ui.view.shop.menu

import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.testutil.LiveShopTest
import karika.distribucija.ba.ui.view.shop.MainConfig
import karika.distribucija.ba.ui.view.shop.menu.blog.BlogsComponent
import karika.distribucija.ba.ui.view.shop.menu.blog.BlogsView
import karika.distribucija.ba.ui.view.shop.menu.blog.overview.BlogOverviewComponent
import karika.distribucija.ba.ui.view.shop.menu.blog.overview.BlogOverviewView
import karika.distribucija.ba.ui.view.shop.menu.faq.FaqComponent
import karika.distribucija.ba.ui.view.shop.menu.faq.FaqView
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * The shop's menu, blog and FAQ for the logged-in customer test account against the real
 * backend, through the real views and components.
 */
class LiveMenuApiTest : LiveShopTest() {

    private fun showMenu() {
        val component = MenuComponent(componentContext(), stateHolder)
        compose.setContent { MenuView(component) }
    }

    private fun tap(text: String) {
        compose.onNodeWithText(text).performScrollTo().performClick()
    }

    @Test
    fun kategorijeOpensTheCategories() {
        showMenu()

        tap("Kategorije proizvoda")
        waitForServer { openedInShop != null }

        assertEquals(MainConfig.Categories, openedInShop)
    }

    @Test
    fun blogOpensTheBlog() {
        showMenu()

        tap("Blog")
        waitForServer { openedInApp != null }

        assertEquals(AppConfig.Blogs, openedInApp)
    }

    @Test
    fun faqOpensTheQuestions() {
        showMenu()

        tap("Često postavljana pitanja")
        waitForServer { openedInApp != null }

        assertEquals(AppConfig.Faq, openedInApp)
    }

    @Test
    fun samoNaKarikiOpensACategoryThatHasProducts() {
        showMenu()

        tap("Samo na Kariki")
        waitForServer { openedInShop != null }

        val category = assertIs<MainConfig.CategoryProducts>(openedInShop).category
        assertEquals("Samo na Kariki", category.name)
        val result = runBlocking {
            ProductRepository().searchProductsByCategory(categoryId = category.getAllCategoryIds()).last()
        }
        @Suppress("UNCHECKED_CAST")
        val products = assertIs<ResultState.Success<*>>(result, "category ${category.id}: $result").data as List<Product>
        // The id is fixed in MenuComponent, not taken per environment like the other categories
        assertTrue(products.isNotEmpty(), "category ${category.id} has no products on this backend")
    }

    @Test
    fun kontaktShowsEmailAndPhone() {
        showMenu()

        tap("Kontaktirajte nas")

        compose.onNodeWithText("info@karika.ba").assertExists()
        compose.onNodeWithText("033/246-830").assertExists()
    }

    @Test
    fun blogListsArticlesThatOpen() {
        val component = BlogsComponent(componentContext(), stateHolder)
        compose.setContent { BlogsView(component) }
        waitForServer { component.blogs.value.isNotEmpty() || !component.loader.value && snackbarMessage() != null }
        val blog = component.blogs.value.firstOrNull()
        assumeTrue("the backend has no blog articles", blog != null)
        blog!!

        compose.onNodeWithText("Svi blog članci").assertExists()
        compose.onAllNodesWithText(blog.title ?: "").onFirst().performScrollTo().performClick()
        waitForServer { openedInApp != null }

        assertEquals(blog, assertIs<AppConfig.Blog>(openedInApp).blog)
    }

    @Test
    fun blogArticleLoads() {
        val list = BlogsComponent(componentContext(), stateHolder)
        waitForServer { list.blogs.value.isNotEmpty() || !list.loader.value && snackbarMessage() != null }
        val blog = list.blogs.value.firstOrNull()
        assumeTrue("the backend has no blog articles", blog != null)

        val component = BlogOverviewComponent(componentContext(), stateHolder, blog!!)
        compose.setContent { BlogOverviewView(component) }
        waitForServer { !component.loader.value }

        compose.onAllNodesWithText(blog.title ?: "").onFirst().assertExists()
    }

    @Test
    fun faqSectionsOpenToTheirQuestions() {
        val component = FaqComponent(componentContext(), stateHolder)
        compose.setContent { FaqView(component) }
        waitForServer { component.faq.value.isNotEmpty() || snackbarMessage() != null }
        val section = component.faq.value.firstOrNull { !it.items.isNullOrEmpty() }
        assumeTrue("the backend has no FAQ", section != null)
        section!!
        val question = section.items!!.first().question ?: ""

        compose.onNodeWithText("Često postavljena pitanja").assertExists()
        compose.onNodeWithText(question).assertDoesNotExist()
        tap(section.section ?: "")

        compose.onNodeWithText(question).assertExists()
    }
}
