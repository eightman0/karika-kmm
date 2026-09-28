package karika.distribucija.ba.ui.view.shop.profile.account

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.testutil.KarikaUiTest
import kotlin.test.assertEquals
import org.junit.Test

class ChangePasswordSheetTest : KarikaUiTest() {

    private val changes = mutableListOf<Pair<String, String>>()
    private var cancels = 0

    private fun show() {
        compose.setContent {
            ChangePasswordSheet(onCancel = { cancels++ }, onChange = { old, new -> changes += old to new })
        }
    }

    private fun type(old: String? = null, new: String? = null) {
        old?.let { compose.onNodeWithTag(AccountTestTags.OLD_PASSWORD).performTextInput(it) }
        new?.let { compose.onNodeWithTag(AccountTestTags.NEW_PASSWORD).performTextInput(it) }
    }

    private fun potvrdi() = compose.onNodeWithText("Potvrdi")

    @Test
    fun confirmIsDisabledUntilBothPasswordsAreGiven() {
        show()

        potvrdi().assertIsNotEnabled()
        type(old = "StaraLozinka1")
        potvrdi().assertIsNotEnabled()
    }

    @Test
    fun shortNewPasswordIsRefused() {
        show()

        type(old = "StaraLozinka1", new = "Kratka1")

        compose.onNodeWithText("Lozinka mora imati najmanje 8 karaktera.").assertExists()
        potvrdi().assertIsNotEnabled()
    }

    @Test
    fun newPasswordNeedsACapitalLetterAndANumber() {
        show()

        type(old = "StaraLozinka1", new = "bezvelikihslova")

        compose.onNodeWithText("Lozinka mora sadržavati najmanje jedno veliko slovo i jedan broj.").assertExists()
        potvrdi().assertIsNotEnabled()
    }

    @Test
    fun validPasswordsAreSubmitted() {
        show()

        type(old = "StaraLozinka1", new = "NovaLozinka2")
        potvrdi().assertIsEnabled().performClick()

        assertEquals(listOf("StaraLozinka1" to "NovaLozinka2"), changes)
    }

    @Test
    fun newPasswordFirstThenTheOldOneAlsoEnablesConfirm() {
        show()

        type(new = "NovaLozinka2")
        potvrdi().assertIsNotEnabled()
        type(old = "StaraLozinka1")

        potvrdi().assertIsEnabled()
    }

    @Test
    fun zatvoriCancels() {
        show()

        compose.onNodeWithText("Zatvori").performClick()

        assertEquals(1, cancels)
        assertEquals(emptyList(), changes)
    }
}
