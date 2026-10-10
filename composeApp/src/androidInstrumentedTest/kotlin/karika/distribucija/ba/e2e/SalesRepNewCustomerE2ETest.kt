package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.OperationalCustomerSearchResults
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.view.salesrep.customers.ADD_CUSTOMER_TAG
import karika.distribucija.ba.ui.view.salesrep.customers.newcustomer.newCustomerFieldTag
import karika.distribucija.ba.util.KarikaConstants
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * End-to-end test of "Dodaj kupca" on stage.karika.ba, see [SalesRepE2ETest]: the "Novi kupac"
 * form (its checks, a real new customer, and an email that already has an account) and "Pozovi
 * kupca" (the search, and a real partnership request).
 *
 * What it makes stays on stage, as there is no way to delete it from the app: each run adds one
 * customer with an `@example.com` email, assigned to the sales rep, and sends one partnership
 * request to a customer that the registration test made (also `@example.com`).
 */
@OptIn(ExperimentalTestApi::class)
class SalesRepNewCustomerE2ETest : SalesRepE2ETest() {

    // Novi kupac

    @Test
    fun sacuvajKupcaChecksTheRequiredFields() {
        openNewCustomer()

        save()
        waitForMessage("Naziv pravnog lica je obavezno polje!")

        type("Naziv pravnog lica", "E2E Komercijalista")
        save()
        waitForMessage("ID broj je obavezno polje!")

        type("ID broj", "4200000000001")
        save()
        waitForMessage("Entitet je obavezno polje!")
    }

    @Test
    fun aWrongEmailOrPostcodeIsNotTaken() {
        assumeCreating()
        openNewCustomer()

        fill(email = "nije-email", postcode = "71000")
        save()
        waitForMessage("Email nije u odgovarajućem formatu!")
        assertTrue(exists(hasText("Informacije o pravnom licu")))
    }

    @Test
    fun sacuvajKupcaCreatesTheCustomerOnTheSalesRepsList() {
        assumeCreating()
        val stamp = System.currentTimeMillis()
        val email = "e2e.komercijalista$stamp@example.com"
        val company = "E2E Komercijalista $stamp"
        openNewCustomer()

        fill(email = email, company = company)
        save()

        // Back on the list, with the new customer on stage
        compose.waitUntilAtLeastOneExists(hasText("Pretraži kupce..."), SERVER_TIMEOUT_MS)
        lateinit var created: OperationalCustomer
        compose.waitUntil(SERVER_TIMEOUT_MS) {
            allCustomers().firstOrNull { it.email.equals(email, ignoreCase = true) }?.also { created = it } != null
        }
        assertEquals(company, created.company)
        assertEquals("E2E", created.firstname)
        assertTrue("the new customer is not assigned: $created",
            created.assignedEmployees.any { it.employeeId == me().employeeId })
    }

    @Test
    fun anEmailThatHasAnAccountOffersAnInvitation() {
        assumeCreating()
        val email = stageAccount(Account.CUSTOMER.emailKey, Account.CUSTOMER.passwordKey).first
        openNewCustomer()

        fill(email = email)
        save()

        compose.waitUntilAtLeastOneExists(hasText("Kupac već postoji"), SERVER_TIMEOUT_MS)
        compose.onNode(dialogButton("Pozovi")).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Pošalji zahtjev"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Odustani").performScrollTo().performClick()
    }

    // Pozovi kupca

    @Test
    fun theInviteSearchNeedsThreeLettersAndSaysWhenNothingMatches() {
        openInvite()

        field(0).performTextInput("E2")
        compose.waitUntilAtLeastOneExists(hasText("Pretraži kupce (unesite najmanje 3 znaka)"), SCREEN_TIMEOUT_MS)

        field(0).performTextInput("xqzv")
        compose.waitUntilAtLeastOneExists(hasText("Nema korisnika za taj pretragu"), SERVER_TIMEOUT_MS)
    }

    @Test
    fun posaljiZahtjevWithoutACustomerAsksForOne() {
        openInvite()

        compose.onNodeWithText("Pošalji zahtjev").performScrollTo().performClick()

        waitForMessage("Izaberite kupca!")
    }

    @Test
    fun posaljiZahtjevSendsAPartnershipRequest() {
        assumeTrue("the sales rep may not invite customers", me().capabilities.canRequestPartnership)
        val invitee = invitable(INVITEE_SEARCH).firstOrNull {
            it.email.orEmpty().endsWith("@example.com") && !it.company.isNullOrBlank()
        }
        assumeTrue("no customer made by the tests can be invited", invitee != null)
        openInvite()

        field(0).performTextInput(invitee!!.company!!)
        val row = hasText(invitee.company!!) and hasAnyAncestor(isPopup())
        compose.waitUntilAtLeastOneExists(row, SERVER_TIMEOUT_MS)
        compose.onAllNodes(row).onFirst().performClick()
        closeKeyboard()
        compose.onNodeWithText("Pošalji zahtjev").performScrollTo().performClick()

        waitForMessage("Zahtjev za partnerstvo uspješno poslan!")
        compose.waitUntilAtLeastOneExists(hasText("Pretraži kupce..."), SCREEN_TIMEOUT_MS)
        compose.waitUntil(SERVER_TIMEOUT_MS) { invitable(invitee.company!!).none { it.customerId == invitee.customerId } }
    }

    // Koraci

    private fun openAddCustomer() {
        goTo("Upravljanje kupcima")
        compose.onNodeWithTag(ADD_CUSTOMER_TAG).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Dodaj kupca"), SCREEN_TIMEOUT_MS)
    }

    private fun openNewCustomer() {
        openAddCustomer()
        compose.onNodeWithText("Novi kupac").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Informacije o pravnom licu"), SCREEN_TIMEOUT_MS)
    }

    private fun openInvite() {
        openAddCustomer()
        compose.onNodeWithText("Pozovi kupca").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Pošalji zahtjev"), SCREEN_TIMEOUT_MS)
    }

    /** Fills every required field of "Novi kupac", in Kanton Sarajevo. */
    private fun fill(
        email: String,
        company: String = "E2E Komercijalista",
        postcode: String = "71000"
    ) {
        val canton = KarikaConstants.cantons(FEDERATION).first { it == CANTON }
        type("Naziv pravnog lica", company)
        type("ID broj", "42" + System.currentTimeMillis().toString().takeLast(11))
        type("Adresa i broj ulice", "Testna 1")
        type("Poštanski broj", postcode)
        pick("Odaberite entitet", FEDERATION)
        pick("Odaberite kanton", canton)
        pick("Odaberite grad", KarikaConstants.cities(canton).first())
        pick("Odaberite veličinu objekta", KarikaConstants.companySizes.first())
        pick("Odaberite tip objekta", KarikaConstants.companyTypes.first())
        type("Ime", "E2E")
        type("Prezime", "Komercijalista")
        type("Broj telefona", "061234567")
        type("Email adresa", email)
        closeKeyboard()
    }

    /** Types in the form's field that has [placeholder]. */
    private fun type(placeholder: String, text: String) {
        val tag = hasTestTag(newCustomerFieldTag(placeholder))
        scrollListTo(tag)
        compose.onNode(tag).performTextInput(text)
    }

    /** Opens the picker that shows [placeholder] and picks [option] from its sheet. */
    private fun pick(placeholder: String, option: String) {
        closeKeyboard()
        scrollListTo(hasText(placeholder))
        compose.onNode(hasText(placeholder) and hasClickAction()).performClick()
        val inSheet = hasText(option) and hasClickAction()
        compose.waitUntilAtLeastOneExists(inSheet, SCREEN_TIMEOUT_MS)
        // The sheet's options are a plain column, which shows them all
        compose.onAllNodes(inSheet).onFirst().performClick()
        // The picker shows the option once the sheet is gone
        compose.waitUntil(SCREEN_TIMEOUT_MS) { !exists(hasText(placeholder) and hasClickAction()) }
        compose.waitForIdle()
    }

    private fun save() {
        closeKeyboard()
        // The bottom bar is hidden while the keyboard is up, and comes back once it is gone
        compose.waitUntilAtLeastOneExists(hasText("Sačuvaj kupca"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Sačuvaj kupca").performClick()
    }

    private fun waitForMessage(text: String) {
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText(text, substring = true), unmerged = true) }
    }

    private fun dialogButton(label: String) = hasText(label) and hasClickAction() and hasAnyAncestor(isDialog())

    private fun assumeCreating() =
        assumeTrue("the sales rep may not create customers", me().capabilities.canCreateCustomer)

    private fun invitable(search: String): List<OperationalCustomer> = asAccount(Account.SALES_REP) {
        val result = runBlocking { SalesRepository().getInvitableCustomers(page = 1, pageSize = 50, search = search).last() }
        assertTrue("invitable customers: $result", result is ResultState.Success)
        ((result as ResultState.Success<*>).data as OperationalCustomerSearchResults).items
    }

    private companion object {
        const val FEDERATION = "Federacija"
        const val CANTON = "Kanton Sarajevo"
        /** The company names CustomerRegistrationE2ETest gives the customers it registers. */
        const val INVITEE_SEARCH = "E2E Firma"
    }
}
