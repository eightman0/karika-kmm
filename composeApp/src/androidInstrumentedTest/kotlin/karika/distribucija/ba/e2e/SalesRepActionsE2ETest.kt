package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.DiscountRule
import karika.distribucija.ba.domain.model.DiscountRuleBody
import karika.distribucija.ba.domain.model.DiscountRuleInput
import karika.distribucija.ba.domain.model.DiscountRuleSearchResults
import karika.distribucija.ba.domain.model.OnBehalfCartResponse
import karika.distribucija.ba.domain.model.OnBehalfOrder
import karika.distribucija.ba.domain.model.OnBehalfOrderResult
import karika.distribucija.ba.domain.model.OnBehalfOrderSearchResults
import karika.distribucija.ba.domain.model.OnBehalfProduct
import karika.distribucija.ba.domain.model.OnBehalfProductSearchResults
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.ui.view.salesrep.customers.detail.discountTag
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-end test of what the sales rep changes on stage.karika.ba, see [SalesRepE2ETest], always
 * for the customer test account (KARIKA_STAGE_SHOP_*): its discounts ("Novi popust", "Izmijeni",
 * "Obriši"), its cart through the catalog ("Dodaj", "Ukloni", "Isprazni korpu"), an order for it
 * ("Potvrdi narudžbu") and a comment on such an order. Every test cleans up after itself: the
 * discounts it made are deleted, the cart is emptied and the supplier rejects its orders.
 */
@OptIn(ExperimentalTestApi::class)
class SalesRepActionsE2ETest : SalesRepE2ETest() {

    private lateinit var customer: OperationalCustomer
    private val madeRules = mutableListOf<Long>()
    private val placedOrders = mutableListOf<String>()

    @Before
    fun findTheTestCustomer() {
        val capabilities = me().capabilities
        customer = testCustomer()
        assumeTrue("the sales rep may neither order nor give discounts",
            capabilities.canPlaceOrderFor || capabilities.canCreateDiscountFor)
        emptyCustomerCart()
    }

    @After
    fun cleanUp() {
        if (!::customer.isInitialized) return
        runCatching { emptyCustomerCart() }
        madeRules.forEach { id -> runCatching { runBlocking { SalesRepository().deleteDiscount(id).last() } } }
        rules().filter { it.discountPercent.toInt() in TEST_PERCENTS }.forEach { rule ->
            runCatching { runBlocking { SalesRepository().deleteDiscount(rule.ruleId!!).last() } }
        }
        placedOrders.forEach { number ->
            runCatching {
                asAccount(Account.VENDOR) {
                    runBlocking { DashRepository().changeOrderStatus("reject", number, "E2E: test komercijaliste").last() }
                }
            }
        }
    }

    // Popusti

    @Test
    fun noviPopustForEveryItemIsSavedAndShown() {
        assumeDiscounts()
        val percent = TEST_PERCENTS.first()
        openCustomer()

        compose.onNodeWithText("Novi popust").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Artikal ili kategorija"), SCREEN_TIMEOUT_MS)
        formField(FORM_MIN_QTY).performTextInput("3")
        formField(FORM_PERCENT).performTextInput("$percent")
        closeKeyboard()
        compose.onNodeWithText("Sačuvaj").performClick()

        // Saved: back on the customer, with the rule on stage and its card
        compose.waitUntilAtLeastOneExists(hasText("PROFIL KUPCA"), SERVER_TIMEOUT_MS)
        lateinit var rule: DiscountRule
        compose.waitUntil(SERVER_TIMEOUT_MS) {
            rules().firstOrNull { it.discountPercent.toInt() == percent }?.also { rule = it } != null
        }
        madeRules += rule.ruleId!!
        assertEquals("per_customer", rule.discountType)
        assertEquals(3, rule.minQty?.toInt())
        assertTrue("the rule is for one item: $rule", rule.productId == null && rule.categoryId == null)
        scrollListTo(hasTestTag(discountTag(rule)))
        assertTrue(exists(hasText("$percent%") and hasAnyAncestor(hasTestTag(discountTag(rule))), unmerged = true))
        assertTrue(exists(hasText("Svi artikli i kategorije") and hasAnyAncestor(hasTestTag(discountTag(rule))), unmerged = true))
    }

    @Test
    fun noviPopustWithoutARabatIsNotSaved() {
        assumeDiscounts()
        val before = rules().size
        openCustomer()

        compose.onNodeWithText("Novi popust").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Artikal ili kategorija"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Sačuvaj").performClick()

        compose.waitUntilAtLeastOneExists(hasText("Unesite rabat %"), SCREEN_TIMEOUT_MS)
        assertTrue(exists(hasText("Artikal ili kategorija")))
        compose.onNodeWithText("Odustani").performClick()
        compose.waitUntilAtLeastOneExists(hasText("PROFIL KUPCA"), SCREEN_TIMEOUT_MS)
        assertEquals(before, rules().size)
    }

    @Test
    fun aDiscountForOneProductIsSavedForThatProduct() {
        assumeDiscounts()
        val percent = TEST_PERCENTS[1]
        val product = catalogProduct()
        openCustomer()

        compose.onNodeWithText("Novi popust").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Artikal ili kategorija"), SCREEN_TIMEOUT_MS)
        formField(FORM_ITEM).performTextInput(product.name)
        // The product rows, marked "ART.", come after a short pause
        compose.waitUntilAtLeastOneExists(hasText("ART."), SERVER_TIMEOUT_MS)
        // The search field shows the name as well
        compose.onAllNodes(hasText(product.name) and hasClickAction() and !hasSetTextAction()).onFirst().performClick()
        compose.waitUntilDoesNotExist(hasText("ART."), SCREEN_TIMEOUT_MS)
        formField(FORM_PERCENT).performTextInput("$percent")
        closeKeyboard()
        compose.onNodeWithText("Sačuvaj").performClick()

        compose.waitUntilAtLeastOneExists(hasText("PROFIL KUPCA"), SERVER_TIMEOUT_MS)
        lateinit var rule: DiscountRule
        compose.waitUntil(SERVER_TIMEOUT_MS) {
            rules().firstOrNull { it.discountPercent.toInt() == percent }?.also { rule = it } != null
        }
        madeRules += rule.ruleId!!
        assertEquals("the rule's product: $rule", product.entityId, rule.productId)
    }

    @Test
    fun izmijeniChangesTheDiscount() {
        assumeDiscounts()
        val rule = discountByApi(TEST_PERCENTS[2])
        val changed = TEST_PERCENTS[3]
        openCustomer()
        scrollListTo(hasTestTag(discountTag(rule)))

        compose.onNode(hasText("Izmijeni") and hasAnyAncestor(hasTestTag(discountTag(rule))), useUnmergedTree = true).performClick()

        compose.waitUntilAtLeastOneExists(hasText("Artikal ili kategorija"), SCREEN_TIMEOUT_MS)
        formField(FORM_PERCENT).performTextReplacement("$changed")
        closeKeyboard()
        compose.onNodeWithText("Sačuvaj").performClick()

        compose.waitUntilAtLeastOneExists(hasText("PROFIL KUPCA"), SERVER_TIMEOUT_MS)
        compose.waitUntil(SERVER_TIMEOUT_MS) {
            rules().firstOrNull { it.ruleId == rule.ruleId }?.discountPercent?.toInt() == changed
        }
        compose.waitUntil(SERVER_TIMEOUT_MS) {
            exists(hasText("$changed%") and hasAnyAncestor(hasTestTag(discountTag(rule))), unmerged = true)
        }
    }

    @Test
    fun obrisiAsksFirstAndThenDeletesTheDiscount() {
        assumeDiscounts()
        val rule = discountByApi(TEST_PERCENTS[4])
        openCustomer()
        scrollListTo(hasTestTag(discountTag(rule)))
        val obrisi = hasText("Obriši") and hasAnyAncestor(hasTestTag(discountTag(rule)))

        // "Odustani" keeps it
        compose.onNode(obrisi, useUnmergedTree = true).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Obriši popust"), SCREEN_TIMEOUT_MS)
        compose.onNode(dialogButton("Odustani")).performClick()
        compose.waitUntilDoesNotExist(hasText("Obriši popust"), SCREEN_TIMEOUT_MS)
        assertTrue(rules().any { it.ruleId == rule.ruleId })

        compose.onNode(obrisi, useUnmergedTree = true).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Obriši popust"), SCREEN_TIMEOUT_MS)
        compose.onNode(dialogButton("Obriši")).performClick()

        compose.waitUntil(SERVER_TIMEOUT_MS) { rules().none { it.ruleId == rule.ruleId } }
        compose.waitUntilDoesNotExist(hasTestTag(discountTag(rule)), SERVER_TIMEOUT_MS)
    }

    // Korpa kupca

    @Test
    fun dodajPutsTheProductInTheCustomersCartAndUkloniTakesItOut() {
        assumeOrdering()
        val product = catalogProduct()
        openCatalog()

        searchCatalog(product)
        tapFirst(hasText("Dodaj") and hasClickAction())

        compose.waitUntil(SERVER_TIMEOUT_MS) { cart().items.any { it.sku == product.sku } }
        compose.waitUntilAtLeastOneExists(hasText("Ažuriraj"), SERVER_TIMEOUT_MS)
        assertEquals(product.minQty(), cart().items.first { it.sku == product.sku }.qty)

        openCart()
        assertTrue(exists(hasText("#${product.sku}"), unmerged = true))
        compose.onNodeWithContentDescription("Ukloni").performClick()

        compose.waitUntilAtLeastOneExists(hasText("Korpa je prazna"), SERVER_TIMEOUT_MS)
        compose.waitUntil(SERVER_TIMEOUT_MS) { cart().items.none { it.sku == product.sku } }
    }

    @Test
    fun theStepperSetsHowManyGoInTheCart() {
        assumeOrdering()
        val product = catalogProduct()
        openCatalog()

        searchCatalog(product)
        // The icon, whose button takes the tap
        repeat(2) { tapFirst(hasContentDescription("+"), unmerged = true) }
        tapFirst(hasText("Dodaj") and hasClickAction())

        compose.waitUntil(SERVER_TIMEOUT_MS) { cart().items.any { it.sku == product.sku } }
        assertEquals(product.minQty() + 2, cart().items.first { it.sku == product.sku }.qty)
    }

    @Test
    fun ispraznikorpuEmptiesTheCustomersCart() {
        assumeOrdering()
        val product = catalogProduct()
        addToCartByApi(product)
        openCatalog()

        openCart()
        compose.onNodeWithText("Isprazni korpu").performScrollTo().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Korpa je prazna"), SERVER_TIMEOUT_MS)
        compose.waitUntil(SERVER_TIMEOUT_MS) { cart().isEmpty }
    }

    // Narudžba za kupca

    @Test
    fun potvrdiNarudzbuPlacesTheOrderForTheCustomer() {
        assumeOrdering()
        assumeTrue("the customer has no default shipping address", customer.defaultShippingAddressId != null)
        val product = catalogProduct()
        val before = orders().items.map { it.orderId }.toSet()
        openCatalog()

        searchCatalog(product)
        tapFirst(hasText("Dodaj") and hasClickAction())
        compose.waitUntil(SERVER_TIMEOUT_MS) { cart().items.any { it.sku == product.sku } }
        openCart()
        compose.onNodeWithText("Pregledaj narudžbu").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Specifikacija narudžbe"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        assertTrue(exists(hasText(customer.email.orEmpty(), substring = true), unmerged = true))
        compose.onNodeWithText("Potvrdi narudžbu").performClick()

        // The order is on stage and its details open
        lateinit var order: OnBehalfOrder
        compose.waitUntil(SERVER_TIMEOUT_MS) {
            orders().items.firstOrNull { it.orderId !in before }?.also { order = it } != null
        }
        placedOrders += order.incrementId
        assertEquals(customer.customerId, order.customerId)
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText("Narudžba ${order.incrementId}", substring = true), unmerged = true) }
        compose.waitUntilAtLeastOneExists(hasText("Informacije o narudžbi"), SERVER_TIMEOUT_MS)
        assertTrue("the cart was not emptied", cart().isEmpty)
    }

    @Test
    fun aCommentOnTheOrderReachesStage() {
        assumeOrdering()
        val order = orderByApi()
        val text = "E2E komentar komercijaliste " + System.currentTimeMillis()

        // A new order is first in the list
        scenario.recreate()
        compose.waitUntilAtLeastOneExists(hasText(SALES_REP_HOME), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        scrollListTo(hasText("#${order.incrementId}", substring = true))
        compose.onAllNodesWithText("#${order.incrementId}", substring = true).onFirst().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Informacije o narudžbi"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()

        // The comments are at the end of the details, which compose only what is on screen;
        // "Napiši komentar kupcu..." is the field's placeholder, not its text
        scrollListTo(hasText("Pošalji komentar"))
        field(shownFieldIndices().size - 1).performTextInput(text)
        closeKeyboard()
        compose.onNodeWithText("Pošalji komentar").performScrollTo().performClick()

        compose.waitUntil(SERVER_TIMEOUT_MS) { comments(order.incrementId).any { it.message() == text } }
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText(text, substring = true), unmerged = true) }
    }

    // Koraci

    /** Opens the test customer's profile from "Upravljanje kupcima", by searching its name. */
    private fun openCustomer() {
        goTo("Upravljanje kupcima")
        val name = customer.displayName()
        customer.company?.takeIf { it.isNotBlank() }?.let { field(0).performTextInput(it) }
        closeKeyboard()
        // The search field shows the name too
        val card = hasText(name, substring = true) and !hasSetTextAction()
        // A tap while the list is still settling after the search is lost, so it is repeated
        repeat(3) {
            scrollListTo(card)
            waitUntilLoaded()
            compose.onAllNodes(card, useUnmergedTree = true).onFirst().performClick()
            val opened = runCatching {
                compose.waitUntilAtLeastOneExists(hasText("PROFIL KUPCA"), SCREEN_TIMEOUT_MS)
            }.isSuccess
            if (opened) {
                waitUntilLoaded()
                return
            }
        }
        dumpScreen("openCustomer")
        throw AssertionError("the customer's profile did not open")
    }

    /** Opens the catalog for the test customer, through its profile's "Naruči za kupca". */
    private fun openCatalog() {
        openCustomer()
        compose.onNodeWithText("Naruči za kupca").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Naruči:", substring = true), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
    }

    private fun searchCatalog(product: OnBehalfProduct) {
        field(0).performTextInput(product.name)
        closeKeyboard()
        // The search starts after a short pause and puts the list away while it loads, so the
        // unfiltered list (which has the product too) must not be taken for the result
        Thread.sleep(SEARCH_PAUSE_MS)
        waitUntilLoaded()
        compose.waitUntilAtLeastOneExists(hasText("#${product.sku}"), SERVER_TIMEOUT_MS)
        compose.waitUntilAtLeastOneExists(hasText("Dodaj") and hasClickAction(), SCREEN_TIMEOUT_MS)
    }

    /** Taps the first node matching [matcher]; when there is none, the screen goes to logcat. */
    private fun tapFirst(matcher: SemanticsMatcher, unmerged: Boolean = false) {
        try {
            compose.onAllNodes(matcher, useUnmergedTree = unmerged).onFirst().performScrollTo().performClick()
        } catch (e: Throwable) {
            dumpScreen("tapFirst")
            throw e
        }
    }

    private fun openCart() {
        compose.onNodeWithContentDescription("Korpa").performClick()
        compose.waitUntil(SERVER_TIMEOUT_MS) {
            exists(hasText("Pregledaj narudžbu")) || exists(hasText("Korpa je prazna"))
        }
        waitUntilLoaded()
    }

    /**
     * The discount form's text fields: the item search, "Min. količina", "Rabat". Once an item
     * is chosen the search is read-only and no longer a text field, so they count from the end.
     */
    private fun formField(fromEnd: Int) = field(shownFieldIndices().size - 1 - fromEnd)

    private fun dialogButton(label: String) = hasText(label) and hasClickAction() and hasAnyAncestor(isDialog())

    private fun assumeDiscounts() =
        assumeTrue("the sales rep may not give discounts", me().capabilities.canCreateDiscountFor)

    private fun assumeOrdering() =
        assumeTrue("the sales rep may not order for customers", me().capabilities.canPlaceOrderFor)

    // Stage, as the sales rep

    private fun rules(): List<DiscountRule> = asAccount(Account.SALES_REP) {
        val result = runBlocking { SalesRepository().getCustomerDiscounts(customer.customerId).last() }
        assertTrue("discounts: $result", result is ResultState.Success)
        ((result as ResultState.Success<*>).data as DiscountRuleSearchResults).items
    }

    private fun discountByApi(percent: Int): DiscountRule = asAccount(Account.SALES_REP) {
        val body = DiscountRuleBody(DiscountRuleInput(discountPercent = percent.toFloat()))
        val result = runBlocking { SalesRepository().createCustomerDiscount(customer.customerId, body).last() }
        assertTrue("could not make a discount: $result", result is ResultState.Success)
        ((result as ResultState.Success<*>).data as DiscountRule).also { madeRules += it.ruleId!! }
    }

    /** A product of the supplier's catalog in stock, with a price. */
    private fun catalogProduct(): OnBehalfProduct = asAccount(Account.SALES_REP) {
        val result = runBlocking { SalesRepository().getProducts(page = 1, pageSize = 30).last() }
        assertTrue("products: $result", result is ResultState.Success)
        val products = ((result as ResultState.Success<*>).data as OnBehalfProductSearchResults).items
        val product = products.firstOrNull { it.isInStock && it.price > 0 && it.sku.isNotBlank() && it.name.length >= 2 }
        assumeTrue("the supplier has no product in stock", product != null)
        product!!
    }

    private fun cart(): OnBehalfCartResponse = asAccount(Account.SALES_REP) {
        val result = runBlocking { SalesRepository().getCart(customer.customerId).last() }
        assertTrue("cart: $result", result is ResultState.Success)
        (result as ResultState.Success<*>).data as OnBehalfCartResponse
    }

    private fun addToCartByApi(product: OnBehalfProduct) = asAccount(Account.SALES_REP) {
        val result = runBlocking { SalesRepository().addCartItem(customer.customerId, product.sku, product.minQty()).last() }
        assertTrue("could not fill the cart: $result", result is ResultState.Success)
    }

    private fun emptyCustomerCart() = asAccount(Account.SALES_REP) {
        cart().items.forEach { runBlocking { SalesRepository().removeCartItem(customer.customerId, it.itemId).last() } }
    }

    private fun orders(): OnBehalfOrderSearchResults = asAccount(Account.SALES_REP) {
        val result = runBlocking { SalesRepository().getOrders(page = 1, pageSize = 20).last() }
        assertTrue("orders: $result", result is ResultState.Success)
        (result as ResultState.Success<*>).data as OnBehalfOrderSearchResults
    }

    /**
     * Orders a catalog product for the customer through the API, the way "Potvrdi narudžbu" does.
     * The supplier first rejects its older pending orders, which would otherwise lock the new
     * one's details.
     */
    private fun orderByApi(): OnBehalfOrderResult {
        addToCartByApi(catalogProduct())
        val order = asAccount(Account.SALES_REP) {
            val result = runBlocking { SalesRepository().placeOrder(customer.customerId, "E2E narudžba komercijaliste").last() }
            assertTrue("could not place the order: $result", result is ResultState.Success)
            ((result as ResultState.Success<*>).data as OnBehalfOrderResult).also { placedOrders += it.incrementId }
        }
        settleOlderOrders(order.incrementId)
        return order
    }

    /** As the supplier, rejects the older pending orders while they keep order [number] locked. */
    private fun settleOlderOrders(number: String) = asAccount(Account.VENDOR) {
        fun locked() = (runBlocking { DashRepository().getOrder(number).last() } as? ResultState.Success<*>)
            ?.let { (it.data as VendorOrder).locked() } ?: false
        repeat(MAX_SETTLED) {
            if (!locked()) return@asAccount
            val result = runBlocking {
                DashRepository().getOrders(pageSize = 30, currentPage = 1, queryParams = listOf(NEWEST_FIRST, PENDING)).last()
            }
            @Suppress("UNCHECKED_CAST")
            val older = ((result as? ResultState.Success<*>)?.data as? List<VendorOrder>).orEmpty()
                .lastOrNull { it.orderId != number } ?: return@asAccount
            runBlocking { DashRepository().changeOrderStatus("reject", older.orderId!!, "E2E: starija narudžba").last() }
        }
        assertTrue("order $number stays locked", !locked())
    }

    private fun comments(orderNumber: String): List<Comment> = asAccount(Account.SALES_REP) {
        val result = runBlocking { DashRepository().getOrderComments(orderNumber).last() }
        assertTrue("comments: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        (result as ResultState.Success<*>).data as List<Comment>
    }

    private companion object {
        /** Discounts only these tests give, so that a run left over by a failed one is cleaned up. */
        val TEST_PERCENTS = listOf(13, 17, 19, 23, 29)
        /** Counted from the form's last field, see [formField]. */
        const val FORM_ITEM = 2
        const val FORM_MIN_QTY = 1
        const val FORM_PERCENT = 0
        const val MAX_SETTLED = 15
        /** Longer than the catalog's 400 ms pause before it searches. */
        const val SEARCH_PAUSE_MS = 1_000L
        const val NEWEST_FIRST =
            "&searchCriteria[sortOrders][0][field]=created_at&searchCriteria[sortOrders][0][direction]=DESC"
        const val PENDING = "&searchCriteria[filterGroups][0][filters][0][field]=real_order_status" +
            "&searchCriteria[filterGroups][0][filters][0][value]=pending" +
            "&searchCriteria[filterGroups][0][filters][0][conditionType]=eq"
    }
}
