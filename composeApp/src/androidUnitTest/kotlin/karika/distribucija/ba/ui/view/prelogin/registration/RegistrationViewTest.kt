package karika.distribucija.ba.ui.view.prelogin.registration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.model.KarikaUnit
import karika.distribucija.ba.testutil.KarikaUiTest
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.view.prelogin.FakeRegistrationComponent
import karika.distribucija.ba.ui.view.prelogin.PreLoginTestTags
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class RegistrationViewTest : KarikaUiTest() {

    private fun show(component: FakeRegistrationComponent = FakeRegistrationComponent()): FakeRegistrationComponent {
        compose.setContent { RegistrationView(component) }
        return component
    }

    private fun type(tag: String, text: String) {
        compose.onNodeWithTag(tag).performScrollTo().performTextInput(text)
    }

    /**
     * Opens the picker whose label is [title] and chooses [option]. The first match is the
     * label; "Veličina objekta*" also uses its title as the placeholder.
     */
    private fun pick(title: String, option: String) {
        compose.onAllNodesWithText(title).onFirst().performScrollTo().performClick()
        compose.onNodeWithText(option).performClick()
    }

    private fun submit() {
        compose.onNodeWithText("Prijavi se").performScrollTo().performClick()
    }

    private fun fillCustomerForm() {
        type(PreLoginTestTags.REGISTRATION_COMPANY_NAME, "Karika d.o.o.")
        type(PreLoginTestTags.REGISTRATION_COMPANY_ID, "4200000000000")
        pick("Entitet*", "Federacija")
        pick("Kanton*", "Kanton Sarajevo")
        pick("Grad*", "Centar")
        pick("Veličina objekta*", "do 50 m2")
        pick("Tip objekta*", "Hotel")
        type(PreLoginTestTags.REGISTRATION_FIRSTNAME, "Amar")
        type(PreLoginTestTags.REGISTRATION_LASTNAME, "Hodzic")
        type(PreLoginTestTags.REGISTRATION_ADDRESS, "Titova 1")
        type(PreLoginTestTags.REGISTRATION_POSTAL, "71000")
        type(PreLoginTestTags.REGISTRATION_PHONE, "061234567")
        type(PreLoginTestTags.REGISTRATION_EMAIL, "amar@karika.ba")
        type(PreLoginTestTags.REGISTRATION_PASSWORD, "Lozinka1")
        type(PreLoginTestTags.REGISTRATION_CONFIRM_PASSWORD, "Lozinka1")
    }

    @Test
    fun customerRegistrationHasCustomerTitle() {
        show()

        compose.onNodeWithText("Registracija kupca").assertIsDisplayed()
    }

    @Test
    fun backArrowReturnsToLogin() {
        val component = show()

        compose.onNodeWithContentDescription("Nazad").performClick()

        assertEquals(1, component.backRequests)
    }

    @Test
    fun submittingEmptyFormAsksForCompanyName() {
        val component = show()

        submit()

        assertEquals(listOf<String?>("Naziv pravnog lica je obavezno polje!"), component.submits)
    }

    @Test
    fun companyIdAcceptsDigitsOnly() {
        val component = show()

        type(PreLoginTestTags.REGISTRATION_COMPANY_ID, "12ab")
        assertEquals("", component.companyId.value)

        type(PreLoginTestTags.REGISTRATION_COMPANY_ID, "123")
        assertEquals("123", component.companyId.value)
    }

    @Test
    fun cantonAndCityPickersAppearOnlyAfterFederacijaIsChosen() {
        val component = show()

        compose.onNodeWithText("Kanton*").assertDoesNotExist()
        compose.onNodeWithText("Grad*").assertDoesNotExist()

        pick("Entitet*", "Federacija")
        assertEquals("Federacija", component.companyEntity.value)
        compose.onAllNodesWithText("Kanton*").onFirst().performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Grad*").assertDoesNotExist()

        pick("Kanton*", "Kanton Sarajevo")
        assertEquals("Kanton Sarajevo", component.companyCanton.value)
        compose.onAllNodesWithText("Grad*").onFirst().performScrollTo().assertIsDisplayed()

        pick("Grad*", "Centar")
        assertEquals("Centar", component.companyCity.value)
    }

    @Test
    fun formWithoutAcceptedTermsIsStoppedAtTheTerms() {
        val component = show()

        fillCustomerForm()
        submit()

        assertEquals(listOf<String?>("Morate prihvatiti uslove korištenja!"), component.submits)
    }

    @Test
    fun mismatchedPasswordsAreReported() {
        val component = show()

        fillCustomerForm()
        type(PreLoginTestTags.REGISTRATION_CONFIRM_PASSWORD, "2")
        compose.onNode(isToggleable()).performScrollTo().performClick()
        submit()

        assertEquals(listOf<String?>("Lozinke se ne podudaraju."), component.submits)
    }

    @Test
    fun completeCustomerFormPassesValidation() {
        val component = show()

        fillCustomerForm()
        compose.onNode(isToggleable()).performScrollTo().performClick()
        submit()

        assertTrue(component.agree.value)
        assertEquals(listOf<String?>(null), component.submits)
    }

    @Test
    fun vendorFormHasNoCustomerOnlyFields() {
        show(FakeRegistrationComponent(KarikaType.VENDOR))

        compose.onNodeWithText("Registracija dobavljača").assertIsDisplayed()
        compose.onNodeWithText("Veličina objekta*").assertDoesNotExist()
        compose.onNodeWithText("Tip objekta*").assertDoesNotExist()
        compose.onNodeWithTag(PreLoginTestTags.REGISTRATION_ADDRESS).assertDoesNotExist()
        compose.onNodeWithTag(PreLoginTestTags.REGISTRATION_POSTAL).assertDoesNotExist()
    }

    @Test
    fun vendorCanDeselectATargetCustomerGroup() {
        val hotels = KarikaUnit(label = "Hoteli", unit = "|1|")
        val restaurants = KarikaUnit(label = "Restorani", unit = "|2|")
        val component = show(
            FakeRegistrationComponent(
                userType = KarikaType.VENDOR,
                customerGroupOptions = listOf(hotels, restaurants)
            )
        )

        compose.onNodeWithText("Hoteli").performScrollTo().performClick()

        assertFalse(component.customerGroups.value.contains(hotels))
        assertTrue(component.customerGroups.value.contains(restaurants))
    }
}
