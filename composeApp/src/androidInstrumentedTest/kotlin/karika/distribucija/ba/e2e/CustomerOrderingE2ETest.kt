package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import karika.distribucija.ba.domain.api.OrdersRepository
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.view.shop.cart.cartProductTag
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * End-to-end test of ordering as the customer on stage.karika.ba, see [CustomerE2ETest]: placing
 * an order from the cart (with the default address and a note, and with a new address), and then,
 * on an order of its own, "Otkaži narudžbu", comments, "Naruči ponovo" and the status filter. The
 * orders stay on stage; each test fills the cart through the API to the vendor's minimum first.
 */
@OptIn(ExperimentalTestApi::class)
class CustomerOrderingE2ETest : CustomerE2ETest() {

    // Završi narudžbu

    @Test
    fun zavrsiNarudzbuPlacesTheOrderWithTheDefaultAddressAndANote() {
        assumeTrue("the customer has no default shipping address", user().shippingAddress() != null)
        val product = fillCartToTheVendorMinimum()
        reopenApp()
        openShippingDetails(product)
        val defaultAddress = user().shippingAddress()!!.address()
        assertTrue("the default address is not offered", exists(hasText(defaultAddress, substring = true), unmerged = true))

        compose.onNode(hasSetTextAction() and hasText("Napomena za dobavljaca (opcionalno)"))
            .performTextInput("E2E napomena " + uniqueLetters())
        closeKeyboard()
        compose.onNodeWithText("Završi narudžbu").performClick()

        val order = assertOrderPlaced()
        assertTrue("the order is not pending", order.orders.all { it.status == "pending" })
        assertTrue("the cart is not empty after the order", currentCart().items.isEmpty())
        compose.onNodeWithText("Nastavi kupovati").performClick()
        compose.waitUntilAtLeastOneExists(bottomTab("Početna"), SCREEN_TIMEOUT_MS)

        openOrders()
        compose.waitUntilAtLeastOneExists(hasText("#${order.incrementId}", substring = true), SERVER_TIMEOUT_MS)
        assertTrue(exists(hasText("Na čekanju"), unmerged = true))
    }

    @Test
    fun dodajNovuAdresuSavesTheAddressAndOrdersToIt() {
        val product = fillCartToTheVendorMinimum()
        reopenApp()
        openShippingDetails(product)
        val letters = uniqueLetters()
        val street = "Nova ulica " + letters + " " + uniqueDigits(2).trimStart('0').ifEmpty { "3" }
        val phone = "06" + uniqueDigits(7)

        compose.onNodeWithText("Dodaj novu adresu").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Spasi i nastavi dalje"), SCREEN_TIMEOUT_MS)
        compose.onNode(hasText("Spasi i nastavi dalje") and hasClickAction()).assertIsNotEnabled()
        type("Ime", "Nova" + letters)
        type("Prezime", "Adresa" + letters)
        type("Grad", "Sarajevo")
        type("Adresa i broj ulice", street)
        type("Poštanski broj", "7100")
        type("Broj telefona", phone)
        closeKeyboard()
        // Four digits are not a postcode
        compose.onNode(hasText("Spasi i nastavi dalje") and hasClickAction()).assertIsNotEnabled()
        compose.onNode(hasSetTextAction() and hasText("7100")).performTextReplacement("71000")
        closeKeyboard()
        compose.onNode(hasText("Spasi i nastavi dalje") and hasClickAction()).assertIsEnabled().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Završi narudžbu"), SCREEN_TIMEOUT_MS)
        assertTrue("the new address is not offered", exists(hasText(street, substring = true), unmerged = true))
        compose.onNodeWithText("Završi narudžbu").performClick()

        val order = assertOrderPlaced()
        assertEquals(street, order.shippingAddress?.street?.firstOrNull() ?: street)
        compose.waitUntil(SERVER_TIMEOUT_MS) { user().addresses.any { it.street.firstOrNull() == street } }
    }

    @Test
    fun odustaniOnTheShippingDetailsGoesBackToTheCart() {
        val product = fillCartToTheVendorMinimum()
        reopenApp()
        openShippingDetails(product)

        compose.onAllNodesWithText("Odustani").onFirst().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Pregled korpe:"), SCREEN_TIMEOUT_MS)
        assertTrue("the cart lost the product", currentCart().items.any { it.sku == product.sku })
    }

    // Narudžba poslije

    @Test
    fun otkaziNarudzbuCancelsTheOrder() {
        val order = placeOrderByApi()
        openOrderDetails(order)

        compose.onAllNodesWithText("Otkaži narudžbu").onFirst().performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Otkazivanje narudžbe"), SCREEN_TIMEOUT_MS)
        listOf("Razlog otkazivanja narudžbe:", "Pogrešna količina/artikal", "Dobavljač ne odgovara na narudžbu", "Ostalo").forEach {
            assertTrue("\"$it\" is not in the dialog", exists(hasText(it), unmerged = true))
        }
        compose.onNodeWithText("Dobavljač ne odgovara na narudžbu").performClick()
        compose.onNode(hasSetTextAction() and hasText("Upiši razlog otkazivanja")).performTextInput("E2E otkazivanje")
        compose.onNode(dialogButton("Potvrdi")).performClick()

        compose.waitUntilDoesNotExist(hasText("Otkazivanje narudžbe"), SCREEN_TIMEOUT_MS)
        compose.waitUntil(SERVER_TIMEOUT_MS) { orderOnStage(order).orders.all { it.status == "cancelled" } }
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText("Otkazana"), unmerged = true) }
    }

    @Test
    fun odustaniInTheCancelDialogKeepsTheOrder() {
        val order = placeOrderByApi()
        openOrderDetails(order)

        compose.onAllNodesWithText("Otkaži narudžbu").onFirst().performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Otkazivanje narudžbe"), SCREEN_TIMEOUT_MS)
        compose.onNode(dialogButton("Odustani")).performClick()

        compose.waitUntilDoesNotExist(hasText("Otkazivanje narudžbe"), SCREEN_TIMEOUT_MS)
        assertTrue("Odustani cancelled the order", orderOnStage(order).orders.all { it.status == "pending" })
    }

    @Test
    fun aCommentIsSentAndShownOnTheOrder() {
        val order = placeOrderByApi()
        val vendorOrder = order.orders.first()
        val text = "E2E komentar " + uniqueLetters()
        openOrderDetails(order)

        compose.onAllNodesWithText("Komentari(", substring = true).onFirst().performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasSetTextAction() and hasText("Napiši komentar"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onNode(hasText("Pošalji") and hasClickAction()).assertIsNotEnabled()
        compose.onNode(hasSetTextAction() and hasText("Napiši komentar")).performTextInput(text)
        closeKeyboard()
        compose.onNode(hasText("Pošalji") and hasClickAction()).assertIsEnabled().performClick()

        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText(text, substring = true), unmerged = true) }
        val last = comments(vendorOrder.orderId, vendorOrder.vendorId.toString()).last()
        assertEquals(text, last.message())
        assertTrue("the comment is not the customer's", last.isMine())
    }

    @Test
    fun naruciPonovoPutsTheOrdersProductsInTheCart() {
        val placed = placeOrderByApi()
        val products = placed.orders.flatMap { it.products }.mapNotNull { it.sku }
        assumeTrue("the order has no product skus", products.isNotEmpty())
        openOrderDetails(placed)

        compose.onAllNodesWithText("Naruči ponovo").onFirst().performScrollTo().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Pregled korpe:"), SERVER_TIMEOUT_MS)
        compose.waitUntil(SERVER_TIMEOUT_MS) { currentCart().items.map { it.sku }.containsAll(products) }
    }

    @Test
    fun statusNaCekanjuListsTheNewOrder() {
        val order = placeOrderByApi()
        reopenApp()
        openOrders()

        // The option in the status menu, not an order's status chip of the same text
        val option = hasText("Na čekanju") and hasAnyAncestor(isPopup())
        compose.onAllNodesWithText("Sve").onFirst().performClick()
        compose.waitUntilAtLeastOneExists(option, SCREEN_TIMEOUT_MS)
        compose.onNode(option).performClick()
        compose.waitUntilDoesNotExist(option, SCREEN_TIMEOUT_MS)
        waitUntilLoaded()

        compose.waitUntilAtLeastOneExists(hasText("#${order.incrementId}", substring = true), SERVER_TIMEOUT_MS)
        assertTrue(exists(hasText("Vidi narudžbu"), unmerged = true))
    }

    private fun fillCartToTheVendorMinimum(): Product {
        val product = recommendedProducts().firstOrNull { it.hasOnStock() && it.sku != null && it.currentPrice() > 0 }
        assumeTrue("no recommended product is in stock", product != null)
        fillCustomerCartToTheVendorMinimum(product!!)
        return product
    }

    private fun openShippingDetails(product: Product) {
        compose.onNode(bottomTab("Korpa")).performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag(cartProductTag(product)), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onNodeWithText("Nastavi dalje").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Informacije za dostavu:"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
    }

    /** Types into the empty field with [placeholder] on the shipping details. */
    private fun type(placeholder: String, text: String) {
        compose.onNode(hasSetTextAction() and hasText(placeholder)).performScrollTo().performTextInput(text)
    }

    /** Waits for the order confirmation and finds the order it names on stage. */
    private fun assertOrderPlaced(): OrdersResponse {
        compose.waitUntilAtLeastOneExists(
            hasText("Vaš zahtjev za narudžbu je uspješno poslan dobavljačima."), SERVER_TIMEOUT_MS
        )
        val shown = screenTexts().first { it.startsWith("Broj Vaše narudžbe je:") }
        val id = shown.removePrefix("Broj Vaše narudžbe je:").trim().trim('"')
        val order = orders().firstOrNull { it.orderId == id }
        assertTrue("stage has no order $id", order != null)
        return order!!
    }

    /** Places an order through the API, as "Završi narudžbu" would, and starts the app again to show it. */
    private fun placeOrderByApi(): OrdersResponse {
        val product = recommendedProducts().firstOrNull { it.hasOnStock() && it.sku != null && it.currentPrice() > 0 }
        assumeTrue("no recommended product is in stock", product != null)
        val id = placeCustomerOrderByApi(product!!)
        val order = orders().firstOrNull { it.orderId == id }
        assertTrue("stage has no order $id", order != null)
        reopenApp()
        return order!!
    }

    private fun openOrders() {
        waitUntilLoaded()
        compose.onNode(bottomTab("Profil")).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Moje narudžbe") and hasClickAction(), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onNode(hasText("Moje narudžbe") and hasClickAction()).performClick()
        compose.waitUntilDoesNotExist(hasText("Zahtjevi za partnerstvo") and hasClickAction(), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
    }

    /** Opens the order's details from "Moje narudžbe", where it is the newest. */
    private fun openOrderDetails(order: OrdersResponse) {
        openOrders()
        compose.waitUntilAtLeastOneExists(hasText("#${order.incrementId}", substring = true), SERVER_TIMEOUT_MS)
        compose.onAllNodesWithText("Vidi narudžbu").onFirst().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Narudžba br.${order.incrementId}", substring = true), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
    }

    private fun orders(): List<OrdersResponse> {
        val result = runBlocking { OrdersRepository().orders(sortBy = "created_at", sortDirection = "DESC").last() }
        assertTrue("orders: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return (result as ResultState.Success<*>).data as List<OrdersResponse>
    }

    private fun orderOnStage(order: OrdersResponse) = orders().first { it.orderId == order.orderId }

    private fun comments(orderId: String?, vendorId: String): List<Comment> {
        val result = runBlocking { OrdersRepository().comments(orderId, vendorId).last() }
        assertTrue("comments: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return (result as ResultState.Success<*>).data as List<Comment>
    }

}
