package karika.distribucija.ba.e2e

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.api.OrdersRepository
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.ui.view.distributer.orders.MIN_ORDER_TAG
import karika.distribucija.ba.ui.view.distributer.orders.vendorOrderTag
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * End-to-end test of what the supplier does with an order on stage.karika.ba, see [VendorE2ETest].
 * Each test first orders one of the supplier's own products as the customer test account (through
 * the API), then works on that order in the app: "Odobri narudžbu", "Odbij narudžbu", comments,
 * "Izmijeni" on an item, the delivery calculator with "Odobri" with delivery, "Pošalji predračun",
 * and the minimum order amount. All of it stays on stage.
 */
@OptIn(ExperimentalTestApi::class)
class VendorOrderActionsE2ETest : VendorE2ETest() {

    @Test
    fun odobriNarudzbuWithoutDeliveryApprovesTheOrder() {
        val order = openNewOrder()

        openAction("Odobri narudžbu")
        assertTrue("\"Bez dostave\" is not offered", exists(hasText("Bez dostave") and hasAnyAncestor(isDialog())))
        compose.onNode(hasSetTextAction() and hasText("Dodajte poruku za kupca")).performTextInput("E2E odobreno")
        closeKeyboard()
        compose.onNode(dialogButton("Odobri narudžbu")).performClick()

        waitForSnackbar("Narudžba je uspješno odobrena.")
        compose.waitUntil(SERVER_TIMEOUT_MS) { orderOnStage(order).realOrderStatus == "approved" }
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText("Odobreno"), unmerged = true) }
    }

    @Test
    fun odbijNarudzbuWithAReasonRejectsTheOrder() {
        val order = openNewOrder()
        val reason = "E2E odbijeno " + System.currentTimeMillis()

        openAction("Odbij narudžbu")
        compose.waitUntilAtLeastOneExists(hasText("Jeste li sigurni da želite odbiti ovu narudžbu?"), SCREEN_TIMEOUT_MS)
        compose.onNode(hasSetTextAction() and hasText("Dodajte poruku za kupca")).performTextInput(reason)
        closeKeyboard()
        compose.onNode(dialogButton("Odbij narudžbu")).performClick()

        compose.waitUntil(SERVER_TIMEOUT_MS) { orderOnStage(order).realOrderStatus == "rejected" }
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText("Odbijeno"), unmerged = true) }
    }

    @Test
    fun odustaniInTheApproveDialogLeavesTheOrderPending() {
        val order = openNewOrder()

        openAction("Odobri narudžbu")
        compose.onNode(dialogButton("Odustani")).performClick()

        compose.waitUntilDoesNotExist(hasText("Bez dostave"), SCREEN_TIMEOUT_MS)
        assertEquals("pending", orderOnStage(order).realOrderStatus)
    }

    @Test
    fun aCommentIsSentToTheCustomer() {
        val order = openNewOrder()
        val text = "E2E komentar dobavljaca " + System.currentTimeMillis()

        val field = hasSetTextAction() and hasText("Napiši komentar")
        compose.onNode(field).performScrollTo()
        compose.onNode(hasText("Pošalji") and hasClickAction()).assertIsNotEnabled()
        compose.onNode(field).performTextInput(text)
        closeKeyboard()
        waitUntilLoaded()
        // A tap on "Pošalji" at the screen's bottom does not reach the button in the test (no
        // request is sent), so the click goes through the button's own action; see E2E_PITANJA.md
        compose.onNode(hasText("Pošalji") and hasClickAction()).performSemanticsAction(SemanticsActions.OnClick)

        // Sent: the field is empty again and the comment is on stage
        compose.waitUntil(SCREEN_TIMEOUT_MS) { exists(field) }
        compose.waitUntil(SERVER_TIMEOUT_MS) { comments(order).any { it.message() == text } }
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText(text, substring = true), unmerged = true) }
    }

    @Test
    fun izmijeniChangesTheItemsQuantity() {
        val order = openNewOrder()
        val item = orderOnStage(order).products.first()
        val newQty = ((item.qtyOrdered?.toDoubleOrNull()?.toInt() ?: 1) + 1).toString()

        openItemEditor()
        sheetField(SHEET_QTY).performTextReplacement(newQty)
        closeKeyboard()
        compose.onNode(dialogButton("Izmijeni")).performClick()

        compose.waitUntil(SERVER_TIMEOUT_MS) {
            orderOnStage(order).products.first().qtyOrdered?.toDoubleOrNull()?.toInt()?.toString() == newQty
        }
    }

    @Test
    fun izmijeniGivesTheItemARabat() {
        val order = openNewOrder()

        openItemEditor()
        sheetField(SHEET_RABAT).performTextReplacement("7")
        closeKeyboard()
        compose.onNode(dialogButton("Izmijeni")).performClick()

        compose.waitUntil(SERVER_TIMEOUT_MS) { orderOnStage(order).products.first().rabat().toDoubleOrNull() == 7.0 }
    }

    @Test
    fun izmijeniDoesNotTakeARabatAbove100OrNoQuantity() {
        openNewOrder()

        openItemEditor()
        sheetField(SHEET_RABAT).performTextReplacement("101")
        compose.waitUntilAtLeastOneExists(hasText("Rabat ne može biti veći od 100%"), SCREEN_TIMEOUT_MS)
        compose.onNode(dialogButton("Izmijeni")).assertIsNotEnabled()

        sheetField(SHEET_RABAT).performTextReplacement("5")
        sheetField(SHEET_QTY).performTextReplacement("0")
        compose.waitUntilAtLeastOneExists(hasText("Količina ne može biti nula"), SCREEN_TIMEOUT_MS)
        compose.onNode(dialogButton("Izmijeni")).assertIsNotEnabled()
        compose.onNode(dialogButton("Odustani")).performClick()
    }

    @Test
    fun izracunajCijenuChecksTheDeliveryFields() {
        openNewOrder()
        openDelivery()

        field("Poštanski broj").performTextReplacement("7100")
        compose.onNodeWithText("Izračunaj cijenu").performScrollTo().performClick()
        waitForSnackbar("Poštanski broj nije u odgovarajućem formatu!")

        field("Poštanski broj").performTextReplacement("71000")
        DIMENSIONS.forEach { (placeholder, _) -> fieldOrEmpty(placeholder).performTextReplacement("") }
        closeKeyboard()
        compose.onNodeWithText("Izračunaj cijenu").performScrollTo().performClick()
        waitForSnackbar("Širina je obavezno polje!")
    }

    @Test
    fun odobriWithA2bSavesTheDeliveryDetails() {
        val order = openNewOrder()
        openDelivery()
        fillDelivery()
        compose.onNodeWithText("Izračunaj cijenu").performScrollTo().performClick()
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText("Cijena dostave sa PDV:", substring = true), unmerged = true) }
        waitUntilLoaded()

        openAction("Odobri narudžbu")
        compose.onAllNodes(hasText("A2B", substring = true) and hasAnyAncestor(isDialog())).onFirst().performClick()
        compose.onNode(dialogButton("Odobri narudžbu")).performClick()

        waitForSnackbar("Narudžba je uspješno odobrena.")
        compose.waitUntil(SERVER_TIMEOUT_MS) { orderOnStage(order).realOrderStatus == "approved" }
        val shipping = orderOnStage(order).shippingDetails
        assertTrue("no delivery details were saved", shipping != null)
        // The app also sends the package and "A2B" (company_code); stage keeps only the contact
        assertEquals("delivery on stage: $shipping", "E2E Kontakt", shipping!!.name)
        assertEquals("e2e.kontakt@example.com", shipping.email)
        assertEquals("71000", shipping.postcode)
    }

    @Test
    fun posaljiPredracunWithoutAFileSendsAnEstimate() {
        val order = openNewOrder()

        openAction("Pošalji predračun")
        compose.waitUntilAtLeastOneExists(hasText("Dodaj predračun"), SCREEN_TIMEOUT_MS)
        compose.onNode(hasSetTextAction() and hasText("Unesi poruku")).performTextInput("E2E predracun")
        closeKeyboard()
        compose.onNode(dialogButton("Pošalji predračun")).performClick()

        compose.waitUntil(SERVER_TIMEOUT_MS) { orderOnStage(order).realOrderStatus == "estimate-sent" }
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText("Čekanje na uplatu"), unmerged = true) }
    }

    @Test
    fun theMinimumOrderAmountIsSaved() {
        val original = profile().minOrderAmount
        val amount = (10 + System.currentTimeMillis() % 40).toString()
        try {
            goTo("Upravljanje narudžbama")
            compose.onNodeWithTag(MIN_ORDER_TAG).performClick()
            compose.waitUntilAtLeastOneExists(hasText("Iznos"), SCREEN_TIMEOUT_MS)

            compose.onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextReplacement(amount)
            closeKeyboard()
            compose.onNode(dialogButton("Sačuvaj")).performClick()

            compose.waitUntil(SERVER_TIMEOUT_MS) { profile().minOrderAmount?.toDoubleOrNull() == amount.toDouble() }
            compose.waitUntil(SCREEN_TIMEOUT_MS) { exists(hasText("$amount KM", substring = true), unmerged = true) }
        } finally {
            // The customer's tests fill their carts up to this minimum
            runBlocking { DashRepository().updateProfile(minOrderAmount = original ?: "0").last() }
        }
    }

    /**
     * Orders one of the supplier's products as the customer, makes sure it can be opened (the
     * supplier has to settle older orders first, so those are rejected), starts the app again and
     * opens the order's details.
     */
    private fun openNewOrder(): VendorOrder {
        val product = vendorProduct()
        val id = placeCustomerOrderByApi(product)
        // The supplier's order_id is the order number the customer sees, not the entity id
        val number = asAccount(Account.CUSTOMER) {
            @Suppress("UNCHECKED_CAST")
            val orders = (runBlocking {
                OrdersRepository().orders(sortBy = "created_at", sortDirection = "DESC").last()
            } as ResultState.Success<*>).data as List<OrdersResponse>
            orders.first { it.orderId == id }.incrementId!!
        }
        lateinit var order: VendorOrder
        compose.waitUntil(SERVER_TIMEOUT_MS) {
            runCatching { orderOnStage(VendorOrder(orderId = number)) }.getOrNull()?.also { order = it }?.orderId == number
        }
        settleOlderOrders(order)
        logInAsVendor()
        goTo("Upravljanje narudžbama")
        compose.waitUntilAtLeastOneExists(hasTestTag(vendorOrderTag(order)), SERVER_TIMEOUT_MS)
        compose.onNodeWithTag(vendorOrderTag(order)).performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Nazad na upravljanje narudžbama"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        return order
    }

    /** Rejects the supplier's older pending orders while they keep [order] locked. */
    private fun settleOlderOrders(order: VendorOrder) {
        repeat(MAX_SETTLED) {
            if (orderOnStage(order).locked() != true) return
            val older = orders(PENDING).lastOrNull { it.orderId != order.orderId } ?: return
            runBlocking { DashRepository().changeOrderStatus("reject", older.orderId!!, "E2E: starija narudžba").last() }
        }
        assertTrue("the new order stays locked", orderOnStage(order).locked() != true)
    }

    /** One of the supplier's own products in stock, as the customer sees it. */
    private fun vendorProduct(): Product {
        val vendorId = profile().entityId
        assumeTrue("the supplier has no id", vendorId != 0)
        val products = asAccount(Account.CUSTOMER) {
            val result = runBlocking { ProductRepository().searchProductsByCategory(vendorId = vendorId).last() }
            @Suppress("UNCHECKED_CAST")
            ((result as? ResultState.Success<*>)?.data as? List<Product>).orEmpty()
        }
        val product = products.firstOrNull { it.hasOnStock() && it.sku != null && it.currentPrice() > 0 }
        assumeTrue("the supplier has no product in stock", product != null)
        return product!!
    }

    private fun openAction(item: String) {
        compose.onNode(hasText("Akcije", substring = true) and hasClickAction()).performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText(item) and hasClickAction(), SCREEN_TIMEOUT_MS)
        compose.onAllNodes(hasText(item) and hasClickAction()).onFirst().performClick()
        compose.waitUntil(SCREEN_TIMEOUT_MS) { exists(hasAnyAncestor(isDialog())) }
    }

    private fun openItemEditor() {
        // The table's last row sits at the screen's bottom edge, where taps go to the system
        compose.onNodeWithText("Usluga dostave").performScrollTo()
        compose.onAllNodesWithText("Izmijeni").onFirst().performScrollTo().performClick()
        try {
            compose.waitUntilAtLeastOneExists(hasText("Izmijeni narudžbu"), SCREEN_TIMEOUT_MS)
        } catch (e: Throwable) {
            dumpScreen("openItemEditor")
            throw e
        }
    }

    /** The [index]th text field of the open sheet: Rabat (%), then Količina. */
    private fun sheetField(index: Int) = compose.onAllNodes(hasSetTextAction() and hasAnyAncestor(isDialog()))[index]

    private fun openDelivery() {
        compose.onNodeWithText("Usluga dostave").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Izračunaj cijenu"), SCREEN_TIMEOUT_MS)
    }

    /** The delivery field titled [title] ("Kontakt osoba", "Ukupna širina", ...), filled or empty. */
    private fun field(title: String) = fieldOrEmpty(title)

    private fun fieldOrEmpty(title: String) =
        compose.onAllNodes(hasSetTextAction())[deliveryFields().indexOf(title)].also { it.performScrollTo() }

    private fun fillDelivery() {
        (CONTACT + DIMENSIONS).forEach { (title, value) -> fieldOrEmpty(title).performTextReplacement(value) }
        closeKeyboard()
    }

    /** The delivery fields in the order the screen shows them; the comment field comes after. */
    private fun deliveryFields() = (CONTACT + DIMENSIONS).map { it.first } + "Napomena"

    private fun dialogButton(label: String) = hasText(label) and hasClickAction() and hasAnyAncestor(isDialog())

    private fun waitForSnackbar(text: String) {
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText(text, substring = true), unmerged = true) }
    }

    private fun profile(): Vendor {
        val result = runBlocking { DashRepository().getProfile().last() }
        assertTrue("profile: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as Vendor
    }

    private fun orders(vararg filters: String): List<VendorOrder> {
        val result = runBlocking {
            DashRepository().getOrders(pageSize = 30, currentPage = 1, queryParams = listOf(NEWEST_FIRST) + filters).last()
        }
        assertTrue("orders: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return (result as ResultState.Success<*>).data as List<VendorOrder>
    }

    private fun orderOnStage(order: VendorOrder): VendorOrder {
        val result = runBlocking { DashRepository().getOrder(order.orderId!!).last() }
        assertTrue("order: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as VendorOrder
    }

    private fun comments(order: VendorOrder): List<Comment> {
        val result = runBlocking { DashRepository().getOrderComments(order.orderId!!).last() }
        assertTrue("comments: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return (result as ResultState.Success<*>).data as List<Comment>
    }

    private companion object {
        const val NEWEST_FIRST =
            "&searchCriteria[sortOrders][0][field]=created_at&searchCriteria[sortOrders][0][direction]=DESC"
        const val PENDING = "&searchCriteria[filterGroups][0][filters][0][field]=real_order_status" +
            "&searchCriteria[filterGroups][0][filters][0][value]=pending" +
            "&searchCriteria[filterGroups][0][filters][0][conditionType]=eq"
        const val MAX_SETTLED = 15
        const val SHEET_RABAT = 0
        const val SHEET_QTY = 1
        val CONTACT = listOf(
            "Kontakt osoba" to "E2E Kontakt", "Email adresa" to "e2e.kontakt@example.com", "Telefon" to "061234567",
            "Grad" to "Sarajevo", "Adresa" to "Testna 1", "Poštanski broj" to "71000"
        )
        val DIMENSIONS = listOf(
            "Ukupna širina" to "30", "Ukupna visina" to "20", "Ukupna dubina" to "10", "Ukupna težina" to "2"
        )
    }
}
