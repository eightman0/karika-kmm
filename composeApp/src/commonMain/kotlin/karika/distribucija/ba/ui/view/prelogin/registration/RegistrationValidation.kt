package karika.distribucija.ba.ui.view.prelogin.registration

import karika.distribucija.ba.ui.components.isEmailFormat
import karika.distribucija.ba.ui.components.isPhoneFormat
import karika.distribucija.ba.ui.components.isPostalCodeValid
import karika.distribucija.ba.ui.view.shop.profile.account.isPassComplex

/** A snapshot of the registration form, validated without any UI or network. */
data class RegistrationInput(
    val companyName: String = "",
    val companyId: String = "",
    val companyEntity: String = "",
    val companyCanton: String = "",
    val companyCity: String = "",
    val companySize: String = "",
    val companyType: String = "",
    val contactFirstname: String = "",
    val contactLastname: String = "",
    val contactAddress: String = "",
    val contactPostal: String = "",
    val contactPhone: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val agree: Boolean = false,
)

/** Returns the message for the first rule the form breaks, or null when it can be submitted. */
fun RegistrationInput.validationError(isShop: Boolean): String? {
    if (companyName.isEmpty()) {
        return "Naziv pravnog lica je obavezno polje!"
    }
    if (companyId.isEmpty()) {
        return "ID broj je obavezno polje!"
    }
    if (companyEntity.isEmpty()) {
        return "Entitet je obavezno polje!"
    }
    if (companyEntity == "Federacija" && companyCanton.isEmpty()) {
        return "Kanton je obavezno polje!"
    }
    if (companyCity.isEmpty()) {
        return "Grad je obavezno polje!"
    }
    if (isShop) {
        if (companySize.isEmpty()) {
            return "Veličina objekta je obavezno polje!"
        }
        if (companyType.isEmpty()) {
            return "Tip objekta je obavezno polje!"
        }
    }
    if (contactFirstname.isEmpty()) {
        return "Ime je obavezno polje!"
    }
    if (contactLastname.isEmpty()) {
        return "Prezime je obavezno polje!"
    }
    if (isShop) {
        if (contactAddress.isEmpty()) {
            return "Adresa je obavezno polje!"
        }
        if (contactPostal.isEmpty()) {
            return "Poštanski broj je obavezno polje!"
        }
        if (!contactPostal.isPostalCodeValid()) {
            return "Poštanski broj nije u odgovarajućem formatu!"
        }
    }
    if (contactPhone.isEmpty()) {
        return "Broj telefona je obavezno polje!"
    }
    if (!contactPhone.isPhoneFormat()) {
        return "Broj telefona nije u odgovarajućem formatu!"
    }
    if (email.isEmpty()) {
        return "Email adresa je obavezno polje!"
    }
    if (!email.isEmailFormat()) {
        return "Email nije u odgovarajućem formatu!"
    }
    if (password.isEmpty()) {
        return "Lozinka je obavezno polje!"
    }
    if (password.length < 8) {
        return "Lozinka mora imati najmanje 8 karaktera."
    }
    if (!password.isPassComplex()) {
        return "Lozinka mora sadržavati najmanje jedno veliko slovo i jedan broj."
    }
    if (password != confirmPassword) {
        return "Lozinke se ne podudaraju."
    }
    if (!agree) {
        return "Morate prihvatiti uslove korištenja!"
    }
    return null
}
