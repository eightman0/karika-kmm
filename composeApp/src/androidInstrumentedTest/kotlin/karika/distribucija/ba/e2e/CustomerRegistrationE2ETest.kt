package karika.distribucija.ba.e2e

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import karika.distribucija.ba.util.KarikaConstants
import org.junit.Before
import org.junit.Test

/**
 * End-to-end test of the customer registration on stage.karika.ba, see [StageE2ETest]: every
 * check "Registruj se" makes, in the order the app makes them, and one real registration with a new
 * email, which stage keeps waiting for approval.
 */
@OptIn(ExperimentalTestApi::class)
class CustomerRegistrationE2ETest : StageE2ETest() {

    @Before
    fun openRegistration() {
        compose.onNodeWithText("Kupac").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Prijava kupac"), SCREEN_TIMEOUT_MS)
        compose.onNode(hasText("Registrujte se", substring = true)).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Registracija kupca"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
    }

    @Test
    fun prijaviSeChecksEveryFieldInTurn() {
        expect("Naziv pravnog lica je obavezno polje!")
        type("Naziv pravnog lica", "E2E Firma")
        expect("ID broj je obavezno polje!")
        type("ID broj", "4200000000001")
        expect("Entitet je obavezno polje!")
        pickOption("Entitet", "Federacija")
        expect("Kanton je obavezno polje!")
        pickOption("Kanton", CANTON)
        expect("Grad je obavezno polje!")
        pickOption("Grad", KarikaConstants.cities(CANTON).first())
        expect("Veličina objekta je obavezno polje!")
        pickOption("Veličina objekta", KarikaConstants.companySizes.first())
        expect("Tip objekta je obavezno polje!")
        pickOption("Tip objekta", KarikaConstants.companyTypes.first())
        expect("Ime je obavezno polje!")
        type("Ime", "Test")
        expect("Prezime je obavezno polje!")
        type("Prezime", "Kupac")
        expect("Adresa je obavezno polje!")
        type("Adresa i broj ulice", "Ulica 1")
        expect("Poštanski broj je obavezno polje!")
        type("Poštanski broj", "7100")
        expect("Poštanski broj nije u odgovarajućem formatu!")
        replace("7100", "71000")
        expect("Broj telefona je obavezno polje!")
        type("Broj telefona", "123")
        expect("Broj telefona nije u odgovarajućem formatu!")
        replace("123", "061234567")
        expect("Email adresa je obavezno polje!")
        type("Email adresa", "neispravan")
        expect("Email nije u odgovarajućem formatu!")
        replace("neispravan", "karikae2e@example.com")
        expect("Lozinka je obavezno polje!")
        typePassword(0, "Kratka1")
        expect("Lozinka mora imati najmanje 8 karaktera.")
        replacePassword(0, "dugackabezbroja")
        expect("Lozinka mora sadržavati najmanje jedno veliko slovo i jedan broj.")
        replacePassword(0, "Dugacka123")
        typePassword(1, "Dugacka124")
        expect("Lozinke se ne podudaraju.")
        replacePassword(1, "Dugacka123")
        expect("Morate prihvatiti uslove korištenja!")
    }

    @Test
    fun aNewCustomerIsRegisteredAndWaitsForApproval() {
        val digits = System.currentTimeMillis().toString().takeLast(9)
        type("Naziv pravnog lica", "E2E Firma $digits")
        type("ID broj", "42$digits")
        pickOption("Entitet", "Federacija")
        pickOption("Kanton", CANTON)
        pickOption("Grad", KarikaConstants.cities(CANTON).first())
        pickOption("Veličina objekta", KarikaConstants.companySizes.first())
        pickOption("Tip objekta", KarikaConstants.companyTypes.first())
        type("Ime", "Test")
        type("Prezime", "Registracija")
        type("Adresa i broj ulice", "Testna ulica 1")
        type("Poštanski broj", "71000")
        type("Broj telefona", "0612" + digits.takeLast(5))
        type("Email adresa", "karikae2e$digits@example.com")
        typePassword(0, "Registracija123")
        typePassword(1, "Registracija123")
        closeKeyboard()
        compose.onAllNodes(isToggleable()).onFirst().performScrollTo().performClick()

        tapPrijaviSe()

        compose.waitUntil(SERVER_TIMEOUT_MS) {
            compose.onAllNodes(hasText("Dobrodošli na Karika.ba", substring = true), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodes(hasText("Prijava kupac")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.waitUntilAtLeastOneExists(hasText("Prijava kupac"), SERVER_TIMEOUT_MS)
    }

    private fun tapPrijaviSe() {
        closeKeyboard()
        compose.onNode(hasText("Registruj se") and hasClickAction()).performScrollTo().performClick()
    }

    /** Taps "Registruj se" and waits for the snackbar with [message]. */
    private fun expect(message: String) {
        tapPrijaviSe()
        compose.waitUntil(SCREEN_TIMEOUT_MS) {
            compose.onAllNodes(hasText(message), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        // The next snackbar only shows once this one is gone
        compose.waitUntil(SCREEN_TIMEOUT_MS) {
            compose.onAllNodes(hasText(message), useUnmergedTree = true).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun type(placeholder: String, text: String) {
        compose.onNode(hasSetTextAction() and hasText(placeholder)).performScrollTo().performTextInput(text)
    }

    private fun replace(current: String, text: String) {
        compose.onNode(hasSetTextAction() and hasText(current)).performScrollTo().performTextReplacement(text)
    }

    /** The password fields: Šifra, then Potvrdi šifru. */
    private val passwordField: SemanticsMatcher =
        hasSetTextAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.Password)

    private fun typePassword(index: Int, text: String) {
        compose.onAllNodes(passwordField)[index].performScrollTo().performTextInput(text)
    }

    private fun replacePassword(index: Int, text: String) {
        compose.onAllNodes(passwordField)[index].performScrollTo().performTextReplacement(text)
    }

    private fun pickOption(title: String, option: String) {
        closeKeyboard()
        compose.onAllNodesWithText(title).onFirst().performScrollTo().performClick()
        val inMenu = hasText(option) and hasAnyAncestor(isPopup())
        compose.waitUntilAtLeastOneExists(inMenu, SCREEN_TIMEOUT_MS)
        compose.onAllNodes(inMenu).onFirst().performScrollTo().performClick()
        compose.waitUntilDoesNotExist(inMenu, SCREEN_TIMEOUT_MS)
    }

    private companion object {
        const val CANTON = "Kanton Sarajevo"
    }
}
