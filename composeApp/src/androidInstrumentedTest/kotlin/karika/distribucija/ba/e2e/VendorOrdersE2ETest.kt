package karika.distribucija.ba.e2e

import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.ui.view.distributer.orders.MIN_ORDER_TAG
import karika.distribucija.ba.ui.view.distributer.orders.vendorOrderTag
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-end test of the supplier's "Upravljanje narudžbama" on stage.karika.ba, see
 * [VendorE2ETest]: the list against stage, search, the filter sheet, an order's details and its
 * actions menu, and the minimum order amount dialog. Nothing is approved, rejected, sent or saved.
 */
@OptIn(ExperimentalTestApi::class)
class VendorOrdersE2ETest : VendorE2ETest() {

    private lateinit var latest: List<VendorOrder>

    @Before
    fun openOrders() {
        latest = orders()
        goTo("Upravljanje narudžbama")
        latest.firstOrNull()?.let {
            compose.waitUntilAtLeastOneExists(hasTestTag(vendorOrderTag(it)), SERVER_TIMEOUT_MS)
        }
        waitUntilLoaded()
    }

    @Test
    fun showsTheLatestOrdersFromTheBackend() {
        if (latest.isEmpty()) {
            compose.onNodeWithText("Nema narudžbi.").assertIsDisplayed()
            return
        }
        latest.take(10).forEach { order ->
            orderList().performScrollToNode(hasTestTag(vendorOrderTag(order)))
            compose.onNodeWithTag(vendorOrderTag(order)).assertIsDisplayed()
        }
    }

    @Test
    fun searchByCompanyShowsOnlyThatCompanysOrders() {
        val company = latest.firstNotNullOfOrNull { it.b2bPravnoLice?.takeIf(String::isNotBlank) }
        assumeTrue("no order has a company name", company != null)
        val expected = orders(filter("b2b_pravno_lice", company!!, "like"))

        compose.onNode(hasSetTextAction() and hasText("Pretraži narudžbe...")).performTextInput(company)

        val others = latest.filter { it !in expected }
        compose.waitUntil(SERVER_TIMEOUT_MS) { others.none { exists(hasTestTag(vendorOrderTag(it))) } }
        waitUntilLoaded()
        assertTrue("stage finds no order of \"$company\"", expected.isNotEmpty())
        orderList().performScrollToNode(hasTestTag(vendorOrderTag(expected.first())))
    }

    @Test
    fun filterSheetOpensAndOdustaniLeavesTheList() {
        compose.onNodeWithText("Filteri").performClick()

        compose.waitUntilAtLeastOneExists(hasText("FILTERI"), SCREEN_TIMEOUT_MS)
        // A field's title and placeholder can be the same text
        listOf("Datum kupovine", "Ukupno VPC", "Broj narudžbe", "Račun na ime").forEach {
            assertTrue("\"$it\" is not in the sheet", exists(hasText(it)))
        }
        compose.onNodeWithText("Odustani").performClick()

        compose.waitUntilDoesNotExist(hasText("FILTERI"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Očisti").assertDoesNotExist()
        latest.firstOrNull()?.let { compose.onNodeWithTag(vendorOrderTag(it)).assertExists() }
    }

    @Test
    fun filterByOrderNumberShowsThatOrderAndOcistiBringsAllBack() {
        val order = latest.firstOrNull { it.orderId != null }
        assumeTrue("no orders", order != null)
        val expected = orders(filter("order_id", order!!.orderId!!, "like"))
        assertTrue(
            "stage returns ${expected.map { "#${it.orderId}" }} for order number ${order.orderId}",
            expected.map { it.orderId } == listOf(order.orderId)
        )

        compose.onNodeWithText("Filteri").performClick()
        compose.waitUntilAtLeastOneExists(hasText("FILTERI"), SCREEN_TIMEOUT_MS)
        compose.onNode(hasSetTextAction() and hasText("Broj narudžbe")).performTextInput(order.orderId!!)
        closeKeyboard()
        waitUntilLoaded()
        compose.onNodeWithText("Filtriraj").performClick()

        compose.waitUntilAtLeastOneExists(hasText("Očisti"), SCREEN_TIMEOUT_MS)
        val others = latest.filter { it.orderId != order.orderId }
        compose.waitUntil(SERVER_TIMEOUT_MS) { others.none { exists(hasTestTag(vendorOrderTag(it))) } }
        compose.onNodeWithTag(vendorOrderTag(order)).assertIsDisplayed()

        compose.onNodeWithText("Očisti").performClick()
        others.firstOrNull()?.let { back ->
            compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasTestTag(vendorOrderTag(back))) }
        }
    }

    @Test
    fun priceRangeShowsOnlyOrdersInThatRange() {
        // A narrow range around one order's total: the list may only hold orders inside it
        val order = latest.firstOrNull { it.orderTotal?.toDoubleOrNull() != null }
        assumeTrue("no order with a total", order != null)
        val total = order!!.orderTotal!!.toDouble()
        val from = (total - 0.5).coerceAtLeast(0.0).toInt()
        val to = (total + 0.5).toInt() + 1
        val outside = latest.filter { other ->
            val t = other.orderTotal?.toDoubleOrNull() ?: return@filter false
            t < from || t > to
        }
        assumeTrue("every recent order costs about the same", outside.isNotEmpty())

        compose.onNodeWithText("Filteri").performClick()
        compose.waitUntilAtLeastOneExists(hasText("FILTERI"), SCREEN_TIMEOUT_MS)
        priceField("OD").performTextInput("$from")
        priceField("DO").performTextInput("$to")
        closeKeyboard()
        waitUntilLoaded()
        compose.onNodeWithText("Filtriraj").performClick()

        compose.waitUntilAtLeastOneExists(hasText("Očisti"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasTestTag(vendorOrderTag(order))) }
        val shownOutside = outside.filter { exists(hasTestTag(vendorOrderTag(it))) }
        assertTrue(
            "orders outside $from–$to KM are still listed: ${shownOutside.map { "#${it.orderId} ${it.orderTotal}" }}",
            shownOutside.isEmpty()
        )
    }

    @Test
    fun tappingAnOrderOpensItsDetailsAndTheBackLinkReturns() {
        val order = latest.firstOrNull { !it.locked() }
        assumeTrue("no unlocked order", order != null)

        compose.onNodeWithTag(vendorOrderTag(order!!)).performClick()

        compose.waitUntilAtLeastOneExists(hasText("Informacije o narudžbi"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        listOf("STATUS NARUDŽBE", "BROJ NARUDŽBE", "DATUM NARUDŽBE", "UKUPNO VPC", "Specifikacija narudžbe")
            .forEach { assertTrue("\"$it\" is not shown", exists(hasText(it), unmerged = true)) }
        assertTrue(exists(hasText(order.orderId!!, substring = true), unmerged = true))

        compose.onNodeWithText("Nazad na upravljanje narudžbama").performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag(vendorOrderTag(order)), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun akcijeListsWhatCanBeDoneWithTheOrder() {
        val order = latest.firstOrNull { !it.locked() }
        assumeTrue("no unlocked order", order != null)
        compose.onNodeWithTag(vendorOrderTag(order!!)).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Informacije o narudžbi"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        val akcije = compose.onNode(hasText("Akcije", substring = true) and isEnabled())
        assumeTrue("this order has no actions", exists(hasText("Akcije", substring = true) and isEnabled()))

        akcije.performClick()

        compose.waitUntilAtLeastOneExists(hasText("Printaj narudžbu"), SCREEN_TIMEOUT_MS)
        if (exists(hasText("Odobri narudžbu"))) {
            compose.onNodeWithText("Odbij narudžbu").assertExists()
        }
        // The back key closes the menu, which has the focus, and does nothing to the order
        pressBackKey()
        compose.waitUntilDoesNotExist(hasText("Printaj narudžbu"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Informacije o narudžbi").assertExists()
    }

    @Test
    fun minimumOrderDialogShowsTheAmountAndOtkaziClosesIt() {
        compose.onNodeWithTag(MIN_ORDER_TAG).performClick()

        compose.waitUntilAtLeastOneExists(hasText("Iznos"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Sačuvaj").assertExists()
        compose.onNodeWithText("Otkaži").performClick()

        compose.waitUntilDoesNotExist(hasText("Iznos"), SCREEN_TIMEOUT_MS)
    }

    private fun priceField(placeholder: String) =
        compose.onNode(hasSetTextAction() and hasText(placeholder) and isEnabled())

    private fun orderList() =
        compose.onNode(hasScrollToNodeAction() and hasAnyDescendant(hasTestTagStartingWith("vendor_order_")))

    private fun hasTestTagStartingWith(prefix: String) =
        SemanticsMatcher("TestTag starts with '$prefix'") { node ->
            node.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.TestTag)?.startsWith(prefix) == true
        }

    /** One condition the way OrdersComponent.filter() writes it. */
    private fun filter(field: String, value: String, condition: String) =
        "&searchCriteria[filterGroups][0][filters][0][field]=$field" +
            "&searchCriteria[filterGroups][0][filters][0][value]=$value" +
            "&searchCriteria[filterGroups][0][filters][0][conditionType]=$condition"

    /** The first page of orders, newest first, from the same request the screen makes. */
    private fun orders(vararg filters: String): List<VendorOrder> {
        val result = runBlocking {
            DashRepository().getOrders(
                pageSize = 30,
                currentPage = 1,
                queryParams = listOf(NEWEST_FIRST) + filters
            ).last()
        }
        assertTrue("orders: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return (result as ResultState.Success<*>).data as List<VendorOrder>
    }

    private companion object {
        const val NEWEST_FIRST =
            "&searchCriteria[sortOrders][0][field]=created_at&searchCriteria[sortOrders][0][direction]=DESC"
    }
}
