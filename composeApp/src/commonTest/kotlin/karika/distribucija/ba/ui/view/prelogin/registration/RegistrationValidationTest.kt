package karika.distribucija.ba.ui.view.prelogin.registration

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RegistrationValidationTest {

    private val validShop = RegistrationInput(
        companyName = "Karika d.o.o.",
        companyId = "4200000000000",
        companyEntity = "Federacija",
        companyCanton = "Kanton Sarajevo",
        companyCity = "Sarajevo",
        companySize = "do 50 m2",
        companyType = "Hotel",
        contactFirstname = "Amar",
        contactLastname = "Hodžić",
        contactAddress = "Titova 1",
        contactPostal = "71000",
        contactPhone = "061234567",
        email = "amar@karika.ba",
        password = "Lozinka1",
        confirmPassword = "Lozinka1",
        agree = true,
    )

    private val validVendor = validShop.copy(
        companySize = "",
        companyType = "",
        contactAddress = "",
        contactPostal = "",
    )

    private fun shopError(input: RegistrationInput) = input.validationError(isShop = true)
    private fun vendorError(input: RegistrationInput) = input.validationError(isShop = false)

    @Test
    fun completeShopFormIsValid() {
        assertNull(shopError(validShop))
    }

    @Test
    fun completeVendorFormIsValidWithoutShopOnlyFields() {
        assertNull(vendorError(validVendor))
    }

    @Test
    fun emptyFormReportsCompanyNameFirst() {
        assertEquals("Naziv pravnog lica je obavezno polje!", shopError(RegistrationInput()))
    }

    @Test
    fun companyIdIsRequired() {
        assertEquals("ID broj je obavezno polje!", shopError(validShop.copy(companyId = "")))
    }

    @Test
    fun entityIsRequired() {
        assertEquals("Entitet je obavezno polje!", shopError(validShop.copy(companyEntity = "")))
    }

    @Test
    fun cantonIsRequiredInFederacija() {
        assertEquals(
            "Kanton je obavezno polje!",
            shopError(validShop.copy(companyCanton = ""))
        )
    }

    @Test
    fun cantonIsNotRequiredOutsideFederacija() {
        assertNull(
            shopError(
                validShop.copy(
                    companyEntity = "Republika Srpska",
                    companyCanton = "",
                    companyCity = "Banja Luka"
                )
            )
        )
    }

    @Test
    fun cityIsRequired() {
        assertEquals("Grad je obavezno polje!", shopError(validShop.copy(companyCity = "")))
    }

    @Test
    fun shopNeedsObjectSizeAndType() {
        assertEquals(
            "Veličina objekta je obavezno polje!",
            shopError(validShop.copy(companySize = ""))
        )
        assertEquals(
            "Tip objekta je obavezno polje!",
            shopError(validShop.copy(companyType = ""))
        )
    }

    @Test
    fun contactNameIsRequired() {
        assertEquals("Ime je obavezno polje!", shopError(validShop.copy(contactFirstname = "")))
        assertEquals("Prezime je obavezno polje!", shopError(validShop.copy(contactLastname = "")))
    }

    @Test
    fun shopNeedsAddressAndPostalCode() {
        assertEquals("Adresa je obavezno polje!", shopError(validShop.copy(contactAddress = "")))
        assertEquals(
            "Poštanski broj je obavezno polje!",
            shopError(validShop.copy(contactPostal = ""))
        )
    }

    @Test
    fun postalCodeMustHaveFiveDigits() {
        assertEquals(
            "Poštanski broj nije u odgovarajućem formatu!",
            shopError(validShop.copy(contactPostal = "7100"))
        )
    }

    @Test
    fun phoneIsRequiredAndValidated() {
        assertEquals(
            "Broj telefona je obavezno polje!",
            shopError(validShop.copy(contactPhone = ""))
        )
        assertEquals(
            "Broj telefona nije u odgovarajućem formatu!",
            shopError(validShop.copy(contactPhone = "123"))
        )
    }

    @Test
    fun internationalPhoneFormatIsAccepted() {
        assertNull(shopError(validShop.copy(contactPhone = "+38761234567")))
    }

    @Test
    fun emailIsRequiredAndValidated() {
        assertEquals("Email adresa je obavezno polje!", shopError(validShop.copy(email = "")))
        assertEquals(
            "Email nije u odgovarajućem formatu!",
            shopError(validShop.copy(email = "amar.karika.ba"))
        )
    }

    @Test
    fun passwordIsRequired() {
        assertEquals(
            "Lozinka je obavezno polje!",
            shopError(validShop.copy(password = "", confirmPassword = ""))
        )
    }

    @Test
    fun passwordNeedsEightCharacters() {
        assertEquals(
            "Lozinka mora imati najmanje 8 karaktera.",
            shopError(validShop.copy(password = "Lozin1", confirmPassword = "Lozin1"))
        )
    }

    @Test
    fun passwordNeedsCapitalLetterAndNumber() {
        assertEquals(
            "Lozinka mora sadržavati najmanje jedno veliko slovo i jedan broj.",
            shopError(validShop.copy(password = "lozinka1", confirmPassword = "lozinka1"))
        )
        assertEquals(
            "Lozinka mora sadržavati najmanje jedno veliko slovo i jedan broj.",
            shopError(validShop.copy(password = "Lozinkaa", confirmPassword = "Lozinkaa"))
        )
    }

    @Test
    fun passwordsMustMatch() {
        assertEquals(
            "Lozinke se ne podudaraju.",
            shopError(validShop.copy(confirmPassword = "Lozinka2"))
        )
    }

    @Test
    fun termsMustBeAccepted() {
        assertEquals(
            "Morate prihvatiti uslove korištenja!",
            shopError(validShop.copy(agree = false))
        )
    }
}
