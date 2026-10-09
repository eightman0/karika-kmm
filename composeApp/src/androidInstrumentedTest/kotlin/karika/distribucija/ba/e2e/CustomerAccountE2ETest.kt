package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import karika.distribucija.ba.domain.api.LoginRepository
import karika.distribucija.ba.domain.api.UserRepository
import karika.distribucija.ba.domain.model.Address
import karika.distribucija.ba.domain.model.LoginDto
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.UpdateCustomerRequest
import karika.distribucija.ba.domain.model.UserDetails
import karika.distribucija.ba.util.KarikaConstants
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-end test of the customer's "Moj nalog" on stage.karika.ba, see [CustomerE2ETest]: every
 * field of the profile, the billing address and the default shipping address is changed, saved,
 * and checked on screen and on stage; deleting a further shipping address, and changing the
 * password (which the test then changes back, as every other test logs in with it).
 */
@OptIn(ExperimentalTestApi::class)
class CustomerAccountE2ETest : CustomerE2ETest() {

    /** The notification settings before the test, which it puts back: other tests rely on them. */
    private lateinit var settingsBefore: Map<String, String>

    @Before
    fun rememberNotificationSettings() {
        settingsBefore = notificationSettings()
    }

    @After
    fun restoreNotificationSettings() {
        if (::settingsBefore.isInitialized && notificationSettings() != settingsBefore) setNotificationSettings(settingsBefore)
    }

    @Before
    fun openMojNalog() {
        compose.onNode(bottomTab("Profil")).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Moj nalog") and hasClickAction(), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onNode(hasText("Moj nalog") and hasClickAction()).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Informacije za naplatu"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
    }

    // Informacije profila

    @Test
    fun informacijeProfilaSavesEveryField() {
        val before = user()
        val size = KarikaConstants.companySizes.first { it != before.objectSize() }
        val type = KarikaConstants.companyTypes.first { it != before.objectType() }
        val employees = uniqueDigits(2).trimStart('0').ifEmpty { "7" }
        val viber = "06" + uniqueDigits(7)

        openForm(EDIT_PROFILE, "Informacije profila")
        pick("Veličina objekta", size)
        pick("Tip objekta", type)
        replaceField(0, employees)
        replaceField(1, viber)
        save()

        // Back on the overview, which shows the new values
        listOf(size, type, employees, viber).forEach {
            compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText(it), unmerged = true) }
        }
        val after = user()
        assertEquals(size, after.objectSize())
        assertEquals(type, after.objectType())
        assertEquals(employees, after.employeeCount())
        assertEquals(viber, after.viberPhoneNumber())
    }

    @Test
    fun notificationSwitchesAreSaved() {
        val before = notifications(user())
        openForm(EDIT_PROFILE, "Informacije profila")
        // Email, Viber, Push: each is turned the other way
        (0..2).forEach { compose.onAllNodes(isToggleable())[it].performScrollTo().performClick() }
        save()

        compose.waitUntil(SERVER_TIMEOUT_MS) { notifications(user()) == before.map { !it } }
    }

    @Test
    fun odustaniLeavesTheProfileAsItWas() {
        val before = user()
        openForm(EDIT_PROFILE, "Informacije profila")
        replaceField(0, "999")
        closeKeyboard()

        // "Odustani" is in the panel pinned below the form
        compose.onNodeWithText("Odustani").performClick()

        compose.waitUntilDoesNotExist(hasText("Sačuvaj izmjene"), SCREEN_TIMEOUT_MS)
        assertEquals(before.employeeCount(), user().employeeCount())
    }

    // Adrese

    @Test
    fun informacijeZaNaplatuSavesEveryField() {
        val billing = user().billingAddress()
        assumeTrue("the customer has no billing address", billing != null)

        val new = editAddress(EDIT_BILLING, "Informacije za naplatu", billing!!)

        assertAddressSaved(billing.id, new)
    }

    @Test
    fun zadanaAdresaZaDostavuSavesEveryField() {
        val shipping = user().shippingAddress()
        assumeTrue("the customer has no default shipping address", shipping != null)

        val new = editAddress(EDIT_SHIPPING, "Adresa za dostavu", shipping!!)

        assertAddressSaved(shipping.id, new)
    }

    @Test
    fun savingAnAddressKeepsTheNotificationSettings() {
        // Turn Viber notifications off and save, then change an address
        openForm(EDIT_PROFILE, "Informacije profila")
        val viber = compose.onAllNodes(isToggleable())[1]
        if (notifications(user())[1]) viber.performScrollTo().performClick()
        save()
        compose.waitUntil(SERVER_TIMEOUT_MS) { !notifications(user())[1] }
        val billing = user().billingAddress()
        assumeTrue("the customer has no billing address", billing != null)
        reopenApp()
        openMojNalog()

        editAddress(EDIT_BILLING, "Informacije za naplatu", billing!!)

        assertEquals("Viber obavijesti after saving an address", false, notifications(user())[1])
    }

    @Test
    fun anotherShippingAddressCanBeDeleted() {
        val address = extraAddress() ?: addExtraAddressByApi()
        reopenApp()
        openMojNalog()
        compose.onNodeWithText("Spisak svih unesenih adresa za dostavu").performScrollTo()
        val street = address.street.first()
        compose.waitUntilAtLeastOneExists(hasText(street, substring = true), SCREEN_TIMEOUT_MS)

        // The first "Obriši" belongs to the first further address, the one the list shows first
        val first = user().addresses.first { it.isExtra() }
        compose.onAllNodesWithText("Obriši").onFirst().performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Jeste li sigurni da želite obrisati ovu adresu za dostavu?"), SCREEN_TIMEOUT_MS)
        compose.onNode(dialogButton("Odustani")).performClick()
        compose.waitUntilDoesNotExist(hasText("Obriši adresu za dostavu"), SCREEN_TIMEOUT_MS)
        assertTrue("Odustani deleted the address", user().addresses.any { it.id == first.id })

        compose.onAllNodesWithText("Obriši").onFirst().performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Obriši adresu za dostavu"), SCREEN_TIMEOUT_MS)
        compose.onNode(dialogButton("Obriši")).performClick()

        compose.waitUntil(SERVER_TIMEOUT_MS) { user().addresses.none { it.id == first.id } }
        waitUntilLoaded()
        compose.waitUntil(SCREEN_TIMEOUT_MS) { !exists(hasText(first.street.first(), substring = true), unmerged = true) }
    }

    // Promijeni lozinku

    @Test
    fun promijeniLozinkuExplainsWhatTheNewPasswordNeeds() {
        openPasswordSheet()
        compose.onNode(hasSetTextAction() and hasText("Stara lozinku")).performTextInput("bilo sta")
        val new = compose.onNode(hasSetTextAction() and hasText("Nova lozinku"))

        new.performTextInput("Kratka1")
        compose.waitUntilAtLeastOneExists(hasText("Lozinka mora imati najmanje 8 karaktera."), SCREEN_TIMEOUT_MS)
        compose.onNode(hasText("Potvrdi") and hasClickAction()).assertIsNotEnabled()

        compose.onNode(hasSetTextAction() and hasText("Kratka1")).performTextReplacement("dugackabezbroja")
        compose.waitUntilAtLeastOneExists(
            hasText("Lozinka mora sadržavati najmanje jedno veliko slovo i jedan broj."), SCREEN_TIMEOUT_MS
        )
        compose.onNode(hasText("Potvrdi") and hasClickAction()).assertIsNotEnabled()

        compose.onNode(hasSetTextAction() and hasText("dugackabezbroja")).performTextReplacement("Dugacka123")
        compose.waitUntilDoesNotExist(hasText("Lozinka mora", substring = true), SCREEN_TIMEOUT_MS)
        compose.onNode(hasText("Potvrdi") and hasClickAction()).assertIsEnabled()
        compose.onNodeWithText("Zatvori").performClick()
    }

    @Test
    fun promijeniLozinkuWithAWrongOldPasswordSaysSo() {
        val (_, password) = stageAccount("KARIKA_STAGE_SHOP_EMAIL", "KARIKA_STAGE_SHOP_PASSWORD")

        changePasswordInApp(old = password + "x", new = "Pogresna123")

        waitForSnackbar("Stara lozinka je pogrešna!")
        assertTrue("the password changed", canLogIn(password))
    }

    @Test
    fun promijeniLozinkuChangesThePasswordAndBack() {
        val (_, password) = stageAccount("KARIKA_STAGE_SHOP_EMAIL", "KARIKA_STAGE_SHOP_PASSWORD")
        val temporary = "E2e" + uniqueDigits(6) + "Tmp"
        try {
            changePasswordInApp(old = password, new = temporary)
            waitForSnackbar("Lozinka uspješno promijenjena!")
            assertTrue("the new password does not log in", logsInSoon(temporary))

            changePasswordInApp(old = temporary, new = password)
            waitForSnackbar("Lozinka uspješno promijenjena!")
            assertTrue("the old password does not log in again", logsInSoon(password))
        } finally {
            // Every other test logs in with the configured password
            if (!canLogIn(password)) runBlocking { UserRepository().changePass(temporary, password).last() }
        }
    }

    private fun openForm(edit: Int, heading: String) {
        compose.onAllNodesWithText("Uredi")[edit].performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Sačuvaj izmjene"), SCREEN_TIMEOUT_MS)
        assertTrue("\"$heading\" is not the form's heading", exists(hasText(heading)))
    }

    private fun save() {
        closeKeyboard()
        // "Sačuvaj izmjene" is in the panel pinned below the form
        compose.onNodeWithText("Sačuvaj izmjene").performClick()
        compose.waitUntilDoesNotExist(hasText("Sačuvaj izmjene"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
    }

    /**
     * Changes every field of the address the [edit]th "Uredi" opens to new values, saves, and
     * returns the address as it should now be.
     */
    private fun editAddress(edit: Int, heading: String, address: Address): Address {
        val letters = uniqueLetters()
        val new = address.copy(
            firstname = "Test" + letters,
            lastname = "Kupac" + letters,
            telephone = "06" + uniqueDigits(7),
            street = listOf("Ulica " + letters.replaceFirstChar { it.uppercase() } + " " + uniqueDigits(2).trimStart('0').ifEmpty { "1" }),
            city = KarikaConstants.cities().first { it != address.city },
            postcode = "7" + uniqueDigits(4),
        )
        openForm(edit, heading)
        assertTrue("Država is not prefilled", exists(hasText("Bosna i Hercegovina")))
        replaceField(FIELD_FIRSTNAME, new.firstname!!)
        replaceField(FIELD_LASTNAME, new.lastname!!)
        replaceField(FIELD_PHONE, new.telephone!!)
        replaceField(FIELD_STREET, new.street.first())
        // Poštanski broj is the last field; the disabled Država before it may not take text
        replaceField(compose.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size - 1, new.postcode!!)
        pick("Grad", new.city!!)
        save()

        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText("${new.firstname} ${new.lastname}", substring = true), unmerged = true) }
        listOf(new.telephone!!, new.street.first(), "${new.city}, ${new.postcode}").forEach {
            assertTrue("\"$it\" is not on the overview", exists(hasText(it, substring = true), unmerged = true))
        }
        return new
    }

    private fun assertAddressSaved(id: Int?, expected: Address) {
        val saved = user().addresses.first { it.id == id }
        assertEquals(expected.firstname, saved.firstname)
        assertEquals(expected.lastname, saved.lastname)
        assertEquals(expected.telephone, saved.telephone)
        assertEquals(expected.street.first(), saved.street.first())
        assertEquals(expected.city, saved.city)
        assertEquals(expected.postcode, saved.postcode)
    }

    private fun openPasswordSheet() {
        compose.onNodeWithText("Promijeni lozinku").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Unesite staru lozinku"), SCREEN_TIMEOUT_MS)
    }

    private fun changePasswordInApp(old: String, new: String) {
        openPasswordSheet()
        compose.onNode(hasSetTextAction() and hasText("Stara lozinku")).performTextInput(old)
        compose.onNode(hasSetTextAction() and hasText("Nova lozinku")).performTextInput(new)
        closeKeyboard()
        compose.onNode(hasText("Potvrdi") and hasClickAction()).performClick()
    }

    /** Stage can refuse a password for a few seconds right after it was changed, so this tries again. */
    private fun logsInSoon(password: String): Boolean {
        repeat(LOGIN_TRIES) {
            if (canLogIn(password)) return true
            Thread.sleep(LOGIN_PAUSE_MS)
        }
        return false
    }

    private fun canLogIn(password: String): Boolean {
        val (email, _) = stageAccount("KARIKA_STAGE_SHOP_EMAIL", "KARIKA_STAGE_SHOP_PASSWORD")
        val result = runBlocking { LoginRepository().login(LoginDto(email, password)).last() }
        return result is ResultState.Success
    }

    /** Email, Viber and Push notifications as stage has them. */
    private fun notifications(user: UserDetails) = listOf(
        "notification_email_enabled", "notification_viber_enabled", "notification_push_enabled"
    ).map { code -> user.customAttributes.firstOrNull { it.attributeCode == code }?.value == "1" }

    /** A shipping address that is neither the default shipping nor the billing one. */
    private fun Address.isExtra() = defaultShipping != "true" && defaultBilling != "true"

    private fun extraAddress() = user().addresses.firstOrNull { it.isExtra() }

    /** Adds a further shipping address, as checkout's "Dodaj novu adresu" would. */
    private fun addExtraAddressByApi(): Address {
        val user = user()
        val base = user.shippingAddress() ?: user.addresses.first()
        val letters = uniqueLetters()
        val address = base.copy(
            id = null, defaultShipping = null, defaultBilling = null,
            firstname = "Dodatna" + letters, street = listOf("Dodatna ulica " + uniqueDigits(3))
        )
        val result = runBlocking {
            UserRepository().put(UpdateCustomerRequest(user.copy(addresses = user.addresses + address))).last()
        }
        assertTrue("could not add an address: $result", result is ResultState.Success)
        return address
    }

    private companion object {
        /** Which "Uredi" on the overview opens which form, in the order the overview shows them. */
        const val EDIT_PROFILE = 0
        const val LOGIN_TRIES = 4
        const val LOGIN_PAUSE_MS = 3_000L
        const val EDIT_BILLING = 1
        const val EDIT_SHIPPING = 2
        /** The address form's first text fields, in order; Grad is a dropdown. */
        const val FIELD_FIRSTNAME = 0
        const val FIELD_LASTNAME = 1
        const val FIELD_PHONE = 2
        const val FIELD_STREET = 3
    }
}
