package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Before
import org.junit.Test

/**
 * End-to-end test of "Zaboravili ste šifru?" on the customer login, on stage.karika.ba, see
 * [StageE2ETest]. Submitting sends a real reset email to the customer test account
 * (KARIKA_STAGE_SHOP_EMAIL); its password stays as it is until that email's link is used.
 */
@OptIn(ExperimentalTestApi::class)
class ForgotPasswordE2ETest : StageE2ETest() {

    @Before
    fun openForgotPassword() {
        compose.onNodeWithText("Kupac").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Prijava kupac"), SCREEN_TIMEOUT_MS)

        compose.onNodeWithText("Zaboravili ste šifru?").performClick()
        compose.waitUntilAtLeastOneExists(hasText(SHEET_TEXT), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun sheetAsksForTheEmailAndClosesAgain() {
        compose.onNodeWithText(SHEET_TEXT).assertIsDisplayed()
        compose.onNode(emailField).assertIsDisplayed()
        compose.onNodeWithText("Potvrdi").assertIsNotEnabled()

        compose.onNodeWithText("Zatvori").performClick()

        compose.waitUntilDoesNotExist(hasText(SHEET_TEXT), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Prijava kupac").assertIsDisplayed()
    }

    @Test
    fun potvrdiStaysDisabledWithoutAValidEmail() {
        compose.onNode(emailField).performTextInput("nije-email")

        compose.onNodeWithText("Potvrdi").assertIsNotEnabled()
    }

    @Test
    fun submittingTheAccountEmailConfirmsTheResetEmail() {
        val (email, _) = stageAccount("KARIKA_STAGE_SHOP_EMAIL", "KARIKA_STAGE_SHOP_PASSWORD")

        compose.onNode(emailField).performTextInput(email)
        compose.onNodeWithText("Potvrdi").assertIsEnabled().performClick()

        compose.waitUntilAtLeastOneExists(hasText(RESET_SENT_MESSAGE, substring = true), SERVER_TIMEOUT_MS)
        compose.onNodeWithText(email, substring = true).assertIsDisplayed()
        compose.onNodeWithText(SHEET_TEXT).assertDoesNotExist()
        compose.onNodeWithText("Prijava kupac").assertIsDisplayed()
    }

    private val emailField = hasSetTextAction() and hasText("Unesite email")

    private companion object {
        const val SHEET_TEXT = "Unesite vašu email adresu da resetujete vašu lozinku."
        const val RESET_SENT_MESSAGE = "dobićete e-poruku sa vezom za resetovanje"
    }
}
