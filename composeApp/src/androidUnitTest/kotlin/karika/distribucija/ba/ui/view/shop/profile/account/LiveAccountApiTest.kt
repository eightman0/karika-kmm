package karika.distribucija.ba.ui.view.shop.profile.account

import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.api.LoginRepository
import karika.distribucija.ba.domain.api.UserRepository
import karika.distribucija.ba.domain.model.LoginDto
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.UserDetails
import karika.distribucija.ba.testutil.LiveShopTest
import karika.distribucija.ba.testutil.LiveTestAccounts
import karika.distribucija.ba.ui.common.KarikaType
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test

/**
 * The account screen of the customer test account against the real backend, through the
 * real AccountView and AccountComponent. What a test changes (a notification setting, the
 * password) it sets back, also when it fails.
 */
class LiveAccountApiTest : LiveShopTest() {

    private lateinit var component: AccountComponent
    private var restore: (() -> Unit)? = null

    @After
    fun restoreAccount() {
        restore?.invoke()
    }

    private fun showAccount() {
        loadCustomer()
        component = AccountComponent(componentContext(), stateHolder)
        compose.setContent { AccountView(component) }
    }

    private fun customerOnServer(): UserDetails {
        val result = runBlocking { UserRepository().get().last() }
        return assertIs<ResultState.Success<*>>(result, "could not read the customer: $result").data as UserDetails
    }

    private fun UserDetails.setting(code: String) =
        customAttributes.filter { it.attributeCode == code }.map { it.value }

    /** Opens the profile form, flips the email switch to [on] if needed, and saves. */
    private fun saveEmailNotifications(on: Boolean) {
        compose.onAllNodesWithText("Uredi").onFirst().performScrollTo().performClick()
        if (component.emailNotifications.value != on) {
            compose.onNode(isToggleable() and hasAnyAncestor(hasTestTag(AccountTestTags.EMAIL_NOTIFICATIONS)))
                .performScrollTo()
                .performClick()
        }
        compose.onNodeWithText("Sačuvaj izmjene").performScrollTo().performClick()
        waitForServer { !component.editContact.value || snackbarMessage() != null }
    }

    @Test
    fun showsTheCustomersDetails() {
        showAccount()
        val customer = stateHolder.customerSpecificHandler.userDetails.value

        compose.onNodeWithText("Informacije profila").assertExists()
        compose.onNodeWithText("Pravno lice").assertExists()
        compose.onAllNodesWithText(customer.companyName()).onFirst().assertExists()
        compose.onAllNodesWithText(customer.email ?: "").onFirst().assertExists()
        compose.onNodeWithText("Informacije za naplatu").performScrollTo()
    }

    @Test
    fun emailNotificationSettingIsSaved() {
        showAccount()
        val before = customerOnServer().setting("notification_email_enabled")
        val wasOn = before.lastOrNull() == "1"
        restore = {
            val current = customerOnServer().setting("notification_email_enabled")
            if (current != before) {
                showAccountAgainAndSave(wasOn)
            }
        }

        saveEmailNotifications(!wasOn)

        assertEquals(null, snackbarMessage(), "saving failed")
        val after = customerOnServer().setting("notification_email_enabled")
        assertEquals(listOf(if (wasOn) "0" else "1"), after, "the setting on the server")
    }

    private fun showAccountAgainAndSave(on: Boolean) {
        // Through the component itself, the screen may be anywhere after a failure
        loadCustomer()
        component.edit(null, "Informacije profila")
        component.emailNotifications.value = on
        component.updateContact()
        waitForServer { !component.editContact.value || snackbarMessage() != null }
    }

    /** Logs in with the temporary password, if it took, and changes the password back with that session. */
    private fun restorePassword(email: String, temporary: String, password: String) {
        val login = runBlocking { LoginRepository().login(LoginDto(email, temporary, KarikaType.SHOP)).last() }
        if (login !is ResultState.Success) {
            return // the change never happened
        }
        // Changing the password may end the sessions opened with the old one
        HttpClientProvider.token = login.data
        val back = runBlocking { UserRepository().changePass(temporary, password).last() }
        check(back is ResultState.Success) {
            "COULD NOT RESTORE the test account's password; it is now '$temporary': $back"
        }
    }

    @Test
    fun passwordCanBeChangedAndTheNewOneLogsIn() {
        val (email, password) = LiveTestAccounts.requireShop()
        val temporary = "Karika${System.currentTimeMillis() % 100000}Test"
        showAccount()
        restore = { restorePassword(email, temporary, password) }

        compose.onNodeWithText("Promijeni lozinku").performScrollTo().performClick()
        compose.onNodeWithTag(AccountTestTags.OLD_PASSWORD).performTextInput(password)
        compose.onNodeWithTag(AccountTestTags.NEW_PASSWORD).performTextInput(temporary)
        dismissSnackbar()
        compose.onNodeWithText("Potvrdi").performClick()
        waitForServer { snackbarMessage() != null }

        val login = runBlocking { LoginRepository().login(LoginDto(email, temporary, KarikaType.SHOP)).last() }
        assertIs<ResultState.Success<*>>(login, "the new password does not log in; message: ${snackbarMessage()}")
    }
}
