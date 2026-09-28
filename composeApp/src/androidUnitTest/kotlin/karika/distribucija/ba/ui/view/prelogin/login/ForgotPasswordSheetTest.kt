package karika.distribucija.ba.ui.view.prelogin.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.ui.view.prelogin.FakeLoginComponent
import karika.distribucija.ba.ui.view.prelogin.PreLoginTestTags
import karika.distribucija.ba.ui.view.prelogin.PreLoginUiTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ForgotPasswordSheetTest : PreLoginUiTest() {

    private val component = FakeLoginComponent()

    private val sheetHint = "Unesite vašu email adresu da resetujete vašu lozinku."

    private fun openSheet() {
        compose.setContent { LoginView(component) }
        compose.onNodeWithText("Zaboravili ste šifru?").performClick()
    }

    @Test
    fun sheetIsClosedInitially() {
        compose.setContent { LoginView(component) }

        compose.onNodeWithText(sheetHint).assertDoesNotExist()
    }

    @Test
    fun forgotPasswordLinkOpensSheet() {
        openSheet()

        assertTrue(component.forgotPassSheet.value)
        compose.onNodeWithText(sheetHint).assertIsDisplayed()
    }

    @Test
    fun confirmIsDisabledUntilEmailIsValid() {
        openSheet()

        compose.onNodeWithText("Potvrdi").assertIsNotEnabled()

        compose.onNodeWithTag(PreLoginTestTags.FORGOT_PASSWORD_EMAIL).performTextInput("amar@")
        compose.onNodeWithText("Potvrdi").assertIsNotEnabled()

        compose.onNodeWithTag(PreLoginTestTags.FORGOT_PASSWORD_EMAIL).performTextInput("karika.ba")
        compose.onNodeWithText("Potvrdi").assertIsEnabled()
    }

    @Test
    fun confirmRequestsResetForTypedEmailAndClosesSheet() {
        openSheet()

        compose.onNodeWithTag(PreLoginTestTags.FORGOT_PASSWORD_EMAIL)
            .performTextInput("amar@karika.ba")
        compose.onNodeWithText("Potvrdi").performClick()

        assertEquals(listOf("amar@karika.ba"), component.forgotPasswordRequests)
        assertFalse(component.forgotPassSheet.value)
        compose.onNodeWithText(sheetHint).assertDoesNotExist()
    }

    @Test
    fun closeDismissesSheetWithoutRequest() {
        openSheet()

        compose.onNodeWithText("Zatvori").performClick()

        assertFalse(component.forgotPassSheet.value)
        assertEquals(emptyList(), component.forgotPasswordRequests)
        compose.onNodeWithText(sheetHint).assertDoesNotExist()
    }
}
