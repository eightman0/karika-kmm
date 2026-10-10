package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.api.ChatRepository
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.domain.model.ChatConversationSearchResults
import karika.distribucija.ba.domain.model.OnBehalfOrderSearchResults
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.OperationalCustomerSearchResults
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.components.conversationTag
import karika.distribucija.ba.ui.view.salesrep.customers.ADD_CUSTOMER_TAG
import karika.distribucija.ba.domain.api.DashRepository
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * End-to-end test of the sales rep's screens on stage.karika.ba, see [SalesRepE2ETest]: orders
 * and an order's details, customers (list, search, status filter, a customer's profile), the
 * "Dodaj kupca" choices up to their forms, ordering for a customer up to the catalog, and the
 * message lists. Nothing is ordered, sent or saved: no product is added to a customer's cart,
 * and every form is left with "Odustani" or the back arrow.
 */
@OptIn(ExperimentalTestApi::class)
class SalesRepScreensE2ETest : SalesRepE2ETest() {

    // Upravljanje narudžbama

    @Test
    fun ordersListStagesOrders() {
        val orders = orders()
        if (orders.items.isEmpty()) {
            compose.waitUntilAtLeastOneExists(hasText("Nema narudžbi"), SERVER_TIMEOUT_MS)
            return
        }
        orders.items.forEach { scrollListTo(hasText("#${it.incrementId}", substring = true)) }
        val ucitajVise = runCatching { scrollListTo(hasText("Učitaj više"), SCREEN_TIMEOUT_MS) }.isSuccess
        assertEquals(
            "\"Učitaj više\" should show exactly when stage has more than one page",
            orders.totalCount > orders.items.size, ucitajVise
        )
    }

    @Test
    fun ucitajViseLoadsTheNextOrders() {
        val firstPage = orders()
        assumeTrue("stage has only one page of orders", firstPage.totalCount > firstPage.items.size)
        val next = orders(page = 2).items.firstOrNull()
        assumeTrue("the second page is empty", next != null)

        scrollListTo(hasText("Učitaj više"))
        compose.onNodeWithText("Učitaj više").performClick()

        scrollListTo(hasText("#${next!!.incrementId}", substring = true))
    }

    @Test
    fun anOrderOpensItsDetailsAndBackReturns() {
        // An order that stage can show: one whose making was cut off can be on the list without details
        val order = orders().items.firstOrNull { o ->
            runBlocking { DashRepository().getOrder(o.incrementId).last() } is ResultState.Success
        }
        assumeTrue("the sales rep has no order that stage can show", order != null)

        scrollListTo(hasText("#${order!!.incrementId}", substring = true))
        compose.onAllNodesWithText("#${order.incrementId}", substring = true).onFirst().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Informacije o narudžbi"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        listOf("BROJ NARUDŽBE", "DATUM", "Specifikacija narudžbe").forEach {
            assertTrue("\"$it\" is not shown", exists(hasText(it, substring = true), unmerged = true))
        }
        tapBack()
        compose.waitUntilDoesNotExist(hasText("Informacije o narudžbi"), SCREEN_TIMEOUT_MS)
        assertTrue(displayed(hasText(SALES_REP_HOME)))
    }

    // Upravljanje kupcima

    @Test
    fun customersListStagesActiveCustomers() {
        val customers = customers()
        goTo("Upravljanje kupcima")

        if (customers.isEmpty()) {
            compose.waitUntilAtLeastOneExists(hasText("Nema kupaca", substring = true), SERVER_TIMEOUT_MS)
            return
        }
        customers.forEach { scrollListTo(hasText(it.displayName(), substring = true)) }
        assertTrue(exists(hasText("Aktivno")))
    }

    @Test
    fun searchShowsTheMatchingCustomers() {
        val customer = customers().firstOrNull { !it.company.isNullOrBlank() }
        assumeTrue("no customer with a company name", customer != null)
        val query = customer!!.company!!
        val expected = customers(search = query)
        assertTrue("stage does not find \"$query\"", expected.any { it.customerId == customer.customerId })
        goTo("Upravljanje kupcima")

        compose.onNode(hasSetTextAction()).performTextInput(query)

        val others = customers().filter { c -> expected.none { it.customerId == c.customerId } }
        compose.waitUntil(SERVER_TIMEOUT_MS) { others.none { exists(hasText(it.displayName())) } }
        compose.waitUntilAtLeastOneExists(hasText(customer.displayName(), substring = true), SERVER_TIMEOUT_MS)
    }

    @Test
    fun statusChipOffersEveryStatus() {
        goTo("Upravljanje kupcima")

        compose.onAllNodesWithText("Aktivno").onFirst().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Filtriraj po statusu"), SCREEN_TIMEOUT_MS)
        listOf("Svi statusi", "Na čekanju", "Odbijeno", "Opozvano").forEach {
            assertTrue("\"$it\" is not offered", exists(hasText(it)))
        }
        compose.onNodeWithText("Svi statusi").performClick()
        compose.waitUntilDoesNotExist(hasText("Filtriraj po statusu"), SCREEN_TIMEOUT_MS)
        compose.waitUntilAtLeastOneExists(hasText("Svi statusi"), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun aCustomerOpensTheirProfile() {
        val customer = customers().firstOrNull()
        assumeTrue("the sales rep has no customers", customer != null)
        goTo("Upravljanje kupcima")
        scrollListTo(hasText(customer!!.displayName(), substring = true))

        // The name, not the middle of the card, which has the card's buttons
        compose.onAllNodesWithText(customer.displayName(), substring = true, useUnmergedTree = true).onFirst().performClick()

        compose.waitUntilAtLeastOneExists(hasText("PROFIL KUPCA"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        listOf("Email adresa", "Kontakt osoba", "Popusti").forEach {
            assertTrue("\"$it\" is not shown", exists(hasText(it), unmerged = true))
        }
        tapBack()
        compose.waitUntilAtLeastOneExists(hasText("Pretraži kupce..."), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun dodajKupcaOffersNewAndInviteAndTheNewCustomerFormCanBeLeft() {
        goTo("Upravljanje kupcima")

        compose.onNodeWithTag(ADD_CUSTOMER_TAG).performClick()

        compose.waitUntilAtLeastOneExists(hasText("Dodaj kupca"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Pozovi kupca").assertExists()
        compose.onNodeWithText("Novi kupac").performClick()

        compose.waitUntilAtLeastOneExists(hasText("Informacije o pravnom licu"), SCREEN_TIMEOUT_MS)
        listOf("Naziv pravnog lica*", "ID broj*", "Kontakt osoba", "Email adresa*").forEach {
            assertTrue("\"$it\" is not in the form", exists(hasText(it), unmerged = true))
        }
        compose.onNodeWithText("Odustani").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Pretraži kupce..."), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun pozoviKupcaOpensTheInviteFormWhichCanBeLeft() {
        goTo("Upravljanje kupcima")
        compose.onNodeWithTag(ADD_CUSTOMER_TAG).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Dodaj kupca"), SCREEN_TIMEOUT_MS)

        compose.onNodeWithText("Pozovi kupca").performClick()

        compose.waitUntilAtLeastOneExists(hasText("Pretraži kupca"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Pošalji zahtjev").assertExists()
        compose.onNodeWithText("Odustani").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Pretraži kupce..."), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun narucOpensTheCatalogForTheCustomer() {
        val customer = customers().firstOrNull { it.partnershipStatus == "active" }
        assumeTrue("the sales rep has no active customer", customer != null)
        goTo("Upravljanje kupcima")
        scrollListTo(hasText(customer!!.displayName(), substring = true))

        compose.onAllNodesWithText("Naruči").onFirst().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Naruči:", substring = true), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        assertTrue(exists(hasText("Pretraži artikle..."), unmerged = true))
        assertTrue(exists(hasSetTextAction()))
        assertTrue(exists(hasContentDescription("Korpa")))
        // Leaving without adding anything leaves the customer's cart as it was
        tapBack()
        compose.waitUntilAtLeastOneExists(hasText("Pretraži kupce..."), SCREEN_TIMEOUT_MS)
    }

    // Poruke

    @Test
    fun customerMessagesListStagesConversations() {
        val conversations = conversations(ChatAxis.VENDOR_CUSTOMER)
        goTo("Poruke kupaca", under = "Poruke")
        if (conversations.isEmpty()) {
            compose.waitUntilAtLeastOneExists(hasText("Još nema poruka"), SERVER_TIMEOUT_MS)
        }
        conversations.take(10).forEach { scrollListTo(hasTestTag(conversationTag(it))) }
    }

    @Test
    fun newCustomerMessageAsksForACustomer() {
        goTo("Poruke kupaca", under = "Poruke")

        compose.onNodeWithText("Pošalji novu poruku").performClick()

        compose.waitUntilAtLeastOneExists(hasText("Nova poruka"), SCREEN_TIMEOUT_MS)
        assertTrue(exists(hasText("Pretraži kupca..."), unmerged = true))
        assertTrue(exists(hasSetTextAction()))
        tapBack()
        compose.waitUntilAtLeastOneExists(hasText("Pošalji novu poruku"), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun internalMessagesListStagesConversations() {
        val conversations = conversations(ChatAxis.STAFF)
        goTo("Interne poruke", under = "Poruke")
        if (conversations.isEmpty()) {
            compose.waitUntilAtLeastOneExists(hasText("Još nema internih poruka"), SERVER_TIMEOUT_MS)
        }
        conversations.take(10).forEach { scrollListTo(hasTestTag(conversationTag(it))) }
    }

    /** The sales rep's orders, as the orders screen loads them. */
    private fun orders(page: Int = 1): OnBehalfOrderSearchResults {
        val result = runBlocking { SalesRepository().getOrders(page = page, pageSize = 10).last() }
        assertTrue("orders: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as OnBehalfOrderSearchResults
    }

    /** The active customers on the customers screen's first page, the way it loads them. */
    private fun customers(search: String? = null): List<OperationalCustomer> {
        val me = me()
        val result = runBlocking {
            if (me.capabilities.canSeeAllVendorCustomers) {
                SalesRepository().getCustomers(page = 1, pageSize = 10, search = search, status = "active").last()
            } else {
                SalesRepository().getEmployeeCustomers(
                    employeeId = me.employeeId?.toInt() ?: 0,
                    page = 1,
                    pageSize = 10,
                    search = search,
                    status = "active"
                ).last()
            }
        }
        assertTrue("customers: $result", result is ResultState.Success)
        return ((result as ResultState.Success<*>).data as OperationalCustomerSearchResults).items
    }

    private fun conversations(axis: ChatAxis): List<ChatConversation> {
        val result = runBlocking { ChatRepository().getConversations(axis, pageSize = 100).last() }
        assertTrue("conversations: $result", result is ResultState.Success)
        return ((result as ResultState.Success<*>).data as ChatConversationSearchResults).items
    }
}
