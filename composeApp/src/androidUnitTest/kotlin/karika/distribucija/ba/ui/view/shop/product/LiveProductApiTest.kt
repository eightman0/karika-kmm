package karika.distribucija.ba.ui.view.shop.product

import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.testutil.LiveShopTest
import karika.distribucija.ba.ui.view.shop.home.productCardTag
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * The product details of the logged-in customer test account against the real backend,
 * through the real ProductView and DefaultProductComponent, for products from the
 * "Karika preporučuje" list.
 */
class LiveProductApiTest : LiveShopTest() {

    private lateinit var component: DefaultProductComponent

    private fun showProduct(product: Product) {
        component = DefaultProductComponent(componentContext(), stateHolder, product)
        compose.setContent { ProductView(component) }
        compose.waitForIdle()
    }

    /** The vendor's other products, as the details screen asks for them. */
    private fun otherProductsOfVendor(product: Product): List<Product> {
        val result = runBlocking {
            ProductRepository().searchProductsByCategory(
                currentPage = 1,
                vendorId = product.vendorId().toIntOrNull()
            ).last()
        }
        @Suppress("UNCHECKED_CAST")
        val products = assertIs<ResultState.Success<*>>(result, "vendor products: $result").data as List<Product>
        return products.filter { it.entityId != product.entityId }
    }

    @Test
    fun detailsShowTheProduct() {
        val product = recommendedProducts().first()

        showProduct(product)

        compose.onAllNodesWithText(product.name()).onFirst().assertExists()
        compose.onNodeWithText("Dostupnost:").performScrollTo()
        compose.onNodeWithText(product.isInStockLabel().uppercase()).assertExists()
        compose.onNodeWithText("Minimalna količina:").assertExists()
        val price = if (product.hasSpecialPrice()) product.specialPriceString() else product.originalPriceString()
        compose.onAllNodesWithText(price).onFirst().assertExists()
    }

    @Test
    fun otherProductsOfTheVendorAreListed() {
        val product = recommendedProducts().firstOrNull { otherProductsOfVendor(it).size > 1 }
        assumeTrue("no recommended product's vendor has two other products", product != null)
        product!!
        val expected = otherProductsOfVendor(product)

        showProduct(product)
        waitForServer { component.products.value.isNotEmpty() }

        assertEquals(expected.map { it.sku }, component.products.value.map { it.sku })
        compose.onNodeWithText("Proizvodi istog dobavljača:").performScrollTo()
        compose.onNodeWithTag(productCardTag(expected.first())).assertExists()
    }

    @Test
    fun addToCartAddsTheChosenQuantity() {
        useCustomerCart()
        val skusInCart = currentCart().items.map { it.sku }.toSet()
        val product = recommendedProducts().firstOrNull { it.hasOnStock() && it.sku !in skusInCart }
        assumeTrue("every recommended product is sold out or already in the cart", product != null)
        product!!
        val stock = product.stockData?.salableQty ?: Long.MAX_VALUE
        val stepUp = stock >= 2L * product.minQty()

        showProduct(product)
        if (stepUp) {
            compose.onNodeWithText("+").performScrollTo().performClick()
        }
        val qty = component.productQty.value
        assertEquals(if (stepUp) 2 * product.minQty() else product.minQty(), qty)

        dismissSnackbar()
        compose.onNodeWithText("Dodaj u Korpu").performScrollTo().performClick()
        waitForServer { snackbarMessage() != null }
        val message = snackbarMessage()

        val item = currentCart().items.find { it.sku == product.sku }
        item?.itemId?.let { cartItemsToRemove += it }
        assertNotNull(item, "${product.sku} is not in the cart, message: $message")
        assertEquals(qty, item.qty)
        assertEquals("Proizvod dodan u korpu!", message)
    }

    @Test
    fun messageButtonOpensTheConversationWithTheVendor() {
        val product = recommendedProducts().first()

        showProduct(product)
        compose.onNodeWithText("Pošalji poruku dobavljaču").performScrollTo().performClick()
        waitForServer { openedInApp != null }

        val conversation = assertIs<AppConfig.MessagesOverview>(openedInApp).conversation
        assertEquals(product.vendorId, conversation.vendorId)
        assertEquals(product.name, conversation.subject)
    }
}
