package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import karika.distribucija.ba.domain.api.CategoryRepository
import karika.distribucija.ba.domain.api.FaqRepository
import karika.distribucija.ba.domain.api.UserRepository
import karika.distribucija.ba.domain.model.Blog
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.Faq
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-end test of the customer's "Meni" tab on stage.karika.ba, see [CustomerE2ETest]:
 * product categories, the blog, "Samo na Kariki", the FAQ and the contact sheet. Read only.
 */
@OptIn(ExperimentalTestApi::class)
class CustomerMenuE2ETest : CustomerE2ETest() {

    @Before
    fun openMenu() {
        compose.onNode(bottomTab("Meni")).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Kontaktirajte nas"), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun menuListsItsItems() {
        listOf(
            "Kategorije proizvoda", "Blog", "Samo na Kariki", "Često postavljana pitanja", "Kontaktirajte nas"
        ).forEach { assertTrue("\"$it\" is not in the menu", exists(hasText(it), unmerged = true)) }
    }

    @Test
    fun kategorijeProizvodaListsStagesCategoriesAndOpensOne() {
        val categories = categories()
        compose.onNodeWithText("Kategorije proizvoda", useUnmergedTree = true).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Svi proizvodi"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()

        categories.take(5).forEach {
            compose.waitUntilAtLeastOneExists(hasText(it.name), SERVER_TIMEOUT_MS)
        }
        val leaf = categories.firstOrNull { it.childrenData.isEmpty() } ?: categories.first().childrenData.first()
        if (leaf !in categories) {
            compose.onNodeWithText(categories.first().name).performClick()
            compose.waitUntilAtLeastOneExists(hasText(leaf.name), SCREEN_TIMEOUT_MS)
        }
        compose.onNodeWithText(leaf.name).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Filteri"), SERVER_TIMEOUT_MS)
    }

    @Test
    fun blogListsStagesArticlesAndOpensOne() {
        val blogs = blogs()
        compose.onNodeWithText("Blog", useUnmergedTree = true).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Svi blog članci"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()

        val first = blogs.firstOrNull { !it.title.isNullOrBlank() }
        assumeTrue("stage has no blog articles", first != null)
        compose.waitUntilAtLeastOneExists(hasText(first!!.title!!), SERVER_TIMEOUT_MS)
        compose.onNodeWithText(first.title!!).performClick()
        compose.waitUntilDoesNotExist(hasText("Svi blog članci"), SCREEN_TIMEOUT_MS)
        assertTrue(exists(hasText(first.title!!), unmerged = true))
    }

    @Test
    fun samoNaKarikiOpensItsCategory() {
        compose.onNodeWithText("Samo na Kariki", useUnmergedTree = true).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Filteri"), SERVER_TIMEOUT_MS)
        assertTrue(exists(hasText("Samo na Kariki")))
    }

    @Test
    fun faqShowsStagesSections() {
        val faq = faq()
        compose.onNodeWithText("Često postavljana pitanja", useUnmergedTree = true).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Često postavljena pitanja"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()

        faq.mapNotNull { it.section }.take(3).forEach {
            compose.waitUntilAtLeastOneExists(hasText(it), SERVER_TIMEOUT_MS)
        }
    }

    @Test
    fun kontaktirajteNasShowsTheEmailAndPhone() {
        compose.onNodeWithText("Kontaktirajte nas", useUnmergedTree = true).performClick()

        compose.waitUntilAtLeastOneExists(hasText("info@karika.ba"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("033/246-830").assertExists()
    }

    private fun categories(): List<Category> {
        val result = runBlocking { CategoryRepository().get().last() }
        assertTrue("categories: $result", result is ResultState.Success)
        return ((result as ResultState.Success<*>).data as Category).childrenData
    }

    private fun blogs(): List<Blog> {
        val result = runBlocking { UserRepository().blogs().last() }
        assertTrue("blogs: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return (result as ResultState.Success<*>).data as List<Blog>
    }

    private fun faq(): List<Faq> {
        val result = runBlocking { FaqRepository().faq().last() }
        assertTrue("faq: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return (result as ResultState.Success<*>).data as List<Faq>
    }
}
