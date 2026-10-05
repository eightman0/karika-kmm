package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.view.shop.home.productCardTag
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * End-to-end test of what a customer browses on stage.karika.ba, see [CustomerE2ETest]: search,
 * a product's details (quantity, adding to the cart), a vendor's page (its products, "Prikaži
 * rasprodate", search) and a category's products (sort, filter sheet).
 */
@OptIn(ExperimentalTestApi::class)
class CustomerBrowseE2ETest : CustomerE2ETest() {

    // Pretraga

    @Test
    fun searchFindsTheProductByName() {
        val product = recommendedProducts().first()
        val query = product.name().take(12)
        val expected = products(searchText = query)
        assertTrue("stage does not find \"$query\"", expected.any { it.sku == product.sku })

        openSearch()
        compose.onNode(hasSetTextAction()).performTextInput(query)

        compose.waitUntilAtLeastOneExists(hasTestTag(productCardTag(product)), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onNodeWithText("Proizvodi").assertExists()
    }

    @Test
    fun searchWithoutAMatchShowsNoResults() {
        openSearch()
        compose.onNode(hasSetTextAction()).performTextInput("zzqxv nema takvog")

        compose.waitUntilAtLeastOneExists(hasText("Nema rezultata."), SERVER_TIMEOUT_MS)
    }

    // Detalji proizvoda

    @Test
    fun productDetailsShowTheProductFromStage() {
        val product = recommendedProducts().first()
        val details = productById(product)

        openProduct(product)

        listOf("Dostupnost:", "Minimalna količina:").forEach {
            assertTrue("\"$it\" is not shown", exists(hasText(it)))
        }
        assertTrue(count(hasText(details.name())) >= 1)
        assertTrue(exists(hasText("${details.minQty()} ", substring = true), unmerged = true))
    }

    @Test
    fun quantityStepsByTheMinimumAndAddsThatMuchToTheCart() {
        val product = recommendedProducts().firstOrNull { it.hasOnStock() && it.sku !in currentCart().items.map { i -> i.sku } }
        assumeTrue("no recommended product in stock that is not in the cart already", product != null)
        openProduct(product!!)

        // The products of the same vendor load after the product, with a loader that takes taps
        waitUntilLoaded()
        compose.onNodeWithText("+", useUnmergedTree = true).performScrollTo().performClick()
        compose.waitUntil(SCREEN_TIMEOUT_MS) { exists(hasText("${2 * product.minQty()}"), unmerged = true) }
        waitUntilLoaded()
        compose.onNodeWithText("Dodaj u Korpu").performScrollTo().performClick()

        // The cart on stage gets two minimum quantities
        compose.waitUntil(SERVER_TIMEOUT_MS) {
            Thread.sleep(500)
            currentCart().items.any { it.sku == product.sku }
        }
        assertEquals(2 * product.minQty(), currentCart().items.single { it.sku == product.sku }.qty)
    }

    @Test
    fun vendorLinkOnTheProductOpensTheVendor() {
        val product = recommendedProducts().first()
        openProduct(product)

        compose.onAllNodesWithText(product.vendorName(), useUnmergedTree = true).onFirst().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Pošalji poruku dobavljaču"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        assertTrue(count(hasText(product.vendorName())) >= 1)
    }

    // Stranica dobavljača

    @Test
    fun vendorPageListsItsProductsInStock() {
        val product = recommendedProducts().first { it.vendorId()?.toIntOrNull() != null }
        val vendorProducts = products(vendorId = product.vendorId()!!.toInt())
        openVendorOf(product)

        vendorProducts.take(4).forEach {
            compose.waitUntilAtLeastOneExists(hasTestTag(productCardTag(it)), SERVER_TIMEOUT_MS)
        }
    }

    @Test
    fun prikaziRasprodateShowsSoldOutProductsToo() {
        val product = recommendedProducts().first { it.vendorId()?.toIntOrNull() != null }
        val vendorId = product.vendorId()!!.toInt()
        val inStock = products(vendorId = vendorId)
        val soldOut = products(vendorId = vendorId, isInStock = "1").firstOrNull { p -> inStock.none { it.sku == p.sku } }
        assumeTrue("the vendor has no sold out product", soldOut != null)
        openVendorOf(product)
        compose.onNodeWithTag(productCardTag(soldOut!!)).assertDoesNotExist()

        compose.onAllNodesWithText("Prikaži rasprodate").onFirst().performClick()

        compose.waitUntilAtLeastOneExists(hasTestTag(productCardTag(soldOut)), SERVER_TIMEOUT_MS)
    }

    // Kategorija s proizvodima

    @Test
    fun sortingByPriceOrdersTheProducts() {
        compose.onNodeWithText("AKCIJE").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Filteri"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()

        compose.onAllNodesWithText("Najnoviji", useUnmergedTree = true).onFirst().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Najjeftiniji"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Najjeftiniji").performClick()

        compose.waitUntilAtLeastOneExists(hasText("Najjeftiniji"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onNodeWithText("Najnoviji").assertDoesNotExist()
    }

    @Test
    fun filterSheetOffersPriceVendorsAndRegions() {
        compose.onNodeWithText("AKCIJE").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Filteri"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()

        compose.onNodeWithText("Filteri").performClick()

        compose.waitUntilAtLeastOneExists(hasText("FILTERI"), SCREEN_TIMEOUT_MS)
        listOf("CIJENA", "DOBAVLJAČI", "REGIJA", "Svi regioni", "Primijeni").forEach {
            assertTrue("\"$it\" is not in the sheet", exists(hasText(it)))
        }
        compose.onNodeWithText("Zatvori").performClick()
        compose.waitUntilDoesNotExist(hasText("FILTERI"), SCREEN_TIMEOUT_MS)
    }

    private fun openSearch() {
        compose.onNodeWithText("Pretraži..", useUnmergedTree = true).performClick()
        compose.waitUntilDoesNotExist(hasText("OUTLET"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
    }

    private fun openProduct(product: Product) {
        compose.waitUntilAtLeastOneExists(hasTestTag(productCardTag(product)), SERVER_TIMEOUT_MS)
        compose.onNodeWithTag(productCardTag(product)).performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Minimalna količina:"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
    }

    private fun openVendorOf(product: Product) {
        openProduct(product)
        compose.onAllNodesWithText(product.vendorName(), useUnmergedTree = true).onFirst().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Pošalji poruku dobavljaču"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onNodeWithText("Prikaži rasprodate").assertIsDisplayed()
    }

    private fun productById(product: Product): Product {
        val result = runBlocking { ProductRepository().productById(product.entityId!!).last() }
        assertTrue("product: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return ((result as ResultState.Success<*>).data as List<Product>).first()
    }

    private fun products(searchText: String = "", vendorId: Int? = null, isInStock: String = ""): List<Product> {
        val result = runBlocking {
            ProductRepository().searchProductsByCategory(
                vendorId = vendorId, searchText = searchText, isInStock = isInStock
            ).last()
        }
        assertTrue("products: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return (result as ResultState.Success<*>).data as List<Product>
    }
}
