package karika.distribucija.ba.ui.view.prelogin.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.common.isKiosk
import karika.distribucija.ba.ui.view.prelogin.FakeLoginComponent
import karika.distribucija.ba.ui.view.prelogin.PreLoginTestTags
import karika.distribucija.ba.ui.view.prelogin.PreLoginUiTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginViewTest : PreLoginUiTest() {

    private fun show(component: FakeLoginComponent = FakeLoginComponent()): FakeLoginComponent {
        compose.setContent { LoginView(component) }
        return component
    }

    private fun typeCredentials(email: String, password: String) {
        compose.onNodeWithTag(PreLoginTestTags.LOGIN_EMAIL).performTextInput(email)
        compose.onNodeWithTag(PreLoginTestTags.LOGIN_PASSWORD).performTextInput(password)
    }

    private fun loginButton() = compose.onNodeWithText("Prijavi se").performScrollTo()

    @Test
    fun customerLoginHasCustomerTitle() {
        show()

        compose.onNodeWithText("Prijava kupac").assertIsDisplayed()
    }

    @Test
    fun vendorLoginHasVendorTitle() {
        show(FakeLoginComponent(KarikaType.VENDOR))

        compose.onNodeWithText("Prijava dobavljač").assertIsDisplayed()
    }

    @Test
    fun loginIsDisabledWhileFormIsEmpty() {
        show()

        loginButton().assertIsNotEnabled()
    }

    @Test
    fun invalidEmailShowsErrorAndKeepsLoginDisabled() {
        show()

        typeCredentials("amar.karika.ba", "Lozinka1")

        compose.onNodeWithText("Unesite valjanu email adresu (npr. johndoe@domain.com).")
            .assertIsDisplayed()
        loginButton().assertIsNotEnabled()
    }

    @Test
    fun validEmailWithoutPasswordKeepsLoginDisabled() {
        show()

        compose.onNodeWithTag(PreLoginTestTags.LOGIN_EMAIL).performTextInput("amar@karika.ba")

        compose.onNodeWithText("Unesite valjanu email adresu (npr. johndoe@domain.com).")
            .assertDoesNotExist()
        loginButton().assertIsNotEnabled()
    }

    @Test
    fun validCredentialsEnableLogin() {
        val component = show()

        typeCredentials("amar@karika.ba", "Lozinka1")
        loginButton().assertIsEnabled().performClick()

        assertEquals("amar@karika.ba", component.email.value)
        assertEquals("Lozinka1", component.pass.value)
        assertEquals(1, component.loginRequests)
    }

    @Test
    fun rememberedCredentialsEnableLoginRightAway() {
        show(FakeLoginComponent(savedEmail = "amar@karika.ba", savedPassword = "Lozinka1"))

        loginButton().assertIsEnabled()
    }

    @Test
    fun rememberMeCanBeSwitchedOn() {
        val component = show()

        compose.onNode(isToggleable()).performClick()

        assertTrue(component.rememberMe.value)
    }

    @Test
    fun registrationLinkOpensRegistration() {
        val component = show()
        val link = compose.onNodeWithText("Registrujte se ovdje.", substring = true)

        if (isKiosk()) {
            // Kiosk devices only log existing customers in
            link.assertDoesNotExist()
            return
        }
        link.performScrollTo().performClick()

        assertEquals(1, component.registrationRequests)
    }

    @Test
    fun backArrowReturnsToLanding() {
        val component = show()
        val back = compose.onNodeWithContentDescription("Nazad")

        if (isKiosk()) {
            // Kiosk devices have no landing screen to go back to
            back.assertDoesNotExist()
            return
        }
        back.performClick()

        assertEquals(1, component.landingRequests)
    }
}
