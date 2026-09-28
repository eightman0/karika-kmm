package karika.distribucija.ba.ui.view.shop.menu.categories.products

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.testutil.LiveShopTest
import karika.distribucija.ba.util.KarikaConfig
import kotlin.test.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * The product list of a category for the logged-in customer test account against the real
 * backend, through the real ProductByCategoryView and DefaultProductByCategoryComponent. It
 * uses the "Karika preporučuje" category, which the home screen shows as well.
 */
class LiveProductByCategoryApiTest : LiveShopTest() {

    private lateinit var component: DefaultProductByCategoryComponent

    private val recommended = Category(id = KarikaConfig.getKarikaProductsId(), name = "Karika preporučuje")

    private fun showCategory(): List<Product> {
        component = DefaultProductByCategoryComponent(componentContext(), stateHolder, recommended)
        compose.setContent { ProductByCategoryView(component) }
        waitForServer { component.products.value.isNotEmpty() && !component.loader.value }
        return component.products.value
    }

    /**
     * Waits until the list reloads after a filter change: the loader comes on and goes off
     * again. (The results can come back in the same order, so a changed list is no signal.)
     * It stays on for at least 700 ms of looper time, so the polling cannot miss it.
     */
    private fun waitForReload() {
        var loading = false
        waitForServer {
            loading = loading || component.loader.value
            loading && !component.loader.value
        }
    }

    @Test
    fun productsOfTheCategoryLoad() {
        val products = showCategory()

        compose.onNodeWithText("Karika preporučuje").assertExists()
        assertTrue(products.isNotEmpty())
    }

    @Test
    fun soldOutProductsAreLeftOutByDefault() {
        val products = showCategory()

        val soldOut = products.filterNot { it.hasOnStock() }.map { it.name() }
        assertTrue(soldOut.isEmpty(), "sold out products listed without \"Prikaži rasprodate\": $soldOut")
    }

    @Test
    fun cheapestFirstSortsByPrice() {
        val before = showCategory()
        assumeMoreThanOne(before)

        compose.onNodeWithText("Najnoviji").performClick()
        compose.onNodeWithText("Najjeftiniji").performClick()
        waitForReload()

        val prices = component.products.value.map { it.price() }
        assertTrue(prices == prices.sorted(), "not cheapest first: $prices")
    }

    @Test
    fun mostExpensiveFirstSortsByPrice() {
        val before = showCategory()
        assumeMoreThanOne(before)

        compose.onNodeWithText("Najnoviji").performClick()
        compose.onNodeWithText("Najskuplji").performClick()
        waitForReload()

        val prices = component.products.value.map { it.price() }
        assertTrue(prices == prices.sortedDescending(), "not most expensive first: $prices")
    }

    @Test
    fun searchingInTheCategoryFindsTheProduct() {
        val before = showCategory()
        val target = before.last()

        compose.onNodeWithTag(CategoryProductsTestTags.SEARCH).performTextInput(target.name())
        compose.mainClock.advanceTimeBy(DEBOUNCE_MS)
        waitForReload()

        val skus = component.products.value.map { it.sku }
        assertTrue(target.sku in skus, "'${target.name()}' not found in the category: $skus")
    }

    @Test
    fun priceFilterKeepsProductsInTheRange() {
        val before = showCategory()
        val prices = before.map { it.currentPrice() }.sorted()
        assumeMoreThanOne(before)
        val from = prices.first().toInt()
        val to = prices[prices.size / 2].toInt() + 1

        compose.onNodeWithText("Filteri").performClick()
        compose.onNodeWithTag(CategoryProductsTestTags.PRICE_FROM).performTextInput("$from")
        compose.onNodeWithTag(CategoryProductsTestTags.PRICE_TO).performTextInput("$to")
        compose.onNodeWithText("Primijeni").performClick()
        waitForReload()

        // Either the list price or the discounted one may be what the backend filters on
        val range = from.toDouble()..to.toDouble()
        val outside = component.products.value
            .filterNot { it.price() in range || it.currentPrice() in range }
            .map { "${it.name()} ${it.currentPrice()}" }
        assertTrue(outside.isEmpty(), "outside $from..$to KM: $outside")
    }

    private fun assumeMoreThanOne(products: List<Product>) {
        assumeTrue("the category has a single product", products.size > 1)
    }

    private companion object {
        const val DEBOUNCE_MS = 600L
    }
}
